package lms.coursehub.models.dtos.topic;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class QuizDataDto {
    private String description;
    private LocalDateTime open;
    private LocalDateTime close;
    private Integer timeLimit;
    private String timeLimitUnit;
    private BigDecimal gradeToPass;
    private String gradingMethod;
    private String attemptAllowed;
    private List<QuestionDto> questions;
}