# Security checklist

Use this checklist before every production release.

- [ ] `android:allowBackup` remains disabled for the MVP.
- [ ] No shared secret, QR payload, recovery material, or OTP is logged or sent to analytics.
- [ ] Account secrets are encrypted with AES-256-GCM and keys remain in Android Keystore.
- [ ] Keystore invalidation and authentication-tag failures are handled without exposing plaintext.
- [ ] OTP clipboard content is cleared after the configured timeout only when unchanged.
- [ ] Screenshot protection is enabled when the user opts in.
- [ ] App lock uses biometric or device credential and relocks after backgrounding.
- [ ] Automatic-time warning is visible when device automatic time is disabled.
- [ ] QR/manual enrollment rejects invalid input before persistence.
- [ ] Database migrations and encryption round-trip/tamper tests pass.
- [ ] `./gradlew test lint assembleDebug assembleRelease` passes.
- [ ] No export, cloud sync, migration QR, or analytics feature is added without a threat-model update.
