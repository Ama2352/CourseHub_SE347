package lms.coursehub.models.dtos.topic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileDataDto {
    private String description;
    private CloudinaryFileDto file;
}