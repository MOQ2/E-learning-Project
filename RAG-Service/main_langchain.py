"""
E-Learning RAG Service with LangChain + Gemini + PostgreSQL
Following the comprehensive plan for course recommendation chatbot
"""
from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import List, Dict, Optional
import logging
import json
import re
from langchain_google_genai import ChatGoogleGenerativeAI
from config import Settings
from embeddings_service import embeddings_service
from memory_service import memory_service
import database
import schemas
import backend_client

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="E-Learning RAG Service - LangChain + Gemini",
    description="Intelligent course recommendation chatbot with persistent memory",
    version="2.0.0"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:4200",
        "http://localhost:57107", 
        "http://localhost:5000",
        "http://127.0.0.1:4200",
        "http://127.0.0.1:5000"
    ],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["*"],
)

@app.on_event("startup")
def on_startup():
    database.init_db()
    logger.info("RAG Service initialized with LangChain + Gemini")

@app.get("/health", response_model=schemas.HealthResponse)
def health_check():
    """Health check endpoint."""
    return schemas.HealthResponse(status="healthy", service="rag-service-langchain")

## ===============================
# Chatbot Endpoints

@app.post("/api/rag/recommend", response_model=schemas.CourseRecommendationResponse, tags=["Chatbot"])
async def recommend_courses(request: schemas.CourseRecommendationRequest, db: Session = Depends(database.get_db)):
    """
    Main chatbot endpoint for course recommendations using LangChain + Gemini.
    Implements hybrid memory (summary + window) and retrieval-augmented generation.
    """
    try:
        logger.info(f"Processing recommendation request for chat {request.chat_id}")
        
        # Get combined memory for this chat session
        memory = memory_service.get_combined_memory(request.chat_id, db)
        
        # Find similar courses using embeddings
        similar_course_ids = embeddings_service.get_similar_courses(request.query, request.top_k, db)
        logger.info(f"Found similar courses: {similar_course_ids}")
        
        # Fetch detailed course information from backend
        courses_context = []
        recommended_courses = []
        
        for course_id in similar_course_ids:
            course_data = await backend_client.fetch_course_details(course_id)
            if course_data:
                courses_context.append(_format_course_for_context(course_data))
                # Map fields from backend CourseDetailsDto (camelCase) to response (snake_case)
                recommended_courses.append({
                    "course_id": course_data.get("id", course_id),
                    "name": course_data.get("name", "Unknown Course"),
                    "description": course_data.get("description", ""),
                    "category": course_data.get("category", ""),
                    "difficulty_level": course_data.get("difficultyLevel") or course_data.get("difficulty_level", ""),
                    "price": course_data.get("oneTimePrice") if course_data.get("oneTimePrice") is not None else course_data.get("price", 0),
                    "currency": course_data.get("currency", "USD"),
                    "duration_hours": course_data.get("estimatedDurationInHours") if course_data.get("estimatedDurationInHours") is not None else course_data.get("duration_hours", 0),
                })
        
        # Create context for the LLM
        context_text = "\n\n".join(courses_context) if courses_context else "No relevant courses found in the database."
        
        # Load memory variables for conversation context
        memory_vars = memory.load_memory_variables({})
        conversation_context = ""
        if memory_vars.get('summary'):
            conversation_context += f"Conversation Summary: {memory_vars['summary']}\n\n"
        if memory_vars.get('recent_history'):
            recent_messages = []
            for msg in memory_vars['recent_history']:
                if hasattr(msg, 'type') and hasattr(msg, 'content'):
                    role = "User" if msg.type == "human" else "Assistant"
                    recent_messages.append(f"{role}: {msg.content}")
            if recent_messages:
                conversation_context += "Recent Messages:\n" + "\n".join(recent_messages) + "\n\n"
        
        # Generate response using Gemini (simplified approach since we don't have full LangChain chain yet)
        from langchain_google_genai import ChatGoogleGenerativeAI
        from config import Settings
        
        settings = Settings()
        llm = ChatGoogleGenerativeAI(
            model="gemini-2.5-flash", 
            temperature=0.4,
            google_api_key=settings.gemini_api_key
        )

        prompt = f"""
You are a course recommendation assistant. Only recommend courses from the provided database.

{conversation_context}Available Courses:
{context_text}

User Query: {request.query}

Please recommend the most suitable courses. Output in JSON format:

{{
"explanation": "A brief explanation of the recommendations",
"recommended_course_ids": [list of course IDs (numbers) from the available courses that you recommend]
}}

Only include course IDs that are explicitly listed in the available courses above.
If no suitable courses are found, set recommended_course_ids to [] and provide an appropriate explanation.
"""
        
        response_text = llm.predict(prompt)
        
        # Parse the JSON response from LLM
        try:
            # Clean the response by removing markdown code blocks
            cleaned_response = re.sub(r'```\w*\n?', '', response_text).strip()
            parsed_response = json.loads(cleaned_response)
            explanation = parsed_response.get("explanation", "I have analyzed your query and here are my recommendations.")
            recommended_course_ids = parsed_response.get("recommended_course_ids", [])
            recommended_course_ids = [int(id) for id in recommended_course_ids if isinstance(id, (int, str)) and str(id).isdigit()]
        except json.JSONDecodeError:
            logger.warning(f"Failed to parse LLM response as JSON: {response_text}")
            explanation = response_text  # Fallback to raw response
            recommended_course_ids = similar_course_ids  # Fallback to all similar
        
        # Filter recommended courses to only those selected by LLM
        recommended_courses = [course for course in recommended_courses if course["course_id"] in recommended_course_ids]
        
        # Update memory with this conversation turn
        memory.save_context(
            inputs={"input": request.query},
            outputs={"output": explanation}
        )
        
        # Save memory to database
        memory_service.save_combined_memory(request.chat_id, memory, db)
        
        return schemas.CourseRecommendationResponse(
            chat_id=request.chat_id,
            response=explanation,
            recommended_courses=recommended_courses
        )
        
    except Exception as e:
        logger.error(f"Error in course recommendation for chat {request.chat_id}: {e}")
        # Return a fallback response
        return schemas.CourseRecommendationResponse(
            chat_id=request.chat_id,
            response="I'm sorry, I encountered an error while processing your request. Please try again.",
            recommended_courses=[]
        )

