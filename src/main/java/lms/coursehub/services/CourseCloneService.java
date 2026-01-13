package lms.coursehub.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.models.dtos.course.CloneCourseRequest;
import lms.coursehub.models.dtos.course.CloneCourseResponse;
import lms.coursehub.models.entities.*;
import lms.coursehub.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for cloning courses with all associated content
 * Handles deep copying of courses, sections, topics, and question banks
 */
@Service
@RequiredArgsConstructor
public class CourseCloneService {

    private final CourseRepo courseRepo;
    private final SectionRepo sectionRepo;
    private final TopicRepo topicRepo;
    private final QuestionRepo questionRepo;
    private final QuestionChoiceRepo questionChoiceRepo;
    private final EnrollmentDetailRepo enrollmentDetailRepo;
    private final UserService userService;
    
    // Topic-specific repos
    private final TopicQuizRepo topicQuizRepo;
    private final TopicAssignmentRepo topicAssignmentRepo;
    private final TopicPageRepo topicPageRepo;
    private final TopicLinkRepo topicLinkRepo;
    private final TopicFileRepo topicFileRepo;
    private final TopicMeetingRepo topicMeetingRepo;
    private final CloudinaryFileRepo cloudinaryFileRepo;

    /**
     * Clone a course with all its content
     */
    @Transactional
    public CloneCourseResponse cloneCourse(String sourceCourseId, CloneCourseRequest request) {
        
        // 1. Validate new course ID doesn't exist
        if (courseRepo.existsById(request.getNewCourseId())) {
            throw new CustomException("Course ID already exists", HttpStatus.BAD_REQUEST);
        }

        Course source = courseRepo.findById(sourceCourseId)
                .orElseThrow(() -> new CustomException("Source course not found", HttpStatus.NOT_FOUND));

        User currentUser = userService.getCurrentUser();
        
        // 2. Permission check: Only creator or admin can clone
        if (!source.getCreator().getId().equals(currentUser.getId()) 
            && !currentUser.getRole().name().equals("ADMIN")) {
            throw new CustomException("Only course creator or admin can clone", HttpStatus.FORBIDDEN);
        }

        // 3. Create new course skeleton
        Course clone = new Course();
        clone.setId(request.getNewCourseId());
        clone.setTitle(request.getTitle() != null ? request.getTitle() : source.getTitle() + " (Copy)");
        clone.setDescription(request.getDescription() != null ? request.getDescription() : source.getDescription());
        clone.setImageUrl(request.getImageUrl() != null ? request.getImageUrl() : source.getImageUrl());
        clone.setPrice(request.getPrice() != null ? request.getPrice() : source.getPrice());
        clone.setCategory(request.getCategory() != null ? request.getCategory() : source.getCategory());
        clone.setLevel(request.getLevel() != null ? request.getLevel() : source.getLevel());
        clone.setPublished(request.getIsPublished() != null ? request.getIsPublished() : false); // Default unpublished
        clone.setTotalJoined(1); // Creator is auto-enrolled
        clone.setCreator(currentUser);
        
        clone = courseRepo.save(clone);

        // 4. Clone sections and topics
        Map<UUID, UUID> topicIdMap = new HashMap<>();
        Map<UUID, UUID> questionIdMap = new HashMap<>();
        
        int sectionCount = 0;
        int topicCount = 0;

        for (Section sourceSection : source.getSections()) {
            Section clonedSection = new Section();
            clonedSection.setCourse(clone);
            clonedSection.setTitle(sourceSection.getTitle());
            clonedSection.setOrder(sourceSection.getOrder());
            
            clonedSection = sectionRepo.save(clonedSection);
            sectionCount++;

            for (Topic sourceTopic : sourceSection.getTopics()) {
                UUID newTopicId = UUID.randomUUID();
                topicIdMap.put(sourceTopic.getId(), newTopicId);

                Topic clonedTopic = new Topic();
                clonedTopic.setId(newTopicId);
                clonedTopic.setSection(clonedSection);
                clonedTopic.setTitle(sourceTopic.getTitle());
                clonedTopic.setTopicType(sourceTopic.getTopicType());
                clonedTopic.setOrder(sourceTopic.getOrder());
                clonedTopic.setCreatedAt(LocalDateTime.now());
                clonedTopic.setUpdatedAt(LocalDateTime.now());
                
                topicRepo.save(clonedTopic);
                topicCount++;
            }
        }

        // 5. Clone question bank
        cloneQuestionBank(sourceCourseId, clone.getId(), questionIdMap, currentUser.getId());

        // 6. Clone topic content (quiz, assignment, page, link, file, meeting)
        cloneTopicContent(source, topicIdMap, questionIdMap);

        // 7. Auto-enroll creator
        EnrollmentDetail enrollment = new EnrollmentDetail();
        enrollment.setCourse(clone);
        enrollment.setStudent(currentUser);
        enrollmentDetailRepo.save(enrollment);

        return new CloneCourseResponse(
            clone.getId(),
            sourceCourseId,
            sectionCount,
            topicCount
        );
    }

