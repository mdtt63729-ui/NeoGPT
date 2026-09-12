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

## V8 — CI compile repair

- Fixed `NeoDrawer` missing `dp` and `clip` imports reported by the release compiler.
- Fixed the splash `painterResource` call that was incorrectly wrapped in `remember`, which caused a composable-scope compiler error with the current Compose toolchain.
- Kept the V7 startup hardening, Android Keystore API-key storage, system-following Material 3 theme, supplied Neo GPT artwork, and release-only unsigned APK workflow unchanged.


## V9 — Premium composer, voice, attachments & Live UI
- Home and new-chat empty states now share the same premium Material 3 visual language.
- Composer action morphs between Live Conversation, Send, and Stop with animated transitions.
- Voice input uses Android SpeechRecognizer with partial transcription, a visible listening state, manual stop, and a two-second silence completion target.
- The composer plus button opens the Android document picker; selected files travel into the chat and are uploaded through Gemini Files API before prompting.
- Live Conversation now has a dedicated animated Material 3 orb/transcript UI. Gemini Live transport/audio is intentionally left as the next integration step.
- Splash redesigned around the supplied Neo GPT artwork with restrained Material 3 tonal/elevation motion.
- Android framework startup theme follows system Light/Dark while Compose uses system theme + dynamic Material colors.

## V10 — Multi-provider AI, Gemini 3 refresh & premium response rendering
- Replaced the old Gemini 2.x model defaults with the current Gemini 3 Flash family used by the app: Gemini 3.8 Flash, 3.7 Flash, 3.6 Flash, 3.5 Flash and the official 3.5 Flash-Lite endpoint.
- Added an explicitly disabled catalogue entry for the requested `gemini-3.7-flash-lite` name because Google currently publishes `gemini-3.5-flash-lite` rather than a 3.7 Flash-Lite endpoint.
- Added encrypted provider-key storage for Google Gemini, OpenRouter and NVIDIA NIM.
- Added OpenAI-compatible streaming transport for OpenRouter (`https://openrouter.ai/api/v1`) and NVIDIA NIM (`https://integrate.api.nvidia.com/v1`) using `/chat/completions` and SSE streaming.
- Added OpenRouter model catalogue entries supplied for this build and the supplied NVIDIA NIM model identifiers. NVIDIA specialized non-chat catalogue entries are shown but disabled so they cannot accidentally be sent to `/chat/completions`.
- Home model picker now filters to configured providers and labels each model by provider.
- First-launch setup now supports connecting Gemini, OpenRouter and NVIDIA instead of requiring Gemini specifically.
- Added premium thinking state animation, streaming cursor, styled inline Markdown (bold/italic/inline code), headings, lists, quotes and code surfaces.
- Existing Gemini Files API attachment flow remains intact for Gemini; OpenAI-compatible providers support image, PDF and text attachments where the provider/model accepts those modalities.

## V12 — Release compile fixes
- Fixed Android Keystore nullable-string smart-cast failure in `SecureStorage`.
- Fixed `NeoIconButton` positional argument calls by using explicit named parameters.
- Fixed `NeoMarkdown` `withStyle` import.
- Fixed Home/Chat coroutine `launch` imports used by voice state collection.
- Fixed `GeminiDataSource` construction to bind the `apiKeyProvider` trailing lambda explicitly.
- Fixed Home model-provider filtering to use the encoded provider/model ID rather than comparing incompatible types.
- Fixed Compose `Box(Alignment.Center)` calls to the current `contentAlignment` parameter form.
- Release workflow remains unsigned-release-only; no debug APK task is introduced.


## V13 — Neo 4.1 Alpha built-in model

- Added **Neo 4.1 Alpha** as the first/default model.
- Neo 4.1 Alpha uses the supplied no-user-key backend and works even when Gemini, OpenRouter, and NVIDIA keys are not configured.
- App startup now opens Home directly because a built-in model is available. API setup remains available for optional providers.
- Added a **Generate image** plugin to the composer + button. Selecting it automatically prefixes `/image ` to the composer text.
- Neo image requests use the supplied image-generation backend. Natural image requests are also supported through the Neo 4.1 Alpha persona flow.
- Added a smooth 1:1 live image-generation card, shimmer/orb animation, blur-to-sharp + fade/scale reveal, and **Image created 🖼️** status.
- Long-press the generated image for **2 seconds** to reveal the Download image action. Images are saved to `Pictures/NeoGPT` on modern Android.
- Composer horizontal margins were reduced so the input surface is wider on mobile screens.

## V14 changes

- Added a local offline Admin Login under Settings; no Firebase/backend is used.
- Neo 4.1 Alpha is hidden from the model picker until the local admin gate is unlocked.
- The app never shows the admin login during startup, splash, landing, or onboarding.
- Admin credential comparison uses SHA-256 digests rather than embedding the clear-text email/password in the APK string table. This is an offline access gate, not server-grade authentication; a determined reverse engineer can still analyze the authentication implementation.
- Added a built-in `zip` tool to the local tool registry so agent/tool orchestration can create ZIP archives from project directories.
- BuildManager now targets `assembleRelease` rather than `assembleDebug`.
- Added an iOS-inspired liquid-glass visual system with translucent layers, specular highlight, and fine glass borders; Android does not expose Apple's private backdrop-material implementation, so this is a native Compose approximation rather than Apple's proprietary rendering stack.
- If no provider API key is configured and admin is not unlocked, the home model picker shows a disabled `No AI connected` state instead of exposing Neo 4.1 Alpha.


## V15 chat streaming polish
- Added a floating liquid-glass scroll-to-latest arrow in Chat.
- AI streaming remains auto-pinned while the user is near the bottom; deliberate upward scrolling is respected.
- Added a restrained active-line settle animation during streamed AI text updates to keep the response motion smooth without a distracting character-by-character effect.
- Chat content reserves bottom space so the floating control stays clear of the composer.


## V16 settings and live UI controls
- Added persistent real-time Settings state: System/Light/Dark/AMOLED theme, Dynamic Material colors, Liquid Glass, animations, AI auto-scroll, Enter-to-send, haptics and timestamps.
- Settings changes are emitted through a process-wide StateFlow and applied immediately without restarting the app.
- Liquid Glass mode changes the app-wide Material color surfaces/backgrounds and enables glass treatment on Neo GPT core cards, top bar, composer, model controls and icon controls.
- When Liquid Glass is off, the app returns to standard Material 3 surfaces and interaction treatment.
- Chat auto-scroll now follows the setting and still respects deliberate upward scrolling.
- Enter-to-send is live configurable.
- Haptic feedback and press-scale behavior are live configurable.
