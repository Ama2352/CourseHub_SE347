package lms.coursehub.models.dtos.course;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCourseRequest {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String category;
    private String level;
    private Boolean isPublished;
}
