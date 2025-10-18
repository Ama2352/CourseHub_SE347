package lms.coursehub.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lms.coursehub.models.dtos.user.UpdatePasswordRequest;
import lms.coursehub.models.dtos.user.UpdateProfileRequest;
import lms.coursehub.models.dtos.user.UserResponseDto;
import lms.coursehub.models.dtos.user.UserWorkResponseDto;
import lms.coursehub.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@SecurityRequirement(name = "bearerToken")
@Tag(name = "User Management", description = "APIs for managing user profiles and activities")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get current user profile",
            description = "Retrieves the profile of the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user profile",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            )
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getProfile() {
        UserResponseDto profile = userService.getProfile();
        return ResponseEntity.ok(profile);
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of users",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class)
                    )
            )
    })
    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Update user profile",
            description = "Updates the profile information of the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            )
    })
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateProfile(
            @Parameter(description = "Profile update data", required = true)
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponseDto updatedProfile = userService.updateProfile(request);
        return ResponseEntity.ok(updatedProfile);
    }

    @Operation(
            summary = "Update user password",
            description = "Updates the password of the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password updated successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or old password is incorrect",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            )
    })
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @Parameter(description = "Password update data", required = true)
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Leave a course",
            description = "Removes the current user's enrollment from a specific course"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully left the course",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Enrollment not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            )
    })
    @DeleteMapping("/leave")
    public ResponseEntity<Void> leaveCourse(
            @Parameter(description = "ID of the course to leave", required = true)
            @RequestParam String courseId) {
        userService.leaveCourse(courseId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get user work",
            description = "Retrieves all work items (quizzes, assignments, meetings) for the current user, optionally filtered by type and date range"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user work",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserWorkResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            )
    })
    @GetMapping("/work")
    public ResponseEntity<List<UserWorkResponseDto>> getUserWork(
            @Parameter(description = "Type of work (quiz, assignment, meeting)")
            @RequestParam(required = false) String type,
            @Parameter(description = "Start date in ISO format (yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) String start,
            @Parameter(description = "End date in ISO format (yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) String end) {
        List<UserWorkResponseDto> work = userService.getUserWork(type, start, end);
        return ResponseEntity.ok(work);
    }
}
