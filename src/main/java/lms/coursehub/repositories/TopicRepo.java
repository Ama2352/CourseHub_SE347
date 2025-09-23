package lms.coursehub.repositories;

import lms.coursehub.models.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TopicRepo extends JpaRepository<Topic, UUID> {
}
