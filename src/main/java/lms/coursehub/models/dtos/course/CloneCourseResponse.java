package lms.coursehub.models.dtos.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for course cloning operation
 * Matches .NET CloneCourseResponse structure
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CloneCourseResponse {
    private String id; // New course ID
    private String sourceCourseId; // Original course ID
    private int sectionCount; // Number of sections cloned
    private int topicCount; // Number of topics cloned
}
