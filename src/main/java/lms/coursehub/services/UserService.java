package lms.coursehub.services;

import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.helpers.mapstructs.UserMapper;
import lms.coursehub.models.dtos.auth.LoginRequest;
import lms.coursehub.models.dtos.auth.RegisterRequest;
import lms.coursehub.models.dtos.user.UpdatePasswordRequest;
import lms.coursehub.models.dtos.user.UpdateProfileRequest;
import lms.coursehub.models.dtos.user.UserResponseDto;
import lms.coursehub.models.dtos.user.UserWorkResponseDto;
import lms.coursehub.models.entities.*;
import lms.coursehub.models.enums.UserRole;
import lms.coursehub.repositories.CourseRepo;
import lms.coursehub.repositories.EnrollmentDetailRepo;
import lms.coursehub.repositories.RefreshTokenRepo;
import lms.coursehub.repositories.TopicRepo;
import lms.coursehub.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final EnrollmentDetailRepo enrollmentDetailRepo;
    private final TopicRepo topicRepo;
    private final CourseRepo courseRepo;
    private final CustomUserDetailsService customUserDetailsService;

    public User findByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
    }

    public void register(RegisterRequest request) {
        String email = request.getEmail();
        String username = request.getUsername();
        String password = request.getPassword();
        UserRole role = UserRole.valueOf(request.getRole());

        if (userRepo.existsByEmail(email)) {
            throw new CustomException("Email already in use", HttpStatus.BAD_REQUEST);
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, username, encodedPassword, role);
        userRepo.save(user);
    }

    private void saveRefreshToken(String refreshToken, String email) {
        User user = findByEmail(email);
        RefreshToken token = new RefreshToken();
        token.setToken(refreshToken);
        token.setUser(user);
        if (user.getRefreshTokens() == null) {
            user.setRefreshTokens(new HashSet<>());
        }
        user.getRefreshTokens().add(token);
        userRepo.save(user);
    }

    public Map<String, String> login(LoginRequest request) {
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(request.getEmail(), userDetails.getAuthorities());
        String refreshToken = jwtService.generateRefreshToken(request.getEmail());

        saveRefreshToken(refreshToken, request.getEmail());

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken);
    }

    public void logout(String refreshToken) {
        try {
            jwtService.revokeRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    public String refreshAccessToken(String refreshToken) {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String email = jwtService.extractEmailFromToken(refreshToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        String newAccessToken = jwtService.generateAccessToken(email, userDetails.getAuthorities());

        return newAccessToken;
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByEmail(email);
    }

    /**
     * Get current user profile
     */
    @Transactional(readOnly = true)
    public UserResponseDto getProfile() {
        User user = getCurrentUser();
        return userMapper.toDto(user);
    }

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepo.findAll();
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponseDto updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        user.setUsername(request.getUsername());
        if (request.getAvatar() != null) {
            user.setAvatarUrl(request.getAvatar());
        }

        User updatedUser = userRepo.save(user);
        return userMapper.toDto(updatedUser);
    }

    /**
     * Update user password
     */
    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        User user = getCurrentUser();

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new CustomException("Old password is incorrect", HttpStatus.BAD_REQUEST);
        }

        // Update to new password
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);
        userRepo.save(user);
    }

    /**
     * Leave a course
     */
    @Transactional
    public void leaveCourse(String courseId) {
        User user = getCurrentUser();

        EnrollmentDetail enrollment = enrollmentDetailRepo
                .findByStudentIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new CustomException("Enrollment not found", HttpStatus.NOT_FOUND));

        // Decrement totalJoined in Course
        Course course = enrollment.getCourse();
        int currentJoined = course.getTotalJoined();
        if (currentJoined > 0) {
            course.setTotalJoined(currentJoined - 1);
        }

        enrollmentDetailRepo.delete(enrollment);
        // Save updated course
        courseRepo.save(course);
    }

    /**
     * Get user work (quizzes, assignments, meetings)
     */
    @Transactional(readOnly = true)
    public List<UserWorkResponseDto> getUserWork(String type, String start, String end) {
        User user = getCurrentUser();

        // Get course IDs based on user role
        List<String> courseIds = getCourseIdsByUserRole(user);

        if (courseIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Parse dates if provided
        LocalDateTime startDate = start != null ? LocalDateTime.parse(start) : null;
        LocalDateTime endDate = end != null ? LocalDateTime.parse(end) : null;

        // Get topics based on type and date range
        List<Topic> topics;
        if (type != null) {
            topics = getTopicsByType(courseIds, type, startDate, endDate);
        } else {
            topics = getAllWorkTopics(courseIds, startDate, endDate);
        }

        // Map to DTO
        return topics.stream()
                .map(this::mapTopicToUserWorkDto)
                .collect(Collectors.toList());
    }

    private List<String> getCourseIdsByUserRole(User user) {
        if (user.getRole() == UserRole.STUDENT) {
            // For students: get courses they are enrolled in
            List<EnrollmentDetail> enrollments = enrollmentDetailRepo.findByStudentId(user.getId());
            return enrollments.stream()
                    .map(e -> e.getCourse().getId())
                    .collect(Collectors.toList());
        } else if (user.getRole() == UserRole.TEACHER) {
            // For teachers: get courses they created
            return courseRepo.findByCreatorId(user.getId()).stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());
        }
        // For other roles (if any), return empty list
        return new ArrayList<>();
    }

    private List<Topic> getTopicsByType(List<String> courseIds, String type, LocalDateTime start, LocalDateTime end) {
        // This is a simplified version - you may need to adjust based on your Topic
        // entity structure
        List<Topic> allTopics = topicRepo.findAll();
        return allTopics.stream()
                .filter(t -> courseIds.contains(t.getSection().getCourse().getId()))
                .filter(t -> t.getType().equalsIgnoreCase(type))
                .filter(t -> filterByDateRange(t, start, end))
                .collect(Collectors.toList());
    }

    private List<Topic> getAllWorkTopics(List<String> courseIds, LocalDateTime start, LocalDateTime end) {
        List<Topic> allTopics = topicRepo.findAll();
        return allTopics.stream()
                .filter(t -> courseIds.contains(t.getSection().getCourse().getId()))
                .filter(t -> Arrays.asList("quiz", "assignment", "meeting").contains(t.getType().toLowerCase()))
                .filter(t -> filterByDateRange(t, start, end))
                .collect(Collectors.toList());
    }

    private boolean filterByDateRange(Topic topic, LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return true;
        }

        // Get open and close dates based on topic type
        LocalDateTime topicOpen = null;
        LocalDateTime topicClose = null;

        String type = topic.getType().toLowerCase();
        switch (type) {
            case "quiz":
                if (topic.getTopicQuiz() != null) {
                    topicOpen = topic.getTopicQuiz().getOpen();
                    topicClose = topic.getTopicQuiz().getClose();
                }
                break;

            case "assignment":
                if (topic.getTopicAssignment() != null) {
                    topicOpen = topic.getTopicAssignment().getOpen();
                    topicClose = topic.getTopicAssignment().getClose();
                }
                break;

            case "meeting":
                // Note: Topic entity doesn't have topicMeeting relationship
                return false;

            default:
                return false;
        }

        // If topic doesn't have dates, exclude it
        if (topicOpen == null) {
            return false;
        }

        // Check if topic's date range overlaps with filter date range
        // Topic is included if its open-close period overlaps with start-end filter
        // period

        if (start != null) {
            LocalDateTime topicEndDate = topicClose != null ? topicClose : topicOpen;
            if (topicEndDate.isBefore(start)) {
                return false;
            }
        }

        if (end != null) {
            if (topicOpen.isAfter(end)) {
                return false;
            }
        }

        return true;
    }

    private UserWorkResponseDto mapTopicToUserWorkDto(Topic topic) {
        UserWorkResponseDto dto = new UserWorkResponseDto();
        dto.setTopicId(topic.getId());
        dto.setTitle(topic.getTitle());
        dto.setType(topic.getType());
        dto.setCourseId(topic.getSection().getCourse().getId());
        dto.setCourseTitle(topic.getSection().getCourse().getTitle());
        dto.setSectionTitle(topic.getSection().getTitle());

        // Set open/close dates based on topic type
        String type = topic.getType().toLowerCase();

        switch (type) {
            case "quiz":
                if (topic.getTopicQuiz() != null) {
                    dto.setOpen(topic.getTopicQuiz().getOpen());
                    dto.setClose(topic.getTopicQuiz().getClose());
                }
                break;
            case "assignment":
                if (topic.getTopicAssignment() != null) {
                    dto.setOpen(topic.getTopicAssignment().getOpen());
                    dto.setClose(topic.getTopicAssignment().getClose());
                }
                break;

            case "meeting":
                // Note: Topic entity doesn't have topicMeeting relationship in the provided
                // code
                // You may need to add this relationship or query separately
                dto.setOpen(null);
                dto.setClose(null);
                break;

            default:
                dto.setOpen(null);
                dto.setClose(null);
        }

        return dto;
    }
}
