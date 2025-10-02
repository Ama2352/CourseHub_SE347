package lms.coursehub.models.entities;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "topic_pages")
public class TopicPage {

    @Id
    private UUID id;

    @OneToOne
    @MapsId
    private Topic topic;

    private String description;

    @Lob
    private String content;
}
