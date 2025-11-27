package lms.coursehub.models.dtos.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private UUID id;
    private String title;
    private String message;
    private LocalDateTime timestamp;
    private Boolean isRead;
}
