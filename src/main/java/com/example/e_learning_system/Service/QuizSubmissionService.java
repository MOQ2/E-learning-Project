package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.quizzes.QuizSubmissionResponseDTO;
import com.example.e_learning_system.Dto.quizzes.QuizSubmitDTO;
import com.example.e_learning_system.Dto.quizzes.StudentAnswerDTO;
import com.example.e_learning_system.Dto.quizzes.StudentAnswerResponseDTO;
import com.example.e_learning_system.Entities.*;
import com.example.e_learning_system.Mapper.Quizzes.QuizSubmissionsMapper;
import com.example.e_learning_system.Repository.*;
import com.example.e_learning_system.Security.UserUtil;
import com.example.e_learning_system.Service.Interfaces.QuizzesInterfaces.QuizSubmissions;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class QuizSubmissionService implements QuizSubmissions {

    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final QuizSubmissionsMapper mapper;

    @Override
    @Transactional
    public void submitQuiz(QuizSubmitDTO quizSubmitDTO) {
        UserEntity user = userRepository.findById(UserUtil.getCurrentUserId().intValue())
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuizEntity quiz = quizRepository.findById(quizSubmitDTO.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        QuizSubmissionEntity submission = mapper.dtoToSubmission(user, quiz);
        quizSubmissionRepository.save(submission);

        float totalScore = 0f;
        List<StudentAnswerEntity> answers = new ArrayList<>();

        List<QuizQuestionEntity> allQuestions = quizQuestionRepository.findByQuizId(quiz.getId());

        for (QuizQuestionEntity question : allQuestions) {
            StudentAnswerDTO dto = quizSubmitDTO.getAnswers().stream()
                    .filter(a -> a.getQuestionId().equals(question.getId()))
                    .findFirst()
                    .orElse(null);

            QuizOptionEntity selectedOption = null;
            boolean isCorrect = false;

            if (dto != null && dto.getSelectedOptionId() != null) {
                selectedOption = quizOptionRepository.findById(dto.getSelectedOptionId())
                        .orElseThrow(() -> new RuntimeException("Option not found"));

                isCorrect = selectedOption.getIsCorrect() != null && selectedOption.getIsCorrect();
                if (isCorrect) {
                    totalScore += question.getQuestionMark();
                }
            }

            StudentAnswerEntity answerEntity = mapper.dtoToAnswer(question, selectedOption, submission, isCorrect);

            answers.add(answerEntity);
        }

        submission.setAnswers(answers);
        submission.setScore(totalScore);

        quizSubmissionRepository.save(submission);
    }

    @Override
    public List<QuizSubmissionResponseDTO> getQuizAttempts(Integer quizId, Integer userId) {
        List<QuizSubmissionEntity> submissions;

        if (userId != null) {
            submissions = quizSubmissionRepository.findByQuizIdAndUserId(quizId, userId);
        } else {
            submissions = quizSubmissionRepository.findByQuizId(quizId);
        }

        return submissions.stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    public List<StudentAnswerResponseDTO> getSubmissionAnswers(Integer submissionId) {
        QuizSubmissionEntity submission = quizSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        return submission.getAnswers()
                .stream()
                .map(mapper::toAnswerResponseDTO)
                .toList();
    }

}