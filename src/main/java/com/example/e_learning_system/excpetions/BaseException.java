package com.example.e_learning_system.excpetions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public abstract class BaseException extends RuntimeException {

    private  String  errorCode;
    private HttpStatus status;
    LocalDateTime timestamp;
    Object [] arguments;


    public BaseException(String message ,String errorCode,HttpStatus status ,Object [] arguments){
        super(message);
        this.errorCode=errorCode;
        this.status=status;
        this.arguments = arguments;
        this.timestamp = LocalDateTime.now();
    }

    public BaseException(String message,String errorCode,HttpStatus status,Throwable cause,Object [] arguments){
        super(message, cause);
        this.errorCode=errorCode;
        this.status=status;
        this.arguments = arguments;
        this.timestamp = LocalDateTime.now();
    }
    public BaseException(String message,String errorCode,HttpStatus status){
        super(message);
        this.errorCode=errorCode;
        this.status=status;
        this.timestamp = LocalDateTime.now();

    }

}
