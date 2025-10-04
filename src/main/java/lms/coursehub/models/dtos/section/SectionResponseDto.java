package lms.coursehub.models.dtos.section;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SectionResponseDto {
    private UUID id;
    private int position;
    private String title;
    private String description;
    private String courseId;
}