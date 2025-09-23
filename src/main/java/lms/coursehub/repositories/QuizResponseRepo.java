package lms.coursehub.repositories;

import lms.coursehub.models.entities.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface QuizResponseRepo extends JpaRepository<QuizResponse, UUID> {
}