    /**
     * Clone all questions in the question bank
     */
    private void cloneQuestionBank(String sourceCourseId, String targetCourseId, 
                                   Map<UUID, UUID> questionIdMap, UUID userId) {
        List<Question> sourceQuestions = questionRepo.findByCourseId(sourceCourseId);

        for (Question sourceQuestion : sourceQuestions) {
            UUID newQuestionId = UUID.randomUUID();
            questionIdMap.put(sourceQuestion.getId(), newQuestionId);

            Question clonedQuestion = new Question();
            clonedQuestion.setId(newQuestionId);
            clonedQuestion.setCourseId(targetCourseId);
            clonedQuestion.setQuestionName(sourceQuestion.getQuestionName());
            clonedQuestion.setQuestionText(sourceQuestion.getQuestionText());
            clonedQuestion.setType(sourceQuestion.getType());
            clonedQuestion.setDefaultMark(sourceQuestion.getDefaultMark());
            clonedQuestion.setStatus(sourceQuestion.getStatus());
            clonedQuestion.setUsage(sourceQuestion.getUsage());
            clonedQuestion.setFeedbackOfTrue(sourceQuestion.getFeedbackOfTrue());
            clonedQuestion.setFeedbackOfFalse(sourceQuestion.getFeedbackOfFalse());
            clonedQuestion.setCorrectAnswer(sourceQuestion.getCorrectAnswer());
            clonedQuestion.setMultiple(sourceQuestion.getMultiple());
            clonedQuestion.setCreatedById(userId);
            clonedQuestion.setModifiedById(userId);
            clonedQuestion.setCreatedAt(LocalDateTime.now());
            clonedQuestion.setUpdatedAt(LocalDateTime.now());
            
            questionRepo.save(clonedQuestion);

            // Clone choices
            for (QuestionChoice sourceChoice : sourceQuestion.getChoices()) {
                QuestionChoice clonedChoice = new QuestionChoice();
                clonedChoice.setId(UUID.randomUUID());
                clonedChoice.setQuestion(clonedQuestion);
                clonedChoice.setText(sourceChoice.getText());
                clonedChoice.setFeedback(sourceChoice.getFeedback());
                clonedChoice.setGradePercent(sourceChoice.getGradePercent());
                
                questionChoiceRepo.save(clonedChoice);
            }
        }
    }

    /**
     * Clone topic-specific content
     */
    private void cloneTopicContent(Course source, Map<UUID, UUID> topicIdMap, 
                                   Map<UUID, UUID> questionIdMap) {
        for (Section section : source.getSections()) {
            for (Topic sourceTopic : section.getTopics()) {
                UUID newTopicId = topicIdMap.get(sourceTopic.getId());
                Topic clonedTopic = topicRepo.findById(newTopicId).orElseThrow();

                String topicType = sourceTopic.getTopicType().toLowerCase();
                
                switch (topicType) {
                    case "quiz":
                        cloneTopicQuiz(sourceTopic, clonedTopic, questionIdMap);
                        break;
                    case "assignment":
                        cloneTopicAssignment(sourceTopic, clonedTopic);
                        break;
                    case "page":
                        cloneTopicPage(sourceTopic, clonedTopic);
                        break;
                    case "link":
                        cloneTopicLink(sourceTopic, clonedTopic);
                        break;
                    case "file":
                        cloneTopicFile(sourceTopic, clonedTopic);
                        break;
                    case "meeting":
                        cloneTopicMeeting(sourceTopic, clonedTopic);
                        break;
                }
            }
        }
    }

