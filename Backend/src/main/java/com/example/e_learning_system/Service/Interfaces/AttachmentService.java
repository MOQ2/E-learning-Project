package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Dto.AttachmentDtos.AttachmentDto;
import com.example.e_learning_system.Dto.AttachmentDtos.CreateAttachmentDto;
import com.example.e_learning_system.Dto.AttachmentDtos.UpdateAttachmentDto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface AttachmentService {
    int createAttachment(CreateAttachmentDto createAttachmentDto, int uploadedByUserId);
    AttachmentDto getAttachmentById(Integer id);
    void deleteAttachment(Integer id);
    void updateAttachment(UpdateAttachmentDto updateAttachmentDto, int id, int uploadedByUserId);
    ResponseEntity<Resource> downloadAttachment(Integer id);
    byte[] getAttachmentFileData(Integer id);
}