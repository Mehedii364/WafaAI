# Wafa AI - AI Assistant by Mehedi364

A production-grade Android AI chatbot powered by real OpenRouter API integration, 10-Key Rotation Engine, Intelligent Failover, Room persistence, and bilingual Bangla/English/Arabic interface.

---

## Key Features

1. **10 API Key Manager & Intelligent Rotation**:
   - Stores up to 10 separate OpenRouter API keys in hardware-backed Android Keystore encrypted storage.
   - **Active All Keys Mode**: Automatically cycles through all configured keys, tracking usage statistics and request counts.
   - **Selected Keys Mode**: Allows user-defined subset of keys with instant Select All / Clear All controls.
   - **Intelligent Failover**: Automatically detects rate limits (HTTP 429), timeouts, and server errors (HTTP 5xx), places failing keys into temporary cooldown, and retries seamlessly with next available keys. Permanently marks invalid keys (HTTP 401).
   - **Test Connection**: Live connection test for every key slot against real OpenRouter models.

2. **Real-Time Streaming & Chat**:
   - Server-Sent Events (SSE) streaming with instantaneous token rendering.
   - Stop generation button that gracefully preserves partial responses.
   - Rich Markdown rendering with code blocks, copy-to-clipboard, headers, lists, and inline code.
   - User message editing, retry, regenerate, and copy/share actions.

3. **Persistent Local Storage (Room Database)**:
   - Full conversation history with title auto-generation.
   - Real-time search across conversation titles and message content.
   - Export conversations to JSON format.

4. **Trilingual Localization**:
   - Default language: **বাংলা (Bangla)**
   - English
   - العربية (Arabic)
   - Dynamic in-app language switching and RTL support.

5. **Optional Companion PHP Backend**:
   - Complete production-ready PHP gateway for **InfinityFree** and custom domain hosting (`/backend/public_html/`).

---

## Build Artifacts

- Debug APK: `.build-outputs/app-debug.apk` & `APK_DOWNLOAD/app-debug.apk`
- GitHub Actions CI/CD: `.github/workflows/android-build.yml`

Developed by **Mehedi364**.
