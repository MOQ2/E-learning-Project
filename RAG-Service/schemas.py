from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

# Request Models
class IndexCourseRequest(BaseModel):
    course_id: int
    name: str
    description: str
    category: Optional[str] = None
    difficulty_level: Optional[str] = None
    tags: Optional[List[str]] = []
    modules: Optional[List[Dict[str, Any]]] = []

class CourseDiscoveryRequest(BaseModel):
    query: str = Field(..., description="User's description of what they want to learn")
    top_k: int = Field(default=5, ge=1, le=20, description="Number of courses to return")

class CourseQuestionRequest(BaseModel):
    course_id: int
    query: str = Field(..., description="User's question about the course")
    module_id: Optional[int] = None
    lesson_id: Optional[int] = None

class ChatHistoryMessage(BaseModel):
    role: str  # 'user' or 'assistant'
    content: str

class ConversationalChatRequest(BaseModel):
    query: str
    course_id: Optional[int] = None
    context_type: Optional[str] = Field(default="general", description="Type of context: general, course, module, lesson")
    context_id: Optional[int] = None
    chat_history: Optional[List[ChatHistoryMessage]] = []

# Response Models
class CourseMatch(BaseModel):
    course_id: int
    similarity_score: float
    name: Optional[str] = None
    description: Optional[str] = None
    category: Optional[str] = None
    difficulty_level: Optional[str] = None
    course_link: Optional[str] = None

class CourseDiscoveryResponse(BaseModel):
    courses: List[CourseMatch]
    explanation: str
    follow_up_suggestions: Optional[List[str]] = []

class ChatResponse(BaseModel):
    response: str
    course_id: Optional[int] = None
    sources: Optional[List[str]] = []
    follow_up_suggestions: Optional[List[str]] = []

# New chatbot schemas
class CourseRecommendationRequest(BaseModel):
    chat_id: str = Field(..., description="Unique identifier for the chat session")
    query: str = Field(..., description="User's query about what they want to learn")
    top_k: int = Field(default=5, ge=1, le=10, description="Maximum number of courses to recommend")

class CourseRecommendationResponse(BaseModel):
    chat_id: str
    response: str
    recommended_courses: List[Dict[str, Any]]
    message_type: str = "recommendation"

class CourseQuestionChatRequest(BaseModel):
    chat_id: str = Field(..., description="Unique identifier for the chat session") 
    course_id: int
    query: str = Field(..., description="Question about the specific course")

class CourseQuestionChatResponse(BaseModel):
    chat_id: str
    response: str
    course_id: int
    message_type: str = "course_question"

class ChatHistoryRequest(BaseModel):
    chat_id: str

class ClearChatRequest(BaseModel):
    chat_id: str

class HealthResponse(BaseModel):
    status: str
    service: str
