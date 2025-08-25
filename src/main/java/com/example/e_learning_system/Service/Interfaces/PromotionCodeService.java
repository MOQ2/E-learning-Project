package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Dto.PromotionDtos.PromotionCodeRequestDTO;
import com.example.e_learning_system.Dto.PromotionDtos.PromotionCodeResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionCodeService {
    
    PromotionCodeResponseDTO createPromotionCode(PromotionCodeRequestDTO requestDTO);
    
    PromotionCodeResponseDTO updatePromotionCode(Integer promotionCodeId, PromotionCodeRequestDTO requestDTO);
    
    void deletePromotionCode(Integer promotionCodeId);
    
    PromotionCodeResponseDTO getPromotionCodeById(Integer promotionCodeId);
    
    List<PromotionCodeResponseDTO> getAllActivePromotionCodes();
    
    PromotionCodeResponseDTO validatePromotionCode(String code, boolean forCourse, boolean forPackage);
    
    BigDecimal calculateDiscount(String promotionCode, BigDecimal originalAmount);
    
    void incrementUsage(String promotionCode);
    
    List<PromotionCodeResponseDTO> searchPromotionCodes(String code, Boolean isActive);
    
    void activatePromotionCode(Integer promotionCodeId);
    
    void deactivatePromotionCode(Integer promotionCodeId);
    
    void expireOldPromotionCodes();
}
