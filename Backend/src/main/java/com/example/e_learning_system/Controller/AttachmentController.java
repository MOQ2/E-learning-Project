package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.ApiResponse;
import com.example.e_learning_system.Dto.AttachmentDtos.AttachmentDto;
import com.example.e_learning_system.Dto.AttachmentDtos.CreateAttachmentDto;
import com.example.e_learning_system.Dto.AttachmentDtos.UpdateAttachmentDto;
import com.example.e_learning_system.Service.Interfaces.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    //TODO use securty context to add uploaded by
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Integer>> createAttachment(
            @RequestParam("name") String title,
            @RequestParam("file") MultipartFile file
            ) {
        System.out.println("Received file:");
        System.out.println("Name: " + file.getOriginalFilename());
        System.out.println("Size: " + file.getSize() + " bytes");
        System.out.println("Content Type: " + file.getContentType());
        System.out.println("Metadata:");
        System.out.println(" - Parameter name: " + file.getName());
        System.out.println(" - Is empty: " + file.isEmpty());
        CreateAttachmentDto createAttachmentDto = new CreateAttachmentDto();
        createAttachmentDto.setTitle(title);
        createAttachmentDto.setFile(file);

        int attachmentId = attachmentService.createAttachment(createAttachmentDto, 1);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attachment created successfully", attachmentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttachmentDto>> getAttachment(@PathVariable Integer id) {
        AttachmentDto attachment = attachmentService.getAttachmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Attachment retrieved successfully", attachment));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateAttachment(
            @PathVariable Integer id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("uploadedByUserId") int uploadedByUserId) {

        UpdateAttachmentDto updateAttachmentDto = new UpdateAttachmentDto();
        updateAttachmentDto.setTitle(title);
        updateAttachmentDto.setFile(file);

        attachmentService.updateAttachment(updateAttachmentDto, id, uploadedByUserId);
        return ResponseEntity.ok(ApiResponse.success("Attachment updated successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable Integer id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted successfully", null));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Integer id) {
        return attachmentService.downloadAttachment(id);
    }

    @GetMapping("/{id}/data")
    public ResponseEntity<ApiResponse<byte[]>> getAttachmentData(@PathVariable Integer id) {
        byte[] fileData = attachmentService.getAttachmentFileData(id);
        return ResponseEntity.ok(ApiResponse.success("File data retrieved successfully", fileData));
    }
}