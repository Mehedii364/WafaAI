package com.example.data.repository

import com.example.data.local.database.dao.ConversationDao
import com.example.data.local.database.dao.MessageDao
import com.example.data.local.database.entity.ConversationEntity
import com.example.data.local.database.entity.MessageEntity
import com.example.data.local.preferences.UserPreferences
import com.example.data.remote.ApiException
import com.example.data.remote.OpenRouterChatRequest
import com.example.data.remote.OpenRouterMessageDto
import com.example.data.remote.OpenRouterService
import com.example.domain.model.Conversation
import com.example.domain.model.KeyUsageMode
import com.example.domain.model.Message
import com.example.domain.model.MessageRole
import com.example.domain.model.MessageStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val apiKeyManager: ApiKeyManager,
    private val openRouterService: OpenRouterService,
    private val userPreferences: UserPreferences
) {
    val conversationsFlow: Flow<List<Conversation>> = conversationDao.getAllConversations().map { list ->
        list.map { it.toDomain() }
    }

    fun getMessagesFlow(conversationId: String): Flow<List<Message>> =
        messageDao.getMessagesForConversation(conversationId).map { list ->
            list.map { it.toDomain() }
        }

    fun searchConversations(query: String): Flow<List<Conversation>> =
        conversationDao.searchConversations(query).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun createNewConversation(
        title: String = "New Chat",
        model: String = "google/gemini-2.0-flash-001"
    ): Conversation = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val entity = ConversationEntity(
            id = id,
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            model = model
        )
        conversationDao.insertConversation(entity)
        entity.toDomain()
    }

    suspend fun getConversation(id: String): Conversation? = withContext(Dispatchers.IO) {
        conversationDao.getConversationById(id)?.toDomain()
    }

    suspend fun renameConversation(id: String, newTitle: String) = withContext(Dispatchers.IO) {
        conversationDao.renameConversation(id, newTitle)
    }

    suspend fun deleteConversation(id: String) = withContext(Dispatchers.IO) {
        conversationDao.deleteConversationById(id)
    }

    suspend fun clearAllConversations() = withContext(Dispatchers.IO) {
        messageDao.deleteAllMessages()
        conversationDao.deleteAllConversations()
    }

    suspend fun deleteMessage(id: String) = withContext(Dispatchers.IO) {
        messageDao.deleteMessageById(id)
    }

    suspend fun clearMessagesForConversation(conversationId: String) = withContext(Dispatchers.IO) {
        messageDao.deleteMessagesForConversation(conversationId)
    }

    suspend fun saveMessage(message: Message) = withContext(Dispatchers.IO) {
        messageDao.insertMessage(MessageEntity.fromDomain(message))
        // Update conversation timestamp
        val conv = conversationDao.getConversationById(message.conversationId)
        if (conv != null) {
            val updatedTitle = if (conv.title == "New Chat" && message.role == MessageRole.USER) {
                message.content.take(40).trim()
            } else {
                conv.title
            }
            conversationDao.updateConversation(conv.copy(title = updatedTitle, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun updateMessageContent(id: String, content: String, status: MessageStatus) = withContext(Dispatchers.IO) {
        messageDao.updateMessageContent(id, content, status.name)
    }

    /**
     * Executes AI completion with intelligent key failover across up to 3 candidate keys.
     */
    suspend fun sendMessageWithFailover(
        conversationId: String,
        userPrompt: String,
        attachmentInfo: String? = null
    ): Result<Message> = withContext(Dispatchers.IO) {
        val settings = userPreferences.appSettingsFlow.first()
        val eligibleKeys = apiKeyManager.getEligibleKeys()

        if (eligibleKeys.isEmpty()) {
            val isSelectedMode = settings.keyUsageMode == KeyUsageMode.SELECTED_KEYS
            val errorMsg = if (isSelectedMode) {
                "Please select at least one configured API key in Settings."
            } else {
                "No API key is configured or all keys are in cooldown. Please check Settings."
            }
            return@withContext Result.failure(IllegalStateException(errorMsg))
        }

        // Build history context
        val existingMessages = messageDao.getMessagesListForConversation(conversationId)
        val promptHistory = mutableListOf<OpenRouterMessageDto>()

        if (settings.systemPrompt.isNotBlank()) {
            promptHistory.add(OpenRouterMessageDto(role = "system", content = settings.systemPrompt))
        }

        // Take last 12 messages for context
        val recent = existingMessages.takeLast(12)
        recent.forEach { m ->
            val role = when (m.role) {
                MessageRole.USER.name -> "user"
                MessageRole.ASSISTANT.name -> "assistant"
                else -> "user"
            }
            promptHistory.add(OpenRouterMessageDto(role = role, content = m.content))
        }

        val request = OpenRouterChatRequest(
            model = settings.model,
            messages = promptHistory,
            temperature = settings.temperature,
            maxTokens = settings.maxTokens,
            stream = false
        )

        val maxAttempts = minOf(3, eligibleKeys.size)
        var lastError: Throwable? = null
        val attemptedSlots = mutableSetOf<Int>()

        for (attempt in 0 until maxAttempts) {
            val keyInfo = apiKeyManager.pickNextKey() ?: break
            if (attemptedSlots.contains(keyInfo.slotIndex) && attemptedSlots.size < eligibleKeys.size) {
                continue
            }
            attemptedSlots.add(keyInfo.slotIndex)

            val result = openRouterService.executeChat(
                apiKey = keyInfo.rawKey,
                request = request,
                backendUrl = settings.backendUrl.ifBlank { null }
            )

            if (result.isSuccess) {
                apiKeyManager.handleKeySuccess(keyInfo.slotIndex)
                val responseText = result.getOrNull() ?: ""

                val assistantMessage = Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = conversationId,
                    role = MessageRole.ASSISTANT,
                    content = responseText,
                    timestamp = System.currentTimeMillis(),
                    status = MessageStatus.SENT,
                    keySlotUsed = keyInfo.slotIndex
                )
                saveMessage(assistantMessage)
                return@withContext Result.success(assistantMessage)
            } else {
                val err = result.exceptionOrNull() ?: Exception("Unknown error")
                lastError = err
                apiKeyManager.handleKeyFailure(keyInfo.slotIndex, err)
            }
        }

        Result.failure(lastError ?: IllegalStateException("All configured API keys failed."))
    }

    /**
     * Streams AI completion with candidate key.
     */
    suspend fun getStreamFlow(
        conversationId: String,
        userPrompt: String
    ): Result<Pair<Flow<String>, Int>> = withContext(Dispatchers.IO) {
        val settings = userPreferences.appSettingsFlow.first()
        val keyInfo = apiKeyManager.pickNextKey()
            ?: return@withContext Result.failure(IllegalStateException("No ready API key available."))

        val existingMessages = messageDao.getMessagesListForConversation(conversationId)
        val promptHistory = mutableListOf<OpenRouterMessageDto>()

        if (settings.systemPrompt.isNotBlank()) {
            promptHistory.add(OpenRouterMessageDto(role = "system", content = settings.systemPrompt))
        }

        val recent = existingMessages.takeLast(12)
        recent.forEach { m ->
            val role = when (m.role) {
                MessageRole.USER.name -> "user"
                MessageRole.ASSISTANT.name -> "assistant"
                else -> "user"
            }
            promptHistory.add(OpenRouterMessageDto(role = role, content = m.content))
        }

        val request = OpenRouterChatRequest(
            model = settings.model,
            messages = promptHistory,
            temperature = settings.temperature,
            maxTokens = settings.maxTokens,
            stream = true
        )

        val flow = openRouterService.streamChat(
            apiKey = keyInfo.rawKey,
            request = request,
            backendUrl = settings.backendUrl.ifBlank { null }
        )

        Result.success(Pair(flow, keyInfo.slotIndex))
    }

    suspend fun exportConversationsAsJson(): String = withContext(Dispatchers.IO) {
        val convs = conversationDao.getAllConversations().first()
        val sb = StringBuilder()
        sb.append("[\n")
        convs.forEachIndexed { i, c ->
            val msgs = messageDao.getMessagesListForConversation(c.id)
            sb.append("  {\n")
            sb.append("    \"id\": \"${c.id}\",\n")
            sb.append("    \"title\": \"${c.title.replace("\"", "\\\"")}\",\n")
            sb.append("    \"model\": \"${c.model}\",\n")
            sb.append("    \"createdAt\": ${c.createdAt},\n")
            sb.append("    \"messages\": [\n")
            msgs.forEachIndexed { mi, m ->
                sb.append("      {\n")
                sb.append("        \"role\": \"${m.role}\",\n")
                sb.append("        \"content\": \"${m.content.replace("\"", "\\\"").replace("\n", "\\n")}\",\n")
                sb.append("        \"timestamp\": ${m.timestamp}\n")
                sb.append("      }${if (mi < msgs.size - 1) "," else ""}\n")
            }
            sb.append("    ]\n")
            sb.append("  }${if (i < convs.size - 1) "," else ""}\n")
        }
        sb.append("]")
        sb.toString()
    }
}
