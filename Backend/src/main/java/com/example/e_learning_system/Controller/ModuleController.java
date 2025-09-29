package com.example.e_learning_system.Controller;




import com.example.e_learning_system.Dto.ApiResponse;
import com.example.e_learning_system.Dto.ModuleDtos.CreateModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.DetailedModuleDto;
import com.example.e_learning_system.Dto.VideoDtos.VideoDto;
import com.example.e_learning_system.Dto.ModuleDtos.UpdateModuleDto;
import com.example.e_learning_system.Service.Interfaces.ModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @PostMapping
    public ResponseEntity<ApiResponse<Integer>> createModule(@Valid @RequestBody CreateModuleDto createModuleDto) {
        if (createModuleDto.getId() != null) {
            UpdateModuleDto updateDto = UpdateModuleDto.builder()
                    .moduleName(createModuleDto.getModuleName())
                    .moduleDescription(createModuleDto.getModuleDescription())
                    .isActive(createModuleDto.isActive())
                    .estimatedDuration(createModuleDto.getEstimatedDuration())
                    .build();
            moduleService.updateModule(updateDto, createModuleDto.getId());
            return ResponseEntity.ok(ApiResponse.success("Module updated successfully", createModuleDto.getId()));
        } else {
            int moduleId = moduleService.createModule(createModuleDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Module created successfully", moduleId));
        }
    }

    @PutMapping("/{moduleId}")
    public ResponseEntity<ApiResponse<Void>> updateModule(
            @PathVariable int moduleId,
            @Valid @RequestBody UpdateModuleDto updateModuleDto) {
        moduleService.updateModule(updateModuleDto, moduleId);
        return ResponseEntity.ok(ApiResponse.success("Module updated successfully", null));
    }

    @DeleteMapping("/{moduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteModule(@PathVariable int moduleId) {
        moduleService.deleteModule(moduleId);
        return ResponseEntity.ok(ApiResponse.success("Module deleted successfully", null));
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<ApiResponse<DetailedModuleDto>> getModule(@PathVariable int moduleId) {
        DetailedModuleDto module = moduleService.getModule(moduleId);
        if (module != null) {
            return ResponseEntity.ok(ApiResponse.success("Module retrieved successfully", module));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Module not found"));
    }

    /**
     * Get lessons (videos) within a module - simplified list endpoint
     */
    @GetMapping("/{moduleId}/lessons")
    public ResponseEntity<ApiResponse<java.util.List<VideoDto>>> getModuleLessons(@PathVariable int moduleId) {
        DetailedModuleDto module = moduleService.getModule(moduleId);
        if (module != null) {
            java.util.List<VideoDto> videos = module.getVideos();
            return ResponseEntity.ok(ApiResponse.success("Module lessons retrieved successfully", videos));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Module not found"));
    }

    @PostMapping("/{moduleId}/videos/{videoId}")
    public ResponseEntity<ApiResponse<Void>> addVideoToModule(
            @PathVariable int moduleId,
            @PathVariable int videoId,
            @RequestParam int order) {
        moduleService.addVideoToModule(moduleId, videoId, order);
        return ResponseEntity.ok(ApiResponse.success("Video added to module successfully", null));
    }

    @DeleteMapping("/{moduleId}/videos/{videoId}")
    public ResponseEntity<ApiResponse<Void>> removeVideoFromModule(
            @PathVariable int moduleId,
            @PathVariable int videoId) {
        moduleService.removeVideoFromModule(moduleId, videoId);
        return ResponseEntity.ok(ApiResponse.success("Video removed from module successfully", null));
    }

    @PutMapping("/{moduleId}/videos/{videoId}/order/{newOrder}")
    public ResponseEntity<ApiResponse<Void>> updateVideoOrder(
            @PathVariable int moduleId,
            @PathVariable int videoId,
            @PathVariable int newOrder) {
        moduleService.updateVideoOrderInModule(moduleId, videoId, newOrder);
        return ResponseEntity.ok(ApiResponse.success("Video order updated successfully", null));
    }

    @PutMapping("/{moduleId}/videos/order")
    public ResponseEntity<ApiResponse<Void>> updateVideoOrders(
            @PathVariable int moduleId,
            @RequestBody java.util.List<com.example.e_learning_system.Dto.OrderDtos.IdOrderDto> orders) {
        moduleService.updateVideoOrdersInModule(moduleId, orders);
        return ResponseEntity.ok(ApiResponse.success("Video orders updated successfully", null));
    }

    @PostMapping("/{moduleId}/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<Void>> addLessonToModule(
            @PathVariable int moduleId,
            @PathVariable int lessonId,
            @RequestParam int order) {
        moduleService.addVideoToModule(moduleId, lessonId, order);
        return ResponseEntity.ok(ApiResponse.success("Lesson added to module successfully", null));
    }

    @DeleteMapping("/{moduleId}/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<Void>> removeLessonFromModule(
            @PathVariable int moduleId,
            @PathVariable int lessonId) {
        moduleService.removeVideoFromModule(moduleId, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Lesson removed from module successfully", null));
    }
}