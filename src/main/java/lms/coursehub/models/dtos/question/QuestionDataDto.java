package lms.coursehub.models.dtos.question;

import lombok.Data;

@Data
public class QuestionDataDto {
    // For CHOICE
    private boolean multiple;
    private QuestionChoiceDto[] choices;
    
    // For TRUE_FALSE
    private boolean correctAnswer;
    private String feedbackOfTrue;
    private String feedbackOfFalse;
}