@app.post("/api/rag/ask-about-course", response_model=schemas.CourseQuestionChatResponse, tags=["Chatbot"])
async def ask_about_course(request: schemas.CourseQuestionChatRequest, db: Session = Depends(database.get_db)):
    """
    Answer specific questions about a course with memory context.
    """
    try:
        logger.info(f"Processing course question for chat {request.chat_id}, course {request.course_id}")
        
        # Get combined memory for this chat session
        memory = memory_service.get_combined_memory(request.chat_id, db)
        
        # Fetch detailed course information
        course_data = await backend_client.fetch_course_details(request.course_id)
        if not course_data:
            return schemas.CourseQuestionChatResponse(
                chat_id=request.chat_id,
                response="I'm sorry, I couldn't find information about that course.",
                course_id=request.course_id
            )
        
        # Create detailed context
        course_context = _format_course_for_context(course_data)
        
        # Load memory variables for conversation context
        memory_vars = memory.load_memory_variables({})
        conversation_context = ""
        if memory_vars.get('summary'):
            conversation_context += f"Conversation Summary: {memory_vars['summary']}\n\n"
        if memory_vars.get('recent_history'):
            recent_messages = []
            for msg in memory_vars['recent_history']:
                if hasattr(msg, 'type') and hasattr(msg, 'content'):
                    role = "User" if msg.type == "human" else "Assistant"
                    recent_messages.append(f"{role}: {msg.content}")
            if recent_messages:
                conversation_context += "Recent Messages:\n" + "\n".join(recent_messages) + "\n\n"
        

        
        settings = Settings()
        llm = ChatGoogleGenerativeAI(
            model="gemini-2.5-flash", 
            temperature=0.4,
            google_api_key=settings.gemini_api_key
        )
        
        prompt = f"""
{conversation_context}Course Information:
{course_context}

User Question: {request.query}

Please answer the user's question based on the course information provided. Be specific and helpful.
Only use information from the course data above.
"""
        
        response_text = llm.predict(prompt)
        
        # Update memory
        memory.save_context(
            inputs={"input": f"Question about {course_data.get('name', 'course')}: {request.query}"},
            outputs={"output": response_text}
        )
        
        # Save memory to database
        memory_service.save_combined_memory(request.chat_id, memory, db)
        
        return schemas.CourseQuestionChatResponse(
            chat_id=request.chat_id,
            response=response_text,
            course_id=request.course_id
        )
        
    except Exception as e:
        logger.error(f"Error answering course question for chat {request.chat_id}: {e}")
        return schemas.CourseQuestionChatResponse(
            chat_id=request.chat_id,
            response="I'm sorry, I encountered an error while processing your question about this course.",
            course_id=request.course_id
        )

# ===============================
# Admin/Backend Integration Endpoints
# ===============================

@app.post("/api/rag/index-course", tags=["Admin"])
async def index_course(request: schemas.IndexCourseRequest, db: Session = Depends(database.get_db)):
    """
    Index a course by creating/updating its embedding.
    Called by Spring backend when courses are created/updated.
    """
    try:
        logger.info(f"Indexing course {request.course_id}: {request.name}")
        
        # Use embeddings service to create/update course embedding
        success = embeddings_service.insert_course(
            course_id=request.course_id,
            course_data=request.dict(),
            db=db
        )
        
        if success:
            return {"message": f"Course {request.course_id} indexed successfully", "success": True}
        else:
            raise HTTPException(status_code=500, detail="Failed to index course")
            
    except Exception as e:
        logger.error(f"Error indexing course {request.course_id}: {e}")
        raise HTTPException(status_code=500, detail=str(e))

