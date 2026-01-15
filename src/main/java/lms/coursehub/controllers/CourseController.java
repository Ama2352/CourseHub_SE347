package lms.coursehub.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.models.dtos.course.CloneCourseRequest;
import lms.coursehub.models.dtos.course.CloneCourseResponse;
import lms.coursehub.models.dtos.course.CreateCourseRequest;
import lms.coursehub.models.dtos.course.CourseResponseDto;
import lms.coursehub.models.dtos.course.UpdateCourseRequest;
import lms.coursehub.models.dtos.reports.AllAssignmentsReportDto;
import lms.coursehub.models.dtos.reports.AllQuizzesReportDto;
import lms.coursehub.models.dtos.topic.TopicResponseDto;
import lms.coursehub.models.entities.Course;
import lms.coursehub.models.entities.User;
import lms.coursehub.services.CourseService;
import lms.coursehub.services.TopicService;
import lms.coursehub.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
@Tag(name = "Course Management")
public class CourseController {

    private final CourseService courseService;
    private final TopicService topicService;
    private final UserService userService;
    
    @Value("${livekit.api-key:devkey}")
    private String liveKitApiKey;
    
    @Value("${livekit.api-secret:thisisaverylongsecretstring1234567890}")
    private String liveKitApiSecret;
    
    @Value("${livekit.ws-url:ws://45.128.222.24:7880}")
    private String liveKitWsUrl;
    private final TopicService topicService;

    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseResponseDto response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<CourseResponseDto> updateCourse(
            @PathVariable String courseId,
            @Valid @RequestBody UpdateCourseRequest request) {
        CourseResponseDto response = courseService.updateCourse(courseId, request);
        return ResponseEntity.ok(response);
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

    @GetMapping("/{courseId}/quiz-report")
    public ResponseEntity<AllQuizzesReportDto> getAllQuizzesReport(
            @PathVariable String courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        AllQuizzesReportDto report = topicService.getAllQuizzesReport(courseId, start, end);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/{courseId}/assignment-report")
    public ResponseEntity<AllAssignmentsReportDto> getAllAssignmentsReport(
            @PathVariable String courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        AllAssignmentsReportDto report = topicService.getAllAssignmentsReport(courseId, start, end);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/{courseId}/meeting/{topicId}/token")
    public ResponseEntity<Map<String, String>> getMeetingToken(
            @PathVariable String courseId,
            @PathVariable UUID topicId) {
        
        User currentUser = userService.getCurrentUser();
        Course course = courseService.findCourseById(courseId);
        
        // Determine role
        boolean isTeacher = course.getCreator().getId().equals(currentUser.getId());
        String role = isTeacher ? "teacher" : "student";
        
        // Build metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", currentUser.getId().toString());
        metadata.put("role", role);
        metadata.put("courseId", courseId);
        metadata.put("topicId", topicId.toString());
        metadata.put("avatarUrl", currentUser.getAvatarUrl() != null ? currentUser.getAvatarUrl() : "");
        
        // Generate token
        String token = createLiveKitToken(
            currentUser.getId().toString(),
            currentUser.getUsername(),
            topicId.toString(),
            metadata
        );
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("roomName", topicId.toString());
        response.put("wsUrl", liveKitWsUrl);
        response.put("role", role);
        response.put("avatarUrl", currentUser.getAvatarUrl() != null ? currentUser.getAvatarUrl() : "");
        response.put("name", currentUser.getUsername());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{courseId}/clone")
    public ResponseEntity<CloneCourseResponse> cloneCourse(
            @PathVariable String courseId,
            @Valid @RequestBody CloneCourseRequest request) {
        CloneCourseResponse response = courseService.cloneCourse(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String createLiveKitToken(String userId, String username, String roomName, 
                                      Map<String, String> metadata) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String metadataJson = mapper.writeValueAsString(metadata);
            
            Map<String, Object> videoClaim = new HashMap<>();
            videoClaim.put("room", roomName);
            videoClaim.put("roomJoin", true);
            videoClaim.put("canPublish", true);
            videoClaim.put("canSubscribe", true);
            
            return Jwts.builder()
                .claim("video", videoClaim)
                .claim("metadata", metadataJson)
                .claim("name", username)
                .subject(userId)
                .issuer(liveKitApiKey)
                .expiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 hours
                .issuedAt(new Date())
                .signWith(Keys.hmacShaKeyFor(liveKitApiSecret.getBytes()))
                .compact();
        } catch (Exception e) {
            throw new CustomException("Failed to generate meeting token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}