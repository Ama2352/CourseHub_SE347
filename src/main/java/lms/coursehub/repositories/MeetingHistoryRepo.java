package lms.coursehub.repositories;

import lms.coursehub.models.entities.MeetingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MeetingHistoryRepo extends JpaRepository<MeetingHistory, UUID> {
    List<MeetingHistory> findByTopicId(UUID topicId);
}
