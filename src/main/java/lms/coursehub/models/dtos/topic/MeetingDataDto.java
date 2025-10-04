package lms.coursehub.models.dtos.topic;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MeetingDataDto {
    private String description;
    private LocalDateTime open;
    private LocalDateTime close;
}