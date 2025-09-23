package lms.coursehub.repositories;

import lms.coursehub.models.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ConversationRepo extends JpaRepository<Conversation, UUID> {
}
