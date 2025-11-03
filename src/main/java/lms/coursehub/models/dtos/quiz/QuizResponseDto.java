package lms.coursehub.models.dtos.quiz;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class QuizResponseDto {
    private UUID id;
    private UUID studentId;
    private UUID topicId;
    private double totalPoints;
    private String status;
    private List<QuizResponseAnswerDto> answers;
    private String startTime;
    private String submitTime;
}