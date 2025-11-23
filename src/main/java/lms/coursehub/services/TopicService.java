package lms.coursehub.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.helpers.mapstructs.TopicMapper;
import lms.coursehub.models.dtos.reports.AllAssignmentsReportDto;
import lms.coursehub.models.dtos.reports.AllQuizzesReportDto;
import lms.coursehub.models.dtos.reports.SingleAssignmentReportDto;
import lms.coursehub.models.dtos.reports.SingleQuizReportDto;
import lms.coursehub.models.dtos.topic.*;
import lms.coursehub.models.entities.*;
import lms.coursehub.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final CourseRepo courseRepo;
    private final UserService userService;
    private final EnrollmentDetailRepo enrollmentDetailRepo;
    private final QuizResponseRepo quizResponseRepo;
    private final AssignmentResponseRepo assignmentResponseRepo;
    private final QuestionRepo questionRepo;
    private final NotificationService notificationService;

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .registerModule(new Hibernate6Module())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Transactional
    public TopicResponseDto createTopic(String courseId, CreateTopicRequest request) {
        try {
            // Validate that section belongs to the course (fetch with course to avoid lazy
            // loading issue)
            Section section = sectionRepo.findByIdWithCourse(request.getSectionId())
                    .orElseThrow(() -> new CustomException("Section not found", HttpStatus.NOT_FOUND));

            // Validate that the section belongs to the specified course
            if (!section.getCourse().getId().equals(courseId)) {
                throw new CustomException("Section does not belong to the specified course", HttpStatus.BAD_REQUEST);
            }

            // Create base topic
            Topic topic = topicMapper.toEntity(request);
            topic.setSection(section);

            topic = topicRepo.save(topic);
            entityManager.flush(); // Ensure Topic is persisted before related entities

            // Create type-specific data and get JSON string response
            String dataJson = createTopicSpecificData(topic, request.getType(), request.getData());

            // Build response
            TopicResponseDto response = new TopicResponseDto();
            response.setId(topic.getId());
            response.setTitle(topic.getTitle());
            response.setType(topic.getType());
            response.setSectionId(topic.getSection().getId());
            response.setData(dataJson);

            notifyStudentsAboutNewTopic(topic);
            return response;

        } catch (Exception e) {
            throw new CustomException("Failed to create topic: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void notifyStudentsAboutNewTopic(Topic topic) {
        if (topic == null || notificationService == null) {
            return;
        }

        Section section = topic.getSection();
        if (section == null || section.getCourse() == null) {
            return;
        }

        Course course = section.getCourse();
        List<EnrollmentDetail> enrollments = enrollmentDetailRepo
                .findByCourseIdAndJoinDateBefore(course.getId(), LocalDateTime.now());
        if (enrollments == null || enrollments.isEmpty()) {
            return;
        }

        User instructor = course.getCreator();
        String courseTitle = course.getTitle() != null ? course.getTitle() : "your course";
        String topicTitle = topic.getTitle() != null ? topic.getTitle() : "a new topic";
        String topicType = topic.getType() != null ? topic.getType().toLowerCase(Locale.ROOT) : "topic";
        String posterName = instructor != null ? instructor.getUsername() : "Your instructor";
        String title = "New " + topicType + " in " + courseTitle;
        String message = String.format("%s just posted \"%s\".", posterName, topicTitle);

        enrollments.stream()
                .map(EnrollmentDetail::getStudent)
                .filter(Objects::nonNull)
                .filter(student -> instructor == null || !student.getId().equals(instructor.getId()))
                .forEach(student -> notificationService.notifyUser(student.getId(), title, message));
    }

    private String createTopicSpecificData(Topic topic, String type, String jsonData) throws Exception {
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

    private String createQuizData(Topic topic, String jsonData) throws Exception {
        QuizDataDto quizData = objectMapper.readValue(jsonData, QuizDataDto.class);

        TopicQuiz topicQuiz = new TopicQuiz();
        // Don't set ID manually when using @MapsId - let JPA handle it
        topicQuiz.setTopic(topic);
        topicQuiz.setDescription(quizData.getDescription());
        topicQuiz.setOpen(quizData.getOpen());
        topicQuiz.setClose(quizData.getClose());
        // Handle null timeLimit safely
        topicQuiz.setTimeLimit(quizData.getTimeLimit() != null ? quizData.getTimeLimit() : 0);
        topicQuiz.setTimeLimitUnit(quizData.getTimeLimitUnit());
        topicQuiz.setGradeToPass(quizData.getGradeToPass());
        topicQuiz.setGradingMethod(quizData.getGradingMethod());
        topicQuiz.setAttemptAllowed(quizData.getAttemptAllowed());

        // Note: Question handling would require additional logic
        // You may want to create a separate service for handling questions

        TopicQuiz savedQuiz = topicQuizRepo.save(topicQuiz);

        // Return only the DTO fields as JSON string
        QuizDataDto responseDto = topicMapper.toQuizDataDto(savedQuiz);
        return objectMapper.writeValueAsString(responseDto);
    }

    private String createAssignmentData(Topic topic, String jsonData) throws Exception {
        AssignmentDataDto assignmentData = objectMapper.readValue(jsonData, AssignmentDataDto.class);

        TopicAssignment topicAssignment = new TopicAssignment();
        // Don't set ID manually when using @MapsId - let JPA handle it
        topicAssignment.setTopic(topic);
        topicAssignment.setDescription(assignmentData.getDescription());
        topicAssignment.setOpen(assignmentData.getOpen());
        topicAssignment.setClose(assignmentData.getClose());
        topicAssignment.setRemindToGrade(assignmentData.getRemindToGrade());
        topicAssignment.setMaximumFile(assignmentData.getMaximumFile() != null ? assignmentData.getMaximumFile() : 0);
        topicAssignment.setMaximumFileSize(assignmentData.getMaximumFileSize());

        // Handle cloudinary files
        if (assignmentData.getAssignmentFiles() != null && !assignmentData.getAssignmentFiles().isEmpty()) {
            List<CloudinaryFile> files = processCloudinaryFiles(assignmentData.getAssignmentFiles());
            topicAssignment.setAssignmentFiles(files);
        }

        TopicAssignment savedAssignment = topicAssignmentRepo.save(topicAssignment);

        // Return only the DTO fields as JSON string
        AssignmentDataDto responseDto = topicMapper.toAssignmentDataDto(savedAssignment);
        return objectMapper.writeValueAsString(responseDto);
    }

    private String createFileData(Topic topic, String jsonData) throws Exception {
        FileDataDto fileData = objectMapper.readValue(jsonData, FileDataDto.class);

        TopicFile topicFile = new TopicFile();
        // Don't set ID manually when using @MapsId - let JPA handle it
        topicFile.setTopic(topic);
        topicFile.setDescription(fileData.getDescription());

        if (fileData.getFile() != null) {
            CloudinaryFile cloudinaryFile = processCloudinaryFile(fileData.getFile());
            topicFile.setFile(cloudinaryFile);
        }

        TopicFile savedFile = topicFileRepo.save(topicFile);

        // Return only the DTO fields as JSON string
        FileDataDto responseDto = topicMapper.toFileDataDto(savedFile);
        return objectMapper.writeValueAsString(responseDto);
    }

    private String createLinkData(Topic topic, String jsonData) throws Exception {
        LinkDataDto linkData = objectMapper.readValue(jsonData, LinkDataDto.class);

        TopicLink topicLink = new TopicLink();
        // Don't set ID manually when using @MapsId - let JPA handle it
        topicLink.setTopic(topic);
        topicLink.setDescription(linkData.getDescription());
        topicLink.setUrl(linkData.getUrl());

        TopicLink savedLink = topicLinkRepo.save(topicLink);

        // Return only the DTO fields as JSON string (not the entire entity)
        LinkDataDto responseDto = new LinkDataDto();
        responseDto.setUrl(savedLink.getUrl());
        responseDto.setDescription(savedLink.getDescription());
        return objectMapper.writeValueAsString(responseDto);
    }

    private String createPageData(Topic topic, String jsonData) throws Exception {
        PageDataDto pageData = objectMapper.readValue(jsonData, PageDataDto.class);

        TopicPage topicPage = new TopicPage();
        // Don't set ID manually when using @MapsId - let JPA handle it
        topicPage.setTopic(topic);
        topicPage.setDescription(pageData.getDescription());
        topicPage.setContent(pageData.getContent());

        TopicPage savedPage = topicPageRepo.save(topicPage);

        // Return only the DTO fields as JSON string
        PageDataDto responseDto = topicMapper.toPageDataDto(savedPage);
        return objectMapper.writeValueAsString(responseDto);
    }

    private String createMeetingData(Topic topic, String jsonData) throws Exception {
        // Handle case where frontend sends "[object Object]" instead of valid JSON
        if (jsonData == null || jsonData.trim().isEmpty() || jsonData.equals("[object Object]")) {
            // Create empty meeting data
            TopicMeeting topicMeeting = new TopicMeeting();
            topicMeeting.setTopic(topic);
            topicMeeting.setDescription(null);
            topicMeeting.setOpen(null);
            topicMeeting.setClose(null);

            TopicMeeting savedMeeting = topicMeetingRepo.save(topicMeeting);

            // Return only the DTO fields as JSON string
            MeetingDataDto responseDto = topicMapper.toMeetingDataDto(savedMeeting);
            return objectMapper.writeValueAsString(responseDto);
        }

        MeetingDataDto meetingData = objectMapper.readValue(jsonData, MeetingDataDto.class);

        TopicMeeting topicMeeting = new TopicMeeting();
        // Don't set ID manually when using @MapsId - let JPA handle it
        topicMeeting.setTopic(topic);
        topicMeeting.setDescription(meetingData.getDescription());
        topicMeeting.setOpen(meetingData.getOpen());
        topicMeeting.setClose(meetingData.getClose());

        TopicMeeting savedMeeting = topicMeetingRepo.save(topicMeeting);

        // Return only the DTO fields as JSON string
        MeetingDataDto responseDto = topicMapper.toMeetingDataDto(savedMeeting);
        return objectMapper.writeValueAsString(responseDto);
    }

    // Update methods for existing topic-specific data
    private String updateTopicSpecificData(Topic topic, String type, String jsonData) throws Exception {
        if (jsonData == null)
            return null;

        switch (type.toLowerCase()) {
            case "quiz":
                return updateQuizData(topic, jsonData);
            case "assignment":
                return updateAssignmentData(topic, jsonData);
            case "file":
                return updateFileData(topic, jsonData);
            case "link":
                return updateLinkData(topic, jsonData);
            case "page":
                return updatePageData(topic, jsonData);
            case "meeting":
                return updateMeetingData(topic, jsonData);
            default:
                throw new IllegalArgumentException("Unsupported topic type: " + type);
        }
    }

    private String updateQuizData(Topic topic, String jsonData) throws Exception {
        QuizDataDto quizData = objectMapper.readValue(jsonData, QuizDataDto.class);

        // Fetch existing quiz or create new one
        TopicQuiz topicQuiz = topicQuizRepo.findById(topic.getId())
                .orElseGet(() -> {
                    TopicQuiz newQuiz = new TopicQuiz();
                    newQuiz.setTopic(topic);
                    return newQuiz;
                });

        // Update fields
        topicQuiz.setDescription(quizData.getDescription());
        topicQuiz.setOpen(quizData.getOpen());
        topicQuiz.setClose(quizData.getClose());
        topicQuiz.setTimeLimit(quizData.getTimeLimit() != null ? quizData.getTimeLimit() : 0);
        topicQuiz.setTimeLimitUnit(quizData.getTimeLimitUnit());
        topicQuiz.setGradeToPass(quizData.getGradeToPass());
        topicQuiz.setGradingMethod(quizData.getGradingMethod());
        topicQuiz.setAttemptAllowed(quizData.getAttemptAllowed());

        // Update questions if provided
        if (quizData.getQuestions() != null) {
            List<UUID> questionIds = quizData.getQuestions().stream()
                    .map(q -> q.getId())
                    .filter(id -> id != null)
                    .toList();
            if (!questionIds.isEmpty()) {
                List<Question> questions = questionRepo.findAllById(questionIds);
                // Replace the questions list
                topicQuiz.getQuestions().clear();
                topicQuiz.getQuestions().addAll(questions);
            } else {
                // Clear questions if empty list provided
                topicQuiz.getQuestions().clear();
            }
        }

        TopicQuiz savedQuiz = topicQuizRepo.save(topicQuiz);
        entityManager.flush(); // Ensure changes are persisted immediately

        // Return only the DTO fields as JSON string
        QuizDataDto responseDto = topicMapper.toQuizDataDto(savedQuiz);
        return objectMapper.writeValueAsString(responseDto);
    }

    private String updateAssignmentData(Topic topic, String jsonData) throws Exception {
        AssignmentDataDto assignmentData = objectMapper.readValue(jsonData, AssignmentDataDto.class);

        // Fetch existing assignment or create new one
        TopicAssignment topicAssignment = topicAssignmentRepo.findById(topic.getId())
                .orElseGet(() -> {
                    TopicAssignment newAssignment = new TopicAssignment();
                    newAssignment.setTopic(topic);
                    return newAssignment;
                });

        // Update fields
        topicAssignment.setDescription(assignmentData.getDescription());
        topicAssignment.setOpen(assignmentData.getOpen());
        topicAssignment.setClose(assignmentData.getClose());
        topicAssignment.setRemindToGrade(assignmentData.getRemindToGrade());
        topicAssignment.setMaximumFile(assignmentData.getMaximumFile() != null ? assignmentData.getMaximumFile() : 0);
        topicAssignment.setMaximumFileSize(assignmentData.getMaximumFileSize());

        // Handle cloudinary files
        if (assignmentData.getAssignmentFiles() != null && !assignmentData.getAssignmentFiles().isEmpty()) {
            List<CloudinaryFile> files = processCloudinaryFiles(assignmentData.getAssignmentFiles());
            topicAssignment.setAssignmentFiles(files);
        }

        TopicAssignment savedAssignment = topicAssignmentRepo.save(topicAssignment);
        entityManager.flush(); // Ensure changes are persisted immediately

        // Return only the DTO fields as JSON string
        AssignmentDataDto responseDto = topicMapper.toAssignmentDataDto(savedAssignment);
        return objectMapper.writeValueAsString(responseDto);
    }

    private String updateFileData(Topic topic, String jsonData) throws Exception {
        FileDataDto fileData = objectMapper.readValue(jsonData, FileDataDto.class);

        // Fetch existing file or create new one
        TopicFile topicFile = topicFileRepo.findById(topic.getId())
                .orElseGet(() -> {
                    TopicFile newFile = new TopicFile();
                    newFile.setTopic(topic);
                    return newFile;
                });

        // Update fields
        topicFile.setDescription(fileData.getDescription());

        if (fileData.getFile() != null) {
            CloudinaryFile cloudinaryFile = processCloudinaryFile(fileData.getFile());
            if (cloudinaryFile != null) {
                topicFile.setFile(cloudinaryFile);
            }
        }

        TopicFile savedFile = topicFileRepo.save(topicFile);
        entityManager.flush(); // Ensure changes are persisted immediately

        // Return only the DTO fields as JSON string
        FileDataDto responseDto = topicMapper.toFileDataDto(savedFile);
        return objectMapper.writeValueAsString(responseDto);
    }

    private String updateLinkData(Topic topic, String jsonData) throws Exception {
        LinkDataDto linkData = objectMapper.readValue(jsonData, LinkDataDto.class);

        // Fetch existing link or create new one
        TopicLink topicLink = topicLinkRepo.findById(topic.getId())
                .orElseGet(() -> {
                    TopicLink newLink = new TopicLink();
                    newLink.setTopic(topic);
                    return newLink;
                });

        // Update fields
        topicLink.setDescription(linkData.getDescription());
        topicLink.setUrl(linkData.getUrl());

        TopicLink savedLink = topicLinkRepo.save(topicLink);

        // Return only the DTO fields as JSON string
        LinkDataDto responseDto = new LinkDataDto();
        responseDto.setUrl(savedLink.getUrl());
        responseDto.setDescription(savedLink.getDescription());
        return objectMapper.writeValueAsString(responseDto);
    }

    private String updatePageData(Topic topic, String jsonData) throws Exception {
        PageDataDto pageData = objectMapper.readValue(jsonData, PageDataDto.class);

        // Fetch existing page or create new one
        TopicPage topicPage = topicPageRepo.findById(topic.getId())
                .orElseGet(() -> {
                    TopicPage newPage = new TopicPage();
                    newPage.setTopic(topic);
                    return newPage;
                });

        // Update fields
        topicPage.setDescription(pageData.getDescription());
        topicPage.setContent(pageData.getContent());

        TopicPage savedPage = topicPageRepo.save(topicPage);

        // Return only the DTO fields as JSON string
        PageDataDto responseDto = topicMapper.toPageDataDto(savedPage);
        return objectMapper.writeValueAsString(responseDto);
    }

    private String updateMeetingData(Topic topic, String jsonData) throws Exception {
        // Handle case where frontend sends "[object Object]" instead of valid JSON
        if (jsonData == null || jsonData.trim().isEmpty() || jsonData.equals("[object Object]")) {
            jsonData = "{\"description\":null,\"open\":null,\"close\":null}";
        }

        MeetingDataDto meetingData = objectMapper.readValue(jsonData, MeetingDataDto.class);

        // Fetch existing meeting or create new one
        TopicMeeting topicMeeting = topicMeetingRepo.findById(topic.getId())
                .orElseGet(() -> {
                    TopicMeeting newMeeting = new TopicMeeting();
                    newMeeting.setTopic(topic);
                    return newMeeting;
                });

        // Update fields
        topicMeeting.setDescription(meetingData.getDescription());
        topicMeeting.setOpen(meetingData.getOpen());
        topicMeeting.setClose(meetingData.getClose());

        TopicMeeting savedMeeting = topicMeetingRepo.save(topicMeeting);

        // Return only the DTO fields as JSON string
        MeetingDataDto responseDto = topicMapper.toMeetingDataDto(savedMeeting);
        return objectMapper.writeValueAsString(responseDto);
    }

    private List<CloudinaryFile> processCloudinaryFiles(List<CloudinaryFileDto> fileDtos) {
        if (fileDtos == null || fileDtos.isEmpty()) {
            return new ArrayList<>();
        }
        List<CloudinaryFile> files = new ArrayList<>();
        for (CloudinaryFileDto fileDto : fileDtos) {
            if (fileDto != null) {
                files.add(processCloudinaryFile(fileDto));
            }
        }
        return files;
    }

    private CloudinaryFile processCloudinaryFile(CloudinaryFileDto fileDto) {
        if (fileDto == null) {
            return null;
        }

        if (fileDto.getId() != null) {
            // Existing file - fetch from database
            return cloudinaryFileRepo.findById(fileDto.getId())
                    .orElseThrow(() -> new CustomException("CloudinaryFile not found: " + fileDto.getId(),
                            HttpStatus.NOT_FOUND));
        } else {
            // New file - create new record with provided URLs
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
        Topic topic = topicRepo.findByIdWithSectionAndCourse(topicId)
                .orElseThrow(() -> new CustomException("Topic not found", HttpStatus.NOT_FOUND));

        // Validate that the topic belongs to a section in the specified course
        if (!topic.getSection().getCourse().getId().equals(courseId)) {
            throw new CustomException("Topic does not belong to the specified course", HttpStatus.BAD_REQUEST);
        }

        return buildTopicResponse(topic);
    } // PUT /course/{courseId}/topic/{id}

    @Transactional
    public TopicResponseDto updateTopic(String courseId, UUID topicId, UpdateTopicRequest request) {
        try {
            Topic existingTopic = topicRepo.findByIdWithSectionAndCourse(topicId)
                    .orElseThrow(() -> new CustomException("Topic not found", HttpStatus.NOT_FOUND));

            // Validate that the topic belongs to a section in the specified course
            if (!existingTopic.getSection().getCourse().getId().equals(courseId)) {
                throw new CustomException("Topic does not belong to the specified course", HttpStatus.BAD_REQUEST);
            }

            // Update base topic fields
            if (request.getTitle() != null)
                existingTopic.setTitle(request.getTitle());
            if (request.getType() != null)
                existingTopic.setType(request.getType());

            existingTopic = topicRepo.save(existingTopic);
            entityManager.flush(); // Ensure Topic is persisted before related entities

            String dataJson;
            // Only handle type-specific data if data is provided
            if (request.getData() != null) {
                // If type changed, delete old type-specific data first
                if (!existingTopic.getType().equals(request.getType())) {
                    deleteTopicSpecificData(existingTopic, existingTopic.getType());
                }
                // Update type-specific data (will create if doesn't exist)
                dataJson = updateTopicSpecificData(existingTopic, request.getType(), request.getData());
            } else {
                // No new data, just return current type-specific data
                dataJson = getTopicSpecificData(existingTopic);
            }

            // Build response
            TopicResponseDto response = new TopicResponseDto();
            response.setId(existingTopic.getId());
            response.setTitle(existingTopic.getTitle());
            response.setType(existingTopic.getType());
            response.setSectionId(existingTopic.getSection().getId());
            response.setData(dataJson);

            return response;

        } catch (Exception e) {
            throw new CustomException("Failed to update topic: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // DELETE /course/{courseId}/topic/{id}
    @Transactional
    public void deleteTopic(String courseId, UUID topicId) {
        Topic topic = topicRepo.findByIdWithSectionAndCourse(topicId)
                .orElseThrow(() -> new CustomException("Topic not found", HttpStatus.NOT_FOUND));

        // Validate that the topic belongs to a section in the specified course
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

    // GET /topic/section/{sectionId} - Get all topics for a section
    @Transactional(readOnly = true)
    public List<TopicResponseDto> getAllTopicsForSection(UUID sectionId) {
        // Validate section exists
        sectionRepo.findById(sectionId)
                .orElseThrow(() -> new CustomException("Section not found", HttpStatus.NOT_FOUND));

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
            String dataJson = getTopicSpecificData(topic);

            TopicResponseDto response = new TopicResponseDto();
            response.setId(topic.getId());
            response.setTitle(topic.getTitle());
            response.setType(topic.getType());
            response.setSectionId(topic.getSection().getId());
            response.setData(dataJson);

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

    private String getTopicSpecificData(Topic topic) throws Exception {
        switch (topic.getType().toLowerCase()) {
            case "quiz":
                return topicQuizRepo.findById(topic.getId())
                        .map(quiz -> {
                            try {
                                QuizDataDto dto = topicMapper.toQuizDataDto(quiz);
                                return objectMapper.writeValueAsString(dto);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .orElse(null);
            case "assignment":
                return topicAssignmentRepo.findById(topic.getId())
                        .map(assignment -> {
                            try {
                                AssignmentDataDto dto = topicMapper.toAssignmentDataDto(assignment);
                                return objectMapper.writeValueAsString(dto);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .orElse(null);
            case "file":
                return topicFileRepo.findById(topic.getId())
                        .map(file -> {
                            try {
                                FileDataDto dto = topicMapper.toFileDataDto(file);
                                return objectMapper.writeValueAsString(dto);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .orElse(null);
            case "link":
                return topicLinkRepo.findById(topic.getId())
                        .map(link -> {
                            try {
                                LinkDataDto dto = topicMapper.toLinkDataDto(link);
                                return objectMapper.writeValueAsString(dto);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .orElse(null);
            case "page":
                return topicPageRepo.findById(topic.getId())
                        .map(page -> {
                            try {
                                PageDataDto dto = topicMapper.toPageDataDto(page);
                                return objectMapper.writeValueAsString(dto);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .orElse(null);
            case "meeting":
                return topicMeetingRepo.findById(topic.getId())
                        .map(meeting -> {
                            try {
                                MeetingDataDto dto = topicMapper.toMeetingDataDto(meeting);
                                return objectMapper.writeValueAsString(dto);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .orElse(null);
            default:
                return null;
        }
    }

    // Additional aggregate endpoints (across courses)

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

    // ================== REPORT GENERATION METHODS ==================

    /**
     * Generate comprehensive quiz report for analytics
     */
    @Transactional(readOnly = true)
    public lms.coursehub.models.dtos.reports.SingleQuizReportDto getSingleQuizReport(String courseId, UUID topicId) {
        // Fetch topic and validate
        Topic topic = topicRepo.findByIdWithSectionAndCourse(topicId)
                .orElseThrow(() -> new CustomException("Topic not found", HttpStatus.NOT_FOUND));

        if (!topic.getSection().getCourse().getId().equals(courseId)) {
            throw new CustomException("Topic does not belong to the specified course", HttpStatus.BAD_REQUEST);
        }

        if (!"quiz".equalsIgnoreCase(topic.getType())) {
            throw new CustomException("Topic is not a quiz", HttpStatus.BAD_REQUEST);
        }

        // Fetch quiz data
        TopicQuiz topicQuiz = topicQuizRepo.findById(topicId)
                .orElseThrow(() -> new CustomException("Quiz data not found", HttpStatus.NOT_FOUND));

        // Get eligible students (enrolled before quiz close date)
        LocalDateTime closeDate = topicQuiz.getClose() != null ? topicQuiz.getClose()
                : LocalDateTime.of(3000, 12, 31, 23, 59, 59);
        List<EnrollmentDetail> eligibleEnrollments = enrollmentDetailRepo.findByCourseIdAndJoinDateBefore(courseId,
                closeDate);

        // Get all quiz responses
        List<QuizResponse> quizResponses = quizResponseRepo.findByTopicQuizId(topicId);

        // Calculate student marks using the quiz's grading method
        Map<UUID, Double> studentMarks = calculateStudentMarks(quizResponses, topicQuiz.getGradingMethod());

        // Build report
        lms.coursehub.models.dtos.reports.SingleQuizReportDto report = new lms.coursehub.models.dtos.reports.SingleQuizReportDto();
        report.setName(topic.getTitle());
        report.setStudents(eligibleEnrollments.stream()
                .map(e -> topicMapper.toUserDto(e.getStudent()))
                .toList());

        // Create student info list with marks
        List<lms.coursehub.models.dtos.reports.SingleQuizReportDto.StudentInfoAndMark> studentInfoList = createStudentInfoList(
                eligibleEnrollments, studentMarks);

        report.setStudentWithMark(studentInfoList);
        report.setStudentWithMarkOver8(filterByMark(studentInfoList, 8.0, 10.0));
        report.setStudentWithMarkOver5(filterByMark(studentInfoList, 5.0, 8.0));
        report.setStudentWithMarkOver2(filterByMark(studentInfoList, 2.0, 5.0));
        report.setStudentWithMarkOver0(filterByMark(studentInfoList, 0.0, 2.0));
        report.setStudentWithNoResponse(studentInfoList.stream()
                .filter(s -> !s.getSubmitted())
                .toList());

        // Calculate statistics
        report.setMarkDistributionCount(calculateMarkDistribution(studentMarks, eligibleEnrollments.size()));
        report.setQuestionCount(topicQuiz.getQuestions().size());
        report.setMaxDefaultMark(topicQuiz.getQuestions().stream()
                .mapToDouble(q -> q.getDefaultMark().doubleValue())
                .sum());
        report.setAvgStudentMarkBase10(studentMarks.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0));
        report.setMaxStudentMarkBase10(studentMarks.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0));
        report.setMinStudentMarkBase10(studentMarks.values().stream()
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0.0));
        report.setAttemptCount((long) quizResponses.size());
        report.setAvgTimeSpend(calculateAvgTimeSpent(quizResponses));
        report.setCompletionRate((double) studentMarks.size() / (double) eligibleEnrollments.size());

        // Count question types
        report.setTrueFalseQuestionCount(topicQuiz.getQuestions().stream()
                .filter(q -> "True/False".equalsIgnoreCase(q.getType()))
                .count());
        report.setMultipleChoiceQuestionCount(topicQuiz.getQuestions().stream()
                .filter(q -> "Choices Answer".equalsIgnoreCase(q.getType())
                        || "Multiple Choice".equalsIgnoreCase(q.getType()))
                .count());
        report.setShortAnswerQuestionCount(topicQuiz.getQuestions().stream()
                .filter(q -> "Short Answer".equalsIgnoreCase(q.getType()))
                .count());

        return report;
    }

    /**
     * Generate comprehensive assignment report for analytics
     */
    @Transactional(readOnly = true)
    public lms.coursehub.models.dtos.reports.SingleAssignmentReportDto getSingleAssignmentReport(String courseId,
            UUID topicId) {
        // Fetch topic and validate
        Topic topic = topicRepo.findByIdWithSectionAndCourse(topicId)
                .orElseThrow(() -> new CustomException("Topic not found", HttpStatus.NOT_FOUND));

        if (!topic.getSection().getCourse().getId().equals(courseId)) {
            throw new CustomException("Topic does not belong to the specified course", HttpStatus.BAD_REQUEST);
        }

        if (!"assignment".equalsIgnoreCase(topic.getType())) {
            throw new CustomException("Topic is not an assignment", HttpStatus.BAD_REQUEST);
        }

        // Fetch assignment data
        TopicAssignment topicAssignment = topicAssignmentRepo.findById(topicId)
                .orElseThrow(() -> new CustomException("Assignment data not found", HttpStatus.NOT_FOUND));

        // Get eligible students (enrolled before assignment close date)
        LocalDateTime closeDate = topicAssignment.getClose() != null ? topicAssignment.getClose()
                : LocalDateTime.of(3000, 12, 31, 23, 59, 59);
        List<EnrollmentDetail> eligibleEnrollments = enrollmentDetailRepo.findByCourseIdAndJoinDateBefore(courseId,
                closeDate);

        // Get all assignment responses
        List<AssignmentResponse> assignmentResponses = assignmentResponseRepo.findByTopicAssignmentId(topicId);

        // Calculate student marks (normalized to base 10)
        Map<UUID, AssignmentMarkInfo> studentMarks = assignmentResponses.stream()
                .filter(r -> r.getMark() != null)
                .collect(Collectors.toMap(
                        r -> r.getStudent().getId(),
                        r -> new AssignmentMarkInfo(
                                r.getMark().doubleValue() / 10.0, // Assuming marks are 0-100, normalize to 0-10
                                r.getId())));

        // Build report
        lms.coursehub.models.dtos.reports.SingleAssignmentReportDto report = new lms.coursehub.models.dtos.reports.SingleAssignmentReportDto();
        report.setName(topic.getTitle());
        report.setStudents(eligibleEnrollments.stream()
                .map(e -> topicMapper.toUserDto(e.getStudent()))
                .toList());

        // Create student info list with marks
        List<lms.coursehub.models.dtos.reports.SingleAssignmentReportDto.StudentInfoAndMark> studentInfoList = createAssignmentStudentInfoList(
                eligibleEnrollments, studentMarks);

        report.setStudentMarks(studentInfoList);
        report.setStudentWithMarkOver8(filterAssignmentByMark(studentInfoList, 8.0, 10.0));
        report.setStudentWithMarkOver5(filterAssignmentByMark(studentInfoList, 5.0, 8.0));
        report.setStudentWithMarkOver2(filterAssignmentByMark(studentInfoList, 2.0, 5.0));
        report.setStudentWithMarkOver0(filterAssignmentByMark(studentInfoList, 0.0, 2.0));
        report.setStudentWithNoResponse(studentInfoList.stream()
                .filter(s -> !s.getSubmitted())
                .toList());

        // Calculate statistics
        Map<UUID, Double> marksOnly = studentMarks.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().mark));
        report.setMarkDistributionCount(calculateMarkDistribution(marksOnly, eligibleEnrollments.size()));
        report.setSubmissionCount((long) assignmentResponses.size());
        report.setGradedSubmissionCount(assignmentResponses.stream()
                .filter(r -> r.getMark() != null)
                .count());
        report.setFileCount(assignmentResponses.stream()
                .mapToInt(r -> r.getAssignmentFiles() != null ? r.getAssignmentFiles().size() : 0)
                .sum());
        report.setAvgMark(assignmentResponses.stream()
                .filter(r -> r.getMark() != null)
                .mapToDouble(r -> r.getMark().doubleValue())
                .average()
                .orElse(0.0));
        report.setMaxMark(assignmentResponses.stream()
                .filter(r -> r.getMark() != null)
                .mapToDouble(r -> r.getMark().doubleValue())
                .max()
                .orElse(0.0));
        report.setCompletionRate((double) assignmentResponses.size() / (double) eligibleEnrollments.size());

        // Count file types
        Map<String, Long> fileTypeCount = assignmentResponses.stream()
                .filter(r -> r.getAssignmentFiles() != null)
                .flatMap(r -> r.getAssignmentFiles().stream())
                .map(file -> {
                    String name = file.getName();
                    int dotIndex = name.lastIndexOf('.');
                    return dotIndex > 0 ? name.substring(dotIndex + 1).toLowerCase() : "unknown";
                })
                .collect(Collectors.groupingBy(ext -> ext, Collectors.counting()));
        report.setFileTypeCount(fileTypeCount);

        return report;
    }

    // ================== HELPER METHODS ==================

    private Map<UUID, Double> calculateStudentMarks(List<QuizResponse> responses, String gradingMethod) {
        // Group responses by student
        Map<UUID, List<QuizResponse>> responsesByStudent = responses.stream()
                .collect(Collectors.groupingBy(r -> r.getStudent().getId()));

        // Calculate mark for each student based on grading method
        return responsesByStudent.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> calculateMarkByMethod(entry.getValue(), gradingMethod)));
    }

    private Double calculateMarkByMethod(List<QuizResponse> studentResponses, String method) {
        // Calculate total mark for each response (normalized to base 10)
        List<Double> marks = studentResponses.stream()
                .map(this::calculateResponseMark)
                .toList();

        return switch (method != null ? method : "Highest Grade") {
            case "Highest Grade" -> marks.stream().max(Double::compare).orElse(0.0);
            case "Average Grade" -> marks.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            case "First Grade" -> !marks.isEmpty() ? marks.get(0) : 0.0;
            case "Last Grade" -> !marks.isEmpty() ? marks.get(marks.size() - 1) : 0.0;
            default -> marks.stream().max(Double::compare).orElse(0.0);
        };
    }

    private Double calculateResponseMark(QuizResponse response) {
        // Calculate mark as percentage (0-10 scale)
        List<QuizResponseAnswer> answers = response.getQuizResponseAnswers();
        if (answers.isEmpty())
            return 0.0;

        double totalMark = answers.stream()
                .mapToDouble(a -> a.getMark() != null ? a.getMark().doubleValue() : 0.0)
                .sum();

        // Normalize to 0-10 scale (assuming marks are already in proper scale)
        return totalMark;
    }

    private List<lms.coursehub.models.dtos.reports.SingleQuizReportDto.StudentInfoAndMark> createStudentInfoList(
            List<EnrollmentDetail> enrollments, Map<UUID, Double> marks) {

        return enrollments.stream()
                .map(enrollment -> {
                    lms.coursehub.models.dtos.reports.SingleQuizReportDto.StudentInfoAndMark info = new lms.coursehub.models.dtos.reports.SingleQuizReportDto.StudentInfoAndMark();
                    info.setStudent(topicMapper.toUserDto(enrollment.getStudent()));

                    UUID studentId = enrollment.getStudent().getId();
                    if (marks.containsKey(studentId)) {
                        info.setMark(marks.get(studentId));
                        info.setSubmitted(true);
                        // Note: responseId is tricky with multiple attempts - leaving null for now
                    } else {
                        info.setMark(0.0);
                        info.setSubmitted(false);
                    }

                    return info;
                })
                .toList();
    }

    private List<lms.coursehub.models.dtos.reports.SingleAssignmentReportDto.StudentInfoAndMark> createAssignmentStudentInfoList(
            List<EnrollmentDetail> enrollments, Map<UUID, AssignmentMarkInfo> marks) {

        return enrollments.stream()
                .map(enrollment -> {
                    lms.coursehub.models.dtos.reports.SingleAssignmentReportDto.StudentInfoAndMark info = new lms.coursehub.models.dtos.reports.SingleAssignmentReportDto.StudentInfoAndMark();
                    info.setStudent(topicMapper.toUserDto(enrollment.getStudent()));

                    UUID studentId = enrollment.getStudent().getId();
                    if (marks.containsKey(studentId)) {
                        AssignmentMarkInfo markInfo = marks.get(studentId);
                        info.setMark(markInfo.mark);
                        info.setResponseId(markInfo.responseId);
                        info.setSubmitted(true);
                    } else {
                        info.setMark(0.0);
                        info.setSubmitted(false);
                    }

                    return info;
                })
                .toList();
    }

    private List<lms.coursehub.models.dtos.reports.SingleQuizReportDto.StudentInfoAndMark> filterByMark(
            List<lms.coursehub.models.dtos.reports.SingleQuizReportDto.StudentInfoAndMark> students,
            double minMark, double maxMark) {
        return students.stream()
                .filter(s -> s.getSubmitted() && s.getMark() != null &&
                        s.getMark() >= minMark && s.getMark() < maxMark)
                .toList();
    }

    private List<lms.coursehub.models.dtos.reports.SingleAssignmentReportDto.StudentInfoAndMark> filterAssignmentByMark(
            List<lms.coursehub.models.dtos.reports.SingleAssignmentReportDto.StudentInfoAndMark> students,
            double minMark, double maxMark) {
        return students.stream()
                .filter(s -> s.getSubmitted() && s.getMark() != null &&
                        s.getMark() >= minMark && s.getMark() < maxMark)
                .toList();
    }

    private Map<Integer, Long> calculateMarkDistribution(Map<UUID, Double> marks, int totalStudents) {
        Map<Integer, Long> distribution = new HashMap<>();

        long count8Plus = marks.values().stream().filter(m -> m >= 8.0).count();
        long count5To8 = marks.values().stream().filter(m -> m >= 5.0 && m < 8.0).count();
        long count2To5 = marks.values().stream().filter(m -> m >= 2.0 && m < 5.0).count();
        long count0To2 = marks.values().stream().filter(m -> m >= 0.0 && m < 2.0).count();
        long noResponse = totalStudents - marks.size();

        distribution.put(8, count8Plus);
        distribution.put(5, count5To8);
        distribution.put(2, count2To5);
        distribution.put(0, count0To2);
        distribution.put(-1, noResponse);

        return distribution;
    }

    private Double calculateAvgTimeSpent(List<QuizResponse> responses) {
        return responses.stream()
                .filter(r -> r.getStartedAt() != null && r.getCompletedAt() != null)
                .mapToDouble(r -> {
                    long seconds = java.time.Duration.between(r.getStartedAt(), r.getCompletedAt()).getSeconds();
                    return (double) seconds;
                })
                .average()
                .orElse(0.0);
    }

    // Helper class for assignment mark info
    private static class AssignmentMarkInfo {
        final Double mark;
        final UUID responseId;

        AssignmentMarkInfo(Double mark, UUID responseId) {
            this.mark = mark;
            this.responseId = responseId;
        }
    }

    // ===== COURSE-LEVEL REPORT METHODS =====

    /**
     * Get aggregated quiz report for all quizzes in a course within a date range
     */
    public AllQuizzesReportDto getAllQuizzesReport(String courseId, LocalDateTime startTime, LocalDateTime endTime) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new CustomException("Course not found", HttpStatus.NOT_FOUND));

        List<SingleQuizReportDto> singleQuizReportDtos = new ArrayList<>();

        // Define min/max times for null open/close dates
        LocalDateTime MIN = LocalDateTime.of(1000, 12, 31, 23, 59, 59);
        LocalDateTime MAX = LocalDateTime.of(3000, 12, 31, 23, 59, 59);

        // Iterate through all sections and topics in the course
        course.getSections().forEach(section -> {
            section.getTopics().forEach(topic -> {
                if ("quiz".equals(topic.getType())) {
                    // Get the quiz details
                    topicQuizRepo.findById(topic.getId()).ifPresent(topicQuiz -> {
                        LocalDateTime topicStart = (topicQuiz.getOpen() == null) ? MIN : topicQuiz.getOpen();
                        LocalDateTime topicEnd = (topicQuiz.getClose() == null) ? MAX : topicQuiz.getClose();

                        // Check if quiz falls within the date range
                        if (topicStart.isBefore(endTime) && topicEnd.isAfter(startTime)) {
                            singleQuizReportDtos.add(getSingleQuizReport(courseId, topic.getId()));
                        }
                    });
                }
            });
        });

        // Calculate average student scores across all quizzes
        List<SingleQuizReportDto.StudentInfoAndMark> studentInfoAndMarks = calculateAverageStudentScoreForQuizzes(
                singleQuizReportDtos);

        // Build the aggregated report
        AllQuizzesReportDto reportDto = new AllQuizzesReportDto();
        reportDto.setQuizCount(singleQuizReportDtos.size());
        reportDto.setAvgCompletionPercentage(
                singleQuizReportDtos.stream()
                        .mapToDouble(SingleQuizReportDto::getCompletionRate)
                        .average()
                        .orElse(0.0));
        reportDto.setMinQuestionCount(
                singleQuizReportDtos.stream()
                        .mapToInt(rep -> rep.getQuestionCount().intValue())
                        .min()
                        .orElse(0));
        reportDto.setMaxQuestionCount(
                singleQuizReportDtos.stream()
                        .mapToInt(rep -> rep.getQuestionCount().intValue())
                        .max()
                        .orElse(0));
        reportDto.setMinStudentScoreBase10(
                singleQuizReportDtos.stream()
                        .mapToDouble(SingleQuizReportDto::getMinStudentMarkBase10)
                        .min()
                        .orElse(0.0));
        reportDto.setMaxStudentScoreBase10(
                singleQuizReportDtos.stream()
                        .mapToDouble(SingleQuizReportDto::getMaxStudentMarkBase10)
                        .max()
                        .orElse(0.0));

        reportDto.setStudentInfoWithMarkAverage(studentInfoAndMarks);

        // Filter students by mark ranges
        reportDto.setStudentWithMarkOver8(
                studentInfoAndMarks.stream()
                        .filter(info -> info.getSubmitted() && info.getMark() != null && info.getMark() >= 8.0)
                        .toList());
        reportDto.setStudentWithMarkOver5(
                studentInfoAndMarks.stream()
                        .filter(info -> info.getSubmitted() && info.getMark() != null &&
                                info.getMark() >= 5.0 && info.getMark() < 8.0)
                        .toList());
        reportDto.setStudentWithMarkOver2(
                studentInfoAndMarks.stream()
                        .filter(info -> info.getSubmitted() && info.getMark() != null &&
                                info.getMark() >= 2.0 && info.getMark() < 5.0)
                        .toList());
        reportDto.setStudentWithMarkOver0(
                studentInfoAndMarks.stream()
                        .filter(info -> info.getSubmitted() && info.getMark() != null && info.getMark() < 2.0)
                        .toList());
        reportDto.setStudentWithNoResponse(
                studentInfoAndMarks.stream()
                        .filter(info -> !info.getSubmitted())
                        .toList());

        // Merge mark distributions from all quizzes
        reportDto.setMarkDistributionCount(
                mergeMarkDistributionCount(
                        singleQuizReportDtos.stream()
                                .map(SingleQuizReportDto::getMarkDistributionCount)
                                .toList()));

        reportDto.setSingleQuizReports(singleQuizReportDtos);

        // Count question types across all quizzes
        reportDto.setTrueFalseQuestionCount(
                singleQuizReportDtos.stream()
                        .mapToInt(rep -> rep.getTrueFalseQuestionCount().intValue())
                        .sum());
        reportDto.setMultipleChoiceQuestionCount(
                singleQuizReportDtos.stream()
                        .mapToInt(rep -> rep.getMultipleChoiceQuestionCount().intValue())
                        .sum());
        reportDto.setShortAnswerQuestionCount(
                singleQuizReportDtos.stream()
                        .mapToInt(rep -> rep.getShortAnswerQuestionCount().intValue())
                        .sum());

        return reportDto;
    }

    /**
     * Get aggregated assignment report for all assignments in a course within a
     * date range
     */
    public AllAssignmentsReportDto getAllAssignmentsReport(String courseId, LocalDateTime startTime,
            LocalDateTime endTime) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new CustomException("Course not found", HttpStatus.NOT_FOUND));

        List<SingleAssignmentReportDto> singleAssignmentReportDtos = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        int[] assignmentsEndingThisMonth = { 0 };
        int[] assignmentsInProgressCounter = { 0 };
        LocalDateTime[] nextClosestEndTime = { null };

        // Define min/max times for null open/close dates
        LocalDateTime MIN = LocalDateTime.of(1000, 12, 31, 23, 59, 59);
        LocalDateTime MAX = LocalDateTime.of(3000, 12, 31, 23, 59, 59);

        // Iterate through all sections and topics in the course
        course.getSections().forEach(section -> {
            section.getTopics().forEach(topic -> {
                if ("assignment".equals(topic.getType())) {
                    topicAssignmentRepo.findById(topic.getId()).ifPresent(topicAssignment -> {
                        LocalDateTime topicStart = (topicAssignment.getOpen() == null) ? MIN
                                : topicAssignment.getOpen();
                        LocalDateTime topicEnd = (topicAssignment.getClose() == null) ? MAX
                                : topicAssignment.getClose();

                        // Check if assignment falls within the date range
                        if (topicStart.isBefore(endTime) && topicEnd.isAfter(startTime)) {
                            singleAssignmentReportDtos.add(getSingleAssignmentReport(courseId, topic.getId()));

                            // Count assignments ending this month
                            if (topicEnd.isAfter(monthStart) && topicEnd.isBefore(monthEnd) && topicEnd.isAfter(now)) {
                                assignmentsEndingThisMonth[0]++;
                            }

                            // Count assignments in progress
                            if (topicEnd.isAfter(now)) {
                                assignmentsInProgressCounter[0]++;
                            }

                            // Find next closest end time
                            if (topicEnd.isAfter(now)) {
                                if (nextClosestEndTime[0] == null || topicEnd.isBefore(nextClosestEndTime[0])) {
                                    nextClosestEndTime[0] = topicEnd;
                                }
                            }
                        }
                    });
                }
            });
        });

        // Calculate average student scores across all assignments
        List<AllAssignmentsReportDto.StudentInfoWithAverageMark> studentInfoWithAverageMarks = calculateAverageStudentScoreForAssignments(
                singleAssignmentReportDtos);

        // Build the aggregated report
        AllAssignmentsReportDto reportDto = new AllAssignmentsReportDto();
        reportDto.setAssignmentsCountInProgress(assignmentsInProgressCounter[0]);
        reportDto.setAssignmentCount(singleAssignmentReportDtos.size());
        reportDto.setAvgMark(
                singleAssignmentReportDtos.stream()
                        .mapToDouble(SingleAssignmentReportDto::getAvgMark)
                        .average()
                        .orElse(0.0));
        reportDto.setAvgCompletionRate(
                singleAssignmentReportDtos.stream()
                        .mapToDouble(SingleAssignmentReportDto::getCompletionRate)
                        .average()
                        .orElse(0.0));
        reportDto.setNumberOfAssignmentEndsAtThisMonth(assignmentsEndingThisMonth[0]);
        reportDto.setClosestNextEndAssignment(nextClosestEndTime[0]);

        // Merge mark distributions
        reportDto.setMarkDistributionCount(
                mergeMarkDistributionCount(
                        singleAssignmentReportDtos.stream()
                                .map(SingleAssignmentReportDto::getMarkDistributionCount)
                                .toList()));

        reportDto.setStudentInfoWithMarkAverage(studentInfoWithAverageMarks);

        // Filter students by mark ranges
        reportDto.setStudentWithMarkOver8(
                studentInfoWithAverageMarks.stream()
                        .filter(info -> info.getSubmitted() && info.getAverageMark() != null &&
                                info.getAverageMark() >= 8.0)
                        .toList());
        reportDto.setStudentWithMarkOver5(
                studentInfoWithAverageMarks.stream()
                        .filter(info -> info.getSubmitted() && info.getAverageMark() != null &&
                                info.getAverageMark() >= 5.0 && info.getAverageMark() < 8.0)
                        .toList());
        reportDto.setStudentWithMarkOver2(
                studentInfoWithAverageMarks.stream()
                        .filter(info -> info.getSubmitted() && info.getAverageMark() != null &&
                                info.getAverageMark() >= 2.0 && info.getAverageMark() < 5.0)
                        .toList());
        reportDto.setStudentWithMarkOver0(
                studentInfoWithAverageMarks.stream()
                        .filter(info -> info.getSubmitted() && info.getAverageMark() != null &&
                                info.getAverageMark() < 2.0)
                        .toList());
        reportDto.setStudentWithNoResponse(
                studentInfoWithAverageMarks.stream()
                        .filter(info -> !info.getSubmitted())
                        .toList());

        // Aggregate file type counts
        reportDto.setFileTypeCount(
                singleAssignmentReportDtos.stream()
                        .map(SingleAssignmentReportDto::getFileTypeCount)
                        .flatMap(map -> map.entrySet().stream())
                        .collect(Collectors.groupingBy(
                                Map.Entry::getKey,
                                Collectors.summingLong(Map.Entry::getValue))));

        reportDto.setSingleAssignmentReports(singleAssignmentReportDtos);

        return reportDto;
    }

    /**
     * Calculate average scores for students across multiple quizzes
     */
    private List<SingleQuizReportDto.StudentInfoAndMark> calculateAverageStudentScoreForQuizzes(
            List<SingleQuizReportDto> singleQuizReports) {

        Map<UUID, List<Double>> studentScoresMap = new HashMap<>();
        Map<UUID, SingleQuizReportDto.StudentInfoAndMark> latestStudentInfo = new HashMap<>();

        // Collect scores and latest info for each student across all quizzes
        for (SingleQuizReportDto report : singleQuizReports) {
            if (report.getStudentWithMark() == null)
                continue;

            report.getStudentWithMark().forEach(infoAndMark -> {
                if (infoAndMark.getStudent() != null && infoAndMark.getSubmitted()) {
                    UUID studentId = infoAndMark.getStudent().getId();
                    studentScoresMap.computeIfAbsent(studentId, k -> new ArrayList<>()).add(infoAndMark.getMark());
                    latestStudentInfo.put(studentId, infoAndMark);
                }
            });
        }

        // Create final list with averaged scores
        return studentScoresMap.entrySet().stream()
                .map(entry -> {
                    UUID studentId = entry.getKey();
                    List<Double> scores = entry.getValue();
                    SingleQuizReportDto.StudentInfoAndMark existingInfo = latestStudentInfo.get(studentId);

                    SingleQuizReportDto.StudentInfoAndMark avgInfo = new SingleQuizReportDto.StudentInfoAndMark();
                    avgInfo.setStudent(existingInfo.getStudent());
                    avgInfo.setSubmitted(existingInfo.getSubmitted());
                    avgInfo.setResponseId(existingInfo.getResponseId());
                    avgInfo.setMark(scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));

                    return avgInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculate average scores for students across multiple assignments
     */
    private List<AllAssignmentsReportDto.StudentInfoWithAverageMark> calculateAverageStudentScoreForAssignments(
            List<SingleAssignmentReportDto> singleAssignmentReports) {

        Map<UUID, List<Double>> studentScoresMap = new HashMap<>();
        Map<UUID, SingleAssignmentReportDto.StudentInfoAndMark> latestStudentInfo = new HashMap<>();

        // Collect scores and latest info for each student across all assignments
        for (SingleAssignmentReportDto report : singleAssignmentReports) {
            if (report.getStudentMarks() == null)
                continue;

            report.getStudentMarks().forEach(infoAndMark -> {
                if (infoAndMark.getStudent() != null && infoAndMark.getSubmitted()) {
                    UUID studentId = infoAndMark.getStudent().getId();
                    studentScoresMap.computeIfAbsent(studentId, k -> new ArrayList<>()).add(infoAndMark.getMark());
                    latestStudentInfo.put(studentId, infoAndMark);
                }
            });
        }

        // Create final list with averaged scores
        return studentScoresMap.entrySet().stream()
                .map(entry -> {
                    UUID studentId = entry.getKey();
                    List<Double> scores = entry.getValue();
                    SingleAssignmentReportDto.StudentInfoAndMark existingInfo = latestStudentInfo.get(studentId);

                    double averageMark = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    return new AllAssignmentsReportDto.StudentInfoWithAverageMark(
                            existingInfo.getStudent(),
                            averageMark,
                            existingInfo.getSubmitted());
                })
                .toList();
    }

    /**
     * Merge mark distribution counts from multiple reports
     */
    private Map<Number, Number> mergeMarkDistributionCount(List<?> markDistributionCount) {
        Map<Integer, Long> groupedValues = new HashMap<>();
        Integer[] keys = { -1, 0, 2, 5, 8 };

        for (Object distObj : markDistributionCount) {
            @SuppressWarnings("unchecked")
            Map<Integer, Long> distribution = (Map<Integer, Long>) distObj;

            for (Integer key : keys) {
                Long value = distribution.getOrDefault(key, 0L);
                groupedValues.put(key, groupedValues.getOrDefault(key, 0L) + value);
            }
        }

        // Convert to Map<Number, Number>
        Map<Number, Number> result = new HashMap<>();
        groupedValues.forEach((k, v) -> result.put(k, v));
        return result;
    }
}
