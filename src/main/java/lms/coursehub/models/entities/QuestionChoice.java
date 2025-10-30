package lms.coursehub.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "question_choices")
public class QuestionChoice {

    @Id
    @UuidGenerator
    private UUID id;

    private String text;
    private boolean isCorrect;
    private BigDecimal grade;
    private String feedback;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Question question;
}
