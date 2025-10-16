package lms.coursehub.models.dtos.course;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UpdateCourseRequest {
    private String title;
    private BigDecimal price;
    private String category;
    private String level;
    private Boolean isPublished;
    private String imageUrl;
    private String description;
}
