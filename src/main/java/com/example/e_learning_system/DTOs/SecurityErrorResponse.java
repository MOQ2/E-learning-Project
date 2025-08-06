package com.example.e_learning_system.DTOs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class SecurityErrorResponse extends BaseErrorResponse {


    private String authenticationScheme;
    private String requiredRole;
    private String requiredPermission;

    public static SecurityErrorResponse from(String message, String errorCode, HttpStatus httpStatus,
                                             String path, String method) {

        return SecurityErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .status(httpStatus.value())
                .statusText(httpStatus.getReasonPhrase())
                .timestamp(java.time.LocalDateTime.now())
                .path(path)
                .method(method)
                .build();
    }
}