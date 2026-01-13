package lms.coursehub.services;

import lms.coursehub.helpers.exceptions.ForbiddenException;
import lms.coursehub.helpers.exceptions.NotFoundException;
import lms.coursehub.models.dtos.message.CreateMessageRequest;
import lms.coursehub.models.dtos.message.GetMessageResponse;
import lms.coursehub.models.entities.Conversation;
import lms.coursehub.models.entities.Message;
import lms.coursehub.models.entities.User;
import lms.coursehub.repositories.ConversationRepo;
import lms.coursehub.repositories.MessageRepo;
import lms.coursehub.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepo messageRepo;
    private final ConversationRepo conversationRepo;
    private final UserRepo userRepo;
    
    @Transactional
    public void createMessage(CreateMessageRequest request, UUID userId) {
        // Verify conversation exists
        Conversation conversation = conversationRepo.findById(request.getConversationId())
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        
        // Verify user is part of the conversation
        if (!isUserInConversation(userId, conversation.getId())) {
            throw new ForbiddenException("You do not have access to this conversation");
        }
        
        // Get sender
        User sender = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Create message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.getContent());
        
        messageRepo.save(message);
        
        // Update conversation lastMessageAt
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepo.save(conversation);
    }
    
    @Transactional(readOnly = true)
    public List<GetMessageResponse> getMessagesByConversationId(UUID conversationId) {
        // Verify conversation exists
        conversationRepo.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        
        List<Message> messages = messageRepo.findByConversationIdOrderBySentAtAsc(conversationId);
        
        return messages.stream()
                .map(this::mapToGetMessageResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public boolean isUserInConversation(UUID userId, UUID conversationId) {
        Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        
        return conversation.getUser1().getId().equals(userId) || 
               conversation.getUser2().getId().equals(userId);
    }
    
    private GetMessageResponse mapToGetMessageResponse(Message message) {
        GetMessageResponse response = new GetMessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderUsername(message.getSender().getUsername());
        response.setSenderAvatarUrl(message.getSender().getAvatarUrl());
        response.setContent(message.getContent());
        response.setSentAt(message.getSentAt());
        response.setConversationId(message.getConversation().getId());
        return response;
    }
}
