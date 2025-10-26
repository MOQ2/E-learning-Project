package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.PackageDtos.PackageRequestDTO;
import com.example.e_learning_system.Dto.PackageDtos.PackageResponseDTO;
import com.example.e_learning_system.Service.Interfaces.PackageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/packages")
@CrossOrigin(origins = "*")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @PostMapping
    public ResponseEntity<PackageResponseDTO> createPackage(@Valid @RequestBody PackageRequestDTO requestDTO) {
        PackageResponseDTO responseDTO = packageService.createPackage(requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{packageId}")
    public ResponseEntity<PackageResponseDTO> updatePackage(@PathVariable Integer packageId,
                                                           @Valid @RequestBody PackageRequestDTO requestDTO) {
        PackageResponseDTO responseDTO = packageService.updatePackage(packageId, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{packageId}")
    public ResponseEntity<Void> deletePackage(@PathVariable Integer packageId) {
        packageService.deletePackage(packageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{packageId}")
    public ResponseEntity<PackageResponseDTO> getPackageById(@PathVariable Integer packageId) {
        PackageResponseDTO responseDTO = packageService.getPackageById(packageId);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<PackageResponseDTO>> getAllActivePackages() {
        List<PackageResponseDTO> packages = packageService.getAllActivePackages();
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PackageResponseDTO>> searchPackages(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<PackageResponseDTO> packages = packageService.searchPackages(name, minPrice, maxPrice);
        return ResponseEntity.ok(packages);
    }

    @PostMapping("/{packageId}/courses/{courseId}")
    public ResponseEntity<Void> addCourseToPackage(@PathVariable Integer packageId,
                                                  @PathVariable Integer courseId) {
        packageService.addCourseToPackage(packageId, courseId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{packageId}/courses/{courseId}")
    public ResponseEntity<Void> removeCourseFromPackage(@PathVariable Integer packageId,
                                                       @PathVariable Integer courseId) {
        packageService.removeCourseFromPackage(packageId, courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<PackageResponseDTO>> getPackagesByCourseId(@PathVariable Integer courseId) {
        List<PackageResponseDTO> packages = packageService.getPackagesByCourseId(courseId);
        return ResponseEntity.ok(packages);
    }

    @PutMapping("/{packageId}/activate")
    public ResponseEntity<Void> activatePackage(@PathVariable Integer packageId) {
        packageService.activatePackage(packageId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{packageId}/deactivate")
    public ResponseEntity<Void> deactivatePackage(@PathVariable Integer packageId) {
        packageService.deactivatePackage(packageId);
        return ResponseEntity.ok().build();
    }
}
