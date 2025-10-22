from langchain.memory import (
    ConversationBufferWindowMemory,
    ConversationSummaryMemory,
    CombinedMemory
)
from langchain_google_genai import ChatGoogleGenerativeAI
from sqlalchemy.orm import Session
from typing import Dict, List, Optional
import json
import logging
from config import Settings
import models

logger = logging.getLogger(__name__)
settings = Settings()

class MemoryService:
    def __init__(self):
        # Initialize summarizer LLM
        self.summarizer_llm = ChatGoogleGenerativeAI(
            model="gemini-2.5-flash", 
            temperature=0.2,
            google_api_key=settings.gemini_api_key
        )
    
    def get_combined_memory(self, chat_id: str, db: Session):
        """Get combined memory for a chat session"""
        try:
            # Load existing chat history from database
            chat_history = db.query(models.ChatHistory).filter(
                models.ChatHistory.chat_id == chat_id
            ).first()
            
            # Create window memory (keeps last 5 messages)
            window_memory = ConversationBufferWindowMemory(
                k=5, 
                memory_key="recent_history", 
                return_messages=True
            )
            
            # Create summary memory
            summary_memory = ConversationSummaryMemory(
                llm=self.summarizer_llm, 
                memory_key="summary", 
                return_messages=False
            )
            
            if chat_history and chat_history.messages:
                # Load existing messages
                messages_data = chat_history.messages
                
                # Load summary if exists
                if messages_data.get('summary'):
                    summary_memory.buffer = messages_data['summary']
                
                # Load recent messages if exists
                if messages_data.get('recent_messages'):
                    for msg in messages_data['recent_messages']:
                        if msg.get('type') == 'human':
                            window_memory.chat_memory.add_user_message(msg['data']['content'])
                        elif msg.get('type') == 'ai':
                            window_memory.chat_memory.add_ai_message(msg['data']['content'])
            
            # Combine both memories
            memory = CombinedMemory(memories=[summary_memory, window_memory])
            
            return memory
            
        except Exception as e:
            logger.error(f"Error loading memory for chat {chat_id}: {e}")
            # Return fresh memory on error
            window_memory = ConversationBufferWindowMemory(k=5, memory_key="recent_history", return_messages=True)
            summary_memory = ConversationSummaryMemory(llm=self.summarizer_llm, memory_key="summary", return_messages=False)
            return CombinedMemory(memories=[summary_memory, window_memory])
    
    def save_combined_memory(self, chat_id: str, memory: CombinedMemory, db: Session):
        """Save combined memory to database"""
        try:
            # Extract summary and recent messages
            summary = ""
            recent_messages = []
            
            for mem in memory.memories:
                if hasattr(mem, 'buffer') and isinstance(mem.buffer, str):
                    # This is the summary memory
                    summary = mem.buffer
                elif hasattr(mem, 'chat_memory') and hasattr(mem.chat_memory, 'messages'):
                    # This is the window memory
                    for msg in mem.chat_memory.messages:
                        if hasattr(msg, 'type') and hasattr(msg, 'content'):
                            recent_messages.append({
                                "type": msg.type,
                                "data": {"content": msg.content}
                            })
            
            # Prepare messages data
            messages_data = {
                "summary": summary,
                "recent_messages": recent_messages
            }
            
            # Save or update in database
            chat_history = db.query(models.ChatHistory).filter(
                models.ChatHistory.chat_id == chat_id
            ).first()
            
            if chat_history:
                chat_history.messages = messages_data
            else:
                chat_history = models.ChatHistory(
                    chat_id=chat_id,
                    messages=messages_data
                )
                db.add(chat_history)
            
            db.commit()
            logger.info(f"Saved memory for chat {chat_id}")
            
        except Exception as e:
            logger.error(f"Error saving memory for chat {chat_id}: {e}")
            db.rollback()
    
    def create_fresh_memory(self) -> CombinedMemory:
        """Create a fresh combined memory instance"""
        window_memory = ConversationBufferWindowMemory(k=5, memory_key="recent_history", return_messages=True)
        summary_memory = ConversationSummaryMemory(llm=self.summarizer_llm, memory_key="summary", return_messages=False)
        return CombinedMemory(memories=[summary_memory, window_memory])
    
    def clear_chat_history(self, chat_id: str, db: Session) -> bool:
        """Clear chat history for a specific chat ID"""
        try:
            chat_history = db.query(models.ChatHistory).filter(
                models.ChatHistory.chat_id == chat_id
            ).first()
            
            if chat_history:
                db.delete(chat_history)
                db.commit()
                logger.info(f"Cleared chat history for {chat_id}")
                return True
            return False
            
        except Exception as e:
            logger.error(f"Error clearing chat history for {chat_id}: {e}")
            db.rollback()
            return False

# Global instance
memory_service = MemoryService()