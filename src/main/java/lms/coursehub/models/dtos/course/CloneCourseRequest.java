package lms.coursehub.models.dtos.course;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request DTO for cloning a course
 * Matches .NET CloneCourseRequest structure
 */
@Getter
@Setter
public class CloneCourseRequest {
    @NotBlank(message = "New course ID is required")
    private String newCourseId;
    
    private String title; // Optional: if null, will use "{original title} (Copy)"
    private String description;
    private String imageUrl;
    private String category;
    private String level;
    private BigDecimal price;
    private Boolean isPublished; // Default to false if null
}
