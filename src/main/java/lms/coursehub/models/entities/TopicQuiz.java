package lms.coursehub.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "topic_quizzes")
public class TopicQuiz {

    @Id
    private UUID id;

    @OneToOne
    @MapsId
    private Topic topic;

    private int studentCount;
    private String description;
    private LocalDateTime open;
    private LocalDateTime close;
    private int timeLimit;
    private String timeLimitUnit;
    private BigDecimal gradeToPass;
    private String gradingMethod;
    private String attemptAllowed;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "topic_quiz_questions", joinColumns = @JoinColumn(name = "topic_quiz_id"), inverseJoinColumns = @JoinColumn(name = "question_id"))
    private List<Question> questions;

    @OneToMany(mappedBy = "topicQuiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizResponse> quizResponses = new ArrayList<>();

}
