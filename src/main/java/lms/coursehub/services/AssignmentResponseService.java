package lms.coursehub.services;

import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.helpers.mapstructs.AssignmentResponseMapper;
import lms.coursehub.models.dtos.assignment.AssignmentResponseDto;
import lms.coursehub.models.dtos.assignment.CreateAssignmentResponseRequest;
import lms.coursehub.models.dtos.assignment.UpdateAssignmentResponseRequest;
import lms.coursehub.models.dtos.topic.CloudinaryFileDto;
import lms.coursehub.models.entities.AssignmentResponse;
import lms.coursehub.models.entities.CloudinaryFile;
import lms.coursehub.models.entities.Topic;
import lms.coursehub.models.entities.TopicAssignment;
import lms.coursehub.models.entities.User;
import lms.coursehub.repositories.AssignmentResponseRepo;
import lms.coursehub.repositories.CloudinaryFileRepo;
import lms.coursehub.repositories.TopicAssignmentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentResponseService {

    private final AssignmentResponseRepo assignmentResponseRepo;
    private final TopicAssignmentRepo topicAssignmentRepo;
    private final CloudinaryFileRepo cloudinaryFileRepo;
    private final AssignmentResponseMapper assignmentResponseMapper;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public AssignmentResponseDto createAssignmentResponse(UUID topicId, CreateAssignmentResponseRequest request) {
        // Get current user
        User currentUser = userService.getCurrentUser();

        // Validate topic assignment exists
        TopicAssignment topicAssignment = topicAssignmentRepo.findById(topicId)
                .orElseThrow(() -> new CustomException("Assignment not found", HttpStatus.NOT_FOUND));

        // Check if user already has a response for this assignment
        if (assignmentResponseRepo.existsByStudentIdAndTopicAssignmentId(currentUser.getId(), topicId)) {
            throw new CustomException("You have already submitted a response for this assignment",
                    HttpStatus.BAD_REQUEST);
        }

        // Create assignment response
        AssignmentResponse assignmentResponse = assignmentResponseMapper.toEntity(request);
        assignmentResponse.setStudent(currentUser);
        assignmentResponse.setTopicAssignment(topicAssignment);
        assignmentResponse.setSubmittedAt(LocalDateTime.now());

        // Handle cloudinary files
        if (request.getAssignmentFiles() != null && !request.getAssignmentFiles().isEmpty()) {
            List<CloudinaryFile> files = processCloudinaryFiles(request.getAssignmentFiles());
            assignmentResponse.setAssignmentFiles(files);
        }

        assignmentResponse = assignmentResponseRepo.save(assignmentResponse);
        notifyInstructorAboutSubmission(topicAssignment, currentUser);

        return assignmentResponseMapper.toDto(assignmentResponse);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponseDto> getAllAssignmentResponsesByTopicId(UUID topicId) {
        // Validate topic assignment exists
        topicAssignmentRepo.findById(topicId)
                .orElseThrow(() -> new CustomException("Assignment not found", HttpStatus.NOT_FOUND));

        List<AssignmentResponse> responses = assignmentResponseRepo.findByTopicAssignmentId(topicId);
        return responses.stream()
                .map(assignmentResponseMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssignmentResponseDto getAssignmentResponseById(UUID responseId) {
        AssignmentResponse response = assignmentResponseRepo.findById(responseId)
                .orElseThrow(() -> new CustomException("Assignment response not found", HttpStatus.NOT_FOUND));

        return assignmentResponseMapper.toDto(response);
    }

    @Transactional
    public AssignmentResponseDto updateAssignmentResponseById(UUID responseId,
            UpdateAssignmentResponseRequest request) {
        AssignmentResponse existingResponse = assignmentResponseRepo.findById(responseId)
                .orElseThrow(() -> new CustomException("Assignment response not found", HttpStatus.NOT_FOUND));

        // Check if the current user is the owner of this response
        User currentUser = userService.getCurrentUser();
        if (!existingResponse.getStudent().getId().equals(currentUser.getId())) {
            throw new CustomException("You are not authorized to update this response", HttpStatus.FORBIDDEN);
        }

        // Update fields if provided
        if (request.getNote() != null) {
            existingResponse.setNote(request.getNote());
        }

        // Handle cloudinary files
        if (request.getAssignmentFiles() != null) {
            List<CloudinaryFile> files = processCloudinaryFiles(request.getAssignmentFiles());
            existingResponse.setAssignmentFiles(files);
        }

        existingResponse = assignmentResponseRepo.save(existingResponse);

        return assignmentResponseMapper.toDto(existingResponse);
    }

    @Transactional
    public void deleteResponse(UUID responseId) {
        AssignmentResponse response = assignmentResponseRepo.findById(responseId)
                .orElseThrow(() -> new CustomException("Assignment response not found", HttpStatus.NOT_FOUND));

        // Check if the current user is the owner of this response
        User currentUser = userService.getCurrentUser();
        if (!response.getStudent().getId().equals(currentUser.getId())) {
            throw new CustomException("You are not authorized to delete this response", HttpStatus.FORBIDDEN);
        }

        assignmentResponseRepo.delete(response);
    }

    private void notifyInstructorAboutSubmission(TopicAssignment topicAssignment, User student) {
        if (topicAssignment == null || student == null || notificationService == null) {
            return;
        }

        Topic topic = topicAssignment.getTopic();
        if (topic == null || topic.getSection() == null || topic.getSection().getCourse() == null) {
            return;
        }

        User instructor = topic.getSection().getCourse().getCreator();
        if (instructor == null || instructor.getId().equals(student.getId())) {
            return;
        }

        String topicTitle = topic.getTitle() != null ? topic.getTitle() : "Assignment";
        String courseTitle = topic.getSection().getCourse().getTitle();
        String title = "New assignment submission";
        String message = String.format("%s just submitted \"%s\" in %s.",
                student.getUsername(), topicTitle, courseTitle);
        notificationService.notifyUser(instructor.getId(), title, message);
    }

    private List<CloudinaryFile> processCloudinaryFiles(List<CloudinaryFileDto> fileDtos) {
        List<CloudinaryFile> files = new ArrayList<>();
        for (CloudinaryFileDto fileDto : fileDtos) {
            files.add(processCloudinaryFile(fileDto));
        }
        return files;
    }

    private CloudinaryFile processCloudinaryFile(CloudinaryFileDto fileDto) {
        if (fileDto.getId() != null) {
            // Existing file - fetch from database
            return cloudinaryFileRepo.findById(fileDto.getId())
                    .orElseThrow(() -> new CustomException("CloudinaryFile not found: " + fileDto.getId(),
                            HttpStatus.NOT_FOUND));
        } else {
            // New file - create new record
            CloudinaryFile cloudinaryFile = new CloudinaryFile();
            cloudinaryFile.setName(fileDto.getName());
            cloudinaryFile.setDisplayUrl(fileDto.getDisplayUrl());
            cloudinaryFile.setDownloadUrl(fileDto.getDownloadUrl());
            return cloudinaryFileRepo.save(cloudinaryFile);
        }
    }
}
