package lms.coursehub.repositories;

import lms.coursehub.models.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TopicRepo extends JpaRepository<Topic, UUID> {

        List<Topic> findBySectionIdOrderByTitle(UUID sectionId);

        @Query("SELECT t FROM Topic t JOIN t.section s ORDER BY s.position ASC, t.title ASC")
        List<Topic> findAllOrderBySectionPositionAscTitle();

        List<Topic> findByTypeOrderByTitle(String type);

        List<Topic> findByTypeInOrderByTitle(List<String> types);

        @Query("SELECT t FROM Topic t JOIN t.section s WHERE s.course.id = :courseId ORDER BY s.position ASC, t.title ASC")
        List<Topic> findBySectionCourseIdOrderBySectionPositionAscTitle(@Param("courseId") String courseId);

        List<Topic> findBySectionCourseId(String courseId);

        @Query("SELECT t FROM Topic t JOIN t.section s WHERE s.course.id = :courseId AND t.type = :type ORDER BY t.title ASC")
        List<Topic> findBySectionCourseIdAndTypeOrderByTitle(@Param("courseId") String courseId,
                        @Param("type") String type);

        @Query("SELECT t FROM Topic t JOIN t.section s WHERE s.course.id = :courseId AND t.type IN :types ORDER BY t.title ASC")
        List<Topic> findBySectionCourseIdAndTypeInOrderByTitle(@Param("courseId") String courseId,
                        @Param("types") List<String> types);

        @Query("SELECT t FROM Topic t JOIN t.section s WHERE s.course.id IN :courseIds AND t.type = :type ORDER BY t.title ASC")
        List<Topic> findBySectionCourseIdInAndTypeOrderByTitle(@Param("courseIds") List<String> courseIds,
                        @Param("type") String type);

        @Query("SELECT DISTINCT s.course.id FROM Topic t JOIN t.section s JOIN s.course c JOIN c.enrollmentDetails e WHERE e.student.id = :userId")
        List<String> findEnrolledCourseIdsByUserId(@Param("userId") UUID userId);

        @Query("SELECT t FROM Topic t JOIN FETCH t.section s JOIN FETCH s.course WHERE t.id = :id")
        java.util.Optional<Topic> findByIdWithSectionAndCourse(@Param("id") UUID id);
}
