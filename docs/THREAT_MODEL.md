# MVP threat model

## Assets

The primary sensitive asset is the TOTP shared secret. Generated OTPs and decrypted secret bytes are also sensitive while in memory.

## Trust boundaries

- Android Keystore protects the wrapping key.
- Room persists metadata and encrypted secret envelopes.
- Camera/ML Kit only produces a raw QR string; parsing and validation remain outside scanner UI code.
- Clipboard and screenshots cross the app process boundary and therefore receive explicit exposure controls.

## MVP controls

- AES-256-GCM with a random IV per secret.
- No plaintext secret in UI state, logs, analytics, or saved state.
- Backup disabled to avoid restoring ciphertext without its Keystore key.
- Optional biometric/device-credential app lock and `FLAG_SECURE`.
- Clipboard clearing for copied OTPs.
- Offline OTP generation using system time.

## Deferred features

Export/import, cloud sync, Google Authenticator migration QR, analytics carrying account data, and key recovery are outside the MVP. Each requires a dedicated threat-model update before implementation.
