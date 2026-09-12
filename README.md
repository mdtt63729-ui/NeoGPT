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
