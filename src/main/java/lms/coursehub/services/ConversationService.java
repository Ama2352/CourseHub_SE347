package lms.coursehub.services;

import lms.coursehub.helpers.exceptions.NotFoundException;
import lms.coursehub.models.dtos.conversation.ConversationDTO;
import lms.coursehub.models.entities.Conversation;
import lms.coursehub.models.entities.User;
import lms.coursehub.repositories.ConversationRepo;
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
public class ConversationService {
    
    private final ConversationRepo conversationRepo;
    private final UserRepo userRepo;
    
    @Transactional(readOnly = true)
    public List<ConversationDTO> getAllByUserId(UUID userId) {
        // Verify user exists
        userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        List<Conversation> conversations = conversationRepo.findByUserId(userId);
        
        return conversations.stream()
                .map(conversation -> mapToConversationDTO(conversation, userId))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ConversationDTO createConversation(UUID userId, UUID otherUserId) {
        // Verify both users exist
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        User otherUser = userRepo.findById(otherUserId)
                .orElseThrow(() -> new NotFoundException("Other user not found"));
        
        // Check if conversation already exists
        return conversationRepo.findByBothUsers(userId, otherUserId)
                .map(conversation -> mapToConversationDTO(conversation, userId))
                .orElseGet(() -> {
                    // Create new conversation
                    Conversation newConversation = new Conversation();
                    newConversation.setUser1(user);
                    newConversation.setUser2(otherUser);
                    newConversation.setLastMessageAt(LocalDateTime.now());
                    
                    Conversation saved = conversationRepo.save(newConversation);
                    return mapToConversationDTO(saved, userId);
                });
    }
    
    private ConversationDTO mapToConversationDTO(Conversation conversation, UUID currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setLastMessageAt(conversation.getLastMessageAt());
        
        // Determine the "other" user
        User otherUser;
        if (conversation.getUser1().getId().equals(currentUserId)) {
            otherUser = conversation.getUser2();
        } else {
            otherUser = conversation.getUser1();
        }
        
        dto.setOtherUserId(otherUser.getId());
        dto.setOtherUserUsername(otherUser.getUsername());
        dto.setOtherUserAvatarUrl(otherUser.getAvatarUrl());
        
        return dto;
    }
}
