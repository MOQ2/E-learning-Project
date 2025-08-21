package com.example.e_learning_system.Controller;




import com.example.e_learning_system.Dto.ApiResponse;
import com.example.e_learning_system.Dto.ModuleDtos.CreateModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.DetailedModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.UpdateModuleDto;
import com.example.e_learning_system.Interfaces.ModuleService;
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
    public ResponseEntity<ApiResponse<Void>> createModule(@Valid @RequestBody CreateModuleDto createModuleDto) {
        moduleService.createModule(createModuleDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Module created successfully", null));
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
}