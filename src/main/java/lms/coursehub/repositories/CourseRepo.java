package lms.coursehub.repositories;

import jakarta.validation.constraints.NotBlank;
import lms.coursehub.models.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepo extends JpaRepository<Course, String> {
    boolean existsByTitle(String title);
}
