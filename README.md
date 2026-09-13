# NeoGPT

NeoGPT is an Android AI assistant built with Jetpack Compose and Material 3.

## Current release build

- Version: 1.6.0 (versionCode 7)
- compileSdk / targetSdk: 36
- Android Gradle Plugin: 9.1.1
- Gradle: 9.3.1
- JDK: 17
- Compose BOM: 2026.06.01 (Compose 1.11.4 line)
- Build output: **unsigned Release APK only**

## GitHub Actions

The workflow at `.github/workflows/android-build.yml` runs on every push to every branch and supports `workflow_dispatch`. It builds only `:app:assembleRelease`, verifies the unsigned APK, and uploads `app-release-unsigned.apk` as the artifact `NeoGPT-unsigned-release-apk`.

The repository intentionally uses the Gradle executable provisioned by `gradle/actions/setup-gradle@v4`; it does not require a Gradle wrapper script.

## UI

- Clean Material 3 surfaces; the Liquid Glass system has been removed.
- iOS-inspired horizontal navigation transitions remain for screen changes.
- Chat composer supports multiline text, attachments, image generation, voice input and Live Conversation.
- Voice input expands into a compact recording bar with cancel, live waveform, stop and send controls.
- AI responses support styled Markdown including bold, italic, underline, strike, inline code, headings, lists and quotes.
- Response text size is adjustable from Settings.
- Settings are organized into clickable category pages rather than one long settings list.

## AI behavior

A locally stored global system prompt is applied to supported text-capable providers/routes: Gemini, OpenRouter, NVIDIA NIM, Neo 4.1 Alpha and Gemini-powered Research. Provider safety policies and higher-priority instructions can still override it.
