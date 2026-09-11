# Release build

## Verification

Run:

```bash
./gradlew test lint assembleDebug assembleRelease
```

This runs unit tests, Android lint, the debug build, and the release build. Instrumented tests still require an emulator/device and should run before a production release.

## R8

The release build must use the optimized default Android rules plus the app's project-specific ProGuard/R8 rules. Avoid broad `-keep class **` rules. Persistence must not depend on obfuscatable enum names; KAuthenticator uses explicit stable persistence values for TOTP algorithms, account sorting, and theme settings.

Before publishing, inspect the generated mapping and run the release APK/AAB through the manual security checklist.

## Signed release

Keep signing credentials outside the repository. Do not commit keystores, passwords, aliases, or local Gradle properties containing credentials. Create the signed APK/AAB with Android Studio's **Generate Signed App Bundle / APK** flow or inject signing credentials through the CI secret store.

Record only the non-secret release metadata in the release ticket: version name/code, commit SHA, artifact checksum, signing certificate fingerprint, and verification result.
