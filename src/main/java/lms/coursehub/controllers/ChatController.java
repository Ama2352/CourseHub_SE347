package lms.coursehub.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lms.coursehub.helpers.exceptions.ForbiddenException;
import lms.coursehub.models.dtos.message.CreateMessageRequest;
import lms.coursehub.models.dtos.message.GetMessageRequest;
import lms.coursehub.models.dtos.message.GetMessageResponse;
import lms.coursehub.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/message")
@SecurityRequirement(name = "bearerToken")
@Tag(name = "Chat", description = "APIs for chat messaging functionality")
public class ChatController {
    
    private final MessageService messageService;
    
    @PostMapping("/sendMessages")
    @Operation(
            summary = "Send a message",
            description = "Send a message to a conversation. User must be part of the conversation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message sent successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have access to this conversation"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conversation or user not found"
            )
    })
    public ResponseEntity<Void> sendMessage(
            @Valid @RequestBody CreateMessageRequest request,
            Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        
        if (!messageService.isUserInConversation(userId, request.getConversationId())) {
            throw new ForbiddenException("You do not have access to this conversation.");
        }
        
        messageService.createMessage(request, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/getMessages")
    @Operation(
            summary = "Get messages by conversation",
            description = "Retrieve all messages from a specific conversation. User must be part of the conversation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved messages",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetMessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have access to this conversation"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conversation not found"
            )
    })
    public ResponseEntity<List<GetMessageResponse>> getMessagesByConversationId(
            @Valid @RequestBody GetMessageRequest request,
            Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        
        if (!messageService.isUserInConversation(userId, request.getConversationId())) {
            throw new ForbiddenException("You do not have access to this conversation.");
        }
        
        List<GetMessageResponse> messages = messageService.getMessagesByConversationId(request.getConversationId());
        return ResponseEntity.ok(messages);
    }
}
