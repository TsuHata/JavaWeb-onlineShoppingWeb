package com.example.auth.model.dto;

import com.example.auth.model.entity.Conversation;
import com.example.auth.model.entity.User;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
public class ConversationDTO {

    private Long id;
    private UserSummaryDTO user1;
    private UserSummaryDTO user2;
    private LocalDateTime createdTime;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
    private List<ChatMessageDTO> recentMessages;
    private ChatMessageDTO lastMessage;

    public ConversationDTO() {
    }

    public ConversationDTO(Conversation conversation, User currentUser) {
        this.id = conversation.getId();
        
        User otherUser;
        if (conversation.getUser1().getId().equals(currentUser.getId())) {
            otherUser = conversation.getUser2();
            this.unreadCount = conversation.getUnreadCountUser1();
        } else {
            otherUser = conversation.getUser1();
            this.unreadCount = conversation.getUnreadCountUser2();
        }
        
        this.user1 = new UserSummaryDTO(currentUser);
        this.user2 = new UserSummaryDTO(otherUser);
        this.createdTime = conversation.getCreatedTime();
        this.lastMessageTime = conversation.getLastMessageTime();
        
        if (!conversation.getMessages().isEmpty()) {
            this.lastMessage = new ChatMessageDTO(
                    Objects.requireNonNull(conversation.getMessages().stream().min((m1, m2) -> m2.getSentTime().compareTo(m1.getSentTime()))
                            .orElse(null))
            );
        }
    }
    
    public ConversationDTO(Conversation conversation, User currentUser, List<ChatMessageDTO> recentMessages) {
        this(conversation, currentUser);
        this.recentMessages = recentMessages;
    }

    // 内部类：用户摘要信息
    @Data
    public static class UserSummaryDTO {
        private Long id;
        private String username;
        private String name;
        private String avatar;
        
        public UserSummaryDTO() {
        }
        
        public UserSummaryDTO(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.name = user.getRealName() != null ? user.getRealName() : user.getUsername();
            this.avatar = user.getAvatar();
        }

    }
} 