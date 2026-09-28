package com.example.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.Message
import com.example.domain.model.MessageRole
import com.example.domain.model.MessageStatus

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conversationId"),
        Index("timestamp")
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val role: String, // USER, ASSISTANT, SYSTEM
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SENT", // SENDING, SENT, STREAMING, ERROR
    val keySlotUsed: Int? = null,
    val attachmentPath: String? = null,
    val attachmentName: String? = null,
    val attachmentMimeType: String? = null
) {
    fun toDomain(): Message = Message(
        id = id,
        conversationId = conversationId,
        role = try { MessageRole.valueOf(role) } catch (_: Exception) { MessageRole.ASSISTANT },
        content = content,
        timestamp = timestamp,
        status = try { MessageStatus.valueOf(status) } catch (_: Exception) { MessageStatus.SENT },
        keySlotUsed = keySlotUsed,
        attachmentPath = attachmentPath,
        attachmentName = attachmentName,
        attachmentMimeType = attachmentMimeType
    )

    companion object {
        fun fromDomain(message: Message): MessageEntity = MessageEntity(
            id = message.id,
            conversationId = message.conversationId,
            role = message.role.name,
            content = message.content,
            timestamp = message.timestamp,
            status = message.status.name,
            keySlotUsed = message.keySlotUsed,
            attachmentPath = message.attachmentPath,
            attachmentName = message.attachmentName,
            attachmentMimeType = message.attachmentMimeType
        )
    }
}
