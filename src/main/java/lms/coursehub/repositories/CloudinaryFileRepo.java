package lms.coursehub.repositories;

import lms.coursehub.models.entities.CloudinaryFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CloudinaryFileRepo extends JpaRepository<CloudinaryFile, UUID> {
}
