package lms.coursehub.repositories;

import lms.coursehub.models.entities.TopicLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TopicLinkRepo extends JpaRepository<TopicLink, UUID> {
}
