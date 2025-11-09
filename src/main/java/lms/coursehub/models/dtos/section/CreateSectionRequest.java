package lms.coursehub.models.dtos.section;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSectionRequest {
    private String courseId;
    private String title;
    private String description;
    private int position;
}
