package lms.coursehub.models.dtos.topic;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TopicResponseDto {
    private UUID id;
    private String title;
    private String type;
    private UUID sectionId;
    private Object data; // Parsed data object specific to topic type
}