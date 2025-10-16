package lms.coursehub.repositories;

import lms.coursehub.models.entities.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SectionRepo extends JpaRepository<Section, UUID> {
    List<Section> findByCourseIdOrderByPosition(String courseId);
}
