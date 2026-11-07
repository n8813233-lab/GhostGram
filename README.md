# GhostGram

Android Telegram client built from the [Mango](https://github.com/2492866096/Mango) 4.9 codebase, with the appearance and feature set of [Cherrygram](https://github.com/arslan4k1390/Cherrygram) 11.4.0 ported in.

- Package: `com.Ghost.wed`
- Appearance: Cherrygram (Solar icon pack, Cherry themes) — Mango's own appearance layer is not the default
- Features: foundation + C1–C6 porting batches fully landed (sleep timer, vCard export, QR name card, local settings backup, message menu customization, message filtering, Gemini (user-supplied key only), per-scenario biometric chat locks, Braille spoilers, profile enhancements, chat toggles, main tab bar & folder customization, Solar icon pack, voice enhancements, CameraX basics)

## Privacy / red lines

Both upstream clients ship developer remote-control mechanisms. They are **removed** here and must stay removed:

- Mango: default remote MTProto proxy & proxy-list sync, auto-join channels, remote pinned dialogs / announcements / promoted accounts, remote scam & keyword lists, cloud (OTA) update channel
- Cherrygram: hourly remote config, AdsGram, Firebase Analytics / Crashlytics identifiers, OTA updates via a Telegram channel, obfuscated self-check module, donation-list-based feature gating

Donation-gated upstream features are ported unlocked and free. Third-party API keys (e.g. Gemini) are user-filled only, never hardcoded.

## Build

Built by GitHub Actions (see `.github/workflows/build.yml`): JDK 17, Android SDK platform 35 / build-tools 35.0.0, NDK 21.4.7075529, CMake 3.10.2.4988404, Gradle wrapper, `:TMessagesProj_App:assembleAfatRelease`.

Signing note: `gradle.properties` carries **public CI placeholder credentials** (`ghostgramci` / `ghostgram-ci`) and the workflow generates a throwaway CI keystore at build time, only so the build can complete its signing step. Release builds distributed by the maintainer are re-signed with the project's real key, which is **not** in this repository.

`TMessagesProj_App/google-services.json` is an obvious placeholder (no real Firebase project); replace it with your own if you need Firebase-dependent functionality.

## Status

Porting complete; compile verification in progress via CI. Not yet verified on a real device.

## License

GPL (same as the upstream Telegram/Mango codebases). Upstream Mango documentation is kept in `README_UPSTREAM.md`.
