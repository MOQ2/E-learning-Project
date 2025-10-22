from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""
    
    # Database settings
    database_url: str = "postgresql://postgres:mohammad@localhost:5432/eLearning"
    
    # Gemini API settings
    gemini_api_key: str = "AIzaSyB6r6HmGASd2zjVMKi7_FzcVA_O9qd3ef8"
    
    # Backend API settings
    backend_url: str = "http://localhost:8080"
    
    # Service settings
    rag_service_port: int = 8000
    rag_service_host: str = "0.0.0.0"
    
    # Vector search settings
    default_top_k: int = 5
    max_top_k: int = 20
    
    # Embedding settings (using sentence-transformers instead of Gemini)
    embedding_model: str = "all-MiniLM-L6-v2"  # Sentence transformer model
    embedding_dimension: int = 384  # Dimension for all-MiniLM-L6-v2
    chat_model: str = "gemini-2.5-flash"
    
    # Generation settings
    temperature: float = 0.7
    top_p: float = 0.95
    top_k: int = 40
    max_output_tokens: int = 2048
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = False


# Global settings instance
settings = Settings()
