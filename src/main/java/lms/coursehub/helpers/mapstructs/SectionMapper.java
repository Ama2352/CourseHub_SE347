package lms.coursehub.helpers.mapstructs;

import lms.coursehub.helpers.utils.MappingUtils;
import lms.coursehub.models.dtos.section.CreateSectionRequest;
import lms.coursehub.models.dtos.section.SectionResponseDto;
import lms.coursehub.models.dtos.topic.TopicResponseDto;
import lms.coursehub.models.entities.Section;
import lms.coursehub.models.entities.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = MappingUtils.class)
public interface SectionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "topics", ignore = true)
    Section toEntity(CreateSectionRequest request);

    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "topics", target = "topics", qualifiedByName = "mapTopicsWithoutCourse")
    SectionResponseDto toResponseDto(Section section);

    @Named("mapTopicsWithoutCourse")
    default List<TopicResponseDto> mapTopicsWithoutCourse(List<Topic> topics) {
        if (topics == null) {
            return List.of();
        }
        return topics.stream()
                .map(topic -> {
                    TopicResponseDto dto = new TopicResponseDto();
                    dto.setId(topic.getId());
                    dto.setTitle(topic.getTitle());
                    dto.setType(topic.getType());
                    dto.setSectionId(topic.getSection().getId());
                    dto.setCourse(null); // Avoid circular reference
                    dto.setData(null); // Data will be populated by TopicService when needed
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
