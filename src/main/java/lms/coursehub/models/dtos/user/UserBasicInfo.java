package lms.coursehub.models.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Simplified user information for nested objects to match .NET UserBasicInfo
 * Used in Course responses to avoid circular references and reduce payload size
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserBasicInfo {
    private UUID id;
    private String username;
    private String avatar;
}
