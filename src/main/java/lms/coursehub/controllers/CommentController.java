package lms.coursehub.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lms.coursehub.models.dtos.comment.CommentResponseDto;
import lms.coursehub.models.dtos.comment.CreateCommentRequest;
import lms.coursehub.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course/{courseId}/topic/{topicId}/comments")
@Tag(name = "Comment Management", description = "APIs for managing comments on topics")
public class CommentController {

    private final CommentService commentService;

    @Operation(
            summary = "Get all comments for a topic",
            description = "Retrieves all comments for a specific topic in a course, ordered by creation date (newest first)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of comments",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Topic not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Topic does not belong to this course",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @Parameter(description = "ID of the course", required = true)
            @PathVariable String courseId,
            @Parameter(description = "ID of the topic", required = true)
            @PathVariable UUID topicId) {

        List<CommentResponseDto> comments = commentService.getComments(courseId, topicId);
        return ResponseEntity.ok(comments);
    }

    @Operation(
            summary = "Create a new comment",
            description = "Creates a new comment for a specific topic. The comment will be associated with the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Comment created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or topic does not belong to course",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Topic or User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(
            @Parameter(description = "ID of the course", required = true)
            @PathVariable String courseId,
            @Parameter(description = "ID of the topic", required = true)
            @PathVariable UUID topicId,
            @Parameter(description = "Comment data", required = true)
            @Valid @RequestBody CreateCommentRequest request) {

        CommentResponseDto comment = commentService.createComment(courseId, topicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @Operation(
            summary = "Delete a comment",
            description = "Deletes a comment by ID. Only the comment owner can delete their own comment."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Comment deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comment not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User is not authorized to delete this comment",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Comment or topic does not belong to specified course",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            )
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "ID of the course", required = true)
            @PathVariable String courseId,
            @Parameter(description = "ID of the topic", required = true)
            @PathVariable UUID topicId,
            @Parameter(description = "ID of the comment to delete", required = true)
            @PathVariable UUID commentId) {

        commentService.deleteComment(courseId, topicId, commentId);
        return ResponseEntity.noContent().build();
    }
}
