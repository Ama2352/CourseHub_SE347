package lms.coursehub.helpers.mapstructs;

import lms.coursehub.helpers.utils.MappingUtils;
import lms.coursehub.models.dtos.assignment.AssignmentResponseDto;
import lms.coursehub.models.dtos.assignment.CreateAssignmentResponseRequest;
import lms.coursehub.models.entities.AssignmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { MappingUtils.class, UserMapper.class })
public interface AssignmentResponseMapper {

    @Mapping(target = "topicId", source = "topicAssignment.id")
    @Mapping(target = "gradedBy", source = "grader")
    @Mapping(target = "student", source = "student")
    AssignmentResponseDto toDto(AssignmentResponse assignmentResponse);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "topicAssignment", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "mark", ignore = true)
    @Mapping(target = "gradedAt", ignore = true)
    @Mapping(target = "grader", ignore = true)
    AssignmentResponse toEntity(CreateAssignmentResponseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "topicAssignment", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "mark", ignore = true)
    @Mapping(target = "gradedAt", ignore = true)
    @Mapping(target = "grader", ignore = true)
    void updateEntityFromDto(CreateAssignmentResponseRequest request,
            @MappingTarget AssignmentResponse assignmentResponse);
}
