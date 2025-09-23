package lms.coursehub.repositories;

import lms.coursehub.models.entities.QuestionChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface QuestionChoiceRepo extends JpaRepository<QuestionChoice, UUID> {
}
