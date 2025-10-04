package lms.coursehub.models.dtos.course;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CourseResponseDto {
    private String id;
    private String title;
    private String description;
    private int totalJoined;
    private String imageUrl;
    private BigDecimal price;
    private String category;
    private String level;
    private boolean isPublished;
    private String creatorId;
}