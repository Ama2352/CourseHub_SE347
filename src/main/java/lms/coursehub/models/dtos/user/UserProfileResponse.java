package lms.coursehub.models.dtos.user;

import lombok.Data;

@Data
public class UserProfileResponse {
    private String id;
    private String username;
    private String email;
    private String role;
    private String avatarUrl;
}
