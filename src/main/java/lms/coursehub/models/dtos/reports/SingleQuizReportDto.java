package lms.coursehub.models.dtos.reports;

import lms.coursehub.models.dtos.user.UserResponseDto;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for quiz analytics and reporting
 */
@Data
public class SingleQuizReportDto {
    private String name; // Topic title
    private List<UserResponseDto> students; // All students who could take the quiz

    // Student performance categorized by marks (base 10)
    private List<StudentInfoAndMark> studentWithMark; // All students with their marks
    private List<StudentInfoAndMark> studentWithMarkOver8;
    private List<StudentInfoAndMark> studentWithMarkOver5; // 5-7.99
    private List<StudentInfoAndMark> studentWithMarkOver2; // 2-4.99
    private List<StudentInfoAndMark> studentWithMarkOver0; // 0-1.99
    private List<StudentInfoAndMark> studentWithNoResponse;

    // Statistics
    private Map<Integer, Long> markDistributionCount; // Keys: 8, 5, 2, 0, -1 (no response)
    private Integer questionCount;
    private Double maxDefaultMark; // Total possible marks
    private Double avgStudentMarkBase10;
    private Double maxStudentMarkBase10;
    private Double minStudentMarkBase10;
    private Long attemptCount; // Total number of attempts
    private Double avgTimeSpend; // Average time spent in seconds
    private Double completionRate; // Percentage of students who completed
    private Long trueFalseQuestionCount;
    private Long multipleChoiceQuestionCount;
    private Long shortAnswerQuestionCount;

    @Data
    public static class StudentInfoAndMark {
        private UserResponseDto student;
        private Double mark; // Mark on base 10 scale
        private UUID responseId;
        private Boolean submitted = true; // false if student didn't respond
    }
}
