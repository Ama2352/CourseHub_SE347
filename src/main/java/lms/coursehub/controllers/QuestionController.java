package lms.coursehub.controllers;

import lms.coursehub.models.dtos.question.CreateQuestionRequest;
import lms.coursehub.models.dtos.question.QuestionResponseDto;
import lms.coursehub.models.dtos.question.UpdateQuestionRequest;
import lms.coursehub.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/question")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<QuestionResponseDto> createQuestion(
            @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.ok(questionService.createQuestion(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponseDto> getQuestion(@PathVariable UUID id) {
        return ResponseEntity.ok(questionService.getQuestion(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponseDto> updateQuestion(
            @PathVariable UUID id,
            @RequestBody UpdateQuestionRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request));
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponseDto>> getQuestionBank() {
        return ResponseEntity.ok(questionService.getQuestionBank());
    }
}