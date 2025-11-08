package lms.coursehub.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lms.coursehub.models.dtos.assignment.AssignmentResponseDto;
import lms.coursehub.models.dtos.assignment.CreateAssignmentResponseRequest;
import lms.coursehub.models.dtos.assignment.UpdateAssignmentResponseRequest;
import lms.coursehub.services.AssignmentResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/topic/{topicId}/assignment-response")
@Tag(name = "Assignment Response Management")
public class AssignmentResponseController {

    private final AssignmentResponseService assignmentResponseService;

    @PostMapping
    public ResponseEntity<AssignmentResponseDto> createAssignmentResponse(
            @PathVariable UUID topicId,
            @Valid @RequestBody CreateAssignmentResponseRequest request) {

        AssignmentResponseDto response = assignmentResponseService.createAssignmentResponse(topicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AssignmentResponseDto>> getAllAssignmentResponsesByTopicId(
            @PathVariable UUID topicId) {

        List<AssignmentResponseDto> responses = assignmentResponseService.getAllAssignmentResponsesByTopicId(topicId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponseDto> getAssignmentResponseById(
            @PathVariable("id") UUID responseId) {

        AssignmentResponseDto response = assignmentResponseService.getAssignmentResponseById(responseId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponseDto> updateAssignmentResponseById(
            @PathVariable("id") UUID responseId,
            @PathVariable("topicId") UUID topicId,
            @Valid @RequestBody UpdateAssignmentResponseRequest request) {

        AssignmentResponseDto response = assignmentResponseService.updateAssignmentResponseById(responseId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignmentResponseById(
            @PathVariable("id") UUID responseId) {

        assignmentResponseService.deleteResponse(responseId);
        return ResponseEntity.noContent().build();
    }
}
