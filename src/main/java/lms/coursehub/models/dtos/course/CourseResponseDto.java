package lms.coursehub.models.dtos.course;

import lms.coursehub.models.dtos.section.SectionResponseDto;
import lms.coursehub.models.dtos.user.UserBasicInfo;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CourseResponseDto {
    private String id;
    private UUID creatorId; // Added to match .NET
    private String title;
    private String description;
    private int totalJoined;
    private String imageUrl;
    private BigDecimal price;
    private String category;
    private String level;
    private boolean isPublished;
    private UserBasicInfo creator; // Changed from UserResponseDto to UserBasicInfo to match .NET
    private List<UserBasicInfo> students; // Changed from UserResponseDto to UserBasicInfo to match .NET
    private List<SectionResponseDto> sections;
}