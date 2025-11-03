package lms.coursehub.models.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lms.coursehub.helpers.annotations.EnumValidator;
import lms.coursehub.models.enums.UserRole;
import lombok.Getter;

@Getter
public class RegisterRequest {

    @NotBlank
    @EnumValidator(enumClass = UserRole.class, message = "Invalid role")
    private String role;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
