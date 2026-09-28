package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenRouterChatRequest(
    val model: String,
    val messages: List<OpenRouterMessageDto>,
    val temperature: Float? = 0.7f,
    @Json(name = "max_tokens")
    val maxTokens: Int? = 4096,
    val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class OpenRouterMessageDto(
    val role: String,
    val content: Any // String or List of content parts for multimodal/images
)

@JsonClass(generateAdapter = true)
data class OpenRouterChatResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<OpenRouterChoiceDto>? = null,
    val usage: OpenRouterUsageDto? = null,
    val error: OpenRouterErrorDto? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterChoiceDto(
    val index: Int? = null,
    val message: OpenRouterMessageResponseDto? = null,
    val delta: OpenRouterDeltaDto? = null,
    @Json(name = "finish_reason")
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterMessageResponseDto(
    val role: String? = null,
    val content: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterDeltaDto(
    val role: String? = null,
    val content: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterUsageDto(
    @Json(name = "prompt_tokens")
    val promptTokens: Int? = null,
    @Json(name = "completion_tokens")
    val completionTokens: Int? = null,
    @Json(name = "total_tokens")
    val totalTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterErrorDto(
    val code: Any? = null,
    val message: String? = null,
    val metadata: Map<String, Any?>? = null
)
