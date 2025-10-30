package lms.coursehub.controllers;

import lms.coursehub.models.dtos.quiz.CreateQuizResponseRequest;
import lms.coursehub.models.dtos.quiz.QuizResponseDto;
import lms.coursehub.models.dtos.quiz.UpdateQuizResponseRequest;
import lms.coursehub.services.QuizResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/topic/{topicId}/quiz-response")
@RequiredArgsConstructor
public class QuizResponseController {
    private final QuizResponseService quizResponseService;

    @PostMapping()
    public ResponseEntity<QuizResponseDto> createQuizResponse(
            @PathVariable UUID topicId,
            @RequestBody CreateQuizResponseRequest request) {
        return ResponseEntity.ok(quizResponseService.createQuizResponse(topicId, request));
    }

    @GetMapping()
    public ResponseEntity<List<QuizResponseDto>> getAllQuizResponses(
            @PathVariable UUID topicId,
            @RequestParam(required = false) UUID studentId) {
        if (studentId != null) {
            return ResponseEntity.ok(quizResponseService.getAllQuizResponsesOfUser(studentId));
        }
        return ResponseEntity.ok(quizResponseService.getAllQuizResponsesOfTopic(topicId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponseDto> getQuizResponseById(
            @PathVariable UUID topicId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(quizResponseService.getQuizResponse(topicId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizResponseDto> updateQuizResponseById(
            @PathVariable UUID topicId,
            @PathVariable UUID id,
            @RequestBody UpdateQuizResponseRequest request) {
        return ResponseEntity.ok(quizResponseService.updateQuizResponse(topicId, id, request));
    }
}