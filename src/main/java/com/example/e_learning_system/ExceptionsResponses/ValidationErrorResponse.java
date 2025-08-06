
package com.example.e_learning_system.ExceptionsResponses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponse extends BaseErrorResponse {


    private List<String> validationErrors;
    private Map<String, String> fieldErrors;

    public static ValidationErrorResponse from(String message, String errorCode, HttpStatus httpStatus,
                                               String path, String method, List<String> validationErrors) {
        return ValidationErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .status(httpStatus.value())
                .statusText(httpStatus.getReasonPhrase())
                .timestamp(java.time.LocalDateTime.now())
                .path(path)
                .method(method)
                .validationErrors(validationErrors)
                .build();
    }
}