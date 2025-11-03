package lms.coursehub.models.dtos.quiz;

import lombok.Data;
import java.util.UUID;

@Data
public class QuizResponseAnswerDto {
    private UUID id;
    private UUID questionId;
    private String answerText;
    private boolean isCorrect;
    private double points;
}