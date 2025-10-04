package lms.coursehub.helpers.mapstructs;

import lms.coursehub.helpers.utils.MappingUtils;
import lms.coursehub.models.dtos.section.CreateSectionRequest;
import lms.coursehub.models.dtos.section.SectionResponseDto;
import lms.coursehub.models.entities.Section;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = MappingUtils.class)
public interface SectionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "topics", ignore = true)
    Section toEntity(CreateSectionRequest request);

    @Mapping(source = "course.id", target = "courseId")
    SectionResponseDto toResponseDto(Section section);
}
