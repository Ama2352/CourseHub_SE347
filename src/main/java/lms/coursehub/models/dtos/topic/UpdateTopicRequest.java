package lms.coursehub.models.dtos.topic;

import lombok.Getter;

@Getter
public class UpdateTopicRequest {
    private String title;
    private String type;
    private String data;
}
