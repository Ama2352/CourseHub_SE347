package lms.coursehub.models.dtos.section;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore extra fields like 'id', 'courseId', 'topics'
public class UpdateSectionRequest {
    private String title;
    private String description;
    private Integer position;
}
