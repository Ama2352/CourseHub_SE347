package lms.coursehub.models.dtos.question;

import lombok.Data;

@Data
public class QuestionDataRequest {
    // For CHOICE and SHORT_ANSWER
    private boolean multiple;
    private QuestionChoiceRequest[] choices;
    
    // For TRUE_FALSE
    private boolean correctAnswer;
    private String feedbackOfTrue;
    private String feedbackOfFalse;
}