package lms.coursehub.models.dtos.course;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore extra fields like 'sections'
public class UpdateCourseRequest {
    private String title;
    private BigDecimal price;
    private String category;
    private String level;
    private Boolean isPublished;
    private String imageUrl;
    private String description;
}
