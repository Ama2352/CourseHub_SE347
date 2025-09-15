package lms.coursehub.services;

import lms.coursehub.helpers.mapstructs.UserMapper;
import lms.coursehub.models.dtos.auth.LoginRequest;
import lms.coursehub.models.dtos.auth.RegisterRequest;
import lms.coursehub.models.dtos.user.UserProfileResponse;
import lms.coursehub.models.entities.User;
import lms.coursehub.models.enums.UserRole;
import lms.coursehub.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public User findByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void register(RegisterRequest request) {
        String email = request.getEmail();
        String username = request.getUsername();
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();
        UserRole role = UserRole.valueOf(request.getRole());

        if(userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        if(!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Confirm passwords do not match");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, username, encodedPassword, role);
        userRepo.save(user);
    }

    public Map<String, String> login(LoginRequest request) {
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(request.getEmail(), userDetails.getAuthorities());
        String refreshToken = jwtService.generateRefreshToken(request.getEmail());

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByEmail(email);
    }

    public UserProfileResponse getProfile() {
        User currentUser = getCurrentUser();
        return userMapper.toDTO(currentUser);
    }
}
