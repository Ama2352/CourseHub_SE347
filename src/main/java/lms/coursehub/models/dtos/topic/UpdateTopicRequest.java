package lms.coursehub.models.dtos.topic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateTopicRequest {
    private String title;
    private String type;
    private String data;
}
