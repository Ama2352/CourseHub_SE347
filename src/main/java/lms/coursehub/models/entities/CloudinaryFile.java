package lms.coursehub.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "cloudinary_files")
public class CloudinaryFile {

    @Id
    @UuidGenerator
    private UUID fileId;

    private String name;
    private String displayUrl;
    private String downloadUrl;
}
