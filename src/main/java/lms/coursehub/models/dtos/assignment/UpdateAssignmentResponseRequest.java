package lms.coursehub.models.dtos.assignment;

import lms.coursehub.models.dtos.topic.CloudinaryFileDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateAssignmentResponseRequest {
    private String note;
    private List<CloudinaryFileDto> assignmentFiles;
}
