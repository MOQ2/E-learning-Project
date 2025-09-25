package com.example.e_learning_system.Mapper.Quizzes;

import com.example.e_learning_system.Dto.quizzes.*;
import com.example.e_learning_system.Entities.*;
import com.example.e_learning_system.excpetions.ClientException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class QuizMapper {

    public QuizEntity dtoToEntity(CreateQuizDTO dto,VideoEntity video) {
        QuizEntity quiz = new QuizEntity();
        quiz.setVideo(video);
        quiz.setTitle(dto.getTitle());
        quiz.setTotalScore(dto.getTotalScore());
        quiz.setIsActive(dto.getIsActive());

        if (dto.getQuestions() != null) {
            quiz.setQuestions(
                    dto.getQuestions().stream()
                            .map(this::mapQuestionCreateDTO)
                            .collect(Collectors.toList())
            );
            quiz.getQuestions().forEach(q -> q.setQuiz(quiz));
        }

        return quiz;
    }

    private QuizQuestionEntity mapQuestionCreateDTO(QuizQuestionCreateDTO dto) {
        QuizQuestionEntity question = new QuizQuestionEntity();
        question.setText(dto.getText());
        question.setQuestionMark(dto.getQuestionMark());

        if (dto.getOptions() != null) {
            question.setOptions(
                    dto.getOptions().stream()
                            .map(this::mapOptionCreateDTO)
                            .collect(Collectors.toList())
            );
            question.getOptions().forEach(o -> o.setQuestion(question));
        }
        return question;
    }

    private QuizOptionEntity mapOptionCreateDTO(QuizOptionCreateDTO dto) {
        QuizOptionEntity option = new QuizOptionEntity();
        option.setText(dto.getText());
        option.setIsCorrect(dto.getIsCorrect());
        return option;
    }

    public void updateEntityFromDto(UpdateQuizDTO dto, QuizEntity quiz) {
        if (dto.getTitle() != null) quiz.setTitle(dto.getTitle());
        if (dto.getTotalScore() != null) quiz.setTotalScore(dto.getTotalScore());
        if (dto.getIsActive() != null) quiz.setIsActive(dto.getIsActive());

        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            if (quiz.getQuestions() == null) {
                quiz.setQuestions(new ArrayList<>());
            }

            for (UpdateQuizQuestionDTO qDto : dto.getQuestions()) {
                QuizQuestionEntity questionEntity;
                if (qDto.getId() != null) {
                    questionEntity = findExistingQuestion(quiz, qDto.getId());
                    questionEntity.setText(qDto.getText());
                    questionEntity.setQuestionMark(qDto.getQuestionMark());

                    if (qDto.getOptions() != null) {
                        if (questionEntity.getOptions() == null) {
                            questionEntity.setOptions(new ArrayList<>());
                        }

                        for (UpdateQuizOptionDTO oDto : qDto.getOptions()) {
                            if (oDto.getId() != null) {
                                QuizOptionEntity existingOption = questionEntity.getOptions().stream()
                                        .filter(opt -> Objects.equals(opt.getId(), oDto.getId()))
                                        .findFirst()
                                        .orElseThrow(() -> new ClientException(
                                                "Option not found: " + oDto.getId(),
                                                "OPTION_NOT_FOUND",
                                                HttpStatus.NOT_FOUND
                                        ));
                                existingOption.setText(oDto.getText());
                                existingOption.setIsCorrect(oDto.getIsCorrect());
                            } else {
                                QuizOptionEntity newOption = new QuizOptionEntity();
                                newOption.setText(oDto.getText());
                                newOption.setIsCorrect(oDto.getIsCorrect());
                                newOption.setQuestion(questionEntity);
                                questionEntity.getOptions().add(newOption);
                            }
                        }
                    }
                } else {
                    questionEntity = new QuizQuestionEntity();
                    questionEntity.setText(qDto.getText());
                    questionEntity.setQuestionMark(qDto.getQuestionMark());
                    questionEntity.setQuiz(quiz);

                    if (qDto.getOptions() != null) {
                        questionEntity.setOptions(new ArrayList<>());
                        for (UpdateQuizOptionDTO oDto : qDto.getOptions()) {
                            QuizOptionEntity option = new QuizOptionEntity();
                            option.setText(oDto.getText());
                            option.setIsCorrect(oDto.getIsCorrect());
                            option.setQuestion(questionEntity);
                            questionEntity.getOptions().add(option);
                        }
                    }

                    quiz.getQuestions().add(questionEntity);
                }
            }
        }
    }

    private QuizQuestionEntity findExistingQuestion(QuizEntity quiz, Integer questionId) {
        return quiz.getQuestions().stream()
                .filter(q -> Objects.equals(q.getId(), questionId))
                .findFirst()
                .orElseThrow(() -> new ClientException(
                        "Question not found: " + questionId,
                        "QUESTION_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));
    }

    public QuizResponseDTO entityToDto(QuizEntity entity) {
        QuizResponseDTO dto = new QuizResponseDTO();
        dto.setId(entity.getId());
        dto.setVideoId(entity.getVideo().getId());
        dto.setTitle(entity.getTitle());
        dto.setTotalScore(entity.getTotalScore());
        dto.setIsActive(entity.getIsActive());

        if (entity.getQuestions() != null) {
            dto.setQuestions(
                    entity.getQuestions().stream()
                            .map(this::mapQuestionEntity)
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }

    private QuizQuestionResponseDTO mapQuestionEntity(QuizQuestionEntity entity) {
        QuizQuestionResponseDTO dto = new QuizQuestionResponseDTO();
        dto.setId(entity.getId());
        dto.setText(entity.getText());
        dto.setQuestionMark(entity.getQuestionMark());

        if (entity.getOptions() != null) {
            dto.setOptions(
                    entity.getOptions().stream()
                            .map(this::mapOptionEntity)
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }

    private QuizOptionResponseDTO mapOptionEntity(QuizOptionEntity entity) {
        QuizOptionResponseDTO dto = new QuizOptionResponseDTO();
        dto.setId(entity.getId());
        dto.setText(entity.getText());
        dto.setIsCorrect(entity.getIsCorrect());
        return dto;
    }
}
