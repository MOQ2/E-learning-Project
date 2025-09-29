package com.example.e_learning_system.Mapper.Quizzes;

import com.example.e_learning_system.Dto.quizzes.QuizSubmissionResponseDTO;
import com.example.e_learning_system.Dto.quizzes.QuizSubmitDTO;
import com.example.e_learning_system.Dto.quizzes.StudentAnswerDTO;
import com.example.e_learning_system.Dto.quizzes.StudentAnswerResponseDTO;
import com.example.e_learning_system.Entities.*;
import org.springframework.stereotype.Component;

@Component
public class QuizSubmissionsMapper {

    public QuizSubmissionEntity dtoToSubmission(UserEntity user, QuizEntity quiz) {
        QuizSubmissionEntity submission = new QuizSubmissionEntity();
        submission.setUser(user);
        submission.setQuiz(quiz);
        submission.setScore(0f);
        return submission;
    }

    public StudentAnswerEntity dtoToAnswer(
            QuizQuestionEntity question, QuizOptionEntity selectedOption,
            QuizSubmissionEntity submission, Boolean isCorrect) {
        StudentAnswerEntity answer = new StudentAnswerEntity();
        answer.setSubmission(submission);
        answer.setQuestion(question);
        answer.setSelectedOption(selectedOption);
        answer.setIsCorrect(isCorrect);
        return answer;
    }

    public QuizSubmissionResponseDTO toResponseDTO(QuizSubmissionEntity entity) {
        QuizSubmissionResponseDTO dto = new QuizSubmissionResponseDTO();
        dto.setSubmissionId(entity.getId());
        dto.setUserName(entity.getUser().getName());
        dto.setScore(entity.getScore());
        dto.setSubmittedAt(entity.getCreatedAt());
        return dto;
    }

    public StudentAnswerResponseDTO toAnswerResponseDTO(StudentAnswerEntity entity) {
        StudentAnswerResponseDTO dto = new StudentAnswerResponseDTO();
        dto.setQuestionId(entity.getQuestion().getId());
        dto.setQuestionText(entity.getQuestion().getText());

        if (entity.getSelectedOption() != null) {
            dto.setSelectedOptionId(entity.getSelectedOption().getId());
            dto.setSelectedOptionText(entity.getSelectedOption().getText());
        }

        dto.setIsCorrect(entity.getIsCorrect() != null ? entity.getIsCorrect() : false);

        String correctAnswer = entity.getQuestion().getOptions()
                .stream()
                .filter(QuizOptionEntity::getIsCorrect)
                .map(QuizOptionEntity::getText)
                .findFirst()
                .orElse("No correct answer");
        dto.setCorrectAnswer(correctAnswer);
        return dto;
    }

}
