package lms.coursehub.repositories;

import lms.coursehub.models.entities.QuizResponseAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface QuizResponseAnswerRepo extends JpaRepository<QuizResponseAnswer, UUID> {
}
