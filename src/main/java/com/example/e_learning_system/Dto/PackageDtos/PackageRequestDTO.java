package com.example.e_learning_system.Dto.PackageDtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageRequestDTO {
    
    @NotBlank(message = "Package name is required")
    @Size(max = 255, message = "Package name cannot exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", message = "Discount percentage must be 0 or greater")
    private BigDecimal discountPercentage;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @NotNull(message = "Course IDs are required")
    @Size(min = 1, message = "At least one course must be selected")
    private List<Integer> courseIds;
}
