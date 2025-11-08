package lms.coursehub.models.dtos.section;

import lms.coursehub.models.dtos.topic.TopicResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SectionResponseDto {
    private UUID id;
    private int position;
    private String title;
    private String description;
    private String courseId;
    private List<TopicResponseDto> topics;
}