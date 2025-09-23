package lms.coursehub.repositories;

import lms.coursehub.models.entities.TopicFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TopicFileRepo extends JpaRepository<TopicFile, UUID> {
}
