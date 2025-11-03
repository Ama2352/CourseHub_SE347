package lms.coursehub.services;

import lms.coursehub.helpers.exceptions.CustomException;
import lms.coursehub.helpers.mapstructs.CommentMapper;
import lms.coursehub.models.dtos.comment.CommentResponseDto;
import lms.coursehub.models.dtos.comment.CreateCommentRequest;
import lms.coursehub.models.entities.Comment;
import lms.coursehub.models.entities.CustomUserDetails;
import lms.coursehub.models.entities.Topic;
import lms.coursehub.models.entities.User;
import lms.coursehub.repositories.CommentRepo;
import lms.coursehub.repositories.TopicRepo;
import lms.coursehub.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepo commentRepo;
    private final TopicRepo topicRepo;
    private final UserRepo userRepo;
    private final CommentMapper commentMapper;

    /**
     * Get all comments for a specific topic
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(String courseId, UUID topicId) {
        // Verify topic exists and belongs to the course
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new CustomException("Topic not found", HttpStatus.NOT_FOUND));

        if (!topic.getSection().getCourse().getId().equals(courseId)) {
            throw new CustomException("Topic does not belong to this course", HttpStatus.BAD_REQUEST);
        }

        // Get all comments for the topic
        List<Comment> comments = commentRepo.findByTopicIdOrderByCreatedAtDesc(topicId);

        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a new comment for a topic
     */
    @Transactional
    public CommentResponseDto createComment(String courseId, UUID topicId, CreateCommentRequest request) {
        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        // Verify topic exists and belongs to the course
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new CustomException("Topic not found", HttpStatus.NOT_FOUND));

        if (!topic.getSection().getCourse().getId().equals(courseId)) {
            throw new CustomException("Topic does not belong to this course", HttpStatus.BAD_REQUEST);
        }

        // Create comment
        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setUser(user);
        comment.setTopic(topic);

        Comment savedComment = commentRepo.save(comment);

        return commentMapper.toDto(savedComment);
    }

    /**
     * Delete a comment by ID
     */
    @Transactional
    public void deleteComment(String courseId, UUID topicId, UUID commentId) {
        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        User currentUser = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        // Verify comment exists
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new CustomException("Comment not found", HttpStatus.NOT_FOUND));

        // Verify comment belongs to the topic
        if (!comment.getTopic().getId().equals(topicId)) {
            throw new CustomException("Comment does not belong to this topic", HttpStatus.BAD_REQUEST);
        }

        // Verify topic belongs to the course
        if (!comment.getTopic().getSection().getCourse().getId().equals(courseId)) {
            throw new CustomException("Topic does not belong to this course", HttpStatus.BAD_REQUEST);
        }

        // Verify user is the comment owner or has permission
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException("You are not authorized to delete this comment", HttpStatus.FORBIDDEN);
        }

        commentRepo.delete(comment);
    }
}
