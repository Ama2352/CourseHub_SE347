package lms.coursehub.repositories;

import lms.coursehub.models.entities.EnrollmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentDetailRepo extends JpaRepository<EnrollmentDetail, UUID> {
    boolean existsByStudentIdAndCourseId(UUID userId, String courseId);
    
    @Query("SELECT e FROM EnrollmentDetail e WHERE e.student.id = :studentId AND e.course.id = :courseId")
    Optional<EnrollmentDetail> findByStudentIdAndCourseId(@Param("studentId") UUID studentId, @Param("courseId") String courseId);
    
    List<EnrollmentDetail> findByStudentId(UUID studentId);
}
