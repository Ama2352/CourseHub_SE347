package lms.coursehub.repositories;

import lms.coursehub.models.entities.TopicMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TopicMeetingRepo extends JpaRepository<TopicMeeting, UUID> {
}
