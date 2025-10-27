package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.ApiResponse;
import com.example.e_learning_system.Service.Interfaces.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final RagService ragService;

    /**
     * Get course recommendations based on user query
     * This is the main endpoint for the chatbot
     */
    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCourseRecommendations(
            @RequestBody CourseRecommendationRequest request) {
        
        try {
            log.info("Processing course recommendation request for chat: {}", request.getChatId());
            
            Map<String, Object> response = ragService.getCourseRecommendations(
                request.getChatId(),
                request.getQuery(),
                request.getTopK()
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "Course recommendations generated successfully", 
                response
            ));
            
        } catch (Exception e) {
            log.error("Error processing course recommendation request: ", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to generate course recommendations"));
        }
    }

    /**
     * Ask specific questions about a course
     */
    @PostMapping("/ask-about-course")
    public ResponseEntity<ApiResponse<Map<String, Object>>> askAboutCourse(
            @RequestBody CourseQuestionRequest request) {
        
        try {
            log.info("Processing course question for chat: {}, course: {}", 
                request.getChatId(), request.getCourseId());
            
            Map<String, Object> response = ragService.askAboutCourse(
                request.getChatId(),
                request.getCourseId(),
                request.getQuery()
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "Course question answered successfully", 
                response
            ));
            
        } catch (Exception e) {
            log.error("Error processing course question: ", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to answer question about course"));
        }
    }

    /**
     * Clear chat history for a session
     */
    @PostMapping("/clear-chat")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearChat(
            @RequestBody ClearChatRequest request) {
        
        try {
            log.info("Clearing chat history for: {}", request.getChatId());
            
            boolean success = ragService.clearChatHistory(request.getChatId());
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success(
                    "Chat history cleared successfully", 
                    Map.of("chatId", request.getChatId(), "cleared", true)
                ));
            } else {
                return ResponseEntity.ok(ApiResponse.success(
                    "No chat history found to clear", 
                    Map.of("chatId", request.getChatId(), "cleared", false)
                ));
            }
            
        } catch (Exception e) {
            log.error("Error clearing chat history: ", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to clear chat history"));
        }
    }

    /**
     * Get health status of RAG service
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> getRagServiceHealth() {
        try {
            Map<String, String> health = ragService.getHealthStatus();
            return ResponseEntity.ok(ApiResponse.success("RAG service health check", health));
        } catch (Exception e) {
            log.error("Error checking RAG service health: ", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to check RAG service health"));
        }
    }

    // DTO Classes
    public static class CourseRecommendationRequest {
        private String chatId;
        private String query;
        private int topK = 5;

        public CourseRecommendationRequest() {}

        public CourseRecommendationRequest(String chatId, String query, int topK) {
            this.chatId = chatId;
            this.query = query;
            this.topK = topK;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }
    }

    public static class CourseQuestionRequest {
        private String chatId;
        private Long courseId;
        private String query;

        public CourseQuestionRequest() {}

        public CourseQuestionRequest(String chatId, Long courseId, String query) {
            this.chatId = chatId;
            this.courseId = courseId;
            this.query = query;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    public static class ClearChatRequest {
        private String chatId;

        public ClearChatRequest() {}

        public ClearChatRequest(String chatId) {
            this.chatId = chatId;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }
    }
}