package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class OpenRouterService {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val chatRequestAdapter = moshi.adapter(OpenRouterChatRequest::class.java)
    private val chatResponseAdapter = moshi.adapter(OpenRouterChatResponse::class.java)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1/chat/completions"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    suspend fun testConnection(apiKey: String, model: String = "google/gemini-2.0-flash-001"): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val testRequest = OpenRouterChatRequest(
                    model = model,
                    messages = listOf(
                        OpenRouterMessageDto(role = "user", content = "ping")
                    ),
                    maxTokens = 5,
                    stream = false
                )
                val jsonBody = chatRequestAdapter.toJson(testRequest)
                val request = Request.Builder()
                    .url(DEFAULT_BASE_URL)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("HTTP-Referer", "https://wafazone.site")
                    .addHeader("X-Title", "Wafa AI by Mehedi364")
                    .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val errorMsg = parseErrorMessage(responseBody, response.code)
                    return@withContext Result.failure(ApiException(response.code, errorMsg))
                }

                val parsed = chatResponseAdapter.fromJson(responseBody)
                val reply = parsed?.choices?.firstOrNull()?.message?.content
                    ?: "Connected successfully! (HTTP ${response.code})"
                Result.success(reply.trim())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun executeChat(
        apiKey: String,
        request: OpenRouterChatRequest,
        backendUrl: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = if (!backendUrl.isNullOrBlank()) {
                backendUrl.trimEnd('/') + "/api/chat.php"
            } else {
                DEFAULT_BASE_URL
            }

            val jsonBody = chatRequestAdapter.toJson(request)
            val requestBuilder = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://wafazone.site")
                .addHeader("X-Title", "Wafa AI by Mehedi364")
                .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))

            val response = client.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = parseErrorMessage(responseBody, response.code)
                return@withContext Result.failure(ApiException(response.code, errorMsg))
            }

            val parsed = chatResponseAdapter.fromJson(responseBody)
            val content = parsed?.choices?.firstOrNull()?.message?.content
                ?: throw ApiException(response.code, "Empty response received from AI model")

            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun streamChat(
        apiKey: String,
        request: OpenRouterChatRequest,
        backendUrl: String? = null
    ): Flow<String> = callbackFlow {
        val url = if (!backendUrl.isNullOrBlank()) {
            backendUrl.trimEnd('/') + "/api/chat.php"
        } else {
            DEFAULT_BASE_URL
        }

        val streamingRequestObj = request.copy(stream = true)
        val jsonBody = chatRequestAdapter.toJson(streamingRequestObj)

        val httpRequest = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("HTTP-Referer", "https://wafazone.site")
            .addHeader("X-Title", "Wafa AI by Mehedi364")
            .addHeader("Accept", "text/event-stream")
            .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val call = client.newCall(httpRequest)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val errorMsg = parseErrorMessage(body, response.code)
                    close(ApiException(response.code, errorMsg))
                    return
                }

                val source = response.body?.byteStream()
                if (source == null) {
                    close(IOException("Response body stream is null"))
                    return
                }

                try {
                    val reader = BufferedReader(InputStreamReader(source, Charsets.UTF_8))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line?.trim() ?: continue
                        if (currentLine.startsWith("data:")) {
                            val data = currentLine.removePrefix("data:").trim()
                            if (data == "[DONE]") {
                                break
                            }
                            try {
                                val chunk = chatResponseAdapter.fromJson(data)
                                val deltaText = chunk?.choices?.firstOrNull()?.delta?.content
                                if (!deltaText.isNullOrEmpty()) {
                                    trySend(deltaText)
                                }
                            } catch (_: Exception) {
                                // Ignore non-json or heartbeats
                            }
                        }
                    }
                    close()
                } catch (e: Exception) {
                    close(e)
                } finally {
                    try {
                        source.close()
                    } catch (_: Exception) {}
                }
            }
        })

        awaitClose {
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)

    private fun parseErrorMessage(responseBody: String, statusCode: Int): String {
        return try {
            val parsed = chatResponseAdapter.fromJson(responseBody)
            val msg = parsed?.error?.message
            if (!msg.isNullOrBlank()) {
                return msg
            }
            when (statusCode) {
                401 -> "Invalid OpenRouter API Key (HTTP 401 Unauthorized)"
                402 -> "OpenRouter Insufficient Credits (HTTP 402)"
                429 -> "OpenRouter Rate Limit Reached (HTTP 429)"
                500, 502, 503, 504 -> "OpenRouter Provider Server Error (HTTP $statusCode)"
                else -> "HTTP $statusCode error"
            }
        } catch (_: Exception) {
            "HTTP $statusCode: $responseBody"
        }
    }
}

class ApiException(val statusCode: Int, message: String) : Exception(message)
