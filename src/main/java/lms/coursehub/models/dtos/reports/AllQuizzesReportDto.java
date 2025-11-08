package lms.coursehub.models.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllQuizzesReportDto {
    private Number quizCount;
    private Double avgCompletionPercentage;

    private Number minQuestionCount;
    private Number maxQuestionCount;
    private Double minStudentScoreBase10;
    private Double maxStudentScoreBase10;

    private List<SingleQuizReportDto.StudentInfoAndMark> studentInfoWithMarkAverage;

    private List<SingleQuizReportDto.StudentInfoAndMark> studentWithMarkOver8;
    private List<SingleQuizReportDto.StudentInfoAndMark> studentWithMarkOver5;
    private List<SingleQuizReportDto.StudentInfoAndMark> studentWithMarkOver2;
    private List<SingleQuizReportDto.StudentInfoAndMark> studentWithMarkOver0;
    private List<SingleQuizReportDto.StudentInfoAndMark> studentWithNoResponse;

    private Map<Number, Number> markDistributionCount;

    private List<SingleQuizReportDto> singleQuizReports;

    private Number trueFalseQuestionCount;
    private Number multipleChoiceQuestionCount;
    private Number shortAnswerQuestionCount;
}
