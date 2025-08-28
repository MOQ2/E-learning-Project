package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.RolesName;
import com.example.e_learning_system.Entities.*;
import com.example.e_learning_system.Repository.*;
import com.example.e_learning_system.Security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuizIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private UserEntity adminUser;
    private Course testCourse;

    @BeforeEach
    void setUpTestData() {
        adminUser = userRepository.findByEmail("admin@example.com")
                .orElseGet(() -> rolesRepository.findByName(RolesName.ADMIN)
                        .map(role -> UserEntity.builder()
                                .name("Admin User")
                                .email("admin@example.com")
                                .password(passwordEncoder.encode("password123"))
                                .role(role)
                                .isActive(true)
                                .emailVerified(true)
                                .build())
                        .map(userRepository::save)
                        .orElseThrow(() -> new RuntimeException("Admin role not found")));

        testCourse = Course.builder()
                .name("Test Course")
                .description("Integration test course")
                .isActive(true)
                .status(CourseStatus.PUBLISHED)
                .createdBy(adminUser)
                .build();
        testCourse = courseRepository.save(testCourse);
    }

    private String generateToken() {
        return jwtUtil.generateToken(adminUser);
    }

    @Test
    void testCreateQuizWithQuestionsAndOptions() throws Exception {
        String requestBody = """
            {
                "title": "Full Integration Quiz",
                "totalScore": 100,
                "isActive": true,
                "questions": [
                    {
                        "text": "Question 1",
                        "questionMark": 50,
                        "options": [
                            {"text": "Option 1", "isCorrect": true},
                            {"text": "Option 2", "isCorrect": false}
                        ]
                    },
                    {
                        "text": "Question 2",
                        "questionMark": 50,
                        "options": [
                            {"text": "Option A", "isCorrect": false},
                            {"text": "Option B", "isCorrect": true}
                        ]
                    }
                ]
            }
        """;

        mockMvc.perform(post("/quizzes/{courseId}", testCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + generateToken()))
                .andExpect(status().isCreated());

        List<QuizEntity> quizzes = quizRepository.findAll();
        assertThat(quizzes).hasSize(1);
        QuizEntity savedQuiz = quizzes.get(0);
        assertThat(savedQuiz.getTitle()).isEqualTo("Full Integration Quiz");
        assertThat(savedQuiz.getQuestions()).hasSize(2);

        savedQuiz.getQuestions().forEach(question ->
                assertThat(question.getOptions()).isNotNull()
        );
    }

    @Test
    void testUpdateQuiz() throws Exception {
        QuizEntity quiz = new QuizEntity();
        quiz.setCourse(testCourse);
        quiz.setTitle("Old Quiz");
        quiz.setTotalScore(50);
        quiz.setIsActive(true);

        QuizQuestionEntity question = new QuizQuestionEntity();
        question.setQuiz(quiz);
        question.setText("Sample Question");
        question.setQuestionMark(50f);
        question.setOptions(List.of());
        quiz.setQuestions(new ArrayList<>(List.of(question)));

        quiz = quizRepository.save(quiz);

        String updateBody = """
        {
            "title": "Updated Quiz",
            "totalScore": 75
        }
        """;

        mockMvc.perform(patch("/quizzes/{quizId}", quiz.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody)
                        .header("Authorization", "Bearer " + generateToken()))
                .andExpect(status().isOk());

        QuizEntity updatedQuiz = quizRepository.findById(quiz.getId()).orElseThrow();
        assertThat(updatedQuiz.getTitle()).isEqualTo("Updated Quiz");
        assertThat(updatedQuiz.getTotalScore()).isEqualTo(75);
    }

    @Test
    void testGetQuizzes() throws Exception {
        QuizEntity quiz = new QuizEntity();
        quiz.setCourse(testCourse);
        quiz.setTitle("Sample Quiz");
        quiz.setTotalScore(100);
        quiz.setIsActive(true);
        quizRepository.save(quiz);

        String response = mockMvc.perform(get("/quizzes/getQuizzes")
                        .param("courseId", String.valueOf(testCourse.getId()))
                        .header("Authorization", "Bearer " + generateToken()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).contains("Sample Quiz");
    }
}
