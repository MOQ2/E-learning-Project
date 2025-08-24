package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.PromotionCodeRequestDTO;
import com.example.e_learning_system.Dto.PromotionCodeResponseDTO;
import com.example.e_learning_system.Entities.PromotionCode;
import com.example.e_learning_system.Repository.PromotionCodeRepository;
import com.example.e_learning_system.Mapper.PromotionCodeMapper;
import com.example.e_learning_system.Service.Interfaces.PromotionCodeService;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionCodeServiceImpl implements PromotionCodeService {

    @Autowired
    private PromotionCodeRepository promotionCodeRepository;

    @Autowired
    private PromotionCodeMapper promotionCodeMapper;

    @Override
    @Transactional
    public PromotionCodeResponseDTO createPromotionCode(PromotionCodeRequestDTO requestDTO) {
        // Check if promotion code already exists
        if (promotionCodeRepository.findByCode(requestDTO.getCode()).isPresent()) {
            throw new IllegalArgumentException("Promotion code already exists: " + requestDTO.getCode());
        }

        PromotionCode promotionCode = promotionCodeMapper.requestDtoToEntity(requestDTO);
        promotionCode.setCreatedAt(LocalDateTime.now());
        
        PromotionCode savedPromotionCode = promotionCodeRepository.save(promotionCode);
        return promotionCodeMapper.entityToResponseDto(savedPromotionCode);
    }

    @Override
    @Transactional
    public PromotionCodeResponseDTO updatePromotionCode(Integer promotionCodeId, PromotionCodeRequestDTO requestDTO) {
        PromotionCode existingPromotionCode = promotionCodeRepository.findById(promotionCodeId)
            .orElseThrow(() -> ResourceNotFound.promotionCodeNotFound(promotionCodeId.toString()));

        // Update fields
        existingPromotionCode.setCode(requestDTO.getCode());
        existingPromotionCode.setDiscountPercentage(requestDTO.getDiscountPercentage());
        existingPromotionCode.setMaxUses(requestDTO.getMaxUses());
        existingPromotionCode.setValidFrom(requestDTO.getValidFrom());
        existingPromotionCode.setValidUntil(requestDTO.getValidUntil());
        existingPromotionCode.setIsActive(requestDTO.getIsActive());
        existingPromotionCode.setApplicableToCourses(requestDTO.getApplicableToCourses());
        existingPromotionCode.setApplicableToPackages(requestDTO.getApplicableToPackages());

        PromotionCode updatedPromotionCode = promotionCodeRepository.save(existingPromotionCode);
        return promotionCodeMapper.entityToResponseDto(updatedPromotionCode);
    }

    @Override
    @Transactional
    public void deletePromotionCode(Integer promotionCodeId) {
        PromotionCode promotionCode = promotionCodeRepository.findById(promotionCodeId)
            .orElseThrow(() -> ResourceNotFound.promotionCodeNotFound(promotionCodeId.toString()));
        
        promotionCodeRepository.delete(promotionCode);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionCodeResponseDTO getPromotionCodeById(Integer promotionCodeId) {
        PromotionCode promotionCode = promotionCodeRepository.findById(promotionCodeId)
            .orElseThrow(() -> ResourceNotFound.promotionCodeNotFound(promotionCodeId.toString()));
        
        return promotionCodeMapper.entityToResponseDto(promotionCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionCodeResponseDTO> getAllActivePromotionCodes() {
        LocalDateTime now = LocalDateTime.now();
        List<PromotionCode> promotionCodes = promotionCodeRepository.findActivePromotionCodes(now);
        return promotionCodes.stream()
            .map(promotionCodeMapper::entityToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionCodeResponseDTO validatePromotionCode(String code, boolean forCourse, boolean forPackage) {
        LocalDateTime now = LocalDateTime.now();
        PromotionCode promotionCode = promotionCodeRepository.findValidPromotionCode(code, now)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired promotion code: " + code));
        
        // Check if it has reached maximum uses
        if (promotionCode.getCurrentUses() >= promotionCode.getMaxUses()) {
            throw new IllegalArgumentException("Promotion code has reached maximum uses");
        }
        
        // Check if it applies to the requested type
        if (forCourse && !promotionCode.getApplicableToCourses()) {
            throw new IllegalArgumentException("Promotion code is not valid for courses");
        }
        
        if (forPackage && !promotionCode.getApplicableToPackages()) {
            throw new IllegalArgumentException("Promotion code is not valid for packages");
        }
        
        return promotionCodeMapper.entityToResponseDto(promotionCode);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(String promotionCode, BigDecimal originalAmount) {
        LocalDateTime now = LocalDateTime.now();
        PromotionCode promoCode = promotionCodeRepository.findValidPromotionCode(promotionCode, now)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired promotion code: " + promotionCode));
        
        BigDecimal discountPercentage = promoCode.getDiscountPercentage();
        return originalAmount.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
    }

    @Override
    @Transactional
    public void incrementUsage(String promotionCode) {
        PromotionCode promoCode = promotionCodeRepository.findByCode(promotionCode)
            .orElseThrow(() -> ResourceNotFound.promotionCodeNotFound(promotionCode));

        promoCode.setCurrentUses(promoCode.getCurrentUses() + 1);
        promotionCodeRepository.save(promoCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionCodeResponseDTO> searchPromotionCodes(String code, Boolean isActive) {
        List<PromotionCode> promotionCodes = promotionCodeRepository.findByCodeContainingIgnoreCaseAndIsActive(code, isActive);
        return promotionCodes.stream()
            .map(promotionCodeMapper::entityToResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void activatePromotionCode(Integer promotionCodeId) {
        PromotionCode promotionCode = promotionCodeRepository.findById(promotionCodeId)
            .orElseThrow(() -> ResourceNotFound.promotionCodeNotFound(promotionCodeId.toString()));
        
        promotionCode.setIsActive(true);
        promotionCodeRepository.save(promotionCode);
    }

    @Override
    @Transactional
    public void deactivatePromotionCode(Integer promotionCodeId) {
        PromotionCode promotionCode = promotionCodeRepository.findById(promotionCodeId)
            .orElseThrow(() -> ResourceNotFound.promotionCodeNotFound(promotionCodeId.toString()));
        
        promotionCode.setIsActive(false);
        promotionCodeRepository.save(promotionCode);
    }

    @Override
    @Transactional
    public void expireOldPromotionCodes() {
        LocalDateTime now = LocalDateTime.now();
        List<PromotionCode> expiredCodes = promotionCodeRepository.findExpiredPromotionCodes(now);
        
        for (PromotionCode promotionCode : expiredCodes) {
            promotionCode.setIsActive(false);
        }
        
        promotionCodeRepository.saveAll(expiredCodes);
    }
}
