package lms.coursehub.models.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lms.coursehub.models.enums.UserRole;
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
@Schema(description = "Response object containing user details")
public class UserResponseDto {

    @Schema(
            description = "Unique identifier of the user",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID id;

    @Schema(
            description = "Email address of the user",
            example = "john.doe@example.com"
    )
    private String email;

    @Schema(
            description = "Username of the user",
            example = "johndoe123"
    )
    private String username;

    @Schema(
            description = "Avatar URL of the user",
            example = "https://example.com/avatar.jpg"
    )
    private String avatarUrl;

    @Schema(
            description = "Role of the user",
            example = "STUDENT"
    )
    private UserRole role;

    @Schema(
            description = "Timestamp when the user was created",
            example = "2025-10-14T10:30:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Timestamp when the user was last updated",
            example = "2025-10-14T10:30:00"
    )
    private LocalDateTime updatedAt;
}
