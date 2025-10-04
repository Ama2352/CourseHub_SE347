package lms.coursehub.models.dtos.section;

import lombok.Getter;

@Getter
public class CreateSectionRequest {
    private String courseId;
    private String title;
    private String description;
    private int position;
}
