package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.AttachmentDtos.AttachmentDto;
import com.example.e_learning_system.Dto.AttachmentDtos.CreateAttachmentDto;
import com.example.e_learning_system.Dto.AttachmentDtos.UpdateAttachmentDto;
import com.example.e_learning_system.Entities.Attachment;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Interfaces.AttachmentService;
import com.example.e_learning_system.Mapper.AttachmentMapper;
import com.example.e_learning_system.Repository.AttachmentRepository;
import com.example.e_learning_system.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;

    @Override
    public void createAttachment(CreateAttachmentDto createAttachmentDto, int uploadedByUserId) {
        try {
            // Find the user who is uploading
            UserEntity user = userRepository.findById(uploadedByUserId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + uploadedByUserId));

            // Create attachment entity
            Attachment attachment = AttachmentMapper.toEntity(createAttachmentDto);
            attachment.setUploadedBy(user);

            // Handle file
            MultipartFile file = createAttachmentDto.getFile();
            if (file != null && !file.isEmpty()) {
                attachment.setFileData(file.getBytes());

                // Set file metadata
                AttachmentMapper.setFileMetadata(
                        attachment,
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getSize()
                );
            }

            attachmentRepository.save(attachment);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    @Override
    public AttachmentDto getAttachmentById(Integer id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + id));

        return AttachmentMapper.fromEntity(attachment);
    }

    @Override
    public void deleteAttachment(Integer id) {
        if (!attachmentRepository.existsById(id)) {
            throw new RuntimeException("Attachment not found with id: " + id);
        }
        attachmentRepository.deleteById(id);
    }

    @Override
    public void updateAttachment(UpdateAttachmentDto updateAttachmentDto, int id, int uploadedByUserId) {
        try {
            Attachment attachment = attachmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + id));

            // Update basic fields
            AttachmentMapper.updateEntity(updateAttachmentDto, attachment);

            // Handle file update if provided
            MultipartFile file = updateAttachmentDto.getFile();
            if (file != null && !file.isEmpty()) {
                attachment.setFileData(file.getBytes());

                // Update file metadata
                AttachmentMapper.setFileMetadata(
                        attachment,
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getSize()
                );
            }

            attachmentRepository.save(attachment);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update file: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<Resource> downloadAttachment(Integer id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + id));

        if (attachment.getFileData() == null) {
            throw new RuntimeException("No file data found for attachment with id: " + id);
        }

        ByteArrayResource resource = new ByteArrayResource(attachment.getFileData());

        String fileName = (String) attachment.getMetadata().get("fileName");
        String contentType = (String) attachment.getMetadata().get("contentType");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + (fileName != null ? fileName : "attachment_" + id) + "\"")
                .body(resource);
    }

    @Override
    public byte[] getAttachmentFileData(Integer id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + id));

        return attachment.getFileData();
    }
}