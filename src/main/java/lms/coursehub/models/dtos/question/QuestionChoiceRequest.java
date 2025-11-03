package lms.coursehub.models.dtos.question;

import lombok.Data;

@Data
public class QuestionChoiceRequest {
    private String text;
    private boolean isCorrect;
    private double grade;
}