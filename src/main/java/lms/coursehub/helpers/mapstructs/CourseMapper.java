package lms.coursehub.helpers.mapstructs;

import lms.coursehub.helpers.utils.MappingUtils;
import lms.coursehub.models.dtos.course.CreateCourseRequest;
import lms.coursehub.models.dtos.course.CourseResponseDto;
import lms.coursehub.models.entities.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = MappingUtils.class)
public interface CourseMapper {

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "enrollmentDetails", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "totalJoined", ignore = true)
    Course toEntity(CreateCourseRequest request);

    @Mapping(source = "creator.id", target = "creatorId")
    CourseResponseDto toResponseDto(Course course);
}
