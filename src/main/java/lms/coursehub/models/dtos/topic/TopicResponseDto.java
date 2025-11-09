package lms.coursehub.models.dtos.topic;

import lms.coursehub.models.dtos.course.CourseResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TopicResponseDto {
    private UUID id;
    private String title;
    private String type;
    private UUID sectionId;
    private String data; // JSON string of topic-specific data
    private Integer studentCount; // Number of students who can access this topic
    private String response; // JSON string of student's response (for quiz/assignment)
    private CourseResponseDto course; // Optional: populated when needed
}