package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.quizzes.*;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.QuizEntity;
import com.example.e_learning_system.Entities.QuizQuestionEntity;
import com.example.e_learning_system.Entities.VideoEntity;
import com.example.e_learning_system.Mapper.Quizzes.QuizMapper;
import com.example.e_learning_system.Repository.CourseRepository;
import com.example.e_learning_system.Repository.QuizQuestionRepository;
import com.example.e_learning_system.Repository.QuizRepository;
import com.example.e_learning_system.Repository.VideoRepository;
import com.example.e_learning_system.excpetions.ClientException;
import com.example.e_learning_system.excpetions.InvalidQuizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuizServiceTest {

    @InjectMocks
    private QuizService quizService;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizMapper quizMapper;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private VideoRepository videoRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createQuiz_shouldReturnQuizResponse_whenValidInput() {
        CreateQuizDTO createDTO = new CreateQuizDTO();
        createDTO.setTitle("Sample Quiz");
        createDTO.setTotalScore(10);
        QuizQuestionCreateDTO question = new QuizQuestionCreateDTO();
        question.setText("Q1");
        question.setQuestionMark(5f);
        createDTO.setQuestions(List.of(question));

        VideoEntity  video = new VideoEntity();
        video.setId(1);
        QuizEntity quizEntity = new QuizEntity();
        quizEntity.setId(100);

        when(videoRepository.findById(video.getId())).thenReturn(Optional.of(video));
        when(quizMapper.dtoToEntity(createDTO, video)).thenReturn(quizEntity);
        when(quizRepository.save(quizEntity)).thenReturn(quizEntity);
        QuizResponseDTO responseDTO = new QuizResponseDTO();
        responseDTO.setId(100);
        when(quizMapper.entityToDto(quizEntity)).thenReturn(responseDTO);

        QuizResponseDTO result = quizService.createQuiz(1, createDTO);

        assertNotNull(result);
        assertEquals(100, result.getId());
        verify(quizRepository, times(1)).save(quizEntity);
    }

    @Test
    void createQuiz_shouldThrowInvalidQuizException_whenTotalMarksExceed() {
        CreateQuizDTO createDTO = new CreateQuizDTO();
        createDTO.setTitle("Quiz");
        createDTO.setTotalScore(5);
        QuizQuestionCreateDTO q1 = new QuizQuestionCreateDTO();
        q1.setQuestionMark(6f);
        createDTO.setQuestions(List.of(q1));

        InvalidQuizException ex = assertThrows(InvalidQuizException.class,
                () -> quizService.createQuiz(1, createDTO));

        assertTrue(ex.getMessage().contains("Total of question marks"));

    }

    @Test
    void createQuiz_shouldThrowClientException_whenCourseNotFound() {
        CreateQuizDTO createDTO = new CreateQuizDTO();
        createDTO.setTitle("Quiz");
        createDTO.setTotalScore(10);
        QuizQuestionCreateDTO q1 = new QuizQuestionCreateDTO();
        q1.setQuestionMark(5f);
        createDTO.setQuestions(List.of(q1));

        when(courseRepository.findById(1)).thenReturn(Optional.empty());

        ClientException ex = assertThrows(ClientException.class,
                () -> quizService.createQuiz(1, createDTO));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }


    @Test
    void getQuizzes_shouldReturnQuizList_whenNoFilter() {
        QuizEntity quiz1 = new QuizEntity();
        quiz1.setId(1);
        QuizEntity quiz2 = new QuizEntity();
        quiz2.setId(2);
        when(quizRepository.findAll()).thenReturn(List.of(quiz1, quiz2));
        when(quizMapper.entityToDto(any())).thenAnswer(i -> {
            QuizEntity q = i.getArgument(0);
            QuizResponseDTO dto = new QuizResponseDTO();
            dto.setId(q.getId());
            return dto;
        });

        List<QuizResponseDTO> result = quizService.getQuizzes(null, null, null, null);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
    }

    @Test
    void getQuizzes_shouldFilterByQuizId() {
        QuizEntity quiz = new QuizEntity();
        quiz.setId(10);
        when(quizRepository.findById(10)).thenReturn(Optional.of(quiz));
        when(quizMapper.entityToDto(quiz)).thenReturn(new QuizResponseDTO());

        List<QuizResponseDTO> result = quizService.getQuizzes(null, 10, null, null);

        assertEquals(1, result.size());
        verify(quizRepository, times(1)).findById(10);
    }

    @Test
    void updateQuiz_shouldReturnUpdatedQuiz_whenValidInput() {
        UpdateQuizDTO updateDTO = new UpdateQuizDTO();
        updateDTO.setTotalScore(10);
        UpdateQuizQuestionDTO questionDTO = new UpdateQuizQuestionDTO();
        questionDTO.setId(1);
        questionDTO.setQuestionMark(5f);
        updateDTO.setQuestions(List.of(questionDTO));

        QuizQuestionEntity questionEntity = new QuizQuestionEntity();
        questionEntity.setId(1);
        questionEntity.setQuestionMark(5f);

        QuizEntity existingQuiz = new QuizEntity();
        existingQuiz.setId(100);
        existingQuiz.setQuestions(List.of(questionEntity));

        when(quizRepository.findById(100)).thenReturn(Optional.of(existingQuiz));
        doNothing().when(quizMapper).updateEntityFromDto(updateDTO, existingQuiz);
        when(quizRepository.save(existingQuiz)).thenReturn(existingQuiz);
        when(quizMapper.entityToDto(existingQuiz)).thenReturn(new QuizResponseDTO(){{
            setId(100);
        }});

        QuizResponseDTO result = quizService.updateQuiz(100, updateDTO);

        assertNotNull(result);
        assertEquals(100, result.getId());
        verify(quizRepository, times(1)).save(existingQuiz);
    }

    @Test
    void updateQuiz_shouldThrowClientException_whenTotalMarksExceed() {
        UpdateQuizDTO updateDTO = new UpdateQuizDTO();
        updateDTO.setTotalScore(5);
        UpdateQuizQuestionDTO questionDTO = new UpdateQuizQuestionDTO();
        questionDTO.setId(1);
        questionDTO.setQuestionMark(10f);
        updateDTO.setQuestions(List.of(questionDTO));

        QuizQuestionEntity questionEntity = new QuizQuestionEntity();
        questionEntity.setId(1);
        questionEntity.setQuestionMark(10f);

        QuizEntity existingQuiz = new QuizEntity();
        existingQuiz.setId(100);
        existingQuiz.setQuestions(List.of(questionEntity));

        when(quizRepository.findById(100)).thenReturn(Optional.of(existingQuiz));

        ClientException ex = assertThrows(ClientException.class,
                () -> quizService.updateQuiz(100, updateDTO));

        assertTrue(ex.getMessage().contains("Total of question marks"));
    }

    @Test
    void updateQuiz_shouldThrowClientException_whenQuizNotFound() {
        UpdateQuizDTO updateDTO = new UpdateQuizDTO();

        when(quizRepository.findById(100)).thenReturn(Optional.empty());

        ClientException ex = assertThrows(ClientException.class,
                () -> quizService.updateQuiz(100, updateDTO));

        assertEquals("QUIZ_NOT_FOUND", ex.getErrorCode());
    }

}
