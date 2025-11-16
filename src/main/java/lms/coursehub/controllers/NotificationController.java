package lms.coursehub.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lms.coursehub.models.dtos.notification.CreateNotificationRequest;
import lms.coursehub.models.dtos.notification.NotificationDto;
import lms.coursehub.models.dtos.notification.SetReadRequest;
import lms.coursehub.models.entities.CustomUserDetails;
import lms.coursehub.models.entities.User;
import lms.coursehub.repositories.UserRepo;
import lms.coursehub.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
@Tag(name = "Notification Management", description = "APIs for managing user notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepo userRepo;

    @Operation(summary = "Get all notifications for current user")
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();

        List<NotificationDto> list = notificationService.getNotifications(user.getId());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Mark notification read/unread")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto> setRead(@PathVariable UUID id, @Valid @RequestBody SetReadRequest request) {
        NotificationDto dto = notificationService.markAsRead(id, request.isRead());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Delete a notification")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Deleted")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create a notification (manual)")
    @PostMapping
    public ResponseEntity<NotificationDto> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationDto dto = notificationService.createNotification(request.getUserId(), request.getTitle(), request.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
