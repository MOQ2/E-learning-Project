package com.example.e_learning_system.Service;

import com.example.e_learning_system.Config.AccessType;
import com.example.e_learning_system.Dto.UserCourseAccessResponseDTO;
import com.example.e_learning_system.Entities.UserCourseAccess;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.Package;
import com.example.e_learning_system.Entities.SimplePayment;
import com.example.e_learning_system.Repository.UserCourseAccessRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.Repository.CourseRepository;
import com.example.e_learning_system.Repository.PackageRepository;
import com.example.e_learning_system.Repository.SimplePaymentRepository;
import com.example.e_learning_system.Repository.PackageCourseRepository;
import com.example.e_learning_system.Mapper.UserCourseAccessMapper;
import com.example.e_learning_system.Service.Interfaces.UserCourseAccessService;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserCourseAccessServiceImpl implements UserCourseAccessService {

    @Autowired
    private UserCourseAccessRepository userCourseAccessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private SimplePaymentRepository simplePaymentRepository;

    @Autowired
    private PackageCourseRepository packageCourseRepository;

    @Autowired
    private UserCourseAccessMapper userCourseAccessMapper;

    @Override
    @Transactional
    public UserCourseAccessResponseDTO grantCourseAccess(Integer userId, Integer courseId, AccessType accessType, 
                                                        LocalDateTime accessUntil, Integer paymentId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFound.userNotFound(userId.toString()));

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId.toString()));

        // Check if user already has active access to this course
        if (userCourseAccessRepository.existsByUserIdAndCourseIdAndIsActiveTrue(userId, courseId)) {
            throw new IllegalArgumentException("User already has active access to this course");
        }

        SimplePayment payment = null;
        if (paymentId != null) {
            payment = simplePaymentRepository.findById(paymentId)
                .orElseThrow(() -> ResourceNotFound.simplePaymentNotFound(paymentId.toString()));
        }

        UserCourseAccess access = UserCourseAccess.builder()
            .user(user)
            .course(course)
            .accessType(accessType)
            .accessUntil(accessUntil)
            .isActive(true)
            .payment(payment)
            .build();

        UserCourseAccess savedAccess = userCourseAccessRepository.save(access);
        return userCourseAccessMapper.entityToResponseDto(savedAccess);
    }

    @Override
    @Transactional
    public UserCourseAccessResponseDTO grantPackageAccess(Integer userId, Integer packageId, AccessType accessType, 
                                                         LocalDateTime accessUntil, Integer paymentId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFound.userNotFound(userId.toString()));

        Package packageEntity = packageRepository.findById(packageId)
            .orElseThrow(() -> ResourceNotFound.packageNotFound(packageId.longValue()));

        SimplePayment payment = null;
        if (paymentId != null) {
            payment = simplePaymentRepository.findById(paymentId)
                .orElseThrow(() -> ResourceNotFound.simplePaymentNotFound(paymentId.toString()));
        }

        // Get all courses in the package and grant access to each
        List<Course> packageCourses = packageCourseRepository.findCoursesByPackageId(packageId);
        
        UserCourseAccessResponseDTO lastCreatedAccess = null;
        
        for (Course course : packageCourses) {
            // Only grant access if user doesn't already have it
            if (!userCourseAccessRepository.existsByUserIdAndCourseIdAndIsActiveTrue(userId, course.getId())) {
                UserCourseAccess access = UserCourseAccess.builder()
                    .user(user)
                    .course(course)
                    .packageEntity(packageEntity)
                    .accessType(accessType)
                    .accessUntil(accessUntil)
                    .isActive(true)
                    .payment(payment)
                    .build();
                
                UserCourseAccess savedAccess = userCourseAccessRepository.save(access);
                lastCreatedAccess = userCourseAccessMapper.entityToResponseDto(savedAccess);
            }
        }
        
        return lastCreatedAccess;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasValidAccess(Integer userId, Integer courseId) {
        return userCourseAccessRepository.findValidAccessByUserAndCourse(userId, courseId, LocalDateTime.now())
            .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public UserCourseAccessResponseDTO getUserCourseAccess(Integer userId, Integer courseId) {
        UserCourseAccess access = userCourseAccessRepository.findActiveAccessByUserAndCourse(userId, courseId)
            .orElseThrow(() -> new IllegalArgumentException("User does not have active access to this course"));

        return userCourseAccessMapper.entityToResponseDto(access);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCourseAccessResponseDTO> getUserActiveAccesses(Integer userId) {
        List<UserCourseAccess> accesses = userCourseAccessRepository.findActiveAccessesByUser(userId, LocalDateTime.now());
        return accesses.stream()
            .map(userCourseAccessMapper::entityToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getUserAccessibleCourses(Integer userId) {
        return userCourseAccessRepository.findAccessibleCoursesByUser(userId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCourseAccessResponseDTO> getCourseActiveAccesses(Integer courseId) {
        List<UserCourseAccess> accesses = userCourseAccessRepository.findByCourseIdAndIsActiveTrue(courseId);
        return accesses.stream()
            .map(userCourseAccessMapper::entityToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeCourseAccess(Integer userId, Integer courseId) {
        UserCourseAccess access = userCourseAccessRepository.findActiveAccessByUserAndCourse(userId, courseId)
            .orElseThrow(() -> new IllegalArgumentException("User does not have active access to this course"));

        access.setIsActive(false);
        userCourseAccessRepository.save(access);
    }

    @Override
    @Transactional
    public void revokePackageAccess(Integer userId, Integer packageId) {
        List<UserCourseAccess> accesses = userCourseAccessRepository.findActiveAccessByUserAndPackage(userId, packageId);
        accesses.forEach(access -> access.setIsActive(false));
        userCourseAccessRepository.saveAll(accesses);
    }

    @Override
    @Transactional
    public UserCourseAccessResponseDTO extendAccess(Integer accessId, LocalDateTime newAccessUntil) {
        UserCourseAccess access = userCourseAccessRepository.findById(accessId)
            .orElseThrow(() -> new IllegalArgumentException("Access record not found"));

        access.setAccessUntil(newAccessUntil);
        UserCourseAccess savedAccess = userCourseAccessRepository.save(access);
        
        return userCourseAccessMapper.entityToResponseDto(savedAccess);
    }

    @Override
    @Transactional
    public void processExpiredAccesses() {
        List<UserCourseAccess> expiredAccesses = userCourseAccessRepository.findExpiredAccesses(LocalDateTime.now());
        expiredAccesses.forEach(access -> access.setIsActive(false));
        userCourseAccessRepository.saveAll(expiredAccesses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCourseAccessResponseDTO> getAccessesByType(AccessType accessType) {
        List<UserCourseAccess> accesses = userCourseAccessRepository.findByAccessTypeAndIsActiveTrue(accessType);
        return accesses.stream()
            .map(userCourseAccessMapper::entityToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessThroughPackage(Integer userId, Integer courseId) {
        // First check if the user has any package-based access
        List<UserCourseAccess> packageAccesses = userCourseAccessRepository.findActiveAccessesByUser(userId, LocalDateTime.now());
        
        return packageAccesses.stream()
            .anyMatch(access -> access.getPackageEntity() != null && 
                       access.getCourse().getId() == courseId &&
                       access.getAccessType() == AccessType.PACKAGE_ACCESS);
    }
}
