from sqlalchemy import Column, BigInteger, String, Text, DateTime, JSON
from sqlalchemy.sql import func
from pgvector.sqlalchemy import Vector
from database import Base

class CourseEmbedding(Base):
    __tablename__ = "course_embeddings"

    course_id = Column(BigInteger, primary_key=True, index=True)
    embedding = Column(Vector(384), nullable=False)

class ChatHistory(Base):
    __tablename__ = "chat_history"
    
    id = Column(BigInteger, primary_key=True, index=True, autoincrement=True)
    chat_id = Column(String(255), unique=True, nullable=False, index=True)
    messages = Column(JSON, nullable=True)
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
