package lms.coursehub.repositories;

import lms.coursehub.models.entities.EnrollmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentDetailRepo extends JpaRepository<EnrollmentDetail, UUID> {
    boolean existsByStudentIdAndCourseId(UUID userId, String courseId);

    @Query("SELECT e FROM EnrollmentDetail e WHERE e.student.id = :studentId AND e.course.id = :courseId")
    Optional<EnrollmentDetail> findByStudentIdAndCourseId(@Param("studentId") UUID studentId,
            @Param("courseId") String courseId);

    List<EnrollmentDetail> findByStudentId(UUID studentId);

    // For calculating student counts who enrolled before a specific date (e.g.,
    // topic close date)
    @Query("SELECT COUNT(e) FROM EnrollmentDetail e WHERE e.course.id = :courseId AND e.joinDate <= :beforeDate")
    long countByCourseIdAndJoinDateBefore(@Param("courseId") String courseId,
            @Param("beforeDate") LocalDateTime beforeDate);

    @Query("SELECT e FROM EnrollmentDetail e WHERE e.course.id = :courseId AND e.joinDate <= :beforeDate")
    List<EnrollmentDetail> findByCourseIdAndJoinDateBefore(@Param("courseId") String courseId,
            @Param("beforeDate") LocalDateTime beforeDate);
}
