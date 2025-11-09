package lms.coursehub.models.dtos.reports;

import lms.coursehub.models.dtos.user.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllAssignmentsReportDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentInfoWithAverageMark {
        private UserResponseDto student;
        private Double averageMark;
        private Boolean submitted;
    }

    private Number assignmentsCountInProgress = 0;
    private Number assignmentCount = 0;

    private Double avgMark = 0.0;
    private Double avgCompletionRate = 0.0;
    private Number numberOfAssignmentEndsAtThisMonth = 0;
    private LocalDateTime closestNextEndAssignment = null;

    private Map<Number, Number> markDistributionCount = new HashMap<>();
    private List<StudentInfoWithAverageMark> studentInfoWithMarkAverage = new ArrayList<>();
    private List<StudentInfoWithAverageMark> studentWithMarkOver8 = new ArrayList<>();
    private List<StudentInfoWithAverageMark> studentWithMarkOver5 = new ArrayList<>();
    private List<StudentInfoWithAverageMark> studentWithMarkOver2 = new ArrayList<>();
    private List<StudentInfoWithAverageMark> studentWithMarkOver0 = new ArrayList<>();
    private List<StudentInfoWithAverageMark> studentWithNoResponse = new ArrayList<>();

    private Map<String, Long> fileTypeCount = new HashMap<>();
    private List<SingleAssignmentReportDto> singleAssignmentReports = new ArrayList<>();
}
