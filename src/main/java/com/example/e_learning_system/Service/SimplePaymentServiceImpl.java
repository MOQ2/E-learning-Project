package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.PaymentDtos.SimplePaymentRequestDTO;
import com.example.e_learning_system.Dto.PaymentDtos.SimplePaymentResponseDTO;
import com.example.e_learning_system.Entities.SimplePayment;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.Package;
import com.example.e_learning_system.Entities.PromotionCode;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Repository.SimplePaymentRepository;
import com.example.e_learning_system.Repository.CourseRepository;
import com.example.e_learning_system.Repository.PackageRepository;
import com.example.e_learning_system.Repository.PromotionCodeRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.Mapper.SimplePaymentMapper;
import com.example.e_learning_system.Service.Interfaces.SimplePaymentService;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import com.example.e_learning_system.Config.SimplePaymentStatus;
import com.example.e_learning_system.Config.SimplePaymentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimplePaymentServiceImpl implements SimplePaymentService {

    @Autowired
    private SimplePaymentRepository simplePaymentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PromotionCodeRepository promotionCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimplePaymentMapper simplePaymentMapper;

    @Override
    @Transactional
    public SimplePaymentResponseDTO createPayment(SimplePaymentRequestDTO requestDTO) {
        UserEntity user = userRepository.findById(requestDTO.getUserId())
            .orElseThrow(() -> ResourceNotFound.userNotFound(requestDTO.getUserId().toString()));

        Course course = null;
        Package packageEntity = null;

        // Set course or package based on payment type
        if (requestDTO.getPaymentType() == SimplePaymentType.COURSE_PURCHASE) {
            course = courseRepository.findById(requestDTO.getCourseId())
                .orElseThrow(() -> ResourceNotFound.courseNotFound(requestDTO.getCourseId().toString()));
        } else if (requestDTO.getPaymentType() == SimplePaymentType.PACKAGE_PURCHASE) {
            packageEntity = packageRepository.findById(requestDTO.getPackageId())
                .orElseThrow(() -> ResourceNotFound.packageNotFound(requestDTO.getPackageId().longValue()));
        }

        PromotionCode promotionCode = null;
        // Apply promotion code if provided
        if (requestDTO.getPromotionCode() != null && !requestDTO.getPromotionCode().trim().isEmpty()) {
            promotionCode = promotionCodeRepository.findValidPromotionCode(
                requestDTO.getPromotionCode(), LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired promotion code"));

            // Validate promotion code applicability
            if (requestDTO.getPaymentType() == SimplePaymentType.COURSE_PURCHASE && !promotionCode.getApplicableToCourses()) {
                throw new IllegalArgumentException("Promotion code is not applicable to courses");
            }
            if (requestDTO.getPaymentType() == SimplePaymentType.PACKAGE_PURCHASE && !promotionCode.getApplicableToPackages()) {
                throw new IllegalArgumentException("Promotion code is not applicable to packages");
            }

            // Check if promotion code has remaining uses
            if (promotionCode.getCurrentUses() >= promotionCode.getMaxUses()) {
                throw new IllegalArgumentException("Promotion code has reached maximum uses");
            }
        }

        SimplePayment payment = simplePaymentMapper.requestDtoToEntity(requestDTO, user, course, packageEntity, promotionCode);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(SimplePaymentStatus.PENDING);

        SimplePayment savedPayment = simplePaymentRepository.save(payment);
        return simplePaymentMapper.entityToResponseDto(savedPayment);
    }

    @Override
    @Transactional
    public SimplePaymentResponseDTO processPayment(Integer paymentId, String stripePaymentIntentId) {
        SimplePayment payment = simplePaymentRepository.findById(paymentId)
            .orElseThrow(() -> ResourceNotFound.simplePaymentNotFound(paymentId));

        if (payment.getStatus() != SimplePaymentStatus.PENDING) {
            throw new IllegalArgumentException("Payment is not in pending status");
        }

        // Process payment
        payment.setStatus(SimplePaymentStatus.COMPLETED);
        payment.setStripePaymentIntentId(stripePaymentIntentId);
        payment.setPaymentDate(LocalDateTime.now());

        // If promotion code was used, increment its usage
        if (payment.getPromotionCode() != null) {
            PromotionCode promotionCode = payment.getPromotionCode();
            promotionCode.setCurrentUses(promotionCode.getCurrentUses() + 1);
            promotionCodeRepository.save(promotionCode);
        }

        SimplePayment updatedPayment = simplePaymentRepository.save(payment);
        return simplePaymentMapper.entityToResponseDto(updatedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public SimplePaymentResponseDTO getPaymentById(Integer paymentId) {
        SimplePayment payment = simplePaymentRepository.findById(paymentId)
            .orElseThrow(() -> ResourceNotFound.simplePaymentNotFound(paymentId));
        
        return simplePaymentMapper.entityToResponseDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimplePaymentResponseDTO> getPaymentsByUserId(Integer userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFound.userNotFound(userId.toString()));

        List<SimplePayment> payments = simplePaymentRepository.findByUserOrderByPaymentDateDesc(user);
        return payments.stream()
            .map(simplePaymentMapper::entityToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimplePaymentResponseDTO> getPaymentsByStatus(SimplePaymentStatus status) {
        List<SimplePayment> payments = simplePaymentRepository.findByStatusOrderByPaymentDateDesc(status);
        return payments.stream()
            .map(simplePaymentMapper::entityToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimplePaymentResponseDTO> getPaymentsByType(SimplePaymentType paymentType) {
        List<SimplePayment> payments = simplePaymentRepository.findByPaymentTypeOrderByPaymentDateDesc(paymentType);
        return payments.stream()
            .map(simplePaymentMapper::entityToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SimplePaymentResponseDTO updatePaymentStatus(Integer paymentId, SimplePaymentStatus status) {
        SimplePayment payment = simplePaymentRepository.findById(paymentId)
            .orElseThrow(() -> ResourceNotFound.simplePaymentNotFound(paymentId));

        payment.setStatus(status);
        SimplePayment updatedPayment = simplePaymentRepository.save(payment);
        return simplePaymentMapper.entityToResponseDto(updatedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimplePaymentResponseDTO> getPaymentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        List<SimplePayment> payments = simplePaymentRepository.findByPaymentDateBetweenOrderByPaymentDateDesc(startDate, endDate);
        return payments.stream()
            .map(simplePaymentMapper::entityToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenueBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return simplePaymentRepository.getTotalRevenueByDateRange(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimplePaymentResponseDTO> getSuccessfulPaymentsByCourse(Integer courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId.toString()));

        List<SimplePayment> payments = simplePaymentRepository.findByCourseAndStatusOrderByPaymentDateDesc(course, SimplePaymentStatus.COMPLETED);
        return payments.stream()
            .map(simplePaymentMapper::entityToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimplePaymentResponseDTO> getSuccessfulPaymentsByPackage(Integer packageId) {
        Package packageEntity = packageRepository.findById(packageId)
            .orElseThrow(() -> ResourceNotFound.packageNotFound(packageId.longValue()));

        List<SimplePayment> payments = simplePaymentRepository.findByPackageEntityAndStatusOrderByPaymentDateDesc(packageEntity, SimplePaymentStatus.COMPLETED);
        return payments.stream()
            .map(simplePaymentMapper::entityToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserPaidForCourse(Integer userId, Integer courseId) {
        return simplePaymentRepository.existsByUserIdAndCourseIdAndStatus(userId, courseId, SimplePaymentStatus.COMPLETED);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserPaidForPackage(Integer userId, Integer packageId) {
        return simplePaymentRepository.existsByUserIdAndPackageEntityIdAndStatus(userId, packageId, SimplePaymentStatus.COMPLETED);
    }

    @Override
    @Transactional(readOnly = true)
    public SimplePaymentResponseDTO findPaymentByStripeId(String stripePaymentIntentId) {
        SimplePayment payment = simplePaymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
            .orElseThrow(() -> ResourceNotFound.simplePaymentNotFound(stripePaymentIntentId));
        
        return simplePaymentMapper.entityToResponseDto(payment);
    }
}
