package lms.coursehub.repositories;

import lms.coursehub.models.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CommentRepo extends JpaRepository<Comment, UUID> {
    List<Comment> findByTopicIdOrderByCreatedAtDesc(UUID topicId);
}
