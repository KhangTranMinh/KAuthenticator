# Dependency and license review

Direct runtime dependencies used by the MVP are intentionally limited to AndroidX/Jetpack components and the bundled ML Kit barcode scanner.

Before each production release:

1. Run `./gradlew dependencies` and compare the resolved graph with the previous release.
2. Review release notes for Android Gradle Plugin, Kotlin, Room, CameraX, Biometric, Compose, Lifecycle, and ML Kit when versions change.
3. Verify every redistributed dependency's license/notice obligations and include required notices in the release artifact or store listing as applicable.
4. Reject dependencies that add network access, analytics, advertising, or secret-handling behavior without a threat-model update.
5. Re-run unit tests, lint, debug/release assembly, and instrumented security tests after dependency upgrades.

Current version pins live in `gradle/libs.versions.toml`; do not use dynamic (`+`) versions.
