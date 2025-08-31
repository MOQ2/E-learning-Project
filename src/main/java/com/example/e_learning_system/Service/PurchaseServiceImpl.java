package com.example.e_learning_system.Service;

import com.example.e_learning_system.Config.AccessType;
import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.SimplePaymentStatus;
import com.example.e_learning_system.Config.SimplePaymentType;
import com.example.e_learning_system.Dto.*;
import com.example.e_learning_system.Dto.PaymentDtos.SimplePaymentRequestDTO;
import com.example.e_learning_system.Dto.PaymentDtos.SimplePaymentResponseDTO;
import com.example.e_learning_system.Dto.PurchaseDtos.CoursePurchaseRequestDTO;
import com.example.e_learning_system.Dto.PurchaseDtos.PackagePurchaseRequestDTO;
import com.example.e_learning_system.Dto.PurchaseDtos.PurchaseResponseDTO;
import com.example.e_learning_system.Dto.PurchaseDtos.SubscriptionRequestDTO;
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
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.Package;

import java.math.BigDecimal;
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
    public PurchaseResponseDTO purchaseCourse(Integer courseId, CoursePurchaseRequestDTO request) {
        log.info("Processing course purchase for courseId: {} and userId: {}", courseId, request.getUserId());

        // Validate course exists and is purchasable
        Course course = validateCourseForPurchase(courseId);
        
        // Calculate amount
        BigDecimal amount = course.getOneTimePrice();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Course does not have a valid one-time purchase price");
        }

        // Create payment request
        SimplePaymentRequestDTO paymentRequest = createCoursePaymentRequest(request, amount, courseId);

        // Create and process payment
        SimplePaymentResponseDTO payment = createAndProcessPayment(paymentRequest, request.getStripePaymentIntentId());

        // Grant access if payment is completed
        List<UserCourseAccessResponseDTO> courseAccesses = new ArrayList<>();
        LocalDateTime accessUntil = null; // Permanent access for purchases
        
        if (payment.getStatus() == SimplePaymentStatus.COMPLETED) {
            UserCourseAccessResponseDTO courseAccess = userCourseAccessService.grantCourseAccess(
                    request.getUserId(), courseId, AccessType.PURCHASED, accessUntil, payment.getId());
            courseAccesses.add(courseAccess);
        }

        return buildPurchaseResponse(payment, courseAccesses, accessUntil, 
                AccessType.PURCHASED, "Course purchase completed successfully");
    }

    @Override
    public PurchaseResponseDTO purchasePackage(Integer packageId, PackagePurchaseRequestDTO request) {
        log.info("Processing package purchase for packageId: {} and userId: {}", packageId, request.getUserId());

        // Validate package exists and is purchasable
        Package packageEntity = validatePackageForPurchase(packageId);
        
        // Calculate amount
        BigDecimal amount = packageEntity.getPrice();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Package does not have a valid purchase price");
        }

        // Create payment request
        SimplePaymentRequestDTO paymentRequest = createPackagePaymentRequest(request, amount, packageId);

        // Create and process payment
        SimplePaymentResponseDTO payment = createAndProcessPayment(paymentRequest, request.getStripePaymentIntentId());

        // Grant access if payment is completed
        List<UserCourseAccessResponseDTO> courseAccesses = new ArrayList<>();
        LocalDateTime accessUntil = null; // Permanent access for purchases
        
        if (payment.getStatus() == SimplePaymentStatus.COMPLETED) {
            UserCourseAccessResponseDTO packageAccess = userCourseAccessService.grantPackageAccess(
                    request.getUserId(), packageId, AccessType.PACKAGE_ACCESS, accessUntil, payment.getId());
            courseAccesses.add(packageAccess);
        }

        return buildPurchaseResponse(payment, courseAccesses, accessUntil, 
                AccessType.PACKAGE_ACCESS, "Package purchase completed successfully");
    }

    @Override
    public PurchaseResponseDTO subscribeToCourse(Integer courseId, SubscriptionRequestDTO request) {
        log.info("Processing course subscription for courseId: {} and userId: {} for {} months", 
                courseId, request.getUserId(), request.getSubscriptionDurationMonths());

        // Validate course exists and supports subscription
        Course course = validateCourseForSubscription(courseId);
        
        // Calculate subscription amount based on duration
        BigDecimal amount = getSubscriptionPrice(course, request.getSubscriptionDurationAsInt());

        // Create payment request
        SimplePaymentRequestDTO paymentRequest = createSubscriptionPaymentRequest(
                request, amount, courseId, null, request.getSubscriptionDurationAsInt());

        // Create and process payment
        SimplePaymentResponseDTO payment = createAndProcessPayment(paymentRequest, request.getStripePaymentIntentId());

        // Grant access if payment is completed
        List<UserCourseAccessResponseDTO> courseAccesses = new ArrayList<>();
        LocalDateTime accessUntil = null;
        
        if (payment.getStatus() == SimplePaymentStatus.COMPLETED) {
            accessUntil = calculateSubscriptionAccessUntil(payment);
            UserCourseAccessResponseDTO courseAccess = userCourseAccessService.grantCourseAccess(
                    request.getUserId(), courseId, AccessType.SUBSCRIPTION_ACCESS, accessUntil, payment.getId());
            courseAccesses.add(courseAccess);
        }

        return buildPurchaseResponse(payment, courseAccesses, accessUntil, 
                AccessType.SUBSCRIPTION_ACCESS, "Course subscription completed successfully");
    }

    @Override
    public PurchaseResponseDTO subscribeToPackage(Integer packageId, SubscriptionRequestDTO request) {
        log.info("Processing package subscription for packageId: {} and userId: {} for {} months", 
                packageId, request.getUserId(), request.getSubscriptionDurationMonths());

        // Validate package exists and supports subscription
        Package packageEntity = validatePackageForSubscription(packageId);
        
        // Calculate subscription amount based on duration
        BigDecimal amount = getSubscriptionPrice(packageEntity, request.getSubscriptionDurationAsInt());

        // Create payment request
        SimplePaymentRequestDTO paymentRequest = createSubscriptionPaymentRequest(
                request, amount, null, packageId, request.getSubscriptionDurationAsInt());

        // Create and process payment
        SimplePaymentResponseDTO payment = createAndProcessPayment(paymentRequest, request.getStripePaymentIntentId());

        // Grant access if payment is completed
        List<UserCourseAccessResponseDTO> courseAccesses = new ArrayList<>();
        LocalDateTime accessUntil = null;
        
        if (payment.getStatus() == SimplePaymentStatus.COMPLETED) {
            accessUntil = calculateSubscriptionAccessUntil(payment);
            UserCourseAccessResponseDTO packageAccess = userCourseAccessService.grantPackageAccess(
                    request.getUserId(), packageId, AccessType.SUBSCRIPTION_ACCESS, accessUntil, payment.getId());
            courseAccesses.add(packageAccess);
        }

        return buildPurchaseResponse(payment, courseAccesses, accessUntil, 
                AccessType.SUBSCRIPTION_ACCESS, "Package subscription completed successfully");
    }

    @Override
    public PurchaseResponseDTO completePurchase(Integer paymentId) {
        log.info("Completing purchase for paymentId: {}", paymentId);

        // Get payment details
        SimplePaymentResponseDTO payment = simplePaymentService.getPaymentById(paymentId);
        
        if (payment.getStatus() != SimplePaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Payment is not completed");
        }

        // Calculate access until and type
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
        } else if (payment.getPaymentType() == SimplePaymentType.SUBSCRIPTION) {
            if (payment.getCourseId() != null) {
                UserCourseAccessResponseDTO courseAccess = userCourseAccessService.grantCourseAccess(
                        payment.getUserId(), payment.getCourseId(), accessType, accessUntil, paymentId);
                courseAccesses.add(courseAccess);
            } else if (payment.getPackageId() != null) {
                UserCourseAccessResponseDTO packageAccess = userCourseAccessService.grantPackageAccess(
                        payment.getUserId(), payment.getPackageId(), accessType, accessUntil, paymentId);
                courseAccesses.add(packageAccess);
            }
        }

        return buildPurchaseResponse(payment, courseAccesses, accessUntil, accessType, "Purchase completed successfully");
    }

    // Private helper methods

    private Course validateCourseForPurchase(Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId.toString()));
        
        if (!course.isActive()) {
            throw new IllegalArgumentException("Course is not active and cannot be purchased");
        }
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException("Course is not published and cannot be purchased");
        }
        if (course.isFree()) {
            throw new IllegalArgumentException("This course is free and does not require purchase");
        }
        
        return course;
    }

    private Course validateCourseForSubscription(Integer courseId) {
        Course course = validateCourseForPurchase(courseId);
        
        if (!course.getAllowsSubscription()) {
            throw new IllegalArgumentException("Course does not support subscription");
        }
        
        return course;
    }

    private Package validatePackageForPurchase(Integer packageId) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> ResourceNotFound.packageNotFound(packageId.longValue()));
        
        if (!packageEntity.getIsActive()) {
            throw new IllegalArgumentException("Package is not active and cannot be purchased");
        }
        
        return packageEntity;
    }

    private Package validatePackageForSubscription(Integer packageId) {
        Package packageEntity = validatePackageForPurchase(packageId);
        
        if (!packageEntity.getAllowsSubscription()) {
            throw new IllegalArgumentException("Package does not support subscription");
        }
        
        return packageEntity;
    }

    private BigDecimal getSubscriptionPrice(Course course, Integer durationMonths) {
        return switch (durationMonths) {
            case 1 -> {
                if (course.getSubscriptionPriceMonthly() == null) {
                    throw new IllegalArgumentException("Course does not support 1-month subscription");
                }
                yield course.getSubscriptionPriceMonthly();
            }
            case 3 -> {
                if (course.getSubscriptionPrice3Months() == null) {
                    throw new IllegalArgumentException("Course does not support 3-month subscription");
                }
                yield course.getSubscriptionPrice3Months();
            }
            case 6 -> {
                if (course.getSubscriptionPrice6Months() == null) {
                    throw new IllegalArgumentException("Course does not support 6-month subscription");
                }
                yield course.getSubscriptionPrice6Months();
            }
            default -> throw new IllegalArgumentException("Invalid subscription duration. Must be 1, 3, or 6 months");
        };
    }

    private BigDecimal getSubscriptionPrice(Package packageEntity, Integer durationMonths) {
        return switch (durationMonths) {
            case 1 -> {
                if (packageEntity.getSubscriptionPriceMonthly() == null) {
                    throw new IllegalArgumentException("Package does not support 1-month subscription");
                }
                yield packageEntity.getSubscriptionPriceMonthly();
            }
            case 3 -> {
                if (packageEntity.getSubscriptionPrice3Months() == null) {
                    throw new IllegalArgumentException("Package does not support 3-month subscription");
                }
                yield packageEntity.getSubscriptionPrice3Months();
            }
            case 6 -> {
                if (packageEntity.getSubscriptionPrice6Months() == null) {
                    throw new IllegalArgumentException("Package does not support 6-month subscription");
                }
                yield packageEntity.getSubscriptionPrice6Months();
            }
            default -> throw new IllegalArgumentException("Invalid subscription duration. Must be 1, 3, or 6 months");
        };
    }

    private SimplePaymentRequestDTO createCoursePaymentRequest(CoursePurchaseRequestDTO request, BigDecimal amount, Integer courseId) {
        SimplePaymentRequestDTO paymentRequest = new SimplePaymentRequestDTO();
        paymentRequest.setUserId(request.getUserId());
        paymentRequest.setPaymentType(SimplePaymentType.COURSE_PURCHASE);
        paymentRequest.setAmount(amount);
        paymentRequest.setPromotionCode(request.getPromotionCode());
        paymentRequest.setCourseId(courseId);
        return paymentRequest;
    }

    private SimplePaymentRequestDTO createPackagePaymentRequest(PackagePurchaseRequestDTO request, BigDecimal amount, Integer packageId) {
        SimplePaymentRequestDTO paymentRequest = new SimplePaymentRequestDTO();
        paymentRequest.setUserId(request.getUserId());
        paymentRequest.setPaymentType(SimplePaymentType.PACKAGE_PURCHASE);
        paymentRequest.setAmount(amount);
        paymentRequest.setPromotionCode(request.getPromotionCode());
        paymentRequest.setPackageId(packageId);
        return paymentRequest;
    }

    private SimplePaymentRequestDTO createSubscriptionPaymentRequest(SubscriptionRequestDTO request, BigDecimal amount, 
                                                                   Integer courseId, Integer packageId, Integer durationMonths) {
        SimplePaymentRequestDTO paymentRequest = new SimplePaymentRequestDTO();
        paymentRequest.setUserId(request.getUserId());
        paymentRequest.setPaymentType(SimplePaymentType.SUBSCRIPTION);
        paymentRequest.setAmount(amount);
        paymentRequest.setPromotionCode(request.getPromotionCode());
        paymentRequest.setSubscriptionDurationMonths(durationMonths);
        
        if (courseId != null) {
            paymentRequest.setCourseId(courseId);
        }
        if (packageId != null) {
            paymentRequest.setPackageId(packageId);
        }
        
        return paymentRequest;
    }

    private SimplePaymentResponseDTO createAndProcessPayment(SimplePaymentRequestDTO paymentRequest, String stripePaymentIntentId) {
        // Create payment
        SimplePaymentResponseDTO payment = simplePaymentService.createPayment(paymentRequest);
        
        // Process payment if Stripe payment intent is provided
        if (stripePaymentIntentId != null && !stripePaymentIntentId.trim().isEmpty()) {
            payment = simplePaymentService.processPayment(payment.getId(), stripePaymentIntentId);
        }
        
        return payment;
    }

    private LocalDateTime calculateAccessUntil(SimplePaymentResponseDTO payment) {
        if (payment.getPaymentType() == SimplePaymentType.SUBSCRIPTION) {
            return calculateSubscriptionAccessUntil(payment);
        }
        return null; // Permanent access for purchases
    }

    private LocalDateTime calculateSubscriptionAccessUntil(SimplePaymentResponseDTO payment) {
        if (payment.getSubscriptionDurationMonths() != null && payment.getSubscriptionDurationMonths() > 0) {
            return payment.getPaymentDate() != null 
                    ? payment.getPaymentDate().plusMonths(payment.getSubscriptionDurationMonths())
                    : LocalDateTime.now().plusMonths(payment.getSubscriptionDurationMonths());
        }
        return null;
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
