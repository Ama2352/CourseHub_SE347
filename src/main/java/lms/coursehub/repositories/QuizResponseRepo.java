package lms.coursehub.repositories;

import lms.coursehub.models.entities.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lms.coursehub.models.entities.TopicQuiz;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizResponseRepo extends JpaRepository<QuizResponse, UUID> {
    List<QuizResponse> findByTopicQuiz(TopicQuiz topicQuiz);
    List<QuizResponse> findByTopicQuizId(UUID topicId);
    List<QuizResponse> findByStudentId(UUID studentId);
}
