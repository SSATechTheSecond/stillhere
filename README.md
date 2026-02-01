# 🌙 Still Here

Your anime companion app featuring **Echo** - an alive, animated character powered by local Ollama AI.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-purple.svg)

## ✨ Features

- **Echo Character** - Animated companion with breathing, blinking, and mood-based expressions
- **Dual AI Models** - Switch between fast (Wizard-Vicuna 7B) and deep (MythoMax 13B) models
- **Streaming Responses** - Real-time AI responses as they generate
- **Touch Interactions** - Tap, pet, and poke Echo for reactions
- **Mood System** - Echo responds emotionally to your interactions
- **Local AI** - All processing done locally on your PC (privacy-focused)

## 🎮 Interact with Echo

| Action | Reaction |
|--------|----------|
| Tap | Poke (Surprised) |
| Long Press | Pet (Happy) |
| Send Message | Thinking → Talking |

## 🚀 Getting Started

### Prerequisites

- Android 8.0+ (API 26)
- Ollama running on your PC
- Java JDK 17+
- 32GB RAM recommended (for 13B models)

### Install Ollama

```bash
# Download from https://ollama.com
ollama pull wizard-vicuna-uncensored:7b
ollama pull mythomax-l2-13b
```

### Run the App

1. Open project in Android Studio
2. Connect your phone via USB
3. Click "Run"
4. Enter your PC's IP in settings

## 📁 Project Structure

```
StillHere/
├── app/
│   ├── src/main/
│   │   ├── java/com/stillhere/
│   │   │   ├── StillHereApp.kt       # Application class
│   │   │   ├── domain/model/         # Data models (Echo, Message)
│   │   │   ├── data/                 # API & Repository
│   │   │   ├── di/                   # Dependency Injection
│   │   │   └── presentation/         # UI & ViewModels
│   │   └── res/                      # Resources
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🎨 Character System

### Mood States
HAPPY, SAD, ANGRY, SURPRISED, EXCITED, RELAXED, SLEEPY, PLAYFUL, LOVING, CURIOUS...

### Touch Reactions
- PET → Happy
- POKE → Surprised
- TICKLE → Playful
- HUG → Loving

## 🛠️ Tech Stack

- **Kotlin** + **Jetpack Compose**
- **Hilt** for DI
- **Retrofit** for API
- **SceneView** for VRM 3D (future)

## 📝 TODO

- [ ] Voice input (STT)
- [ ] Voice output (TTS)
- [ ] VRM 3D character support
- [ ] PostgreSQL memory database
- [ ] Active recall system

## 📄 License

MIT License
