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
@Table(name = "topic_links")
public class TopicLink {

    @Id
    private UUID id;

    @OneToOne
    @MapsId
    private Topic topic;

    private String description;
    private String url;
}
