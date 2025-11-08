package lms.coursehub.models.dtos.assignment;

import jakarta.validation.constraints.NotNull;
import lms.coursehub.models.dtos.topic.CloudinaryFileDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateAssignmentResponseRequest {
    @NotNull(message = "Note is required")
    private String note;

    private List<CloudinaryFileDto> assignmentFiles;
}
