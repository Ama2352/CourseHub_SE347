package lms.coursehub.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lms.coursehub.models.dtos.course.CreateCourseRequest;
import lms.coursehub.models.dtos.course.CourseResponseDto;
import lms.coursehub.models.dtos.course.UpdateCourseRequest;
import lms.coursehub.models.dtos.topic.TopicResponseDto;

import lms.coursehub.services.CourseService;
import lms.coursehub.services.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
@Tag(name = "Course Management")
public class CourseController {

    private final CourseService courseService;
    private final TopicService topicService;

    @PostMapping
    public ResponseEntity<Void> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable String courseId) {
        CourseResponseDto response = courseService.getCourseById(courseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CourseResponseDto>> getCourses(@RequestParam(required = false) String userId) {
        List<CourseResponseDto> responses;
        if (userId != null) {
            responses = courseService.getTeacherCourses(UUID.fromString(userId));
        } else {
            responses = courseService.getPublicCourses();
        }
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<Void> updateCourse(
            @PathVariable String courseId,
            @Valid @RequestBody UpdateCourseRequest request) {
        courseService.updateCourse(courseId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{courseId}/join")
    public ResponseEntity<Void> joinCourse(@PathVariable String courseId) {
        courseService.addUserToCourse(courseId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{courseId}/work")
    public ResponseEntity<List<TopicResponseDto>> getCourseWork(
            @PathVariable String courseId,
            @RequestParam(required = false) String type) {
        List<TopicResponseDto> responses;

        if (type != null) {
            switch (type.toLowerCase()) {
                case "quiz":
                    responses = topicService.getAllQuizzesOfCourse(courseId);
                    break;
                case "assignment":
                    responses = topicService.getAllAssignmentsOfCourse(courseId);
                    break;
                case "meeting":
                    responses = topicService.getAllMeetingsOfCourse(courseId);
                    break;
                default:
                    responses = topicService.getAllWorksOfCourse(courseId);
                    break;
            }
        } else {
            // Return all work types (quizzes, assignments, meetings)
            responses = topicService.getAllWorksOfCourse(courseId);
        }

        return ResponseEntity.ok(responses);
    }
}