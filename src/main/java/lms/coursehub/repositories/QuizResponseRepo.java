package lms.coursehub.repositories;

import lms.coursehub.models.entities.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import lms.coursehub.models.entities.TopicQuiz;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizResponseRepo extends JpaRepository<QuizResponse, UUID> {
    List<QuizResponse> findByTopicQuiz(TopicQuiz topicQuiz);

    List<QuizResponse> findByTopicQuizId(UUID topicQuizId);

    List<QuizResponse> findByStudentId(UUID studentId);

    // Find quiz responses by topic quiz ID and student ID
    @Query("SELECT qr FROM QuizResponse qr WHERE qr.topicQuiz.id = :topicQuizId AND qr.student.id = :studentId")
    List<QuizResponse> findByTopicQuizIdAndStudentId(@Param("topicQuizId") UUID topicQuizId,
            @Param("studentId") UUID studentId);
}
