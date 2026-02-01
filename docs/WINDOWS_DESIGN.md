# Still Here - Windows Desktop Version Design

## Overview

Create a Windows desktop companion for Still Here with Echo - your AI companion that lives on your PC alongside you.

## 🎯 Goals

- Echo should be visible while you work
- Quick access via hotkeys
- System tray integration
- Multiple window modes
- Better performance (runs directly on PC)
- Local Ollama integration (no network needed)

---

## 💻 Technology Options

### Option A: Kotlin Multiplatform + Compose Desktop

**Pros:**
- Share ~80% code with Android app
- Same UI framework
- Modern Kotlin
- Single codebase

**Cons:**
- Compose Desktop still in beta
- Larger binary size
- Some Java interop needed

**Best for:** Long-term, maintain single codebase

---

### Option B: JavaFX

**Pros:**
- Mature, stable
- Good Windows support
- Smaller binaries
- Rich UI controls

**Cons:**
- Separate codebase
- Less modern look
- More boilerplate

**Best for:** Quick Windows-only version

---

### Option C: Electron + Web Technologies

**Pros:**
- Web technologies (HTML/CSS/JS)
- Easy to build
- Cross-platform

**Cons:**
- Heavy (100MB+)
- WebView overhead
- Separate codebase

**Best for:** Web developers

---

## 🏆 Recommendation: Kotlin Multiplatform

**Why?**
1. Share Echo character, Ollama client, chat logic
2. Same Compose UI framework
3. Future: iOS version with same code
4. Modern, fun to develop

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Still Here (Monorepo)                 │
├─────────────────────────────────────────────────────────┤
│  shared/                                                │
│  ├── core/                                              │
│  │   ├── models/                    (Echo, Message)     │
│  │   ├── OllamaClient.kt            (API logic)         │
│  │   ├── EchoEngine.kt              (character logic)   │
│  │   └── MemoryManager.kt           (persistent mem)   │
│  ├── AndroidApp/                   (Android UI)         │
│  └── WindowsApp/                   (Windows UI)         │
└─────────────────────────────────────────────────────────┘
```

---

## 🪟 Windows Features

### 1. Window Modes

| Mode | Description | Use Case |
|------|-------------|----------|
| **Overlay** | Semi-transparent over other windows | Work while chatting |
| **Compact** | Small chat-only window | Quick conversations |
| **Full** | Full companion app | Deep interaction |
| **Desktop Pet** | Echo floats on desktop | Ambient presence |
| **Minimized** | System tray with notifications | Background mode |

### 2. System Tray

- Echo icon in tray
- Right-click menu: Open, Settings, Exit
- Left-click: Toggle window
- Notifications for messages

### 3. Hotkeys

| Hotkey | Action |
|--------|--------|
| `Win + E` | Toggle Echo window |
| `Win + Shift + E` | Quick chat input |
| `Win + Alt + E` | Send message |
| `Esc` | Close/minimize |

### 4. Ollama Integration (Windows)

Since app runs ON the PC (not connecting remotely):

```yaml
ollama:
  host: "http://localhost:11434"  # Local only
  models:
    - wizard-vicuna-uncensored:7b
    - mythomax-l2-13b
