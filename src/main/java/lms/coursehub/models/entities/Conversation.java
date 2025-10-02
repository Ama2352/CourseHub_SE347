package lms.coursehub.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "conversations", uniqueConstraints = @UniqueConstraint(columnNames = { "user1_id", "user2_id" }))
public class Conversation {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user1;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user2;

    private LocalDateTime lastMessageAt;
}
