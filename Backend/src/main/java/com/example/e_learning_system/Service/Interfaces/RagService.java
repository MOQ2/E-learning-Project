package com.example.e_learning_system.Service.Interfaces;

import java.util.Map;

/**
 * Service interface for RAG (Retrieval-Augmented Generation) operations
 * Handles communication with the Python RAG service for course recommendations and Q&A
 */
public interface RagService {
    
    /**
     * Get course recommendations based on user query
     * @param chatId Unique identifier for the chat session
     * @param query User's query about what they want to learn
     * @param topK Maximum number of courses to recommend
     * @return Map containing response and recommended courses
     */
    Map<String, Object> getCourseRecommendations(String chatId, String query, int topK);
    
    /**
     * Ask specific questions about a course
     * @param chatId Unique identifier for the chat session
     * @param courseId ID of the course to ask about
     * @param query User's question about the course
     * @return Map containing the response
     */
    Map<String, Object> askAboutCourse(String chatId, Long courseId, String query);
    
    /**
     * Clear chat history for a specific chat session
     * @param chatId Unique identifier for the chat session
     * @return true if chat history was cleared, false if no history found
     */
    boolean clearChatHistory(String chatId);
    
    /**
     * Index a course in the RAG system (called when course is created/updated)
     * @param courseId ID of the course to index
     * @param courseData Course data to index
     * @return true if indexing was successful
     */
    boolean indexCourse(Long courseId, Map<String, Object> courseData);
    
    /**
     * Get health status of the RAG service
     * @return Map containing health information
     */
    Map<String, String> getHealthStatus();
}