package lms.coursehub.models.dtos.question;

import lombok.Data;

@Data
public class QuestionResponseDto {
    private String id;
    private String topicQuizId;
    private String questionName;
    private String questionText;
    private String status;
    private QuestionTypeEnum type;
    private double defaultMark;
    private long usage;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String modifiedBy;
    private QuestionDataDto data;
}