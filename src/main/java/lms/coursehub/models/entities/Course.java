package lms.coursehub.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "courses")
public class Course {

    @Id
    private String id;

    @Column(unique = true)
    private String title;

    private String description;

    private int totalJoined; // not as same as currentJoin
    private String imageUrl;
    private BigDecimal price;
    private String category;
    private String level;
    private Boolean isPublished;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User creator;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnrollmentDetail> enrollmentDetails = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();
}
