package lms.coursehub.services;

import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.helpers.mapstructs.QuestionMapper;
import lms.coursehub.models.dtos.question.*;
import lms.coursehub.models.entities.*;
import lms.coursehub.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepo questionRepo;
    private final CourseRepo courseRepo;
    private final QuestionChoiceRepo questionChoiceRepo;
    private final UserService userService;

    private Question findQuestionById(UUID id) {
        return questionRepo.findById(id)
                .orElseThrow(() -> new CustomException("Question not found", HttpStatus.NOT_FOUND));
    }

    private Course findCourseById(String id) {
        return courseRepo.findById(id)
                .orElseThrow(() -> new CustomException("Course not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public QuestionResponseDto createQuestion(CreateQuestionRequest request, String courseId) {
        Course course = findCourseById(courseId);
        
        Question question = QuestionMapper.INSTANCE.toEntity(request);
        question.setCourse(course);
        question.setCreator(userService.getCurrentUser());
        
        Question savedQuestion = questionRepo.save(question);
        handleQuestionChoices(savedQuestion, request.getData());
        
        // Refresh to get updated choices
        savedQuestion = findQuestionById(savedQuestion.getId());
        return QuestionMapper.INSTANCE.toDto(savedQuestion);
    }

    @Transactional(readOnly = true)
    public QuestionResponseDto getQuestion(UUID id) {
        Question question = findQuestionById(id);
        return QuestionMapper.INSTANCE.toDto(question);
    }

    @Transactional
    public QuestionResponseDto updateQuestion(UUID id, UpdateQuestionRequest request, String courseId) {
        Question existingQuestion = findQuestionById(id);
        Course course = findCourseById(courseId);

        // Use mapper to update fields
        QuestionMapper.INSTANCE.updateEntityFromDto(request, existingQuestion);
        
        existingQuestion.setCourse(course);
        existingQuestion.setModifier(userService.getCurrentUser());

        // Update choices if present and type is CHOICE
        if (request.getData() != null && request.getType() == QuestionTypeEnum.CHOICE) {
            existingQuestion.getQuestionChoices().clear();
            handleQuestionChoices(existingQuestion, request.getData());
        }

        Question updatedQuestion = questionRepo.save(existingQuestion);
        return QuestionMapper.INSTANCE.toDto(updatedQuestion);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponseDto> getQuestionBank(String courseId) {
        Course course = findCourseById(courseId);
        List<Question> questions = questionRepo.findByCourse(course);
        return questions.stream()
                .map(QuestionMapper.INSTANCE::toDto)
                .toList();
    }

    private void handleQuestionChoices(Question question, QuestionDataRequest data) {
        if (data != null && data.getChoices() != null) {
            for (QuestionChoiceRequest choiceReq : data.getChoices()) {
                QuestionChoice choice = new QuestionChoice();
                choice.setText(choiceReq.getText());
                choice.setCorrect(choiceReq.isCorrect());
                choice.setGrade(BigDecimal.valueOf(choiceReq.getGrade()));
                choice.setQuestion(question);
                questionChoiceRepo.save(choice);
            }
        }
    }


}