package lms.coursehub.models.dtos.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetMessageResponse {
    
    private UUID id;
    private UUID senderId;
    private String senderUsername;
    private String senderAvatarUrl;
    private String content;
    private LocalDateTime sentAt;
    private UUID conversationId;
}
