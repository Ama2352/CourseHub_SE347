package lms.coursehub.models.dtos.assignment;

import lms.coursehub.models.dtos.topic.CloudinaryFileDto;
import lms.coursehub.models.dtos.user.UserResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AssignmentResponseDto {
    private UUID id;
    private UUID topicId;
    private UserResponseDto student;
    private LocalDateTime submittedAt;
    private String note;
    private BigDecimal mark;
    private LocalDateTime gradedAt;
    private UserResponseDto gradedBy;
    private List<CloudinaryFileDto> assignmentFiles;
}
