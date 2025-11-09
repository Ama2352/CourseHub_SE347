package lms.coursehub.repositories;

import lms.coursehub.models.entities.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SectionRepo extends JpaRepository<Section, UUID> {
    List<Section> findByCourseIdOrderByPosition(String courseId);

    @Query("SELECT s FROM Section s JOIN FETCH s.course WHERE s.id = :id")
    Optional<Section> findByIdWithCourse(@Param("id") UUID id);
}
