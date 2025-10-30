package lms.coursehub.models.dtos.question;

import lombok.Data;

@Data
public class QuestionChoiceDto {
    private String id;
    private String text;
    private boolean isCorrect;
    private double grade;
}