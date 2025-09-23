package lms.coursehub.repositories;

import lms.coursehub.models.entities.TopicQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TopicQuizRepo extends JpaRepository<TopicQuiz, UUID> {
}
