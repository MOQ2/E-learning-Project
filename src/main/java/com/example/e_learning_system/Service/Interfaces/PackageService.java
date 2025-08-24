package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Dto.PackageRequestDTO;
import com.example.e_learning_system.Dto.PackageResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PackageService {
    
    PackageResponseDTO createPackage(PackageRequestDTO requestDTO);
    
    PackageResponseDTO updatePackage(Integer packageId, PackageRequestDTO requestDTO);
    
    void deletePackage(Integer packageId);
    
    PackageResponseDTO getPackageById(Integer packageId);
    
    List<PackageResponseDTO> getAllActivePackages();
    
    List<PackageResponseDTO> searchPackages(String name, BigDecimal minPrice, BigDecimal maxPrice);
    
    void addCourseToPackage(Integer packageId, Integer courseId);
    
    void removeCourseFromPackage(Integer packageId, Integer courseId);
    
    List<PackageResponseDTO> getPackagesByCourseId(Integer courseId);
    
    void activatePackage(Integer packageId);
    
    void deactivatePackage(Integer packageId);
}
