package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.PromotionDtos.PromotionCodeRequestDTO;
import com.example.e_learning_system.Dto.PromotionDtos.PromotionCodeResponseDTO;
import com.example.e_learning_system.Service.Interfaces.PromotionCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/promotion-codes")
@CrossOrigin(origins = "*")
public class PromotionCodeController {

    @Autowired
    private PromotionCodeService promotionCodeService;

    @PostMapping
    public ResponseEntity<PromotionCodeResponseDTO> createPromotionCode(@Valid @RequestBody PromotionCodeRequestDTO requestDTO) {
        PromotionCodeResponseDTO responseDTO = promotionCodeService.createPromotionCode(requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{promotionCodeId}")
    public ResponseEntity<PromotionCodeResponseDTO> updatePromotionCode(@PathVariable Integer promotionCodeId,
                                                                       @Valid @RequestBody PromotionCodeRequestDTO requestDTO) {
        PromotionCodeResponseDTO responseDTO = promotionCodeService.updatePromotionCode(promotionCodeId, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{promotionCodeId}")
    public ResponseEntity<Void> deletePromotionCode(@PathVariable Integer promotionCodeId) {
        promotionCodeService.deletePromotionCode(promotionCodeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{promotionCodeId}")
    public ResponseEntity<PromotionCodeResponseDTO> getPromotionCodeById(@PathVariable Integer promotionCodeId) {
        PromotionCodeResponseDTO responseDTO = promotionCodeService.getPromotionCodeById(promotionCodeId);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<PromotionCodeResponseDTO>> getAllActivePromotionCodes() {
        List<PromotionCodeResponseDTO> promotionCodes = promotionCodeService.getAllActivePromotionCodes();
        return ResponseEntity.ok(promotionCodes);
    }

    @PostMapping("/validate")
    public ResponseEntity<PromotionCodeResponseDTO> validatePromotionCode(
            @RequestParam String code,
            @RequestParam(defaultValue = "false") boolean forCourse,
            @RequestParam(defaultValue = "false") boolean forPackage) {
        PromotionCodeResponseDTO responseDTO = promotionCodeService.validatePromotionCode(code, forCourse, forPackage);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/calculate-discount")
    public ResponseEntity<BigDecimal> calculateDiscount(
            @RequestParam String promotionCode,
            @RequestParam BigDecimal originalAmount) {
        BigDecimal discountAmount = promotionCodeService.calculateDiscount(promotionCode, originalAmount);
        return ResponseEntity.ok(discountAmount);
    }

    @PostMapping("/{promotionCode}/use")
    public ResponseEntity<Void> usePromotionCode(@PathVariable String promotionCode) {
        promotionCodeService.incrementUsage(promotionCode);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<PromotionCodeResponseDTO>> searchPromotionCodes(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Boolean isActive) {
        List<PromotionCodeResponseDTO> promotionCodes = promotionCodeService.searchPromotionCodes(code, isActive);
        return ResponseEntity.ok(promotionCodes);
    }

    @PutMapping("/{promotionCodeId}/activate")
    public ResponseEntity<Void> activatePromotionCode(@PathVariable Integer promotionCodeId) {
        promotionCodeService.activatePromotionCode(promotionCodeId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{promotionCodeId}/deactivate")
    public ResponseEntity<Void> deactivatePromotionCode(@PathVariable Integer promotionCodeId) {
        promotionCodeService.deactivatePromotionCode(promotionCodeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/expire-old")
    public ResponseEntity<Void> expireOldPromotionCodes() {
        promotionCodeService.expireOldPromotionCodes();
        return ResponseEntity.ok().build();
    }
}
