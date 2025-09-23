package lms.coursehub.repositories;

import lms.coursehub.models.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MessageRepo extends JpaRepository<Message, UUID> {
}
