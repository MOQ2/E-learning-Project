from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import PGVector
from sqlalchemy.orm import Session
from typing import List, Dict, Optional
import logging
from config import Settings
import models
import database

logger = logging.getLogger(__name__)
settings = Settings()

# Initialize embeddings model (local HuggingFace)
embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")

class EmbeddingsService:
    def __init__(self):
        self.embeddings = embeddings
        
    def create_course_document(self, course_data: Dict) -> str:
        """Create a document string from course data for embedding"""
        doc_parts = []
        
        if course_data.get('name'):
            doc_parts.append(f"Title: {course_data['name']}")
            
        if course_data.get('description'):
            doc_parts.append(f"Description: {course_data['description']}")
            
        if course_data.get('category'):
            doc_parts.append(f"Category: {course_data['category']}")
            
        if course_data.get('difficulty_level'):
            doc_parts.append(f"Level: {course_data['difficulty_level']}")
            
        if course_data.get('tags') and isinstance(course_data['tags'], list):
            doc_parts.append(f"Tags: {', '.join(course_data['tags'])}")
            
        if course_data.get('modules') and isinstance(course_data['modules'], list):
            module_names = [module.get('title', '') for module in course_data['modules'] if module.get('title')]
            if module_names:
                doc_parts.append(f"Modules: {', '.join(module_names)}")
        
        return ". ".join(doc_parts)
    
    def insert_course(self, course_id: int, course_data: Dict, db: Session) -> bool:
        """Insert or update course embedding in the database"""
        try:
            # Create document text for embedding
            doc_text = self.create_course_document(course_data)
            logger.info(f"Created document for course {course_id}: {doc_text[:200]}...")
            
            # Generate embedding
            embedding_vector = self.embeddings.embed_query(doc_text)
            logger.info(f"Generated embedding for course {course_id}, dimension: {len(embedding_vector)}")
            
            # Check if course embedding exists
            existing_embedding = db.query(models.CourseEmbedding).filter(
                models.CourseEmbedding.course_id == course_id
            ).first()
            
            if existing_embedding:
                # Update existing embedding
                existing_embedding.embedding = embedding_vector
                logger.info(f"Updated embedding for course {course_id}")
            else:
                # Create new embedding
                new_embedding = models.CourseEmbedding(
                    course_id=course_id,
                    embedding=embedding_vector
                )
                db.add(new_embedding)
                logger.info(f"Created new embedding for course {course_id}")
            
            db.commit()
            return True
            
        except Exception as e:
            logger.error(f"Error inserting course {course_id} embedding: {e}")
            db.rollback()
            return False
    
    def get_similar_courses(self, query: str, top_k: int, db: Session) -> List[Dict]:
        """Find similar courses using vector similarity search"""
        try:
            # Generate query embedding
            query_embedding = self.embeddings.embed_query(query)
            logger.info(f"Generated query embedding, dimension: {len(query_embedding)}")
            
            # Perform similarity search using cosine distance
            similar_embeddings = db.query(models.CourseEmbedding).order_by(
                models.CourseEmbedding.embedding.l2_distance(query_embedding)
            ).limit(top_k).all()
            
            course_ids = [embedding.course_id for embedding in similar_embeddings]
            logger.info(f"Found {len(course_ids)} similar courses: {course_ids}")
            
            return course_ids
            
        except Exception as e:
            logger.error(f"Error in similarity search: {e}")
            return []
    
    def embed_query(self, query: str) -> List[float]:
        """Generate embedding for a query string"""
        return self.embeddings.embed_query(query)

# Global instance
embeddings_service = EmbeddingsService()