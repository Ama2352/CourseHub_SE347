package lms.coursehub.services;

/**
 * Integration samples / hooks for firing notifications from other services.
 *
 * Usage examples (inside other services):
 *
 * // 1) Notify teacher when student submits an assignment
 * notificationService.notifyUser(teacherId, "Assignment submitted", "Student John has submitted Assignment 3.");
 *
 * // 2) Notify student when teacher grades
 * notificationService.notifyUser(studentId, "Assignment graded", "Your assignment 3 has been graded.");
 *
 * // 3) Notify topic owner when a comment is added
 * notificationService.notifyUser(topicOwnerId, "New comment", "Someone commented on your topic");
 *
 * // 4) Notify enrolled students when a new topic is created
 * enrolledStudentIds.forEach(id -> notificationService.notifyUser(id, "New topic", "A new topic has been posted"));
 *
 * These calls assume you have `@Autowired private NotificationService notificationService;` in the calling service.
 */
public class NotificationIntegrationSamples {
    // purely illustrative; no runtime logic here
}
