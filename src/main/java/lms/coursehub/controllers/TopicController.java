package lms.coursehub.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lms.coursehub.models.dtos.reports.SingleAssignmentReportDto;
import lms.coursehub.models.dtos.reports.SingleQuizReportDto;
import lms.coursehub.models.dtos.topic.CreateTopicRequest;
import lms.coursehub.models.dtos.topic.SaveMeetingHistoryRequest;
import lms.coursehub.models.dtos.topic.TopicResponseDto;
import lms.coursehub.models.dtos.topic.UpdateTopicRequest;
import lms.coursehub.services.TopicService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course/{courseId}/topic")
@Tag(name = "Topic Management")
public class TopicController {

    private final TopicService topicService;

    @PostMapping
    public ResponseEntity<TopicResponseDto> createTopic(
            @PathVariable String courseId,
            @Valid @RequestBody CreateTopicRequest request) {

        TopicResponseDto response = topicService.createTopic(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<TopicResponseDto> getTopic(
            @PathVariable String courseId,
            @PathVariable UUID topicId) {

        TopicResponseDto response = topicService.getTopic(courseId, topicId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{topicId}")
    public ResponseEntity<TopicResponseDto> updateTopic(
            @PathVariable String courseId,
            @PathVariable UUID topicId,
            @Valid @RequestBody UpdateTopicRequest request) {

        TopicResponseDto response = topicService.updateTopic(courseId, topicId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{topicId}")
    public ResponseEntity<Void> deleteTopic(
            @PathVariable String courseId,
            @PathVariable UUID topicId) {

        topicService.deleteTopic(courseId, topicId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{topicId}/quiz-report")
    public ResponseEntity<SingleQuizReportDto> getQuizReport(
            @PathVariable String courseId,
            @PathVariable UUID topicId) {

        SingleQuizReportDto report = topicService.getSingleQuizReport(courseId, topicId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/{topicId}/assignment-report")
    public ResponseEntity<SingleAssignmentReportDto> getAssignmentReport(
            @PathVariable String courseId,
            @PathVariable UUID topicId) {

        SingleAssignmentReportDto report = topicService.getSingleAssignmentReport(courseId, topicId);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/{topicId}/meeting-history")
    public ResponseEntity<Map<String, String>> saveMeetingHistory(
            @PathVariable String courseId,
            @PathVariable UUID topicId,
            @Valid @RequestBody SaveMeetingHistoryRequest request) {
        
        topicService.saveMeetingHistory(courseId, topicId, request);
        return ResponseEntity.ok(Map.of("message", "History saved successfully"));
    }
}

// Separate controller for user-specific endpoints
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/topics")
@Tag(name = "User Topic Management")
class UserTopicController {

    private final TopicService topicService;

    @GetMapping("/quizzes")
    public ResponseEntity<List<TopicResponseDto>> getAllQuizzesOfUser() {
        List<TopicResponseDto> responses = topicService.getAllQuizzesOfUser();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<TopicResponseDto>> getAllAssignmentsOfUser() {
        List<TopicResponseDto> responses = topicService.getAllAssignmentsOfUser();
        return ResponseEntity.ok(responses);
    }
}
