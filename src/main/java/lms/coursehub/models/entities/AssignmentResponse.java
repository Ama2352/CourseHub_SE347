package lms.coursehub.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

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
@Table(name = "assignment_responses")
public class AssignmentResponse {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User student;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_assignment_id")
    private TopicAssignment topicAssignment;

    private LocalDateTime submittedAt;
    private String note;
    private BigDecimal mark;
    private LocalDateTime gradedAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User grader;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "assignment_response_files", joinColumns = @JoinColumn(name = "assignment_response_id"), inverseJoinColumns = @JoinColumn(name = "file_id"))
    private List<CloudinaryFile> assignmentFiles = new ArrayList<>();
}
