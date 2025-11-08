package lms.coursehub.repositories;

import lms.coursehub.models.entities.AssignmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentResponseRepo extends JpaRepository<AssignmentResponse, UUID> {

    // Find all assignment responses for a specific topic assignment
    @Query("SELECT ar FROM AssignmentResponse ar WHERE ar.topicAssignment.id = :topicAssignmentId")
    List<AssignmentResponse> findByTopicAssignmentId(@Param("topicAssignmentId") UUID topicAssignmentId);

    // Find assignment response by topic assignment ID and student ID
    @Query("SELECT ar FROM AssignmentResponse ar WHERE ar.topicAssignment.id = :topicAssignmentId AND ar.student.id = :studentId")
    Optional<AssignmentResponse> findByTopicAssignmentIdAndStudentId(@Param("topicAssignmentId") UUID topicAssignmentId,
            @Param("studentId") UUID studentId);

    // Check if assignment response exists for student and topic
    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END FROM AssignmentResponse ar WHERE ar.student.id = :studentId AND ar.topicAssignment.id = :topicAssignmentId")
    boolean existsByStudentIdAndTopicAssignmentId(@Param("studentId") UUID studentId,
            @Param("topicAssignmentId") UUID topicAssignmentId);
}
