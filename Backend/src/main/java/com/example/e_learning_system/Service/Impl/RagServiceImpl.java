package com.example.e_learning_system.Service.Impl;

import com.example.e_learning_system.Service.Interfaces.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RagServiceImpl implements RagService {

    @Value("${rag.service.url:http://localhost:8000}")
    private String ragServiceUrl;

    private final RestTemplate restTemplate;

    public RagServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Map<String, Object> getCourseRecommendations(String chatId, String query, int topK) {
        try {
            String url = ragServiceUrl + "/api/rag/recommend";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("chat_id", chatId);
            requestBody.put("query", query);
            requestBody.put("top_k", topK);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Calling RAG service for recommendations: {}", url);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                (Class<Map<String, Object>>)(Class<?>)Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                log.info("Successfully received recommendations from RAG service");
                return responseBody;
            } else {
                log.error("RAG service returned non-successful status: {}", response.getStatusCode());
                return createErrorResponse("RAG service returned an error");
            }

        } catch (RestClientException e) {
            log.error("Error calling RAG service for recommendations: ", e);
            return createErrorResponse("Failed to connect to RAG service");
        } catch (Exception e) {
            log.error("Unexpected error in getCourseRecommendations: ", e);
            return createErrorResponse("An unexpected error occurred");
        }
    }

    @Override
    public Map<String, Object> askAboutCourse(String chatId, Long courseId, String query) {
        try {
            String url = ragServiceUrl + "/api/rag/ask-about-course";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("chat_id", chatId);
            requestBody.put("course_id", courseId);
            requestBody.put("query", query);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Calling RAG service for course question: {}", url);
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                log.info("Successfully received course answer from RAG service");
                return responseBody;
            } else {
                log.error("RAG service returned non-successful status: {}", response.getStatusCode());
                return createErrorResponse("RAG service returned an error");
            }

        } catch (RestClientException e) {
            log.error("Error calling RAG service for course question: ", e);
            return createErrorResponse("Failed to connect to RAG service");
        } catch (Exception e) {
            log.error("Unexpected error in askAboutCourse: ", e);
            return createErrorResponse("An unexpected error occurred");
        }
    }

    @Override
    public boolean clearChatHistory(String chatId) {
        try {
            String url = ragServiceUrl + "/api/rag/clear-chat";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("chat_id", chatId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Calling RAG service to clear chat: {}", url);
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");
                log.info("Chat history clear result: {}", success);
                return success != null ? success : false;
            } else {
                log.error("RAG service returned non-successful status: {}", response.getStatusCode());
                return false;
            }

        } catch (RestClientException e) {
            log.error("Error calling RAG service to clear chat: ", e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error in clearChatHistory: ", e);
            return false;
        }
    }

    @Override
    public boolean indexCourse(Long courseId, Map<String, Object> courseData) {
        try {
            String url = ragServiceUrl + "/api/rag/index-course";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("course_id", courseId);
            requestBody.putAll(courseData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Calling RAG service to index course {}: {}", courseId, url);
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");
                log.info("Course indexing result for course {}: {}", courseId, success);
                return success != null ? success : false;
            } else {
                log.error("RAG service returned non-successful status for course {}: {}", courseId, response.getStatusCode());
                return false;
            }

        } catch (RestClientException e) {
            log.error("Error calling RAG service to index course {}: ", courseId, e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error in indexCourse for course {}: ", courseId, e);
            return false;
        }
    }

    @Override
    public Map<String, String> getHealthStatus() {
        try {
            String url = ragServiceUrl + "/health";
            
            log.info("Checking RAG service health: {}", url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, String> health = (Map<String, String>) response.getBody();
                log.info("RAG service health check successful");
                return health;
            } else {
                log.error("RAG service health check returned non-successful status: {}", response.getStatusCode());
                Map<String, String> errorHealth = new HashMap<>();
                errorHealth.put("status", "unhealthy");
                errorHealth.put("service", "rag-service");
                errorHealth.put("error", "Health check returned non-2xx status");
                return errorHealth;
            }

        } catch (RestClientException e) {
            log.error("Error checking RAG service health: ", e);
            Map<String, String> errorHealth = new HashMap<>();
            errorHealth.put("status", "unreachable");
            errorHealth.put("service", "rag-service");
            errorHealth.put("error", "Failed to connect to RAG service");
            return errorHealth;
        } catch (Exception e) {
            log.error("Unexpected error in getHealthStatus: ", e);
            Map<String, String> errorHealth = new HashMap<>();
            errorHealth.put("status", "error");
            errorHealth.put("service", "rag-service");
            errorHealth.put("error", "Unexpected error occurred");
            return errorHealth;
        }
    }

    /**
     * Create a standard error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("chat_id", "");
        errorResponse.put("response", "I'm sorry, " + message + ". Please try again later.");
        errorResponse.put("recommended_courses", new Object[0]);
        errorResponse.put("message_type", "error");
        return errorResponse;
    }
}