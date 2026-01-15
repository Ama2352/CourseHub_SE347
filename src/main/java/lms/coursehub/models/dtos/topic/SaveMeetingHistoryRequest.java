package lms.coursehub.models.dtos.topic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for saving meeting history
 * Matches .NET SaveMeetingHistoryRequest structure
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SaveMeetingHistoryRequest {
    private List<ParticipantInfo> participants;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration; // in seconds
    
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ParticipantInfo {
        private UUID userId;
        private String username;
        private Integer joinDuration; // in seconds
        private LocalDateTime joinedAt;
        private LocalDateTime leftAt;
    }
}
