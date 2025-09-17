package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.PurchaseDtos.CoursePurchaseRequestDTO;
import com.example.e_learning_system.Dto.PurchaseDtos.PackagePurchaseRequestDTO;
import com.example.e_learning_system.Dto.PurchaseDtos.PurchaseResponseDTO;
import com.example.e_learning_system.Dto.PurchaseDtos.SubscriptionRequestDTO;
import com.example.e_learning_system.Service.Interfaces.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
@Slf4j
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * Purchase a course with one-time payment for permanent access
     */
    @PostMapping("/course/{courseId}")
    public ResponseEntity<PurchaseResponseDTO> purchaseCourse(
            @PathVariable Integer courseId,
            @Valid @RequestBody CoursePurchaseRequestDTO request) {
        
        log.info("Processing course purchase request for courseId: {} and userId: {}", 
                courseId, request.getUserId());
        
        try {
            PurchaseResponseDTO response = purchaseService.purchaseCourse(courseId, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error processing course purchase for courseId: {}, userId: {}", 
                    courseId, request.getUserId(), e);
            throw e;
        }
    }

    /**
     * Purchase a package with one-time payment for permanent access to all courses
     */
    @PostMapping("/package/{packageId}")
    public ResponseEntity<PurchaseResponseDTO> purchasePackage(
            @PathVariable Integer packageId,
            @Valid @RequestBody PackagePurchaseRequestDTO request) {
        
        log.info("Processing package purchase request for packageId: {} and userId: {}", 
                packageId, request.getUserId());
        
        try {
            PurchaseResponseDTO response = purchaseService.purchasePackage(packageId, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error processing package purchase for packageId: {}, userId: {}", 
                    packageId, request.getUserId(), e);
            throw e;
        }
    }

    /**
     * Subscribe to a course for limited time access (1, 3, or 6 months)
     */
    @PostMapping("/subscribe/course/{courseId}")
    public ResponseEntity<PurchaseResponseDTO> subscribeToCourse(
            @PathVariable Integer courseId,
            @Valid @RequestBody SubscriptionRequestDTO request) {
        
        log.info("Processing course subscription request for courseId: {} and userId: {} for {} months", 
                courseId, request.getUserId(), request.getSubscriptionDurationMonths());
        
        try {
            PurchaseResponseDTO response = purchaseService.subscribeToCourse(courseId, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error processing course subscription for courseId: {}, userId: {}", 
                    courseId, request.getUserId(), e);
            throw e;
        }
    }

    /**
     * Subscribe to a package for limited time access (1, 3, or 6 months)
     */
    @PostMapping("/subscribe/package/{packageId}")
    public ResponseEntity<PurchaseResponseDTO> subscribeToPackage(
            @PathVariable Integer packageId,
            @Valid @RequestBody SubscriptionRequestDTO request) {
        
        log.info("Processing package subscription request for packageId: {} and userId: {} for {} months", 
                packageId, request.getUserId(), request.getSubscriptionDurationMonths());
        
        try {
            PurchaseResponseDTO response = purchaseService.subscribeToPackage(packageId, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error processing package subscription for packageId: {}, userId: {}", 
                    packageId, request.getUserId(), e);
            throw e;
        }
    }

    /**
     * Complete a purchase after payment processing
     * This endpoint can be called after successful payment processing to grant access
     */
    @PostMapping("/complete/{paymentId}")
    public ResponseEntity<PurchaseResponseDTO> completePurchase(@PathVariable Integer paymentId) {
        
        log.info("Completing purchase for paymentId: {}", paymentId);
        
        try {
            PurchaseResponseDTO response = purchaseService.completePurchase(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error completing purchase for paymentId: {}", paymentId, e);
            throw e;
        }
    }

    /**
     * Health check endpoint for purchase service
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Purchase service is running");
    }
}
