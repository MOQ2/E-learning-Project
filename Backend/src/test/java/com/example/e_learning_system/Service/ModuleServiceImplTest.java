package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.ModuleDtos.CreateModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.DetailedModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.UpdateModuleDto;
import com.example.e_learning_system.Entities.Module;
import com.example.e_learning_system.Entities.ModuleVideos;
import com.example.e_learning_system.Entities.VideoEntity;
import com.example.e_learning_system.Mapper.ModuleMapper;
import com.example.e_learning_system.Repository.ModuleRepository;
import com.example.e_learning_system.Repository.ModuleVideosRepository;
import com.example.e_learning_system.Repository.VideoRepository;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleServiceImplTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private ModuleVideosRepository moduleVideosRepository;

    @InjectMocks
    private ModuleServiceImpl moduleService;

    @Mock
    private Module module;

    private CreateModuleDto createModuleDto;
    private UpdateModuleDto updateModuleDto;
    private VideoEntity video;
    private ModuleVideos moduleVideos;

    @BeforeEach
    void setUp() {
        // Setup test data
        createModuleDto = new CreateModuleDto();
        updateModuleDto = new UpdateModuleDto();
        
        video = new VideoEntity();
        video.setId(1);
        video.setTitle("Test Video");
        
        moduleVideos = new ModuleVideos();
        moduleVideos.setModule(module);
        moduleVideos.setVideo(video);
        moduleVideos.setVideoOrder(1);
    }

    @Test
    void createModule_ShouldSaveModule_WhenValidDto() {
        // Arrange
        try (MockedStatic<ModuleMapper> mockedMapper = mockStatic(ModuleMapper.class)) {
            mockedMapper.when(() -> ModuleMapper.fromCreateModuleDtoToModule(createModuleDto))
                       .thenReturn(module);
            when(moduleRepository.save(module)).thenReturn(module);

            // Act
            moduleService.createModule(createModuleDto);

            // Assert
            verify(moduleRepository).save(module);
            mockedMapper.verify(() -> ModuleMapper.fromCreateModuleDtoToModule(createModuleDto));
        }
    }

    @Test
    void updateModule_ShouldUpdateModule_WhenModuleExists() {
        // Arrange
        int moduleId = 1;
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        try (MockedStatic<ModuleMapper> mockedMapper = mockStatic(ModuleMapper.class)) {
            mockedMapper.when(() -> ModuleMapper.fromUpdateModuleDtoToModule(updateModuleDto, module))
                       .thenAnswer(invocation -> null);
            when(moduleRepository.save(module)).thenReturn(module);

            // Act
            moduleService.updateModule(updateModuleDto, moduleId);

            // Assert
            verify(moduleRepository).findById(moduleId);
            verify(moduleRepository).save(module);
            mockedMapper.verify(() -> ModuleMapper.fromUpdateModuleDtoToModule(updateModuleDto, module));
        }
    }

    @Test
    void updateModule_ShouldDoNothing_WhenModuleNotExists() {
        // Arrange
        int moduleId = 1;
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        // Act
        moduleService.updateModule(updateModuleDto, moduleId);

        // Assert
        verify(moduleRepository).findById(moduleId);
        verify(moduleRepository, never()).save(any());
    }

    @Test
    void deleteModule_ShouldCallDeleteById() {
        // Arrange
        int moduleId = 1;

        // Act
        moduleService.deleteModule(moduleId);

        // Assert
        verify(moduleRepository).deleteById(moduleId);
    }

    @Test
    void getModule_ShouldReturnDetailedModuleDto_WhenModuleExists() {
        // Arrange
        int moduleId = 1;
        DetailedModuleDto expectedDto = new DetailedModuleDto();
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        
        try (MockedStatic<ModuleMapper> mockedMapper = mockStatic(ModuleMapper.class)) {
            mockedMapper.when(() -> ModuleMapper.fromModuleToDetailedModuleDto(module))
                       .thenReturn(expectedDto);

            // Act
            DetailedModuleDto result = moduleService.getModule(moduleId);

            // Assert
            assertNotNull(result);
            assertEquals(expectedDto, result);
            verify(moduleRepository).findById(moduleId);
            mockedMapper.verify(() -> ModuleMapper.fromModuleToDetailedModuleDto(module));
        }
    }

    @Test
    void getModule_ShouldReturnNull_WhenModuleNotExists() {
        // Arrange
        int moduleId = 1;
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        // Act
        DetailedModuleDto result = moduleService.getModule(moduleId);

        // Assert
        assertNull(result);
        verify(moduleRepository).findById(moduleId);
    }

    @Test
    void addVideoToModule_ShouldCallRepositoryMethods_WhenValidParameters() {
        // Arrange
        int moduleId = 1;
        int videoId = 1;
        int order = 1;
        
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(moduleVideosRepository.findByModuleAndVideo(module, video)).thenReturn(Optional.empty());
        when(module.isUniqOrder(order)).thenReturn(true);

        // Act
        moduleService.addVideoToModule(moduleId, videoId, order);

        // Assert
        verify(moduleRepository).findById(moduleId);
        verify(videoRepository).findById(videoId);
        verify(moduleVideosRepository).findByModuleAndVideo(module, video);
        verify(module).isUniqOrder(order);
        verify(moduleVideosRepository).save(any(ModuleVideos.class));
    }

    @Test
    void addVideoToModule_ShouldThrowException_WhenModuleNotFound() {
        // Arrange
        int moduleId = 1;
        int videoId = 1;
        int order = 1;
        
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFound.class, () -> 
            moduleService.addVideoToModule(moduleId, videoId, order));
        verify(moduleRepository).findById(moduleId);
        verify(videoRepository, never()).findById(anyInt());
    }

    @Test
    void addVideoToModule_ShouldThrowException_WhenVideoNotFound() {
        // Arrange
        int moduleId = 1;
        int videoId = 1;
        int order = 1;
        
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            moduleService.addVideoToModule(moduleId, videoId, order));
        verify(moduleRepository).findById(moduleId);
        verify(videoRepository).findById(videoId);
    }

    @Test
    void addVideoToModule_ShouldDoNothing_WhenOrderIsNegative() {
        // Arrange
        int moduleId = 1;
        int videoId = 1;
        int order = -1;

        // Act
        moduleService.addVideoToModule(moduleId, videoId, order);

        // Assert
        verify(moduleRepository, never()).findById(anyInt());
        verify(videoRepository, never()).findById(anyInt());
    }

    @Test
    void addVideoToModule_ShouldThrowException_WhenOrderNotUnique() {
        // Arrange
        int moduleId = 1;
        int videoId = 1;
        int order = 1;
        
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(moduleVideosRepository.findByModuleAndVideo(module, video)).thenReturn(Optional.empty());
        when(module.isUniqOrder(order)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            moduleService.addVideoToModule(moduleId, videoId, order));
        verify(moduleRepository).findById(moduleId);
        verify(videoRepository).findById(videoId);
        verify(moduleVideosRepository).findByModuleAndVideo(module, video);
        verify(module).isUniqOrder(order);
        verify(moduleVideosRepository, never()).save(any());
    }

    @Test
    void addVideoToModule_ShouldSkip_WhenModuleVideoAlreadyExists() {
        // Arrange
        int moduleId = 1;
        int videoId = 1;
        int order = 1;
        
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(moduleVideosRepository.findByModuleAndVideo(module, video)).thenReturn(Optional.of(moduleVideos));

        // Act
        moduleService.addVideoToModule(moduleId, videoId, order);

        // Assert
        verify(moduleRepository).findById(moduleId);
        verify(videoRepository).findById(videoId);
        verify(moduleVideosRepository).findByModuleAndVideo(module, video);
        verify(module, never()).isUniqOrder(anyInt());
        verify(moduleVideosRepository, never()).save(any());
    }

    @Test
    void removeVideoFromModule_ShouldRemoveVideo_WhenModuleVideoExists() {
        // Arrange
        int moduleId = 1;
        int videoId = 1;
        
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(moduleVideosRepository.findByModuleAndVideo(module, video)).thenReturn(Optional.of(moduleVideos));

        // Act
        moduleService.removeVideoFromModule(moduleId, videoId);

        // Assert
        verify(moduleRepository).findById(moduleId);
        verify(videoRepository).findById(videoId);
        verify(moduleVideosRepository).findByModuleAndVideo(module, video);
        verify(module).removeVideoFromModule(moduleVideos);
        verify(moduleVideosRepository).save(moduleVideos);
    }

    @Test
    void removeVideoFromModule_ShouldSkip_WhenModuleVideoNotExists() {
        // Arrange
        int moduleId = 1;
        int videoId = 1;
        
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(moduleVideosRepository.findByModuleAndVideo(module, video)).thenReturn(Optional.empty());

        // Act
        moduleService.removeVideoFromModule(moduleId, videoId);

        // Assert
        verify(moduleRepository).findById(moduleId);
        verify(videoRepository).findById(videoId);
        verify(moduleVideosRepository).findByModuleAndVideo(module, video);
        verify(module, never()).removeVideoFromModule(any());
        verify(moduleVideosRepository, never()).save(any());
    }

    @Test
    void removeVideoFromModule_ShouldThrowException_WhenModuleNotFound() {
        // Arrange
        int moduleId = 1;
        int videoId = 1;
        
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFound.class, () -> 
            moduleService.removeVideoFromModule(moduleId, videoId));
        verify(moduleRepository).findById(moduleId);
        verify(videoRepository, never()).findById(anyInt());
    }

    @Test
    void removeVideoFromModule_ShouldThrowException_WhenVideoNotFound() {
        // Arrange
        int moduleId = 1;
        int videoId = 1;
        
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            moduleService.removeVideoFromModule(moduleId, videoId));
        verify(moduleRepository).findById(moduleId);
        verify(videoRepository).findById(videoId);
    }
}
