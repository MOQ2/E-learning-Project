package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Config.SimplePaymentStatus;
import com.example.e_learning_system.Config.SimplePaymentType;
import com.example.e_learning_system.Dto.PaymentDtos.SimplePaymentRequestDTO;
import com.example.e_learning_system.Dto.PaymentDtos.SimplePaymentResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SimplePaymentService {
    
    SimplePaymentResponseDTO createPayment(SimplePaymentRequestDTO requestDTO);
    
    SimplePaymentResponseDTO processPayment(Integer paymentId, String stripePaymentIntentId);
    
    SimplePaymentResponseDTO getPaymentById(Integer paymentId);
    
    List<SimplePaymentResponseDTO> getPaymentsByUserId(Integer userId);
    
    List<SimplePaymentResponseDTO> getPaymentsByStatus(SimplePaymentStatus status);
    
    List<SimplePaymentResponseDTO> getPaymentsByType(SimplePaymentType paymentType);
    
    SimplePaymentResponseDTO updatePaymentStatus(Integer paymentId, SimplePaymentStatus status);
    
    List<SimplePaymentResponseDTO> getPaymentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
    
    BigDecimal getTotalRevenueBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
    
    List<SimplePaymentResponseDTO> getSuccessfulPaymentsByCourse(Integer courseId);
    
    List<SimplePaymentResponseDTO> getSuccessfulPaymentsByPackage(Integer packageId);
    
    boolean hasUserPaidForCourse(Integer userId, Integer courseId);
    
    boolean hasUserPaidForPackage(Integer userId, Integer packageId);
    
    SimplePaymentResponseDTO findPaymentByStripeId(String stripePaymentIntentId);
}
