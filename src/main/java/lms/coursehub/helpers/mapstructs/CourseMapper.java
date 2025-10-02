package lms.coursehub.helpers.mapstructs;

import lms.coursehub.helpers.utils.MappingUtils;
import lms.coursehub.models.dtos.course.CreateCourseRequest;
import lms.coursehub.models.entities.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = MappingUtils.class)
public interface CourseMapper {

    Course toEntity(CreateCourseRequest request);
}
