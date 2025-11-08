package lms.coursehub.models.dtos.reports;

import lms.coursehub.models.dtos.user.UserResponseDto;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for assignment analytics and reporting
 */
@Data
public class SingleAssignmentReportDto {
    private String name; // Topic title
    private List<UserResponseDto> students; // All students who could submit

    // Student performance categorized by marks (base 10)
    private List<StudentInfoAndMark> studentMarks; // All students with their marks
    private List<StudentInfoAndMark> studentWithMarkOver8;
    private List<StudentInfoAndMark> studentWithMarkOver5; // 5-7.99
    private List<StudentInfoAndMark> studentWithMarkOver2; // 2-4.99
    private List<StudentInfoAndMark> studentWithMarkOver0; // 0-1.99
    private List<StudentInfoAndMark> studentWithNoResponse;

    // Statistics
    private Map<Integer, Long> markDistributionCount; // Keys: 8, 5, 2, 0, -1 (no response)
    private Long submissionCount; // Total number of submissions
    private Long gradedSubmissionCount; // Number of graded submissions
    private Integer fileCount; // Total files submitted
    private Double avgMark; // Average mark (in original scale, e.g., 0-100)
    private Double maxMark; // Maximum mark achieved
    private Double completionRate; // Percentage of students who submitted
    private Map<String, Long> fileTypeCount; // Count by file extension

    @Data
    public static class StudentInfoAndMark {
        private UserResponseDto student;
        private Double mark; // Mark on base 10 scale
        private UUID responseId;
        private Boolean submitted = true; // false if student didn't submit
    }
}
