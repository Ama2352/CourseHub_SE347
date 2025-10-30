package lms.coursehub.models.dtos.quiz;

import lombok.Data;

@Data
public class CreateQuizResponseAnswerDto {
    private String questionId;
    private String answerText;
}