package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.quizzes.QuizSubmitDTO;
import com.example.e_learning_system.Dto.quizzes.StudentAnswerDTO;
import com.example.e_learning_system.Entities.*;
import com.example.e_learning_system.Repository.*;
import com.example.e_learning_system.Security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class QuizSubmissionsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizOptionRepository quizOptionRepository;

    @Autowired
    private QuizSubmissionRepository quizSubmissionRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private UserEntity adminUser;
    private Course testCourse;
    private QuizEntity testQuiz;
    private String token;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.findByEmail("admin@example.com").orElseThrow();

        testCourse = new Course();
        testCourse.setName("Test Course");
        testCourse.setDescription("Integration Test Course");
        testCourse.setActive(true);
        testCourse.setCreatedBy(adminUser);
        testCourse = courseRepository.save(testCourse);

        QuizQuestionEntity question = new QuizQuestionEntity();
        question.setText("Sample Question");
        question.setQuestionMark(100f);

        QuizOptionEntity option1 = new QuizOptionEntity();
        option1.setText("Option 1");
        option1.setIsCorrect(true);

        QuizOptionEntity option2 = new QuizOptionEntity();
        option2.setText("Option 2");
        option2.setIsCorrect(false);

        testQuiz = new QuizEntity();
        testQuiz.setTitle("Sample Quiz");
        testQuiz.setTotalScore(100);
        testQuiz.setIsActive(true);
        testQuiz.setCourse(testCourse);

        testQuiz.setQuestions(new ArrayList<>(List.of(question)));
        question.setQuiz(testQuiz);
        question.setOptions(new ArrayList<>(List.of(option1, option2)));
        option1.setQuestion(question);
        option2.setQuestion(question);

        testQuiz = quizRepository.save(testQuiz);
        quizQuestionRepository.save(question);
        quizOptionRepository.saveAll(new ArrayList<>(List.of(option1, option2)));

        token = jwtUtil.generateToken(adminUser);
    }

    @Test
    void submitQuiz_and_getAttempts_and_getAnswers() throws Exception {
        QuizSubmitDTO submitDTO = new QuizSubmitDTO();
        submitDTO.setQuizId(testQuiz.getId());
        submitDTO.setAnswers(new ArrayList<>(List.of(
                new StudentAnswerDTO(
                        quizQuestionRepository.findAll().get(0).getId(),
                        quizOptionRepository.findAll().get(0).getId()
                )
        )));

        String response = mockMvc.perform(post("/submitQuiz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(submitDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response).contains("submissionId");

        mockMvc.perform(get("/" + testQuiz.getId() + "/attempts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Integer submissionId = quizSubmissionRepository.findAll().get(0).getId();
        mockMvc.perform(get("/" + submissionId + "/answers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
