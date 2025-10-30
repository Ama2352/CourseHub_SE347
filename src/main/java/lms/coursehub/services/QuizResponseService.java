package lms.coursehub.services;

import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.helpers.mapstructs.QuizResponseMapper;
import lms.coursehub.models.dtos.quiz.CreateQuizResponseRequest;
import lms.coursehub.models.dtos.quiz.CreateQuizResponseAnswerDto;
import lms.coursehub.models.dtos.quiz.QuizResponseDto;
import lms.coursehub.models.dtos.quiz.UpdateQuizResponseRequest;
import lms.coursehub.models.entities.*;
import lms.coursehub.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizResponseService {
    private final QuizResponseRepo quizResponseRepo;
    private final QuizResponseAnswerRepo answerRepo;
    private final TopicQuizRepo topicQuizRepo;
    private final UserService userService;

    private TopicQuiz findTopicQuizById(UUID topicId) {
        return topicQuizRepo.findById(topicId)
                .orElseThrow(() -> new CustomException("Topic quiz not found", HttpStatus.NOT_FOUND));
    }

    private QuizResponse findQuizResponseById(UUID responseId) {
        return quizResponseRepo.findById(responseId)
                .orElseThrow(() -> new CustomException("Quiz response not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public QuizResponseDto getQuizResponse(UUID topicId, UUID responseId) {
        TopicQuiz topicQuiz = findTopicQuizById(topicId);
        QuizResponse response = findQuizResponseById(responseId);
        
        if (!response.getTopicQuiz().getId().equals(topicQuiz.getId())) {
            throw new CustomException("Quiz response does not belong to this topic", HttpStatus.BAD_REQUEST);
        }

        return QuizResponseMapper.INSTANCE.toDto(response);
    }

    @Transactional
    public QuizResponseDto createQuizResponse(UUID topicId, CreateQuizResponseRequest request) {
        TopicQuiz topicQuiz = findTopicQuizById(topicId);
        User currentUser = userService.getCurrentUser();

        // Create quiz response
        QuizResponse quizResponse = new QuizResponse();
        quizResponse.setTopicQuiz(topicQuiz);
        quizResponse.setStudent(currentUser);
        quizResponse.setStartedAt(LocalDateTime.now());
        quizResponse.setStatus("SUBMITTED");
        
        QuizResponse savedResponse = quizResponseRepo.save(quizResponse);

        // Create answers
        if (request.getAnswers() != null) {
            for (CreateQuizResponseAnswerDto answerRequest : request.getAnswers()) {
                QuizResponseAnswer answer = new QuizResponseAnswer();
                answer.setQuizResponse(savedResponse);
                answer.setQuestion(answerRequest.getQuestionId());
                answer.setAnswer(answerRequest.getAnswerText());
                answer.setMark(BigDecimal.ZERO);
                
                // Save answer
                answerRepo.save(answer);
            }

            savedResponse.setCompletedAt(LocalDateTime.now());
            savedResponse = quizResponseRepo.save(savedResponse);
        }

        return QuizResponseMapper.INSTANCE.toDto(savedResponse);
    }

    @Transactional(readOnly = true)
    public List<QuizResponseDto> getAllQuizResponsesOfTopic(UUID topicId) {
        List<QuizResponse> responses = quizResponseRepo.findByTopicQuizId(topicId);
        return responses.stream()
                .map(QuizResponseMapper.INSTANCE::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuizResponseDto> getAllQuizResponsesOfUser(UUID userId) {
        List<QuizResponse> responses = quizResponseRepo.findByStudentId(userId);
        return responses.stream()
                .map(QuizResponseMapper.INSTANCE::toDto)
                .toList();
    }

    @Transactional
    public QuizResponseDto updateQuizResponse(UUID topicId, UUID responseId, UpdateQuizResponseRequest request) {
        TopicQuiz topicQuiz = findTopicQuizById(topicId);
        QuizResponse response = findQuizResponseById(responseId);

        if (!response.getTopicQuiz().getId().equals(topicQuiz.getId())) {
            throw new CustomException("Quiz response does not belong to this topic", HttpStatus.BAD_REQUEST);
        }

        // Update status if provided
        if (request.getStatus() != null) {
            response.setStatus(request.getStatus());
        }

        // Update completedAt based on isCompleted flag
        if (request.getIsCompleted() != null) {
            if (request.getIsCompleted()) {
                if (response.getCompletedAt() == null) {
                    response.setCompletedAt(LocalDateTime.now());
                }
                response.setStatus("COMPLETED");
            } else {
                response.setCompletedAt(null);
                response.setStatus("IN_PROGRESS");
            }
        }

        // Save and return updated response
        QuizResponse updatedResponse = quizResponseRepo.save(response);
        return QuizResponseMapper.INSTANCE.toDto(updatedResponse);
    }


}