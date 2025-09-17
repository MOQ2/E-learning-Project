package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Dto.PurchaseDtos.CoursePurchaseRequestDTO;
import com.example.e_learning_system.Dto.PurchaseDtos.PackagePurchaseRequestDTO;
import com.example.e_learning_system.Dto.PurchaseDtos.PurchaseResponseDTO;
import com.example.e_learning_system.Dto.PurchaseDtos.SubscriptionRequestDTO;

public interface PurchaseService {

    /**
     * Purchase a course - one-time payment for permanent access
     * @param courseId The ID of the course to purchase
     * @param request The purchase request details
     * @return Purchase response with payment and access information
     */
    PurchaseResponseDTO purchaseCourse(Integer courseId, CoursePurchaseRequestDTO request);

    /**
     * Purchase a package - one-time payment for permanent access to all courses in package
     * @param packageId The ID of the package to purchase
     * @param request The purchase request details
     * @return Purchase response with payment and access information
     */
    PurchaseResponseDTO purchasePackage(Integer packageId, PackagePurchaseRequestDTO request);

    /**
     * Subscribe to a course - recurring payment for limited time access
     * @param courseId The ID of the course to subscribe to
     * @param request The subscription request details
     * @return Purchase response with payment and access information
     */
    PurchaseResponseDTO subscribeToCourse(Integer courseId, SubscriptionRequestDTO request);

    /**
     * Subscribe to a package - recurring payment for limited time access to all courses in package
     * @param packageId The ID of the package to subscribe to
     * @param request The subscription request details
     * @return Purchase response with payment and access information
     */
    PurchaseResponseDTO subscribeToPackage(Integer packageId, SubscriptionRequestDTO request);

    /**
     * Complete a purchase after successful payment processing
     * @param paymentId The ID of the processed payment
     * @return Purchase response with updated access information
     */
    PurchaseResponseDTO completePurchase(Integer paymentId);
}
