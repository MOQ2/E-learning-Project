package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.quizzes.*;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.QuizEntity;
import com.example.e_learning_system.Entities.QuizQuestionEntity;
import com.example.e_learning_system.Mapper.Quizzes.QuizMapper;
import com.example.e_learning_system.Repository.CourseRepository;
import com.example.e_learning_system.Repository.QuizRepository;
import com.example.e_learning_system.Service.Interfaces.QuizzesInterfaces.QuizInterface;
import com.example.e_learning_system.excpetions.ClientException;
import com.example.e_learning_system.excpetions.InvalidQuizException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class QuizService implements QuizInterface {
    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public QuizResponseDTO createQuiz(Integer courseId, CreateQuizDTO createQuizDTO) {
        float totalQuestionsMark = createQuizDTO.getQuestions()
                .stream()
                .map(QuizQuestionCreateDTO::getQuestionMark)
                .reduce(0f, Float::sum);

        if (totalQuestionsMark > createQuizDTO.getTotalScore()) {
            throw new InvalidQuizException(
                    "Total of question marks (" + totalQuestionsMark + ") exceeds quiz total score (" + createQuizDTO.getTotalScore() + ")"
            );
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ClientException(
                        "Course not found",
                        "COURSE_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        QuizEntity quiz = quizMapper.dtoToEntity(createQuizDTO, course);
        QuizEntity savedQuiz = quizRepository.save(quiz);

        return quizMapper.entityToDto(savedQuiz);
    }


    @Override
    @Transactional
    public QuizResponseDTO updateQuiz(Integer quizId, UpdateQuizDTO updateDTO) {
        QuizEntity quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ClientException(
                        "Quiz not found",
                        "QUIZ_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        float totalMarks = updateDTO.getQuestions() != null
                ? updateDTO.getQuestions().stream().map(UpdateQuizQuestionDTO::getQuestionMark).reduce(0f, Float::sum)
                : 0f;

        float existingMarks = quiz.getQuestions().stream()
                .filter(q -> updateDTO.getQuestions() == null || updateDTO.getQuestions().stream()
                        .noneMatch(uq -> uq.getId() != null && uq.getId().equals(q.getId())))
                .map(QuizQuestionEntity::getQuestionMark)
                .reduce(0f, Float::sum);

        totalMarks += existingMarks;

        if (updateDTO.getTotalScore() != null && totalMarks > updateDTO.getTotalScore()) {
            throw new ClientException(
                    "Total of question marks (" + totalMarks + ") exceeds quiz total score (" + updateDTO.getTotalScore() + ")",
                    "TOTAL_QUESTION_MARKS_EXCEED",
                    HttpStatus.BAD_REQUEST
            );
        }

        quizMapper.updateEntityFromDto(updateDTO, quiz);
        QuizEntity updatedQuiz = quizRepository.save(quiz);

        return quizMapper.entityToDto(updatedQuiz);
    }





    @Override
    @Transactional
    public List<QuizResponseDTO> getQuizzes(Integer courseId, Integer quizId, String title, Boolean isActive) {
        List<QuizEntity> quizzes;

        if (quizId != null) {
            QuizEntity quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new ClientException(
                            "Quiz not found", "QUIZ_NOT_FOUND", HttpStatus.NOT_FOUND
                    ));
            quizzes = List.of(quiz);
        } else {
            quizzes = quizRepository.findAll().stream()
                    .filter(q -> courseId == null || Objects.equals(q.getCourse().getId(), courseId))
                    .filter(q -> title == null || (q.getTitle() != null && q.getTitle().toLowerCase().contains(title.toLowerCase())))
                    .filter(q -> isActive == null || q.getIsActive().equals(isActive))
                    .toList();
        }

        return quizzes.stream()
                .map(quizMapper::entityToDto)
                .toList();
    }
}