package lms.coursehub.models.dtos.topic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTopicRequest {
    @NotNull(message = "Section ID is required")
    private UUID sectionId;

    @NotBlank(message = "Topic title is required")
    private String title;

    @NotBlank(message = "Topic type is required")
    private String type;

    private String data; // JSON string containing type-specific data
}
