package com.example.e_learning_system.Service;

import com.example.e_learning_system.Config.AccessType;
import com.example.e_learning_system.Config.SimplePaymentStatus;
import com.example.e_learning_system.Config.SimplePaymentType;
import com.example.e_learning_system.Dto.*;
import com.example.e_learning_system.Dto.PaymentDtos.SimplePaymentRequestDTO;
import com.example.e_learning_system.Dto.PaymentDtos.SimplePaymentResponseDTO;
import com.example.e_learning_system.Dto.PurchaseDtos.PurchaseRequestDTO;
import com.example.e_learning_system.Dto.PurchaseDtos.PurchaseResponseDTO;
import com.example.e_learning_system.Repository.CourseRepository;
import com.example.e_learning_system.Repository.PackageRepository;
import com.example.e_learning_system.Service.Interfaces.PurchaseService;
import com.example.e_learning_system.Service.Interfaces.SimplePaymentService;
import com.example.e_learning_system.Service.Interfaces.UserCourseAccessService;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {

    private final SimplePaymentService simplePaymentService;
    private final UserCourseAccessService userCourseAccessService;
    private final CourseRepository courseRepository;
    private final PackageRepository packageRepository;

    @Override
    public PurchaseResponseDTO purchaseCourse(Integer courseId, PurchaseRequestDTO request) {
        log.info("Processing course purchase for courseId: {} and userId: {}", courseId, request.getUserId());

        // Validate course exists
        courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId.toString()));

        // Create payment request
        SimplePaymentRequestDTO paymentRequest = createPaymentRequest(request, courseId, null);
        
        // Create payment
        SimplePaymentResponseDTO payment = simplePaymentService.createPayment(paymentRequest);
        
        // Process payment if Stripe payment intent is provided
        if (request.getStripePaymentIntentId() != null) {
            payment = simplePaymentService.processPayment(payment.getId(), request.getStripePaymentIntentId());
        }

        // If payment is completed, grant access
        List<UserCourseAccessResponseDTO> courseAccesses = new ArrayList<>();
        LocalDateTime accessUntil = null;
        
        if (payment.getStatus() == SimplePaymentStatus.COMPLETED) {
            accessUntil = calculateAccessUntil(payment);
            AccessType accessType = determineAccessType(request.getPaymentType());
            
            UserCourseAccessResponseDTO courseAccess = userCourseAccessService.grantCourseAccess(
                    request.getUserId(), courseId, accessType, accessUntil, payment.getId());
            courseAccesses.add(courseAccess);
        }

        return buildPurchaseResponse(payment, courseAccesses, accessUntil, 
                determineAccessType(request.getPaymentType()), "Course purchase completed successfully");
    }

    @Override
    public PurchaseResponseDTO purchasePackage(Integer packageId, PurchaseRequestDTO request) {
        log.info("Processing package purchase for packageId: {} and userId: {}", packageId, request.getUserId());

        // Validate package exists
        packageRepository.findById(packageId)
                .orElseThrow(() -> ResourceNotFound.packageNotFound(packageId.longValue()));

        // Create payment request
        SimplePaymentRequestDTO paymentRequest = createPaymentRequest(request, null, packageId);
        
        // Create payment
        SimplePaymentResponseDTO payment = simplePaymentService.createPayment(paymentRequest);
        
        // Process payment if Stripe payment intent is provided
        if (request.getStripePaymentIntentId() != null) {
            payment = simplePaymentService.processPayment(payment.getId(), request.getStripePaymentIntentId());
        }

        // If payment is completed, grant package access
        List<UserCourseAccessResponseDTO> courseAccesses = new ArrayList<>();
        LocalDateTime accessUntil = null;
        
        if (payment.getStatus() == SimplePaymentStatus.COMPLETED) {
            accessUntil = calculateAccessUntil(payment);
            AccessType accessType = determineAccessType(request.getPaymentType());
            
            UserCourseAccessResponseDTO packageAccess = userCourseAccessService.grantPackageAccess(
                    request.getUserId(), packageId, accessType, accessUntil, payment.getId());
            courseAccesses.add(packageAccess);
        }

        return buildPurchaseResponse(payment, courseAccesses, accessUntil, 
                determineAccessType(request.getPaymentType()), "Package purchase completed successfully");
    }

    @Override
    public PurchaseResponseDTO completePurchase(Integer paymentId) {
        log.info("Completing purchase for paymentId: {}", paymentId);

        // Get payment details
        SimplePaymentResponseDTO payment = simplePaymentService.getPaymentById(paymentId);
        
        if (payment.getStatus() != SimplePaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Payment is not completed");
        }

        // Calculate access until
        LocalDateTime accessUntil = calculateAccessUntil(payment);
        AccessType accessType = determineAccessType(payment.getPaymentType());
        
        // Grant access based on payment type
        List<UserCourseAccessResponseDTO> courseAccesses = new ArrayList<>();
        
        if (payment.getPaymentType() == SimplePaymentType.COURSE_PURCHASE && payment.getCourseId() != null) {
            UserCourseAccessResponseDTO courseAccess = userCourseAccessService.grantCourseAccess(
                    payment.getUserId(), payment.getCourseId(), accessType, accessUntil, paymentId);
            courseAccesses.add(courseAccess);
        } else if (payment.getPaymentType() == SimplePaymentType.PACKAGE_PURCHASE && payment.getPackageId() != null) {
            UserCourseAccessResponseDTO packageAccess = userCourseAccessService.grantPackageAccess(
                    payment.getUserId(), payment.getPackageId(), accessType, accessUntil, paymentId);
            courseAccesses.add(packageAccess);
        }

        return buildPurchaseResponse(payment, courseAccesses, accessUntil, accessType, "Purchase completed successfully");
    }

    // Private helper methods

    private SimplePaymentRequestDTO createPaymentRequest(PurchaseRequestDTO request, Integer courseId, Integer packageId) {
        SimplePaymentRequestDTO paymentRequest = new SimplePaymentRequestDTO();
        paymentRequest.setUserId(request.getUserId());
        paymentRequest.setPaymentType(request.getPaymentType());
        paymentRequest.setAmount(request.getAmount());
        paymentRequest.setPromotionCode(request.getPromotionCode());
        paymentRequest.setSubscriptionDurationMonths(request.getSubscriptionDurationMonths());
        
        if (courseId != null) {
            paymentRequest.setCourseId(courseId);
        }
        if (packageId != null) {
            paymentRequest.setPackageId(packageId);
        }
        
        return paymentRequest;
    }

    private LocalDateTime calculateAccessUntil(SimplePaymentResponseDTO payment) {
        if (payment.getSubscriptionDurationMonths() != null && payment.getSubscriptionDurationMonths() > 0) {
            return payment.getPaymentDate() != null 
                    ? payment.getPaymentDate().plusMonths(payment.getSubscriptionDurationMonths())
                    : LocalDateTime.now().plusMonths(payment.getSubscriptionDurationMonths());
        }
        return null; // Lifetime access for one-time purchases
    }

    private AccessType determineAccessType(SimplePaymentType paymentType) {
        return switch (paymentType) {
            case COURSE_PURCHASE -> AccessType.PURCHASED;
            case PACKAGE_PURCHASE -> AccessType.PACKAGE_ACCESS;
            case SUBSCRIPTION -> AccessType.SUBSCRIPTION_ACCESS;
        };
    }

    private PurchaseResponseDTO buildPurchaseResponse(SimplePaymentResponseDTO payment, 
                                                     List<UserCourseAccessResponseDTO> courseAccesses,
                                                     LocalDateTime accessUntil, 
                                                     AccessType accessType, 
                                                     String message) {
        PurchaseResponseDTO response = new PurchaseResponseDTO();
        response.setPayment(payment);
        response.setCourseAccesses(courseAccesses);
        response.setPurchaseDate(payment.getPaymentDate());
        response.setAccessUntil(accessUntil);
        response.setAccessType(accessType);
        response.setMessage(message);
        return response;
    }
}
