package lms.coursehub.models.dtos.comment;

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
@Schema(description = "Response object containing comment details")
public class CommentResponseDto {

    @Schema(
            description = "Unique identifier of the comment",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID id;

    @Schema(
            description = "The text content of the comment",
            example = "This is a great topic! Thanks for sharing."
    )
    private String text;

    @Schema(
            description = "Timestamp when the comment was created",
            example = "2025-10-14T10:30:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Unique identifier of the user who created the comment",
            example = "123e4567-e89b-12d3-a456-426614174001"
    )
    private UUID userId;

    @Schema(
            description = "Name of the user who created the comment",
            example = "John Doe"
    )
    private String userName;

    @Schema(
            description = "Email of the user who created the comment",
            example = "john.doe@example.com"
    )
    private String userEmail;

    @Schema(
            description = "Unique identifier of the topic this comment belongs to",
            example = "123e4567-e89b-12d3-a456-426614174002"
    )
    private UUID topicId;
}
