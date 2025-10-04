package lms.coursehub.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.helpers.mapstructs.TopicMapper;
import lms.coursehub.models.dtos.topic.*;
import lms.coursehub.models.entities.*;
import lms.coursehub.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepo topicRepo;
    private final TopicMapper topicMapper;
    private final TopicQuizRepo topicQuizRepo;
    private final TopicAssignmentRepo topicAssignmentRepo;
    private final TopicFileRepo topicFileRepo;
    private final TopicLinkRepo topicLinkRepo;
    private final TopicPageRepo topicPageRepo;
    private final TopicMeetingRepo topicMeetingRepo;
    private final CloudinaryFileRepo cloudinaryFileRepo;
    private final SectionRepo sectionRepo;
    private final UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());

    @Transactional
    public TopicResponseDto createTopic(String courseId, CreateTopicRequest request) {
        try {
            // Validate that section belongs to the course
            Section section = sectionRepo.findById(request.getSectionId())
                    .orElseThrow(() -> new CustomException("Section not found", HttpStatus.NOT_FOUND));

            if (!section.getCourse().getId().equals(courseId)) {
                throw new CustomException("Section does not belong to the specified course", HttpStatus.BAD_REQUEST);
            }

            // Create base topic
            Topic topic = topicMapper.toEntity(request);
            topic.setSection(section);
            topic = topicRepo.save(topic);

            // Create type-specific data and get parsed response
            Object parsedData = createTopicSpecificData(topic, request.getType(), request.getData());

            // Build response
            TopicResponseDto response = new TopicResponseDto();
            response.setId(topic.getId());
            response.setTitle(topic.getTitle());
            response.setType(topic.getType());
            response.setSectionId(topic.getSection().getId());
            response.setData(parsedData);

            return response;

        } catch (Exception e) {
            throw new CustomException("Failed to create topic: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Object createTopicSpecificData(Topic topic, String type, String jsonData) throws Exception {
        if (jsonData == null)
            return null;

        switch (type.toLowerCase()) {
            case "quiz":
                return createQuizData(topic, jsonData);
            case "assignment":
                return createAssignmentData(topic, jsonData);
            case "file":
                return createFileData(topic, jsonData);
            case "link":
                return createLinkData(topic, jsonData);
            case "page":
                return createPageData(topic, jsonData);
            case "meeting":
                return createMeetingData(topic, jsonData);
            default:
                throw new IllegalArgumentException("Unsupported topic type: " + type);
        }
    }

    private QuizDataDto createQuizData(Topic topic, String jsonData) throws Exception {
        QuizDataDto quizData = objectMapper.readValue(jsonData, QuizDataDto.class);

        TopicQuiz topicQuiz = new TopicQuiz();
        topicQuiz.setId(topic.getId());
        topicQuiz.setTopic(topic);
        topicQuiz.setDescription(quizData.getDescription());
        topicQuiz.setOpen(quizData.getOpen());
        topicQuiz.setClose(quizData.getClose());
        topicQuiz.setTimeLimit(quizData.getTimeLimit());
        topicQuiz.setTimeLimitUnit(quizData.getTimeLimitUnit());
        topicQuiz.setGradeToPass(quizData.getGradeToPass());
        topicQuiz.setGradingMethod(quizData.getGradingMethod());
        topicQuiz.setAttemptAllowed(quizData.getAttemptAllowed());

        // Note: Question handling would require additional logic
        // You may want to create a separate service for handling questions

        topicQuizRepo.save(topicQuiz);
        return quizData;
    }

    private AssignmentDataDto createAssignmentData(Topic topic, String jsonData) throws Exception {
        AssignmentDataDto assignmentData = objectMapper.readValue(jsonData, AssignmentDataDto.class);

        TopicAssignment topicAssignment = new TopicAssignment();
        topicAssignment.setId(topic.getId());
        topicAssignment.setTopic(topic);
        topicAssignment.setDescription(assignmentData.getDescription());
        topicAssignment.setOpen(assignmentData.getOpen());
        topicAssignment.setClose(assignmentData.getClose());
        topicAssignment.setRemindToGrade(assignmentData.getRemindToGrade());
        topicAssignment.setMaximumFile(assignmentData.getMaximumFile());

        // Handle cloudinary files
        if (assignmentData.getAssignmentFiles() != null && !assignmentData.getAssignmentFiles().isEmpty()) {
            List<CloudinaryFile> files = processCloudinaryFiles(assignmentData.getAssignmentFiles());
            topicAssignment.setAssignmentFiles(files);
        }

        topicAssignmentRepo.save(topicAssignment);
        return assignmentData;
    }

    private FileDataDto createFileData(Topic topic, String jsonData) throws Exception {
        FileDataDto fileData = objectMapper.readValue(jsonData, FileDataDto.class);

        TopicFile topicFile = new TopicFile();
        topicFile.setId(topic.getId());
        topicFile.setTopic(topic);
        topicFile.setDescription(fileData.getDescription());

        if (fileData.getFile() != null) {
            CloudinaryFile cloudinaryFile = processCloudinaryFile(fileData.getFile());
            topicFile.setFile(cloudinaryFile);
        }

        topicFileRepo.save(topicFile);
        return fileData;
    }

    private LinkDataDto createLinkData(Topic topic, String jsonData) throws Exception {
        LinkDataDto linkData = objectMapper.readValue(jsonData, LinkDataDto.class);

        TopicLink topicLink = new TopicLink();
        topicLink.setId(topic.getId());
        topicLink.setTopic(topic);
        topicLink.setDescription(linkData.getDescription());
        topicLink.setUrl(linkData.getUrl());

        topicLinkRepo.save(topicLink);
        return linkData;
    }

    private PageDataDto createPageData(Topic topic, String jsonData) throws Exception {
        PageDataDto pageData = objectMapper.readValue(jsonData, PageDataDto.class);

        TopicPage topicPage = new TopicPage();
        topicPage.setId(topic.getId());
        topicPage.setTopic(topic);
        topicPage.setDescription(pageData.getDescription());
        topicPage.setContent(pageData.getContent());

        topicPageRepo.save(topicPage);
        return pageData;
    }

    private MeetingDataDto createMeetingData(Topic topic, String jsonData) throws Exception {
        MeetingDataDto meetingData = objectMapper.readValue(jsonData, MeetingDataDto.class);

        TopicMeeting topicMeeting = new TopicMeeting();
        topicMeeting.setId(topic.getId());
        topicMeeting.setTopic(topic);
        topicMeeting.setDescription(meetingData.getDescription());
        topicMeeting.setOpen(meetingData.getOpen());
        topicMeeting.setClose(meetingData.getClose());

        topicMeetingRepo.save(topicMeeting);
        return meetingData;
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

    // GET /course/{courseId}/topic/{id}
    @Transactional(readOnly = true)
    public TopicResponseDto getTopic(String courseId, UUID topicId) {
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new CustomException("Topic not found", HttpStatus.NOT_FOUND));

        // Validate that topic belongs to the course
        if (!topic.getSection().getCourse().getId().equals(courseId)) {
            throw new CustomException("Topic does not belong to the specified course", HttpStatus.BAD_REQUEST);
        }

        return buildTopicResponse(topic);
    }

    // PUT /course/{courseId}/topic/{id}
    @Transactional
    public TopicResponseDto updateTopic(String courseId, UUID topicId, CreateTopicRequest request) {
        try {
            Topic existingTopic = topicRepo.findById(topicId)
                    .orElseThrow(() -> new CustomException("Topic not found", HttpStatus.NOT_FOUND));

            // Validate that topic belongs to the course
            if (!existingTopic.getSection().getCourse().getId().equals(courseId)) {
                throw new CustomException("Topic does not belong to the specified course", HttpStatus.BAD_REQUEST);
            }

            // Validate new section if it's being changed
            if (!existingTopic.getSection().getId().equals(request.getSectionId())) {
                Section newSection = sectionRepo.findById(request.getSectionId())
                        .orElseThrow(() -> new CustomException("Section not found", HttpStatus.NOT_FOUND));

                if (!newSection.getCourse().getId().equals(courseId)) {
                    throw new CustomException("New section does not belong to the specified course",
                            HttpStatus.BAD_REQUEST);
                }
                existingTopic.setSection(newSection);
            }

            // Update base topic fields
            existingTopic.setTitle(request.getTitle());
            existingTopic.setType(request.getType());
            existingTopic = topicRepo.save(existingTopic);

            // Delete existing type-specific data if type changed
            if (!existingTopic.getType().equals(request.getType())) {
                deleteTopicSpecificData(existingTopic, existingTopic.getType());
            }

            // Create/update type-specific data
            Object parsedData = createTopicSpecificData(existingTopic, request.getType(), request.getData());

            // Build response
            TopicResponseDto response = new TopicResponseDto();
            response.setId(existingTopic.getId());
            response.setTitle(existingTopic.getTitle());
            response.setType(existingTopic.getType());
            response.setSectionId(existingTopic.getSection().getId());
            response.setData(parsedData);

            return response;

        } catch (Exception e) {
            throw new CustomException("Failed to update topic: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // DELETE /course/{courseId}/topic/{id}
    @Transactional
    public void deleteTopic(String courseId, UUID topicId) {
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new CustomException("Topic not found", HttpStatus.NOT_FOUND));

        // Validate that topic belongs to the course
        if (!topic.getSection().getCourse().getId().equals(courseId)) {
            throw new CustomException("Topic does not belong to the specified course", HttpStatus.BAD_REQUEST);
        }

        // Delete type-specific data first (handled by cascade, but being explicit)
        deleteTopicSpecificData(topic, topic.getType());

        // Delete the base topic (cascade will handle related entities)
        topicRepo.delete(topic);
    }

    // GET /course/{courseId}/topics - Get all topics for a course
    @Transactional(readOnly = true)
    public List<TopicResponseDto> getAllTopicsForCourse(String courseId) {
        // You might want to add a custom query to TopicRepo for this
        // For now, we'll fetch through sections
        List<Topic> topics = topicRepo.findBySectionCourseIdOrderBySectionPositionAscTitle(courseId);

        return topics.stream()
                .map(this::buildTopicResponse)
                .toList();
    }

    // GET /course/{courseId}/section/{sectionId}/topics - Get all topics for a
    // section
    @Transactional(readOnly = true)
    public List<TopicResponseDto> getAllTopicsForSection(String courseId, UUID sectionId) {
        Section section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new CustomException("Section not found", HttpStatus.NOT_FOUND));

        if (!section.getCourse().getId().equals(courseId)) {
            throw new CustomException("Section does not belong to the specified course", HttpStatus.BAD_REQUEST);
        }

        List<Topic> topics = topicRepo.findBySectionIdOrderByTitle(sectionId);

        return topics.stream()
                .map(this::buildTopicResponse)
                .toList();
    }

    private void deleteTopicSpecificData(Topic topic, String type) {
        switch (type.toLowerCase()) {
            case "quiz":
                topicQuizRepo.deleteById(topic.getId());
                break;
            case "assignment":
                topicAssignmentRepo.deleteById(topic.getId());
                break;
            case "file":
                topicFileRepo.deleteById(topic.getId());
                break;
            case "link":
                topicLinkRepo.deleteById(topic.getId());
                break;
            case "page":
                topicPageRepo.deleteById(topic.getId());
                break;
            case "meeting":
                topicMeetingRepo.deleteById(topic.getId());
                break;
            // Other types don't throw error as they might not exist yet
        }
    }

    private TopicResponseDto buildTopicResponse(Topic topic) {
        try {
            Object parsedData = getTopicSpecificData(topic);

            TopicResponseDto response = new TopicResponseDto();
            response.setId(topic.getId());
            response.setTitle(topic.getTitle());
            response.setType(topic.getType());
            response.setSectionId(topic.getSection().getId());
            response.setData(parsedData);

            return response;
        } catch (Exception e) {
            // Log the error but don't fail the entire response
            TopicResponseDto response = new TopicResponseDto();
            response.setId(topic.getId());
            response.setTitle(topic.getTitle());
            response.setType(topic.getType());
            response.setSectionId(topic.getSection().getId());
            response.setData(null);
            return response;
        }
    }

    private Object getTopicSpecificData(Topic topic) throws Exception {
        switch (topic.getType().toLowerCase()) {
            case "quiz":
                return topicQuizRepo.findById(topic.getId())
                        .map(topicMapper::toQuizDataDto)
                        .orElse(null);
            case "assignment":
                return topicAssignmentRepo.findById(topic.getId())
                        .map(this::convertAssignmentToDto)
                        .orElse(null);
            case "file":
                return topicFileRepo.findById(topic.getId())
                        .map(this::convertFileToDto)
                        .orElse(null);
            case "link":
                return topicLinkRepo.findById(topic.getId())
                        .map(topicMapper::toLinkDataDto)
                        .orElse(null);
            case "page":
                return topicPageRepo.findById(topic.getId())
                        .map(topicMapper::toPageDataDto)
                        .orElse(null);
            case "meeting":
                return topicMeetingRepo.findById(topic.getId())
                        .map(topicMapper::toMeetingDataDto)
                        .orElse(null);
            default:
                return null;
        }
    }

    // Conversion methods for entities to DTOs

    private AssignmentDataDto convertAssignmentToDto(TopicAssignment topicAssignment) {
        AssignmentDataDto dto = topicMapper.toAssignmentDataDto(topicAssignment);

        if (topicAssignment.getAssignmentFiles() != null) {
            List<CloudinaryFileDto> fileDtos = topicAssignment.getAssignmentFiles().stream()
                    .map(topicMapper::toCloudinaryFileDto)
                    .toList();
            dto.setAssignmentFiles(fileDtos);
        }
        return dto;
    }

    private FileDataDto convertFileToDto(TopicFile topicFile) {
        return topicMapper.toFileDataDto(topicFile);
    }

    // Additional methods to match frontend API

    // Get all quizzes for a specific course
    @Transactional(readOnly = true)
    public List<TopicResponseDto> getAllQuizzesOfCourse(String courseId) {
        List<Topic> quizTopics = topicRepo.findBySectionCourseIdAndTypeOrderByTitle(courseId, "quiz");
        return quizTopics.stream()
                .map(this::buildTopicResponse)
                .toList();
    }

    // Get all assignments for a specific course
    @Transactional(readOnly = true)
    public List<TopicResponseDto> getAllAssignmentsOfCourse(String courseId) {
        List<Topic> assignmentTopics = topicRepo.findBySectionCourseIdAndTypeOrderByTitle(courseId, "assignment");
        return assignmentTopics.stream()
                .map(this::buildTopicResponse)
                .toList();
    }

    // Get all meetings for a specific course
    @Transactional(readOnly = true)
    public List<TopicResponseDto> getAllMeetingsOfCourse(String courseId) {
        List<Topic> meetingTopics = topicRepo.findBySectionCourseIdAndTypeOrderByTitle(courseId, "meeting");
        return meetingTopics.stream()
                .map(this::buildTopicResponse)
                .toList();
    }

    // Get all works (assignments + quizzes + meetings) for a specific course
    @Transactional(readOnly = true)
    public List<TopicResponseDto> getAllWorksOfCourse(String courseId) {
        List<Topic> workTopics = topicRepo.findBySectionCourseIdAndTypeInOrderByTitle(courseId,
                List.of("quiz", "assignment", "meeting"));
        return workTopics.stream()
                .map(this::buildTopicResponse)
                .toList();
    }

    // Get all quizzes for current authenticated user (from all enrolled courses)
    @Transactional(readOnly = true)
    public List<TopicResponseDto> getAllQuizzesOfUser() {
        // Get all course IDs where the current user is enrolled
        List<String> enrolledCourseIds = topicRepo.findEnrolledCourseIdsByUserId(userService.getCurrentUser().getId());

        if (enrolledCourseIds.isEmpty()) {
            return List.of();
        }

        // Get all quizzes from those courses
        List<Topic> quizTopics = topicRepo.findBySectionCourseIdInAndTypeOrderByTitle(enrolledCourseIds, "quiz");
        return quizTopics.stream()
                .map(this::buildTopicResponse)
                .toList();
    }

    // Get all assignments for current authenticated user (from all enrolled
    // courses)
    @Transactional(readOnly = true)
    public List<TopicResponseDto> getAllAssignmentsOfUser() {
        // Get all course IDs where the current user is enrolled
        List<String> enrolledCourseIds = topicRepo.findEnrolledCourseIdsByUserId(userService.getCurrentUser().getId());

        if (enrolledCourseIds.isEmpty()) {
            return List.of();
        }

        // Get all assignments from those courses
        List<Topic> assignmentTopics = topicRepo.findBySectionCourseIdInAndTypeOrderByTitle(enrolledCourseIds,
                "assignment");
        return assignmentTopics.stream()
                .map(this::buildTopicResponse)
                .toList();
    }
}
