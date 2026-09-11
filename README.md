# Neo GPT

Neo GPT is a Material 3 Android AI workspace built with Jetpack Compose and Josefin Sans.

## Included

- Polished Material 3 home, chat, settings, projects, files, tasks, research, code, canvas, search and notification screens.
- Gemini API connection with encrypted API-key storage.
- Streaming Gemini responses.
- Stop generation, copy and share response actions.
- Android speech-recognition permission flow.
- File picker UI.
- GitHub Actions debug build on every push and pull request.

## Run

Open the `NeoGPT` directory in Android Studio with JDK 17 and sync the project.

To use live AI:
1. Install the app.
2. Open **Settings**.
3. Add a Gemini API key.
4. Return to Home and start a chat.

The API key is stored with AndroidX Security `EncryptedSharedPreferences`.

## CI

`.github/workflows/android-build.yml` builds `assembleDebug` on every push and pull request and uploads the generated APK as a workflow artifact.
