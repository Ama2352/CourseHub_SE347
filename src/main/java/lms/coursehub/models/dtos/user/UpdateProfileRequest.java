package lms.coursehub.models.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Request payload for updating user profile")
public class UpdateProfileRequest {

    @Schema(
            description = "Username of the user",
            example = "johndoe123"
    )
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Schema(
            description = "Avatar URL of the user",
            example = "https://example.com/avatar.jpg"
    )
    private String avatar;
}
