package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Dto.PurchaseRequestDTO;
import com.example.e_learning_system.Dto.PurchaseResponseDTO;

public interface PurchaseService {

    /**
     * Purchase a course - creates payment and grants course access
     * @param courseId The ID of the course to purchase
     * @param request The purchase request details
     * @return Purchase response with payment and access information
     */
    PurchaseResponseDTO purchaseCourse(Integer courseId, PurchaseRequestDTO request);

    /**
     * Purchase a package - creates payment and grants access to all courses in package
     * @param packageId The ID of the package to purchase
     * @param request The purchase request details
     * @return Purchase response with payment and access information
     */
    PurchaseResponseDTO purchasePackage(Integer packageId, PurchaseRequestDTO request);

    /**
     * Complete a purchase after successful payment processing
     * @param paymentId The ID of the processed payment
     * @return Purchase response with updated access information
     */
    PurchaseResponseDTO completePurchase(Integer paymentId);
}
