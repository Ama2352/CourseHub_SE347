package lms.coursehub.models.dtos.topic;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AssignmentDataDto {
    private String description;
    private LocalDateTime open;
    private LocalDateTime close;
    private String remindToGrade;
    private Integer maximumFile;
    private List<CloudinaryFileDto> assignmentFiles;
}