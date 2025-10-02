package lms.coursehub.models.dtos.course;

import lombok.Getter;

@Getter
public class CreateCourseRequest {
    private String title;
    private String description;
    private String imageUrl;
    private String category;
    private String level;
    private boolean isPublished;
}
