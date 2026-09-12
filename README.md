# NeoGPT

NeoGPT is an Android AI assistant application built with Jetpack Compose and Material 3.

## CI build

GitHub Actions is configured in `.github/workflows/android-build.yml`.

- Every push to any branch starts the workflow automatically.
- Pull requests also run the same validation/build.
- `workflow_dispatch` is enabled for a manual run from the GitHub Actions tab.
- The workflow builds **only the unsigned Release APK** with `:app:assembleRelease`.
- No Debug APK is built or uploaded by the workflow.
- The resulting unsigned APK is uploaded as a GitHub Actions artifact.

## Toolchain

- Android Gradle Plugin 9.1.1
- Gradle 9.3.1
- JDK 17
- compileSdk / targetSdk 37
- Jetpack Compose BOM 2026.08.00
- Material 3 1.4.0

Compose 1.12 requires compileSdk 37 and AGP 9.x, so the project uses that toolchain rather than mixing the newer Compose libraries with AGP 8.x.

The CI workflow does not request the invalid literal `platforms;android-37` package. It detects the API 37 platform revision exposed by the GitHub runner (preferring `android-37.0`) and creates the canonical `android-37` SDK path when the runner stores the preview platform under a revisioned directory.


## Build compatibility

The project uses Room 2.7.1 with SQLite 2.5.0 and keeps KSP on the KSP1 implementation for the Kotlin 2.0 toolchain. Room DAO write methods return affected-row/insert IDs instead of `Unit`, avoiding the known `unexpected jvm signature V` annotation-processing failure seen with older Room/KSP combinations.

## Latest CI compile fixes (V4)
- Removed the duplicate `NeoEmptyState` composable that caused overload ambiguity.
- Added missing Compose `dp` imports in Chat and Custom AI screens.
- Fixed the clickable `ElevatedCard` call in Projects by using named parameters.
- Explicitly opted into the experimental Material 3 API used by `CenterAlignedTopAppBar`.
- The workflow still builds only the unsigned release APK.

## V5 UI / launch / AI connection update

- Replaced the old splash presentation with a fully in-app animated launch screen; no logo tile or icon artwork is rendered by the Compose splash.
- Android 12+ starting-window icon is neutral/transparent so the custom launch experience can take over cleanly.
- First launch now opens a dedicated Gemini API key setup screen when no key is stored. The key is verified against the Gemini model list before it is saved.
- Home screen redesigned as a clean AI workspace: no large logo card, no search button, no quick-action clutter.
- Top controls are reduced to menu, model selector, and new-chat.
- Model IDs are corrected to real Gemini API IDs and the app now discovers stream-capable Gemini models from the API when possible.
- Composer receives IME padding so it moves above the Android keyboard instead of being covered.
- Drawer primary navigation is simplified to New Chat and Chats; Search is no longer a primary drawer button.
- Secondary-screen back navigation continues to pop the current route so it returns to the screen that opened it.
- At the root Home screen, the system back action uses a double-back guard and then a confirmation dialog before exiting.

## V6 CI compile-fix update

- Replaced the unavailable `BackHandler` import with the Activity `OnBackPressedDispatcher` callback so back handling compiles reliably with the pinned Activity Compose dependency.
- Added the missing Compose `size` import used by the first-launch API key screen.
- Added the missing Compose `setValue` delegate import used by the custom splash state.
- Moved `MaterialTheme.colorScheme` reads out of the non-`@Composable` Canvas draw lambda and into composable scope, fixing the splash `@Composable invocations can only happen from the context of a @Composable function` errors.
- Preserved the intended behavior: drawer closes first, secondary screens pop back to the previous screen, Home requires two back presses and then shows the exit confirmation dialog.


## V7 — Crash-safe startup, system theme & official app artwork

- Startup path simplified to remove unnecessary Hilt initialization.
- API-key storage now uses Android Keystore directly with AES-256-GCM and safely handles invalidated keys.
- Release shrinking is disabled to prevent release-only runtime stripping while the product is under active development.
- The supplied Neo GPT artwork is now the launcher/round icon and is reused subtly in the splash, Home and API setup surfaces.
- Compose theme now follows the Android system light/dark setting. Android 12+ uses system dynamic Material colors.
- Premium in-app splash animation remains separate from the neutral Android starting window.
