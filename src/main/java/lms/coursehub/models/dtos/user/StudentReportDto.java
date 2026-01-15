package lms.coursehub.models.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Student performance report DTO
 * Matches .NET StudentReportDTO structure
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentReportDto {
    // Quiz statistics
    private double totalQuizCount;
    private double completedQuizCount;
    private double averageQuizScore;
    
    // Assignment statistics
    private double totalAssignmentCount;
    private double submittedAssignmentCount;
    private int lateSubmissions;
    
    // Overall metrics
    private double completionRate; // Percentage of total work completed
    private String courseId;
    private String courseName;
    private LocalDateTime reportGeneratedAt;
}
