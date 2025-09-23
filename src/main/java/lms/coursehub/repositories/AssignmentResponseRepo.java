package lms.coursehub.repositories;

import lms.coursehub.models.entities.AssignmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AssignmentResponseRepo extends JpaRepository<AssignmentResponse, UUID> {
}
