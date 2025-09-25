package com.example.e_learning_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ELearningSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(ELearningSystemApplication.class, args);
	}

}