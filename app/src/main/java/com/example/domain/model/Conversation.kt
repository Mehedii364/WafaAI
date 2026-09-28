package com.example.domain.model

data class Conversation(
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val model: String = "google/gemini-2.0-flash-001",
    val messageCount: Int = 0
)

data class Message(
    val id: String,
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val keySlotUsed: Int? = null,
    val attachmentPath: String? = null,
    val attachmentName: String? = null,
    val attachmentMimeType: String? = null
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class MessageStatus {
    SENDING,
    SENT,
    STREAMING,
    ERROR
}
