package com.example.e_learning_system.Dto;

import com.example.e_learning_system.Config.AccessType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponseDTO {

    // Payment information
    private SimplePaymentResponseDTO payment;

    // Access information
    private List<UserCourseAccessResponseDTO> courseAccesses;

    // Additional purchase info
    private LocalDateTime purchaseDate;
    private LocalDateTime accessUntil;
    private AccessType accessType;
    private String message;
}
