package com.example.domain.model

data class AppSettings(
    val selectedLanguage: String = "bn", // Default বাংলা as requested!
    val themeMode: String = "system",    // system, light, dark
    val model: String = "google/gemini-2.0-flash-001",
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096,
    val streamingEnabled: Boolean = true,
    val enterToSend: Boolean = true,
    val keyUsageMode: KeyUsageMode = KeyUsageMode.ACTIVE_ALL,
    val backendUrl: String = "" // Optional PHP backend URL for InfinityFree
) {
    companion object {
        const val DEFAULT_SYSTEM_PROMPT =
            "You are Wafa AI, a helpful, accurate, respectful, multilingual AI assistant developed by Mehedi364. " +
            "Answer clearly and naturally. Support Bangla, English, and Arabic. " +
            "Do not invent facts when reliable information is unavailable. Explain uncertainty when necessary."
    }
}
