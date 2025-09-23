package lms.coursehub.repositories;

import lms.coursehub.models.entities.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SectionRepo extends JpaRepository<Section, UUID> {
}
