package lms.coursehub.repositories;

import lms.coursehub.models.entities.EnrollmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EnrollmentDetailRepo extends JpaRepository<EnrollmentDetail, UUID> {
    boolean existsByStudentIdAndCourseId(UUID userId, String courseId);
}
