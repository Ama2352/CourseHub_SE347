package lms.coursehub.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "topic_assignments")
public class TopicAssignment {

    @Id
    @OneToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    private String description;
    private LocalDateTime open;
    private LocalDateTime close;
    private String remindToGrade;
    private int maximumFile;

    @ManyToMany
    @JoinTable(name = "topic_assignment_files", joinColumns = @JoinColumn(name = "topic_assignment_id"), inverseJoinColumns = @JoinColumn(name = "file_id"))
    private List<CloudinaryFile> assignmentFiles;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentResponse> assignmentResponses = new ArrayList<>();
}
