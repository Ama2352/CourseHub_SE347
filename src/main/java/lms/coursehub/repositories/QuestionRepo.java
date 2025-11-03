package lms.coursehub.repositories;

import lms.coursehub.models.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface QuestionRepo extends JpaRepository<Question, UUID> {
}