package com.example.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.preferences.UserPreferences
import com.example.data.repository.ApiKeyManager
import com.example.data.repository.ChatRepository
import com.example.domain.model.AppSettings
import com.example.domain.model.Conversation
import com.example.domain.model.Message
import com.example.domain.model.MessageRole
import com.example.domain.model.MessageStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val apiKeyManager: ApiKeyManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _partialStreamingText = MutableStateFlow("")
    val partialStreamingText: StateFlow<String> = _partialStreamingText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _activeSlotIndex = MutableStateFlow<Int?>(null)
    val activeSlotIndex: StateFlow<Int?> = _activeSlotIndex.asStateFlow()

    val appSettings: StateFlow<AppSettings> = userPreferences.appSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    private var streamingJob: Job? = null
    private var observeMessagesJob: Job? = null

    init {
        // Initialize or restore active conversation
        viewModelScope.launch {
            val convos = chatRepository.conversationsFlow.stateIn(viewModelScope)
            // Wait for initial list or create default
            startNewChat()
        }
    }

    fun startNewChat() {
        streamingJob?.cancel()
        _isGenerating.value = false
        _partialStreamingText.value = ""
        viewModelScope.launch {
            val conv = chatRepository.createNewConversation(
                title = "New Chat",
                model = appSettings.value.model
            )
            _currentConversationId.value = conv.id
            _currentConversation.value = conv
            observeConversationMessages(conv.id)
        }
    }

    fun selectConversation(conversationId: String) {
        if (_currentConversationId.value == conversationId) return
        streamingJob?.cancel()
        _isGenerating.value = false
        _partialStreamingText.value = ""
        _currentConversationId.value = conversationId

        viewModelScope.launch {
            _currentConversation.value = chatRepository.getConversation(conversationId)
            observeConversationMessages(conversationId)
        }
    }

    private fun observeConversationMessages(conversationId: String) {
        observeMessagesJob?.cancel()
        observeMessagesJob = viewModelScope.launch {
            chatRepository.getMessagesFlow(conversationId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun sendMessage(
        prompt: String,
        attachmentPath: String? = null,
        attachmentName: String? = null
    ) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() && attachmentPath == null) return
        if (_isGenerating.value) return

        val convId = _currentConversationId.value ?: return

        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            conversationId = convId,
            role = MessageRole.USER,
            content = trimmed,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            attachmentPath = attachmentPath,
            attachmentName = attachmentName
        )

        viewModelScope.launch {
            chatRepository.saveMessage(userMessage)
            _isGenerating.value = true
            _errorMessage.value = null
            _partialStreamingText.value = ""

            val streamingAllowed = appSettings.value.streamingEnabled

            if (streamingAllowed) {
                val streamResult = chatRepository.getStreamFlow(convId, trimmed)
                if (streamResult.isSuccess) {
                    val (flow, slotIndex) = streamResult.getOrThrow()
                    _activeSlotIndex.value = slotIndex
                    val fullResponse = StringBuilder()

                    streamingJob = launch {
                        flow.catch { error ->
                            apiKeyManager.handleKeyFailure(slotIndex, error)
                            _errorMessage.value = error.message ?: "Streaming error occurred"
                            _isGenerating.value = false
                        }.collect { chunk ->
                            fullResponse.append(chunk)
                            _partialStreamingText.value = fullResponse.toString()
                        }

                        // Stream completed normally
                        val finalContent = fullResponse.toString()
                        if (finalContent.isNotBlank()) {
                            apiKeyManager.handleKeySuccess(slotIndex)
                            val assistantMessage = Message(
                                id = UUID.randomUUID().toString(),
                                conversationId = convId,
                                role = MessageRole.ASSISTANT,
                                content = finalContent,
                                timestamp = System.currentTimeMillis(),
                                status = MessageStatus.SENT,
                                keySlotUsed = slotIndex
                            )
                            chatRepository.saveMessage(assistantMessage)
                        }
                        _partialStreamingText.value = ""
                        _isGenerating.value = false
                    }
                } else {
                    // Fallback to non-streaming failover execution
                    executeNonStreaming(convId, trimmed)
                }
            } else {
                executeNonStreaming(convId, trimmed)
            }
        }
    }

    private suspend fun executeNonStreaming(convId: String, prompt: String) {
        val result = chatRepository.sendMessageWithFailover(convId, prompt)
        result.fold(
            onSuccess = { assistantMsg ->
                _activeSlotIndex.value = assistantMsg.keySlotUsed
                _isGenerating.value = false
            },
            onFailure = { err ->
                _errorMessage.value = err.message ?: "Failed to generate AI response"
                _isGenerating.value = false
            }
        )
    }

    fun stopStreaming() {
        val currentPartial = _partialStreamingText.value
        val convId = _currentConversationId.value
        streamingJob?.cancel()
        _isGenerating.value = false

        if (currentPartial.isNotBlank() && convId != null) {
            viewModelScope.launch {
                val assistantMessage = Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = convId,
                    role = MessageRole.ASSISTANT,
                    content = currentPartial,
                    timestamp = System.currentTimeMillis(),
                    status = MessageStatus.SENT,
                    keySlotUsed = _activeSlotIndex.value
                )
                chatRepository.saveMessage(assistantMessage)
                _partialStreamingText.value = ""
            }
        } else {
            _partialStreamingText.value = ""
        }
    }

    fun regenerate() {
        val lastUserMessage = _messages.value.lastOrNull { it.role == MessageRole.USER } ?: return
        sendMessage(lastUserMessage.content)
    }

    fun retryMessage(messageId: String) {
        val message = _messages.value.firstOrNull { it.id == messageId } ?: return
        if (message.role == MessageRole.USER) {
            sendMessage(message.content)
        } else {
            regenerate()
        }
    }

    fun editUserMessage(messageId: String, newContent: String) {
        viewModelScope.launch {
            chatRepository.updateMessageContent(messageId, newContent, MessageStatus.SENT)
            sendMessage(newContent)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId)
        }
    }

    fun clearConversation() {
        val convId = _currentConversationId.value ?: return
        viewModelScope.launch {
            chatRepository.clearMessagesForConversation(convId)
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}
