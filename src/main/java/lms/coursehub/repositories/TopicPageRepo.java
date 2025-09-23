package lms.coursehub.repositories;

import lms.coursehub.models.entities.TopicPage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TopicPageRepo extends JpaRepository<TopicPage, UUID> {
}
