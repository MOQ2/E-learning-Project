package com.example.e_learning_system.DTOs;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseErrorResponse {

    private String message;
    private String errorCode;
    private int status;
    private String statusText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;
    private String method;
    private String requestId;

    // for additional info
    private Map<String, Object> metadata;


    public static BaseErrorResponse from(String message, String errorCode, HttpStatus httpStatus, String path) {
        return BaseErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .status(httpStatus.value())
                .statusText(httpStatus.getReasonPhrase())
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    public static BaseErrorResponse from(String message, String errorCode, HttpStatus httpStatus, String path, String method) {
        return BaseErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .status(httpStatus.value())
                .statusText(httpStatus.getReasonPhrase())
                .timestamp(LocalDateTime.now())
                .path(path)
                .method(method)
                .build();
    }
}
