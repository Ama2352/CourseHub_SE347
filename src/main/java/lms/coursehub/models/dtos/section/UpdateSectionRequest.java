package lms.coursehub.models.dtos.section;

import lombok.Getter;

@Getter
public class UpdateSectionRequest {
    private String title;
    private String description;
    private Integer position;
}