# ===============================
# Chat Management Endpoints  
# ===============================

@app.post("/api/rag/clear-chat", tags=["Chat Management"])
async def clear_chat(request: schemas.ClearChatRequest, db: Session = Depends(database.get_db)):
    """Clear chat history for a specific chat session"""
    try:
        success = memory_service.clear_chat_history(request.chat_id, db)
        if success:
            return {"message": f"Chat history cleared for {request.chat_id}", "success": True}
        else:
            return {"message": f"No chat history found for {request.chat_id}", "success": False}
    except Exception as e:
        logger.error(f"Error clearing chat {request.chat_id}: {e}")
        raise HTTPException(status_code=500, detail=str(e))

# ===============================
# Utility Functions
# ===============================

def _format_course_for_context(course_data: Dict) -> str:
    """Format course data for LLM context"""
    context_parts = []
    
    # Basic course information
    if course_data.get('id'):
        context_parts.append(f"Course ID: {course_data['id']}")
    if course_data.get('name'):
        context_parts.append(f"Course: {course_data['name']}")
    
    if course_data.get('description'):
        context_parts.append(f"Description: {course_data['description']}")
    
    if course_data.get('category'):
        context_parts.append(f"Category: {course_data['category']}")
    
    # Support both backend camelCase and snake_case
    difficulty = course_data.get('difficultyLevel') or course_data.get('difficulty_level')
    if difficulty:
        context_parts.append(f"Difficulty: {difficulty}")
    
    # Price may be oneTimePrice (backend) or price (local mapping)
    price = course_data.get('oneTimePrice') if course_data.get('oneTimePrice') is not None else course_data.get('price')
    if price is not None:
        currency_raw = course_data.get('currency', 'USD')
        currency = currency_raw.get('name') if isinstance(currency_raw, dict) and 'name' in currency_raw else currency_raw
        if price == 0:
            context_parts.append("Price: Free")
        else:
            context_parts.append(f"Price: {price} {currency}")

    # Duration may be estimatedDurationInHours (backend) or duration_hours
    duration = course_data.get('estimatedDurationInHours') or course_data.get('duration_hours')
    if duration:
        context_parts.append(f"Duration: {duration} hours")

    # Subscription details (if present)
    allows_sub = course_data.get('allowsSubscription')
    monthly = course_data.get('subscriptionPriceMonthly')
    three_m = course_data.get('subscriptionPrice3Months')
    six_m = course_data.get('subscriptionPrice6Months')
    if allows_sub or monthly or three_m or six_m:
        sub_parts = []
        if monthly:
            sub_parts.append(f"Monthly: {monthly}")
        if three_m:
            sub_parts.append(f"3-Months: {three_m}")
        if six_m:
            sub_parts.append(f"6-Months: {six_m}")
        if sub_parts:
            context_parts.append("Subscription: " + ", ".join(sub_parts))

    # Instructor and stats if available
    if course_data.get('instructor'):
        context_parts.append(f"Instructor: {course_data['instructor']}")
    if course_data.get('averageRating') is not None:
        context_parts.append(f"Avg Rating: {course_data['averageRating']}")
    if course_data.get('enrolledCount') is not None:
        context_parts.append(f"Enrolled: {course_data['enrolledCount']}")
    
    # Tags
    if course_data.get('tags') and isinstance(course_data['tags'], list):
        tag_names = [tag.get('name', str(tag)) for tag in course_data['tags'] if tag]
        if tag_names:
            context_parts.append(f"Tags: {', '.join(tag_names)}")
    
    # Modules information
    if course_data.get('modules'):
        context_parts.append("Modules:")
        for module in course_data['modules']:
            if module.get('title'):
                context_parts.append(f"  - {module['title']}")
    
    return "\n".join(context_parts)
    if course_data.get('enrolledCount') is not None:
        context_parts.append(f"Enrolled: {course_data['enrolledCount']}")
    
    # Tags
    if course_data.get('tags') and isinstance(course_data['tags'], list):
        tag_names = [tag.get('name', str(tag)) for tag in course_data['tags'] if tag]
        if tag_names:
            context_parts.append(f"Tags: {', '.join(tag_names)}")
    
    # Modules information
    if course_data.get('modules'):
        context_parts.append("Modules:")
        for module in course_data['modules']:
            if module.get('title'):
                context_parts.append(f"  - {module['title']}")
    
    return "\n".join(context_parts)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)