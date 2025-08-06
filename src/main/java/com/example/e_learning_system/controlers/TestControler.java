package com.example.e_learning_system.controlers;

import com.example.e_learning_system.excpetions.ResourceNotFound;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestControler {


    @GetMapping("/test-exception")
    public String testException() {
        throw ResourceNotFound.courseNotFound("course");
    }

}