```

**Benefits:**
- No network latency
- Works offline
- Privacy (all local)
- Faster responses

### 5. TTS Integration (Windows)

Windows built-in Speech API:

```kotlin
// Use Windows SpeechSynthesizer
val synth = SpeechSynthesizer()
synth.Speak("Hello! I'm Echo!")
```

Benefits:
- Natural voices
- Low CPU
- Works offline
- No phone battery drain

---

## 🎨 Echo on Desktop - Enhanced

### Visuals
- **Higher resolution** (1080p character)
- **Smoother animations** (60fps)
- **VRM 3D support** (SceneView works on desktop)
- **More expressions** (full VRoid blendshapes)

### Interactions
- **Keyboard shortcuts**
- **Mouse wheel** to scroll
- **Drag to move** floating window
- **Click to chat**

---

## 📁 Project Structure

```
StillHere/
├── shared/                    # Kotlin Multiplatform
│   ├── src/
│   │   ├── commonMain/
│   │   │   ├── kotlin/
│   │   │   │   ├── com/stillhere/core/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Echo.kt
│   │   │   │   │   │   ├── Message.kt
│   │   │   │   │   │   └── Mood.kt
│   │   │   │   │   ├── api/
│   │   │   │   │   │   └── OllamaClient.kt
│   │   │   │   │   └── engine/
│   │   │   │   │       └── EchoEngine.kt
│   │   │   └── resources/
│   │   ├── androidMain/
│   │   │   └── kotlin/
│   │   └── desktopMain/
│   │       └── kotlin/
├── androidApp/
│   ├── src/main/
│   └── build.gradle.kts
├── windowsApp/
│   ├── src/main/
│   │   ├── kotlin/
│   │   │   └── com/stillhere/desktop/
│   │   │       ├── Main.kt
│   │   │       ├── ui/
│   │   │       │   ├── DesktopWindow.kt
│   │   │       │   ├── EchoDisplay.kt
│   │   │       │   └── ChatPanel.kt
│   │   │       ├── system/
│   │   │       │   ├── SystemTray.kt
│   │   │       │   ├── HotkeyManager.kt
│   │   │       │   └── WindowsTTS.kt
│   │   │       └── config/
│   │   │           └── Settings.kt
│   │   └── resources/
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Implementation Steps

### Phase 1: Setup (1-2 days)
- [ ] Create Kotlin Multiplatform project
- [ ] Set up Gradle with shared module
- [ ] Configure desktop build

### Phase 2: Shared Core (2-3 days)
- [ ] Move models to shared
- [ ] Implement Ollama client in shared
- [ ] Create Echo engine
- [ ] Add memory system

### Phase 3: Windows UI (3-4 days)
- [ ] Main window with Compose Desktop
- [ ] Echo character display (2D Canvas + VRM)
- [ ] Chat interface
- [ ] Settings panel

### Phase 4: Windows Features (2-3 days)
- [ ] System tray integration
- [ ] Hotkey manager
- [ ] Window overlay mode
- [ ] Windows TTS integration
- [ ] Notifications

### Phase 5: Polish (1-2 days)
- [ ] Performance optimization
- [ ] Bug fixes
- [ ] Installer (NSIS)
- [ ] Testing

---

## 📦 Dependencies (Windows)

```kotlin
// build.gradle.kts (desktop)
dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-desktop:1.7.3")
    implementation("io.ktor:ktor-client-okhttp:2.3.7")  // HTTP client
    implementation("com.github.sceneview:sceneview:2.0.3")  // VRM 3D
}
```

---

## 🎯 Key Decisions

### 1. Run Ollama Locally
- Ollama runs on Windows, app connects to localhost
- No network calls, fast responses

### 2. Use Windows TTS
- Windows Speech API for voice
- Better quality than Android TTS

### 3. Compose Desktop for UI
- Modern, declarative
- Share UI logic with Android
- Good Windows support

### 4. System Tray + Overlay
- Echo always accessible
- Non-intrusive presence

---

## 🔧 Technical Details

### Window Overlay Implementation

```kotlin
// Compose Desktop
Window(
    onCloseRequest = { },
    undecorated = true,
    transparent = true,
    alwaysOnTop = true
) {
    // Semi-transparent background
    Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.3f))) {
        EchoDisplay()
    }
}
```

### Hotkey Registration

```kotlin
// Using JNA library for Windows hotkeys
GlobalHotkeyManager.register("Win+E") {
    toggleWindow()
}
```

### System Tray

```kotlin
// Java AWT System Tray
val tray = SystemTray.getSystemTray()
val image = createTrayImage()
val trayIcon = TrayIcon(image, "Echo")
tray.add(trayIcon)
```

---

## 📊 Effort Estimate

| Phase | Time |
|-------|------|
| Setup | 1-2 days |
| Shared Core | 2-3 days |
| Windows UI | 3-4 days |
| Windows Features | 2-3 days |
| Polish | 1-2 days |
| **Total** | **9-14 days** |

---

## ✅ Next Steps

1. **Approve design** - User confirms architecture
2. **Set up multi-platform project** - Create KMP structure
3. **Start implementation** - Begin with shared core

---

*Design created: 2026-01-31*
