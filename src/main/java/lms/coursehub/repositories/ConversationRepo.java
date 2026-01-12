package lms.coursehub.repositories;

import lms.coursehub.models.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepo extends JpaRepository<Conversation, UUID> {
    
    @Query("SELECT c FROM Conversation c WHERE c.user1.id = :userId OR c.user2.id = :userId ORDER BY c.lastMessageAt DESC")
    List<Conversation> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT c FROM Conversation c WHERE (c.user1.id = :userId1 AND c.user2.id = :userId2) OR (c.user1.id = :userId2 AND c.user2.id = :userId1)")
    Optional<Conversation> findByBothUsers(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);
}
