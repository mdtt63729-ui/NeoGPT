# Neo GPT — Android AI Workspace

> Simple on the surface. Powerful underneath.

## Overview
Neo GPT is an all-in-one AI workspace for Android built with Kotlin, Jetpack Compose, and Material 3.

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Font:** Josefin Sans (global)
- **Architecture:** MVVM + Clean Architecture + Repository Pattern
- **AI Backend:** Gemini API (configurable model IDs)
- **Min SDK:** API 24+
- **Target SDK:** API 35

## Project Structure
```
NeoGPT/
├── app/src/main/java/com/neogpt/app/
│   ├── ui/
│   │   ├── theme/          # Color, Type, Shape, Dimens, Theme
│   │   ├── components/      # Neo* reusable components
│   │   ├── navigation/     # NavGraph, Routes, Animations
│   │   ├── animations/     # Motion constants
│   │   └── screens/        # 13 screen packages
│   ├── data/
│   │   ├── remote/gemini/  # Gemini API layer
│   │   ├── local/           # Room database, DAOs, entities
│   │   └── repository/     # 7 repositories
│   ├── domain/
│   │   ├── model/           # 8 domain models
│   │   └── usecase/         # 7 use cases
│   ├── security/            # SecureStorage, ApiKeyProvider, Encryption
│   ├── network/             # NetworkMonitor, HttpClient, errors
│   ├── tools/               # Tool interface + 5 tools + executor
│   ├── agent/               # Agent engine, planner, executor, checkpoints
│   ├── voice/               # Voice input, audio recorder
│   ├── files/               # File manager, type detector, picker
│   ├── research/            # Research engine, planner, sources, citations
│   ├── canvas/              # Canvas editor, history, export
│   ├── coding/              # Code workspace, diff, patch, build, test
│   └── integrations/
│       ├── github/          # GitHub API, repos, commits, PRs
│       └── mcp/             # MCP client/server/tools
├── app/src/main/res/
│   ├── font/                # Josefin Sans font family XML
│   ├── values/              # colors, strings, themes
│   └── xml/                 # FileProvider paths
├── app/build.gradle.kts
├── gradle/libs.versions.toml
├── settings.gradle.kts
└── build.gradle.kts
```

## Setup
1. Josefin Sans TTF files are bundled in `app/src/main/res/font/` and are referenced locally by the Compose theme.
2. Set your Gemini API key in Settings → Advanced → API Configuration
3. Build with Android Studio (Ladybug+) or Gradle 8.9.
4. GitHub Actions automatically starts a debug build on every push and pull request; the generated APK is uploaded as a workflow artifact.

## Development Phases
See `NeoGPT_Design_System_Spec.md` for the complete design system and the PRD for 10 development phases.
