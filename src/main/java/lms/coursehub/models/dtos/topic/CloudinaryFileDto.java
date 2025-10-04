package lms.coursehub.models.dtos.topic;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CloudinaryFileDto {
    private UUID id;
    private String name;
    private String displayUrl;
    private String downloadUrl;
}