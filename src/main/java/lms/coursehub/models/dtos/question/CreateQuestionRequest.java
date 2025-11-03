package lms.coursehub.models.dtos.question;

import lombok.Data;

@Data
public class CreateQuestionRequest {
    private String topicQuizId;
    private String questionName;
    private String questionText;
    private String status;
    private QuestionTypeEnum type;
    private double defaultMark;
    private QuestionDataRequest data;
}

