package lms.coursehub.models.dtos.user;

import lombok.Data;

@Data
public class UserProfileResponse {
    private String userId;
    private String username;
    private String email;
    private String role;
    private String avatarUrl;
}
