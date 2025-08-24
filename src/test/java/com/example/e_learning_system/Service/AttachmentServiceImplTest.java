package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.AttachmentDtos.AttachmentDto;
import com.example.e_learning_system.Dto.AttachmentDtos.CreateAttachmentDto;
import com.example.e_learning_system.Dto.AttachmentDtos.UpdateAttachmentDto;
import com.example.e_learning_system.Entities.Attachment;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Mapper.AttachmentMapper;
import com.example.e_learning_system.Repository.AttachmentRepository;
import com.example.e_learning_system.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    @Mock
    private Attachment attachment;

    @Mock
    private UserEntity user;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private UpdateAttachmentDto updateAttachmentDto;

    private CreateAttachmentDto createAttachmentDto;
    private AttachmentDto attachmentDto;

    @BeforeEach
    void setUp() {
        createAttachmentDto = new CreateAttachmentDto();
        createAttachmentDto.setFile(multipartFile);
        createAttachmentDto.setTitle("Test Attachment");

        attachmentDto = new AttachmentDto();
        attachmentDto.setId(1);
        attachmentDto.setTitle("Test Attachment");
    }

    @Test
    void createAttachment_ShouldCreateAttachment_WhenValidDto() throws IOException {
        // Arrange
        int uploadedByUserId = 1;
        byte[] fileData = "test file content".getBytes();
        
        when(userRepository.findById(uploadedByUserId)).thenReturn(Optional.of(user));
        when(multipartFile.getBytes()).thenReturn(fileData);
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getSize()).thenReturn((long) fileData.length);
        when(multipartFile.isEmpty()).thenReturn(false);
        
        try (MockedStatic<AttachmentMapper> mockedMapper = mockStatic(AttachmentMapper.class)) {
            mockedMapper.when(() -> AttachmentMapper.toEntity(createAttachmentDto))
                       .thenReturn(attachment);
            mockedMapper.when(() -> AttachmentMapper.setFileMetadata(
                    attachment, "test.txt", "text/plain", (long) fileData.length))
                       .thenAnswer(invocation -> null);

            // Act
            attachmentService.createAttachment(createAttachmentDto, uploadedByUserId);

            // Assert
            verify(userRepository).findById(uploadedByUserId);
            verify(attachment).setUploadedBy(user);
            verify(attachment).setFileData(fileData);
            verify(attachmentRepository).save(attachment);
            mockedMapper.verify(() -> AttachmentMapper.toEntity(createAttachmentDto));
            mockedMapper.verify(() -> AttachmentMapper.setFileMetadata(
                    attachment, "test.txt", "text/plain", (long) fileData.length));
        }
    }

    @Test
    void createAttachment_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        int uploadedByUserId = 1;
        
        when(userRepository.findById(uploadedByUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            attachmentService.createAttachment(createAttachmentDto, uploadedByUserId));
        verify(userRepository).findById(uploadedByUserId);
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void createAttachment_ShouldThrowException_WhenIOException() throws IOException {
        // Arrange
        int uploadedByUserId = 1;
        
        when(userRepository.findById(uploadedByUserId)).thenReturn(Optional.of(user));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenThrow(new IOException("File read error"));
        
        try (MockedStatic<AttachmentMapper> mockedMapper = mockStatic(AttachmentMapper.class)) {
            mockedMapper.when(() -> AttachmentMapper.toEntity(createAttachmentDto))
                       .thenReturn(attachment);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> 
                attachmentService.createAttachment(createAttachmentDto, uploadedByUserId));
            verify(userRepository).findById(uploadedByUserId);
            verify(attachmentRepository, never()).save(any());
        }
    }

    @Test
    void createAttachment_ShouldSkipFileData_WhenFileIsEmpty() {
        // Arrange
        int uploadedByUserId = 1;
        
        when(userRepository.findById(uploadedByUserId)).thenReturn(Optional.of(user));
        when(multipartFile.isEmpty()).thenReturn(true);
        
        try (MockedStatic<AttachmentMapper> mockedMapper = mockStatic(AttachmentMapper.class)) {
            mockedMapper.when(() -> AttachmentMapper.toEntity(createAttachmentDto))
                       .thenReturn(attachment);

            // Act
            attachmentService.createAttachment(createAttachmentDto, uploadedByUserId);

            // Assert
            verify(userRepository).findById(uploadedByUserId);
            verify(attachment).setUploadedBy(user);
            verify(attachment, never()).setFileData(any());
            verify(attachmentRepository).save(attachment);
            mockedMapper.verify(() -> AttachmentMapper.toEntity(createAttachmentDto));
        }
    }

    @Test
    void getAttachmentById_ShouldReturnAttachmentDto_WhenAttachmentExists() {
        // Arrange
        Integer attachmentId = 1;
        
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        
        try (MockedStatic<AttachmentMapper> mockedMapper = mockStatic(AttachmentMapper.class)) {
            mockedMapper.when(() -> AttachmentMapper.fromEntity(attachment))
                       .thenReturn(attachmentDto);

            // Act
            AttachmentDto result = attachmentService.getAttachmentById(attachmentId);

            // Assert
            assertNotNull(result);
            assertEquals(attachmentDto, result);
            verify(attachmentRepository).findById(attachmentId);
            mockedMapper.verify(() -> AttachmentMapper.fromEntity(attachment));
        }
    }

    @Test
    void getAttachmentById_ShouldThrowException_WhenAttachmentNotExists() {
        // Arrange
        Integer attachmentId = 1;
        
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> attachmentService.getAttachmentById(attachmentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Attachment not found with id: 1");
        
        verify(attachmentRepository).findById(attachmentId);
    }

    @Test
    void deleteAttachment_ShouldDeleteAttachment_WhenAttachmentExists() {
        // Arrange
        Integer attachmentId = 1;
        
        when(attachmentRepository.existsById(attachmentId)).thenReturn(true);

        // Act
        attachmentService.deleteAttachment(attachmentId);

        // Assert
        verify(attachmentRepository).existsById(attachmentId);
        verify(attachmentRepository).deleteById(attachmentId);
    }

    @Test
    void deleteAttachment_ShouldThrowException_WhenAttachmentNotExists() {
        // Arrange
        Integer attachmentId = 1;
        
        when(attachmentRepository.existsById(attachmentId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> attachmentService.deleteAttachment(attachmentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Attachment not found with id: 1");
        
        verify(attachmentRepository).existsById(attachmentId);
        verify(attachmentRepository, never()).deleteById(any());
    }

    @Test
    void updateAttachment_ShouldUpdateAttachment_WhenAttachmentExists() throws IOException {
        // Arrange
        int attachmentId = 1;
        int uploadedByUserId = 1;
        byte[] fileData = "updated file content".getBytes();
        
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(updateAttachmentDto.getFile()).thenReturn(multipartFile);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn(fileData);
        when(multipartFile.getOriginalFilename()).thenReturn("updated.txt");
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getSize()).thenReturn((long) fileData.length);
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);
        
        try (MockedStatic<AttachmentMapper> mockedMapper = mockStatic(AttachmentMapper.class)) {
            // Act
            attachmentService.updateAttachment(updateAttachmentDto, attachmentId, uploadedByUserId);

            // Assert
            verify(attachmentRepository).findById(attachmentId);
            verify(attachmentRepository).save(any(Attachment.class));
            mockedMapper.verify(() -> AttachmentMapper.updateEntity(eq(updateAttachmentDto), any(Attachment.class)));
            mockedMapper.verify(() -> AttachmentMapper.setFileMetadata(
                    any(Attachment.class), eq("updated.txt"), eq("text/plain"), eq((long) fileData.length)));
        }
    }

    @Test
    void updateAttachment_ShouldThrowException_WhenAttachmentNotExists() {
        // Arrange
        int attachmentId = 1;
        int uploadedByUserId = 1;
        
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            attachmentService.updateAttachment(updateAttachmentDto, attachmentId, uploadedByUserId));
        verify(attachmentRepository).findById(attachmentId);
        verify(userRepository, never()).findById(anyInt());
    }

    @Test
    void downloadAttachment_ShouldReturnResponseEntity_WhenAttachmentExists() {
        // Arrange
        Integer attachmentId = 1;
        byte[] fileData = "file content".getBytes();
        String fileName = "test.txt";
        String contentType = "text/plain";
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileName", fileName);
        metadata.put("contentType", contentType);
        
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(attachment.getFileData()).thenReturn(fileData);
        when(attachment.getMetadata()).thenReturn(metadata);

        // Act
        ResponseEntity<Resource> result = attachmentService.downloadAttachment(attachmentId);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        verify(attachmentRepository).findById(attachmentId);
        verify(attachment, times(2)).getFileData(); // Called twice in download method
        verify(attachment, atLeastOnce()).getMetadata();
    }

    @Test
    void downloadAttachment_ShouldThrowException_WhenAttachmentNotExists() {
        // Arrange
        Integer attachmentId = 1;
        
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> attachmentService.downloadAttachment(attachmentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Attachment not found with id: 1");
        
        verify(attachmentRepository).findById(attachmentId);
    }

    @Test
    void getAttachmentFileData_ShouldReturnFileData_WhenAttachmentExists() {
        // Arrange
        Integer attachmentId = 1;
        byte[] expectedFileData = "file content".getBytes();
        
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(attachment.getFileData()).thenReturn(expectedFileData);

        // Act
        byte[] result = attachmentService.getAttachmentFileData(attachmentId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedFileData, result);
        verify(attachmentRepository).findById(attachmentId);
        verify(attachment).getFileData();
    }

    @Test
    void getAttachmentFileData_ShouldThrowException_WhenAttachmentNotExists() {
        // Arrange
        Integer attachmentId = 1;
        
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            attachmentService.getAttachmentFileData(attachmentId));
        verify(attachmentRepository).findById(attachmentId);
    }
}
