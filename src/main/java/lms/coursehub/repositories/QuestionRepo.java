package lms.coursehub.repositories;

import lms.coursehub.models.entities.Course;
import lms.coursehub.models.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface QuestionRepo extends JpaRepository<Question, UUID> {
    List<Question> findByCourse(Course course);
}
