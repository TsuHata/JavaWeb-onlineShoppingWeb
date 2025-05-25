package com.example.auth.model.dto;

import com.example.auth.model.entity.ChatMessage;
import lombok.Data;


import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    // Setters
    // Getters
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long recipientId;
    private String recipientName;
    private String recipientAvatar;
    private String content;
    private LocalDateTime sentTime;
    private boolean isRead;
    // 文件相关字段
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;

    public ChatMessageDTO() {
    }

    public ChatMessageDTO(ChatMessage message) {
        this.id = message.getId();
        this.conversationId = message.getConversationId();
        this.senderId = message.getSender().getId();
        this.senderName = message.getSender().getRealName() != null ? 
                message.getSender().getRealName() : message.getSender().getUsername();
        this.senderAvatar = message.getSender().getAvatar();
        this.recipientId = message.getRecipient().getId();
        this.recipientName = message.getRecipient().getRealName() != null ? 
                message.getRecipient().getRealName() : message.getRecipient().getUsername();
        this.recipientAvatar = message.getRecipient().getAvatar();
        this.content = message.getContent();
        this.sentTime = message.getSentTime();
        this.isRead = message.isRead();
        this.fileUrl = message.getFileUrl();
        this.fileName = message.getFileName();
        this.fileType = message.getFileType();
        this.fileSize = message.getFileSize();
    }

}