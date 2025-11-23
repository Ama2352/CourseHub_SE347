package lms.coursehub.repositories;

import lms.coursehub.models.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepo extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserIdOrderByTimestampDesc(UUID userId);
}
