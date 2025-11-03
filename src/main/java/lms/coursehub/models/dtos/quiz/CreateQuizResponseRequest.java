package lms.coursehub.models.dtos.quiz;

import lombok.Data;
import java.util.List;

@Data
public class CreateQuizResponseRequest {
    private List<CreateQuizResponseAnswerDto> answers;
}