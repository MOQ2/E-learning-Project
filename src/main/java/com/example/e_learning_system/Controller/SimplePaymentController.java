package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Config.SimplePaymentStatus;
import com.example.e_learning_system.Config.SimplePaymentType;
import com.example.e_learning_system.Dto.PaymentDtos.SimplePaymentRequestDTO;
import com.example.e_learning_system.Dto.PaymentDtos.SimplePaymentResponseDTO;
import com.example.e_learning_system.Service.Interfaces.SimplePaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class SimplePaymentController {

    @Autowired
    private SimplePaymentService simplePaymentService;

    @PostMapping
    public ResponseEntity<SimplePaymentResponseDTO> createPayment(@Valid @RequestBody SimplePaymentRequestDTO requestDTO) {
        SimplePaymentResponseDTO responseDTO = simplePaymentService.createPayment(requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PostMapping("/{paymentId}/process")
    public ResponseEntity<SimplePaymentResponseDTO> processPayment(@PathVariable Integer paymentId,
                                                                  @RequestParam String stripePaymentIntentId) {
        SimplePaymentResponseDTO responseDTO = simplePaymentService.processPayment(paymentId, stripePaymentIntentId);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<SimplePaymentResponseDTO> getPaymentById(@PathVariable Integer paymentId) {
        SimplePaymentResponseDTO responseDTO = simplePaymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SimplePaymentResponseDTO>> getPaymentsByUserId(@PathVariable Integer userId) {
        List<SimplePaymentResponseDTO> payments = simplePaymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SimplePaymentResponseDTO>> getPaymentsByStatus(@PathVariable SimplePaymentStatus status) {
        List<SimplePaymentResponseDTO> payments = simplePaymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/type/{paymentType}")
    public ResponseEntity<List<SimplePaymentResponseDTO>> getPaymentsByType(@PathVariable SimplePaymentType paymentType) {
        List<SimplePaymentResponseDTO> payments = simplePaymentService.getPaymentsByType(paymentType);
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<SimplePaymentResponseDTO> updatePaymentStatus(@PathVariable Integer paymentId,
                                                                       @RequestParam SimplePaymentStatus status) {
        SimplePaymentResponseDTO responseDTO = simplePaymentService.updatePaymentStatus(paymentId, status);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<SimplePaymentResponseDTO>> getPaymentsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<SimplePaymentResponseDTO> payments = simplePaymentService.getPaymentsBetweenDates(startDate, endDate);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        BigDecimal totalRevenue = simplePaymentService.getTotalRevenueBetweenDates(startDate, endDate);
        return ResponseEntity.ok(totalRevenue);
    }

    @GetMapping("/course/{courseId}/successful")
    public ResponseEntity<List<SimplePaymentResponseDTO>> getSuccessfulPaymentsByCourse(@PathVariable Integer courseId) {
        List<SimplePaymentResponseDTO> payments = simplePaymentService.getSuccessfulPaymentsByCourse(courseId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/package/{packageId}/successful")
    public ResponseEntity<List<SimplePaymentResponseDTO>> getSuccessfulPaymentsByPackage(@PathVariable Integer packageId) {
        List<SimplePaymentResponseDTO> payments = simplePaymentService.getSuccessfulPaymentsByPackage(packageId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/user/{userId}/course/{courseId}/check")
    public ResponseEntity<Boolean> hasUserPaidForCourse(@PathVariable Integer userId, @PathVariable Integer courseId) {
        boolean hasPaid = simplePaymentService.hasUserPaidForCourse(userId, courseId);
        return ResponseEntity.ok(hasPaid);
    }

    @GetMapping("/user/{userId}/package/{packageId}/check")
    public ResponseEntity<Boolean> hasUserPaidForPackage(@PathVariable Integer userId, @PathVariable Integer packageId) {
        boolean hasPaid = simplePaymentService.hasUserPaidForPackage(userId, packageId);
        return ResponseEntity.ok(hasPaid);
    }

    @GetMapping("/stripe/{stripePaymentIntentId}")
    public ResponseEntity<SimplePaymentResponseDTO> findPaymentByStripeId(@PathVariable String stripePaymentIntentId) {
        SimplePaymentResponseDTO responseDTO = simplePaymentService.findPaymentByStripeId(stripePaymentIntentId);
        return ResponseEntity.ok(responseDTO);
    }
}
