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
@Schema(description = "Request payload for updating user password")
public class UpdatePasswordRequest {

    @Schema(
            description = "Current password of the user",
            example = "oldPassword123"
    )
    @NotBlank(message = "Old password cannot be blank")
    private String oldPassword;

    @Schema(
            description = "New password for the user",
            example = "newPassword456"
    )
    @NotBlank(message = "New password cannot be blank")
    private String newPassword;
}
