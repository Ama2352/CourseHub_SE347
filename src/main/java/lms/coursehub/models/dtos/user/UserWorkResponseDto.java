package lms.coursehub.models.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Response object containing user work/assignment details")
public class UserWorkResponseDto {

    @Schema(description = "Unique identifier of the topic")
    private UUID topicId;

    @Schema(description = "Title of the topic")
    private String title;

    @Schema(description = "Type of the topic (quiz, assignment, meeting)")
    private String type;

    @Schema(description = "Course ID")
    private String courseId;

    @Schema(description = "Course title")
    private String courseTitle;

    @Schema(description = "Opening date/time")
    private LocalDateTime open;

    @Schema(description = "Closing date/time")
    private LocalDateTime close;

    @Schema(description = "Section title")
    private String sectionTitle;
}
