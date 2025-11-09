package lms.coursehub.helpers.mapstructs;

import lms.coursehub.helpers.utils.MappingUtils;
import lms.coursehub.models.dtos.course.CreateCourseRequest;
import lms.coursehub.models.dtos.course.CourseResponseDto;
import lms.coursehub.models.dtos.user.UserResponseDto;
import lms.coursehub.models.entities.Course;
import lms.coursehub.models.entities.EnrollmentDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = { MappingUtils.class, SectionMapper.class, UserMapper.class })
public interface CourseMapper {

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "enrollmentDetails", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "totalJoined", ignore = true)
    Course toEntity(CreateCourseRequest request);

    @Mapping(source = "creator", target = "creator")
    @Mapping(source = "enrollmentDetails", target = "students", qualifiedByName = "enrollmentDetailsToStudents")
    @Mapping(source = "sections", target = "sections")
    CourseResponseDto toResponseDto(Course course);

    @Named("enrollmentDetailsToStudents")
    default List<UserResponseDto> enrollmentDetailsToStudents(List<EnrollmentDetail> enrollmentDetails) {
        if (enrollmentDetails == null) {
            return List.of();
        }
        return enrollmentDetails.stream()
                .map(EnrollmentDetail::getStudent)
                .map(student -> {
                    UserResponseDto dto = new UserResponseDto();
                    dto.setId(student.getId());
                    dto.setEmail(student.getEmail());
                    dto.setUsername(student.getUsername());
                    dto.setAvatarUrl(student.getAvatarUrl());
                    dto.setRole(student.getRole());
                    dto.setCreatedAt(student.getCreatedAt());
                    dto.setUpdatedAt(student.getUpdatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
