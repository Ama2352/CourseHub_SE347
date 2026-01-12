package lms.coursehub.models.dtos.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    
    private UUID id;
    private UUID otherUserId;
    private String otherUserUsername;
    private String otherUserAvatarUrl;
    private LocalDateTime lastMessageAt;
}
