package lms.coursehub.repositories;

import lms.coursehub.models.entities.TopicAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TopicAssignmentRepo extends JpaRepository<TopicAssignment, UUID> {
}
