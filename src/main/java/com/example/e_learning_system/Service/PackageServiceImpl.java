package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.PackageDtos.PackageRequestDTO;
import com.example.e_learning_system.Dto.PackageDtos.PackageResponseDTO;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.Package;
import com.example.e_learning_system.Entities.PackageCourse;
import com.example.e_learning_system.Mapper.PackageMapper;
import com.example.e_learning_system.Repository.CourseRepository;
import com.example.e_learning_system.Repository.PackageCourseRepository;
import com.example.e_learning_system.Repository.PackageRepository;
import com.example.e_learning_system.Service.Interfaces.PackageService;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;
    private final PackageCourseRepository packageCourseRepository;
    private final CourseRepository courseRepository;
    private final PackageMapper packageMapper;

    @Override
    public PackageResponseDTO createPackage(PackageRequestDTO requestDTO) {
        // Create the package entity
        Package packageEntity = packageMapper.requestDtoToEntity(requestDTO);
        Package savedPackage = packageRepository.save(packageEntity);

        // Add courses to the package
        if (requestDTO.getCourseIds() != null && !requestDTO.getCourseIds().isEmpty()) {
            for (Integer courseId : requestDTO.getCourseIds()) {
                Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId.toString()));
                
                PackageCourse packageCourse = PackageCourse.builder()
                    .packageEntity(savedPackage)
                    .course(course)
                    .build();
                packageCourseRepository.save(packageCourse);
            }
        }

        // Get course count and return response
        int courseCount = packageCourseRepository.countCoursesByPackageId(savedPackage.getId());
        return packageMapper.entityToResponseDtoWithCourseCount(savedPackage, courseCount);
    }

    @Override
    public PackageResponseDTO updatePackage(Integer packageId, PackageRequestDTO requestDTO) {
        Package existingPackage = packageRepository.findById(packageId)
            .orElseThrow(() -> ResourceNotFound.packageNotFound(packageId.longValue()));

        // Update package details
        packageMapper.updateEntityFromRequestDto(existingPackage, requestDTO);
        Package updatedPackage = packageRepository.save(existingPackage);

        // Update package courses if provided
        if (requestDTO.getCourseIds() != null) {
            // Remove existing course associations
            List<PackageCourse> existingPackageCourses = packageCourseRepository.findByPackageEntityId(packageId);
            packageCourseRepository.deleteAll(existingPackageCourses);

            // Add new course associations
            for (Integer courseId : requestDTO.getCourseIds()) {
                Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId.toString()));
                
                PackageCourse packageCourse = PackageCourse.builder()
                    .packageEntity(updatedPackage)
                    .course(course)
                    .build();
                packageCourseRepository.save(packageCourse);
            }
        }

        int courseCount = packageCourseRepository.countCoursesByPackageId(updatedPackage.getId());
        return packageMapper.entityToResponseDtoWithCourseCount(updatedPackage, courseCount);
    }

    @Override
    public void deletePackage(Integer packageId) {
        Package packageEntity = packageRepository.findById(packageId)
            .orElseThrow(() -> ResourceNotFound.packageNotFound(packageId.longValue()));
        
        packageRepository.delete(packageEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public PackageResponseDTO getPackageById(Integer packageId) {
        Package packageEntity = packageRepository.findActivePackageById(packageId)
            .orElseThrow(() -> ResourceNotFound.packageNotFound(packageId.longValue()));
        
        int courseCount = packageCourseRepository.countCoursesByPackageId(packageId);
        return packageMapper.entityToResponseDtoWithCourseCount(packageEntity, courseCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponseDTO> getAllActivePackages() {
        List<Package> packages = packageRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        return packages.stream()
            .map(packageEntity -> {
                int courseCount = packageCourseRepository.countCoursesByPackageId(packageEntity.getId());
                return packageMapper.entityToResponseDtoWithCourseCount(packageEntity, courseCount);
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponseDTO> searchPackages(String name, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Package> packages = packageRepository.findActivePackagesWithFilters(name, minPrice, maxPrice);
        return packages.stream()
            .map(packageEntity -> {
                int courseCount = packageCourseRepository.countCoursesByPackageId(packageEntity.getId());
                return packageMapper.entityToResponseDtoWithCourseCount(packageEntity, courseCount);
            })
            .collect(Collectors.toList());
    }

    @Override
    public void addCourseToPackage(Integer packageId, Integer courseId) {
        Package packageEntity = packageRepository.findById(packageId)
            .orElseThrow(() -> ResourceNotFound.packageNotFound(packageId.longValue()));
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId.toString()));

        // Check if the course is already in the package
        if (packageCourseRepository.existsByPackageEntityIdAndCourseId(packageId, courseId)) {
            throw new IllegalArgumentException("Course is already in this package");
        }

        PackageCourse packageCourse = PackageCourse.builder()
            .packageEntity(packageEntity)
            .course(course)
            .build();
        packageCourseRepository.save(packageCourse);
    }

    @Override
    public void removeCourseFromPackage(Integer packageId, Integer courseId) {
        if (!packageCourseRepository.existsByPackageEntityIdAndCourseId(packageId, courseId)) {
            throw new IllegalArgumentException("Course is not in this package");
        }
        
        packageCourseRepository.deleteByPackageEntityIdAndCourseId(packageId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponseDTO> getPackagesByCourseId(Integer courseId) {
        List<Package> packages = packageCourseRepository.findPackagesByCourseId(courseId);
        return packages.stream()
            .map(packageEntity -> {
                int courseCount = packageCourseRepository.countCoursesByPackageId(packageEntity.getId());
                return packageMapper.entityToResponseDtoWithCourseCount(packageEntity, courseCount);
            })
            .collect(Collectors.toList());
    }

    @Override
    public void activatePackage(Integer packageId) {
        Package packageEntity = packageRepository.findById(packageId)
            .orElseThrow(() -> ResourceNotFound.packageNotFound(packageId.longValue()));
        
        packageEntity.setIsActive(true);
        packageRepository.save(packageEntity);
    }

    @Override
    public void deactivatePackage(Integer packageId) {
        Package packageEntity = packageRepository.findById(packageId)
            .orElseThrow(() -> ResourceNotFound.packageNotFound(packageId.longValue()));
        
        packageEntity.setIsActive(false);
        packageRepository.save(packageEntity);
    }
}
