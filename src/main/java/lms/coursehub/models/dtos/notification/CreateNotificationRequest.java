package lms.coursehub.models.dtos.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {
    private UUID userId;
    private String title;
    private String message;
}
