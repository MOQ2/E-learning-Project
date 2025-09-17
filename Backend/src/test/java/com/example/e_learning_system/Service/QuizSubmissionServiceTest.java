package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.quizzes.QuizSubmitDTO;
import com.example.e_learning_system.Dto.quizzes.QuizSubmissionResponseDTO;
import com.example.e_learning_system.Dto.quizzes.StudentAnswerDTO;
import com.example.e_learning_system.Dto.quizzes.StudentAnswerResponseDTO;
import com.example.e_learning_system.Entities.*;
import com.example.e_learning_system.Mapper.Quizzes.QuizSubmissionsMapper;
import com.example.e_learning_system.Repository.*;
import com.example.e_learning_system.Security.UserUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class QuizSubmissionServiceTest {

    @InjectMocks
    private QuizSubmissionService quizSubmissionService;

    @Mock private QuizSubmissionRepository quizSubmissionRepository;
    @Mock private UserRepository userRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private QuizQuestionRepository quizQuestionRepository;
    @Mock private QuizOptionRepository quizOptionRepository;
    @Mock private QuizSubmissionsMapper mapper;

    private UserEntity mockUser;
    private QuizEntity mockQuiz;

    private MockedStatic<UserUtil> userUtilMock; // 👈 نحتفظ فيه كـ field

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new UserEntity();
        mockUser.setId(1);
        mockUser.setName("testUser");

        mockQuiz = new QuizEntity();
        mockQuiz.setId(10);

        // Mock static method once ويضل شغال
        userUtilMock = mockStatic(UserUtil.class);
        userUtilMock.when(UserUtil::getCurrentUserId).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        userUtilMock.close(); // 👈 نسكّره بعد كل تيست
    }
    @Test
    void testSubmitQuiz_success() {
        // Arrange
        QuizSubmitDTO submitDTO = new QuizSubmitDTO();
        submitDTO.setQuizId(mockQuiz.getId());
        StudentAnswerDTO answerDTO = new StudentAnswerDTO();
        answerDTO.setQuestionId(100);
        answerDTO.setSelectedOptionId(200);
        submitDTO.setAnswers(List.of(answerDTO));

        QuizQuestionEntity question = new QuizQuestionEntity();
        question.setId(100);
        question.setQuestionMark(5f);

        QuizOptionEntity option = new QuizOptionEntity();
        option.setId(200);
        option.setIsCorrect(true);

        QuizSubmissionEntity submission = new QuizSubmissionEntity();
        submission.setId(50);

        QuizSubmissionEntity savedSubmission = new QuizSubmissionEntity();
        savedSubmission.setId(50);
        savedSubmission.setScore(5f);

        QuizSubmissionResponseDTO responseDTO = new QuizSubmissionResponseDTO();
        responseDTO.setSubmissionId(50);
        responseDTO.setUserName("testUser");
        responseDTO.setScore(5f);
        responseDTO.setSubmittedAt(LocalDateTime.now());

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(mockUser));
        when(quizRepository.findById(mockQuiz.getId())).thenReturn(Optional.of(mockQuiz));
        when(mapper.dtoToSubmission(mockUser, mockQuiz)).thenReturn(submission);
        when(quizQuestionRepository.findByQuizId(mockQuiz.getId())).thenReturn(List.of(question));
        when(quizOptionRepository.findById(200)).thenReturn(Optional.of(option));
        when(mapper.dtoToAnswer(eq(question), eq(option), eq(submission), eq(true)))
                .thenReturn(new StudentAnswerEntity());
        when(quizSubmissionRepository.save(any())).thenReturn(savedSubmission);
        when(mapper.toResponseDTO(savedSubmission)).thenReturn(responseDTO);

        // Act
        QuizSubmissionResponseDTO result = quizSubmissionService.submitQuiz(submitDTO);

        // Assert
        assertNotNull(result);
        assertEquals(50, result.getSubmissionId());
        assertEquals("testUser", result.getUserName());
        assertEquals(5f, result.getScore());
        assertNotNull(result.getSubmittedAt());

        verify(quizSubmissionRepository, times(2)).save(any()); // save called twice
    }

    @Test
    void testSubmitQuiz_userNotFound() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
        QuizSubmitDTO dto = new QuizSubmitDTO();
        dto.setQuizId(10);

        assertThrows(RuntimeException.class,
                () -> quizSubmissionService.submitQuiz(dto));
    }

    @Test
    void testGetQuizAttempts_withUserId() {
        QuizSubmissionEntity entity = new QuizSubmissionEntity();
        entity.setId(1);

        QuizSubmissionResponseDTO dto = new QuizSubmissionResponseDTO();
        dto.setSubmissionId(1);
        dto.setUserName("testUser");
        dto.setScore(8.0f);
        dto.setSubmittedAt(LocalDateTime.now());

        when(quizSubmissionRepository.findByQuizIdAndUserId(10, 1))
                .thenReturn(List.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(dto);

        List<QuizSubmissionResponseDTO> result =
                quizSubmissionService.getQuizAttempts(10, 1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getSubmissionId());
        assertEquals("testUser", result.get(0).getUserName());
    }

    @Test
    void testGetSubmissionAnswers_success() {
        QuizSubmissionEntity submission = new QuizSubmissionEntity();
        submission.setId(5);

        StudentAnswerEntity answerEntity = new StudentAnswerEntity();
        answerEntity.setId(100);
        submission.setAnswers(List.of(answerEntity));

        StudentAnswerResponseDTO dto = new StudentAnswerResponseDTO();
        dto.setQuestionId(100);
        dto.setQuestionText("What is Java?");
        dto.setSelectedOptionId(200);
        dto.setSelectedOptionText("A programming language");
        dto.setIsCorrect(true);

        when(quizSubmissionRepository.findById(5)).thenReturn(Optional.of(submission));
        when(mapper.toAnswerResponseDTO(answerEntity)).thenReturn(dto);

        List<StudentAnswerResponseDTO> result = quizSubmissionService.getSubmissionAnswers(5);

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getQuestionId());
        assertEquals("What is Java?", result.get(0).getQuestionText());
        assertEquals(200, result.get(0).getSelectedOptionId());
        assertEquals("A programming language", result.get(0).getSelectedOptionText());
        assertTrue(result.get(0).getIsCorrect());
    }

    @Test
    void testGetSubmissionAnswers_notFound() {
        when(quizSubmissionRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> quizSubmissionService.getSubmissionAnswers(99));
    }
}
