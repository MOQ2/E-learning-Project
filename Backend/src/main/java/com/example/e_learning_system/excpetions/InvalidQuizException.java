package com.example.e_learning_system.excpetions;

import org.springframework.http.HttpStatus;

public class InvalidQuizException extends BaseException {

    public InvalidQuizException(String message) {
        super(message, "INVALID_QUIZ", HttpStatus.BAD_REQUEST);
    }
}
