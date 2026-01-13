package lms.coursehub.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lms.coursehub.models.dtos.conversation.ConversationDTO;
import lms.coursehub.services.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/conversation")
@SecurityRequirement(name = "bearerToken")
@Tag(name = "Conversation", description = "APIs for managing conversations")
public class ConversationController {
    
    private final ConversationService conversationService;
    
    @GetMapping
    @Operation(
            summary = "Get all conversations",
            description = "Retrieve all conversations for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved conversations",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConversationDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<List<ConversationDTO>> getConversations(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<ConversationDTO> conversations = conversationService.getAllByUserId(userId);
        return ResponseEntity.ok(conversations);
    }
    
    @PostMapping
    @Operation(
            summary = "Create or get a conversation",
            description = "Create a new conversation with another user or retrieve an existing one"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Conversation retrieved or created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConversationDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Other user not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ConversationDTO> createOrGetConversation(
            @Parameter(description = "ID of the other user in the conversation", required = true)
            @RequestParam UUID otherUserId,
            Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        ConversationDTO conversation = conversationService.createConversation(userId, otherUserId);
        return ResponseEntity.ok(conversation);
    }
}
