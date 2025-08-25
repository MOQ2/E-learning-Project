package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.PurchaseRequestDTO;
import com.example.e_learning_system.Dto.PurchaseResponseDTO;
import com.example.e_learning_system.Service.Interfaces.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * Purchase a course
     * Creates payment and grants course access in a single transaction
     */
    @PostMapping("/course/{courseId}")
    public ResponseEntity<PurchaseResponseDTO> purchaseCourse(
            @PathVariable Integer courseId,
            @Valid @RequestBody PurchaseRequestDTO request) {
        
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
     * Purchase a package
     * Creates payment and grants access to all courses in the package
     */
    @PostMapping("/package/{packageId}")
    public ResponseEntity<PurchaseResponseDTO> purchasePackage(
            @PathVariable Integer packageId,
            @Valid @RequestBody PurchaseRequestDTO request) {
        
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
