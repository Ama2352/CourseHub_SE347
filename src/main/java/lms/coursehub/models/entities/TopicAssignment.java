package lms.coursehub.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "topic_assignments")
public class TopicAssignment {

    @Id
    private UUID id;

    @OneToOne
    @MapsId
    private Topic topic;

    private String description;
    private LocalDateTime open;
    private LocalDateTime close;
    private String remindToGrade;
    private int maximumFile;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "topic_assignment_files", joinColumns = @JoinColumn(name = "topic_assignment_id"), inverseJoinColumns = @JoinColumn(name = "file_id"))
    private List<CloudinaryFile> assignmentFiles = new ArrayList<>();

    @OneToMany(mappedBy = "topicAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentResponse> assignmentResponses = new ArrayList<>();

}