    private void cloneTopicQuiz(Topic source, Topic target, Map<UUID, UUID> questionIdMap) {
        TopicQuiz sourceQuiz = topicQuizRepo.findByTopicId(source.getId()).orElse(null);
        if (sourceQuiz == null) return;

        TopicQuiz clonedQuiz = new TopicQuiz();
        clonedQuiz.setTopic(target);
        clonedQuiz.setDescription(sourceQuiz.getDescription());
        clonedQuiz.setOpen(sourceQuiz.getOpen());
        clonedQuiz.setClose(sourceQuiz.getClose());
        clonedQuiz.setTimeLimit(sourceQuiz.getTimeLimit());
        clonedQuiz.setTimeLimitUnit(sourceQuiz.getTimeLimitUnit());
        clonedQuiz.setGradeToPass(sourceQuiz.getGradeToPass());
        clonedQuiz.setGradingMethod(sourceQuiz.getGradingMethod());
        clonedQuiz.setAttemptAllowed(sourceQuiz.getAttemptAllowed());
        clonedQuiz.setShuffle(sourceQuiz.getShuffle());
        
        // Remap question IDs
        List<UUID> newQuestionIds = new ArrayList<>();
        for (UUID oldId : sourceQuiz.getQuestionIds()) {
            UUID newId = questionIdMap.getOrDefault(oldId, oldId);
            newQuestionIds.add(newId);
        }
        clonedQuiz.setQuestionIds(newQuestionIds);
        
        topicQuizRepo.save(clonedQuiz);
    }

    private void cloneTopicAssignment(Topic source, Topic target) {
        TopicAssignment sourceAssignment = topicAssignmentRepo.findByTopicId(source.getId()).orElse(null);
        if (sourceAssignment == null) return;

        TopicAssignment clonedAssignment = new TopicAssignment();
        clonedAssignment.setTopic(target);
        clonedAssignment.setInstruction(sourceAssignment.getInstruction());
        clonedAssignment.setOpen(sourceAssignment.getOpen());
        clonedAssignment.setDueDate(sourceAssignment.getDueDate());
        clonedAssignment.setMaximumFile(sourceAssignment.getMaximumFile());
        clonedAssignment.setMaximumFileSize(sourceAssignment.getMaximumFileSize());
        clonedAssignment.setRemindToGrade(sourceAssignment.getRemindToGrade());
        
        // Clone cloudinary files
        for (CloudinaryFile sourceFile : sourceAssignment.getCloudinaryFiles()) {
            CloudinaryFile clonedFile = new CloudinaryFile();
            clonedFile.setTopicAssignment(clonedAssignment);
            clonedFile.setPublicId(sourceFile.getPublicId());
            clonedFile.setUrl(sourceFile.getUrl());
            clonedFile.setFormat(sourceFile.getFormat());
            clonedFile.setResourceType(sourceFile.getResourceType());
            cloudinaryFileRepo.save(clonedFile);
        }
        
        topicAssignmentRepo.save(clonedAssignment);
    }

    private void cloneTopicPage(Topic source, Topic target) {
        TopicPage sourcePage = topicPageRepo.findByTopicId(source.getId()).orElse(null);
        if (sourcePage == null) return;

        TopicPage clonedPage = new TopicPage();
        clonedPage.setTopic(target);
        clonedPage.setDescription(sourcePage.getDescription());
        clonedPage.setContent(sourcePage.getContent());
        
        topicPageRepo.save(clonedPage);
    }

    private void cloneTopicLink(Topic source, Topic target) {
        TopicLink sourceLink = topicLinkRepo.findByTopicId(source.getId()).orElse(null);
        if (sourceLink == null) return;

        TopicLink clonedLink = new TopicLink();
        clonedLink.setTopic(target);
        clonedLink.setDescription(sourceLink.getDescription());
        clonedLink.setUrl(sourceLink.getUrl());
        
        topicLinkRepo.save(clonedLink);
    }

    private void cloneTopicFile(Topic source, Topic target) {
        TopicFile sourceFile = topicFileRepo.findByTopicId(source.getId()).orElse(null);
        if (sourceFile == null) return;

        TopicFile clonedFile = new TopicFile();
        clonedFile.setTopic(target);
        clonedFile.setDescription(sourceFile.getDescription());
        clonedFile.setFileUrl(sourceFile.getFileUrl());
        clonedFile.setFileName(sourceFile.getFileName());
        
        topicFileRepo.save(clonedFile);
    }

    private void cloneTopicMeeting(Topic source, Topic target) {
        TopicMeeting sourceMeeting = topicMeetingRepo.findByTopicId(source.getId()).orElse(null);
        if (sourceMeeting == null) return;

        TopicMeeting clonedMeeting = new TopicMeeting();
        clonedMeeting.setTopic(target);
        clonedMeeting.setDescription(sourceMeeting.getDescription());
        clonedMeeting.setOpen(sourceMeeting.getOpen());
        clonedMeeting.setClose(sourceMeeting.getClose());
        
        topicMeetingRepo.save(clonedMeeting);
    }
}
