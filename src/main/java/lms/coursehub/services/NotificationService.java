package lms.coursehub.services;

import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.helpers.mapstructs.NotificationMapper;
import lms.coursehub.models.dtos.notification.NotificationDto;
import lms.coursehub.models.entities.Notification;
import lms.coursehub.models.entities.User;
import lms.coursehub.repositories.NotificationRepo;
import lms.coursehub.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(UUID userId) {
        // verify user exists
        userRepo.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        List<Notification> list = notificationRepo.findByUserIdOrderByTimestampDesc(userId);

        return list.stream().map(notificationMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public NotificationDto markAsRead(UUID id, boolean isRead) {
        Notification notification = notificationRepo.findById(id)
                .orElseThrow(() -> new CustomException("Notification not found", HttpStatus.NOT_FOUND));

        notification.setIsRead(isRead);
        Notification saved = notificationRepo.save(notification);
        return notificationMapper.toDto(saved);
    }

    @Transactional
    public void deleteNotification(UUID id) {
        Notification notification = notificationRepo.findById(id)
                .orElseThrow(() -> new CustomException("Notification not found", HttpStatus.NOT_FOUND));

        notificationRepo.delete(notification);
    }

    @Transactional
    public NotificationDto createNotification(UUID userId, String title, String message) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);

        Notification saved = notificationRepo.save(notification);
        return notificationMapper.toDto(saved);
    }

    // Convenience hook for other services to call when events happen
    public void notifyUser(UUID userId, String title, String message) {
        createNotification(userId, title, message);
    }
}
