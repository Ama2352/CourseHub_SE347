package lms.coursehub.models.dtos.comment;

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
@Schema(description = "Request payload for creating a new comment")
public class CreateCommentRequest {

    @Schema(
            description = "The text content of the comment",
            example = "This is a great topic! Thanks for sharing.",
            required = true
    )
    @NotBlank(message = "Comment text cannot be blank")
    private String text;
}
