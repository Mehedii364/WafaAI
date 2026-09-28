# ✨ Wafa AI

### Premium Multilingual AI Assistant for Android

**Wafa AI** is a production-grade Android AI assistant built with **Kotlin + Jetpack Compose**, powered by real **OpenRouter API integration** with a secure **10-Key Rotation Engine**, intelligent failover, persistent conversation history, streaming responses, and a premium multilingual interface.

> **Smart. Secure. Multilingual.**
> **Wafa AI — AI Assistant by Mehedi364**

---

<p align="center">

![Android](https://img.shields.io/badge/Android-Real_App-3DDC84?style=for-the-badge\&logo=android\&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=for-the-badge\&logo=kotlin\&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge\&logo=jetpackcompose\&logoColor=white)
![OpenRouter](https://img.shields.io/badge/OpenRouter-AI-111111?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge\&logo=openjdk\&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

</p>

---

## 🌟 Overview

Wafa AI is designed as a **real everyday AI assistant**, not a static chatbot demo.

It provides a polished Android experience with:

* 🤖 Real AI conversations
* 🔑 10 OpenRouter API key slots
* 🔄 Intelligent key rotation
* 🛡️ Automatic API failover
* ⚡ Real-time streaming responses
* 💬 Persistent conversations
* 🔎 Conversation search
* 📝 Markdown & code rendering
* 🌐 বাংলা / English / العربية
* 🌙 Premium Dark & Light themes
* 🎙️ Voice input
* 📎 Attachment support where supported
* 💾 Room database
* 🔐 Android Keystore-backed secure local credential storage
* 🚀 Automated APK/AAB builds with GitHub Actions

---

# 🚀 Core Features

## 🔑 10-Key OpenRouter Manager

Wafa AI supports up to **10 OpenRouter API keys**.

```text
API Key 01
API Key 02
API Key 03
API Key 04
API Key 05
API Key 06
API Key 07
API Key 08
API Key 09
API Key 10
```

Each key supports:

* Secure masked storage
* Show / Hide
* Save
* Clear
* Live connection test
* Status tracking
* Request count
* Error count
* Last-used information
* Cooldown state
* Invalid-key detection

---

# 🔄 API Key Usage Modes

Wafa AI provides two flexible modes.

## 🟢 Active All Keys

Use every configured API key automatically.

```text
Key 01
   ↓
Key 02
   ↓
Key 03
   ↓
...
Key 10
   ↓
Available Key
```

The engine automatically:

* distributes requests
* skips empty slots
* skips unavailable keys
* tracks usage
* handles temporary failures
* applies cooldowns
* recovers keys automatically

---

## 🔵 Selected Keys

Choose exactly which API keys Wafa AI is allowed to use.

Example:

```text
☑ API Key 01
☑ API Key 02
☐ API Key 03
☑ API Key 04
☐ API Key 05
☐ API Key 06
☑ API Key 07
☐ API Key 08
☐ API Key 09
☑ API Key 10
```

Includes:

* **Select All**
* **Clear All**
* Individual key selection
* Automatic removal of cleared keys
* Validation when no key is selected

Only selected and configured keys participate in AI requests.

---

# 🧠 Intelligent Failover Engine

Wafa AI does not continuously retry a failing key.

The rotation engine detects common temporary failures including:

* HTTP `429` — Rate Limited
* HTTP `408` — Timeout
* HTTP `5xx` — Server Errors
* Network timeout
* Temporary provider errors

The failing key is placed into a temporary cooldown.

Then:

```text
Current Key
     ↓
Failure
     ↓
Cooldown
     ↓
Next Available Key
     ↓
Retry
     ↓
AI Response
```

For authentication failures such as HTTP `401`, the key can be marked invalid until the user fixes or replaces it.

### Safety controls

* Bounded retry count
* No infinite retry loops
* Cooldown handling
* Invalid-key detection
* Automatic recovery
* Empty-key skipping

---

# 🧪 Live API Connection Testing

Every API key slot includes a real connection test.

```text
API Key 01
••••••••••••7A2F

[ Test Connection ]
```

The test performs a real request against OpenRouter.

Possible states:

```text
✓ Available
✓ Connected
⚠ Rate Limited
⚠ Cooldown
✕ Invalid
○ Not Configured
```

No fake connection results are used.

---

# ⚡ Real-Time AI Streaming

Wafa AI supports real-time response streaming where supported by the selected provider/model.

Instead of waiting for the complete answer:

```text
Generating...
```

The response appears progressively.

Users can:

* Stop generation
* Preserve partial responses
* Retry
* Regenerate
* Copy
* Share

---

# 💬 Advanced Chat Experience

Each conversation supports:

* New Chat
* Automatic title generation
* Message timestamps
* Edit user message
* Retry
* Regenerate
* Copy
* Share
* Delete
* Clear conversation
* Scroll to latest message
* Stop generation

---

# 📝 Rich Markdown

AI responses support modern formatting.

Supported presentation includes:

* Headings
* Paragraphs
* Bold
* Italic
* Lists
* Tables where practical
* Inline code
* Code blocks
* Syntax highlighting
* Copy code
* Horizontal scrolling for long code

---

# 💾 Persistent Conversations

Powered by **Room Database**.

Conversation data includes:

```text
Conversation
├── ID
├── Title
├── Created At
├── Updated At
└── Model
```

Messages include:

```text
Message
├── ID
├── Conversation ID
├── Role
├── Content
├── Timestamp
└── Status
```

Users can:

* Browse history
* Search conversations
* Rename conversations
* Delete conversations
* Export conversations

---

# 🔎 Powerful Conversation Search

Search across:

* Conversation titles
* User messages
* AI messages

Includes:

* Instant filtering
* Recent conversations
* Empty states
* No-result states

---

# 🌍 Multilingual Interface

Wafa AI is designed for a global and multilingual experience.

### 🇧🇩 বাংলা

Default application language.

### 🇬🇧 English

Full English interface.

### 🇸🇦 العربية

Arabic interface with RTL support.

Language can be changed directly from Settings.

---

# 🎨 Premium UI / UX

Wafa AI uses a modern premium design system combining:

* Material 3
* Neumorphic-inspired surfaces
* Smooth animations
* Rounded cards
* Premium icons
* Clean typography
* Responsive layouts
* Dark mode
* Light mode
* System theme
* Accessibility-friendly controls

Designed for:

📱 Android Phones
📲 Tablets
🖥️ Large Screens

---

# 🌙 Theme System

Available modes:

```text
☀ Light
🌙 Dark
⚙ System Default
```

The interface automatically adapts to the selected appearance.

---

# 🎙️ Voice Input

Wafa AI supports Android speech recognition where available.

Flow:

```text
🎙️
 ↓
Speech Recognition
 ↓
Text Composer
 ↓
Send
 ↓
AI Response
```

Runtime permissions are handled properly.

---

# 📎 Attachments

Attachment support is available where the selected model/provider supports the requested input type.

The interface supports:

* File selection
* Image selection
* Filename display
* File size
* Remove attachment
* Processing state
* Validation

Unsupported formats/models are clearly communicated rather than pretending they are supported.

---

# 🔐 Security

Security is a core part of Wafa AI.

API credentials should never be hard-coded into source code.

The application is designed to avoid:

❌ API keys in Kotlin source
❌ API keys in GitHub repositories
❌ API keys in BuildConfig
❌ API keys in resources
❌ API keys in logs
❌ API keys in analytics
❌ Full API-key display after saving

Where credentials must be persisted locally, the application uses **Android Keystore-backed encryption**.

---

# 🛡️ Recommended Production Architecture

For production deployment, the recommended architecture is:

```text
┌───────────────────────┐
│      Wafa AI App      │
│   Android / Compose   │
└───────────┬───────────┘
            │ HTTPS
            ▼
┌───────────────────────┐
│    Secure PHP API     │
│     Gateway Layer     │
└───────────┬───────────┘
            │
            ▼
┌───────────────────────┐
│      OpenRouter       │
└───────────────────────┘
```

This architecture helps prevent API credentials from being permanently embedded inside a distributed APK.

---

# 🐘 Optional PHP Backend

A companion PHP gateway is included/planned for deployment on:

* InfinityFree
* PHP 8+
* MySQL / PDO
* Custom domains
* Standard cPanel hosting

Backend directory:

```text
backend/
└── public_html/
```

The backend can handle:

* OpenRouter requests
* API key management
* Rotation
* Failover
* Model configuration
* Secure server-side credentials
* Application API endpoints

---

# 🤖 Technology Stack

| Technology        | Purpose                     |
| ----------------- | --------------------------- |
| Kotlin            | Android development         |
| Jetpack Compose   | UI                          |
| Material 3        | Design system               |
| Java 17           | Build/runtime compatibility |
| Room              | Local database              |
| Coroutines        | Async operations            |
| StateFlow         | Reactive state              |
| OkHttp / Retrofit | Networking                  |
| OpenRouter        | AI provider                 |
| PHP 8+            | Optional backend            |
| MySQL/PDO         | Backend data                |
| GitHub Actions    | CI/CD                       |
| Android Keystore  | Credential protection       |

---

# 📁 Project Architecture

```text
WafaAI/
│
├── app/
│   ├── data/
│   │   ├── local/
│   │   ├── remote/
│   │   └── repository/
│   │
│   ├── domain/
│   │   ├── model/
│   │   ├── repository/
│   │   └── usecase/
│   │
│   ├── ui/
│   │   ├── chat/
│   │   ├── conversations/
│   │   ├── settings/
│   │   ├── components/
│   │   └── theme/
│   │
│   ├── security/
│   ├── service/
│   └── MainActivity.kt
│
├── backend/
│   └── public_html/
│
├── .github/
│   └── workflows/
│       └── android-build.yml
│
├── .build-outputs/
│   └── app-debug.apk
│
└── APK_DOWNLOAD/
    └── app-debug.apk
```

---

# 🚀 Automated Android Build

GitHub Actions automatically handles the Android build pipeline.

```text
Git Push
   ↓
GitHub Actions
   ↓
Java 17
   ↓
Gradle
   ↓
Android Build
   ↓
APK Validation
   ↓
APK Artifact
   ↓
AAB Artifact
```

Build outputs:

```text
.build-outputs/app-debug.apk
APK_DOWNLOAD/app-debug.apk
```

The workflow validates that the APK:

* exists
* is a valid APK
* is greater than 1 MB
* was produced by the real Gradle build

---

# 📦 Build Artifacts

### Debug APK

```text
.build-outputs/app-debug.apk
```

### Download Copy

```text
APK_DOWNLOAD/app-debug.apk
```

### Android App Bundle

Generated through GitHub Actions for release deployment.

---

# ⚙️ Configuration

Before using Wafa AI with OpenRouter:

1. Open the application.
2. Go to:

```text
Settings
→ AI Configuration
→ OpenRouter
```

3. Enter your API keys.
4. Choose:

```text
Active All Keys
```

or

```text
Selected Keys
```

5. Select the desired model.
6. Test the configured keys.
7. Start chatting.

---

# 🧩 API Configuration

OpenRouter base URL:

```text
https://openrouter.ai/api/v1
```

Chat endpoint:

```text
/chat/completions
```

The model is configurable and should not be assumed to remain permanently available.

---

# 📊 Key Status Dashboard

Example:

```text
┌──────────────────────────────┐
│ API Key 01                   │
│ ● Available                  │
│ Requests: 42                 │
│ Errors: 2                    │
│ Last Used: 10:42 PM          │
└──────────────────────────────┘
```

The dashboard helps users understand which configured keys are currently available without exposing sensitive credentials.

---

# 🧠 AI Configuration

Settings can include:

* OpenRouter model
* System prompt
* Temperature where supported
* Maximum tokens where supported
* Streaming
* API key usage mode
* Selected keys
* Connection testing

Default system behavior:

> Wafa AI is a helpful, accurate, respectful, multilingual AI assistant. It supports Bangla, English, and Arabic, communicates clearly, and avoids inventing information when reliable information is unavailable.

---

# 📱 App Navigation

```text
Wafa AI
│
├── 💬 Chat
│
├── 🗂 Conversations
│
├── 🔎 Search
│
├── ⚙ Settings
│   ├── AI Configuration
│   ├── OpenRouter
│   ├── API Key Manager
│   ├── Appearance
│   ├── Language
│   ├── Chat
│   └── Data
│
└── ℹ About
```

---

# 🗺️ Roadmap

Potential future improvements:

* [ ] More AI providers
* [ ] Multiple model profiles
* [ ] Advanced prompt presets
* [ ] Cloud conversation synchronization
* [ ] Advanced document processing
* [ ] Image understanding improvements
* [ ] Voice conversations
* [ ] AI productivity tools
* [ ] Custom assistant profiles
* [ ] Advanced usage analytics

---

# 🧪 Quality Standards

Wafa AI follows a **real-functionality-first** approach.

The project should never ship core features as:

* Fake
* Mock
* Placeholder
* Simulated
* Static

Every production feature should connect to its actual implementation or clearly communicate when external configuration is required.

---

# 📜 License

This project is licensed under the **MIT License** unless otherwise specified.

---

# 👨‍💻 Developer

## Mehedi364

**Md. Mehedi Hasan**

> Web Developer • Software Engineer • Programmer

**Wafa AI** is part of the **Wafa Zone** ecosystem.

### Brand

**Wafa Zone by Mehedi364**

### Developer

**Mehedi364**

---

<p align="center">

### ✨ Wafa AI

**Smart. Secure. Multilingual.**

**Developed by Mehedi364 ❤️**

</p>

---

## ⭐ Support the Project

If you find Wafa AI useful:

⭐ Star the repository
🍴 Fork the project
🐛 Report issues
💡 Suggest improvements
🚀 Build something amazing

---

<p align="center">

**© Mehedi364 — Wafa AI**

</p>
