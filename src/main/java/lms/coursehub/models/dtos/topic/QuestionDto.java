package lms.coursehub.models.dtos.topic;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class QuestionDto {
    private UUID id;
    private String questionName;
    private String questionText;
    private String status;
    private String type;
    private BigDecimal defaultMark;
    private Long usage;

    // For True/False questions
    private Boolean correctAnswer;
    private String feedbackOfTrue;
    private String feedbackOfFalse;

    // For Choice questions
    private Boolean multiple;
    private List<QuestionChoiceDto> choices;
}

@Getter
@Setter
class QuestionChoiceDto {
    private UUID id;
    private String text;
    private BigDecimal gradePercent;
    private String feedback;
}