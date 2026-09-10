# KAuthenticator

An offline-first Android authenticator application built with Kotlin and Jetpack Compose.

## Project status

The Android project skeleton is initialized. TOTP functionality and encrypted account storage are planned but not implemented yet. See [Implementation plan](docs/IMPLEMENTATION_PLAN.md).

## Requirements

- Android Studio with Android SDK 35
- JDK 17

## Build

```bash
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
