package lms.coursehub.models.dtos.course;

import lms.coursehub.models.dtos.section.SectionResponseDto;
import lms.coursehub.models.dtos.user.UserResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CourseResponseDto {
    private String id;
    private String title;
    private String description;
    private int totalJoined;
    private String imageUrl;
    private BigDecimal price;
    private String category;
    private String level;
    private Boolean isPublished;
    private UserResponseDto creator;
    private List<UserResponseDto> students;
    private List<SectionResponseDto> sections;
}