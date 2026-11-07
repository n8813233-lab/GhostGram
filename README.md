# GhostGram

A privacy-first Android Telegram client: built on Mango as its codebase, with Cherrygram's appearance and feature set fully stitched in, and with both upstreams' developer remote-control and monetization components thoroughly stripped out.

| Item | Details |
| --- | --- |
| Package name | `com.Ghost.wed` |
| Codebase | Mango Client 4.9 |
| Appearance / feature donor | Cherrygram 11.4.0 |
| Build | GitHub Actions cloud builds (see `.github/workflows/build.yml`) |
| License | GPL (same as the upstream Telegram-family codebases) |
| Status | Feature stitching complete; cloud compile verification in progress; not yet tested on a real device |

---

## 1. Origins

GhostGram was not written from scratch. It is a deliberate fusion of two existing clients, with clear choices about what to take and what to refuse:

- **The codebase comes from Mango (4.9):** Mango is a full-featured Android Telegram client with a complete, independently compilable code structure, making it the right skeleton to build on.
- **The appearance and features come from Cherrygram (11.4.0):** Cherrygram's Solar icon system, theme colors, and many practical features are well regarded, but its public source is incomplete — roughly 30 critical constants (including its own API credentials) were never published, so the repository cannot be compiled on its own and can only serve as a donor of appearance and features.
- **The guiding choices:** Cherrygram defines the look, and Mango's own appearance layer is no longer the default; features from both sides are combined in full; and both upstreams' remote-control, advertising/analytics, and monetization code is refused entirely (see Section 3).

The work proceeded in audited batches: renaming and the dual strip-down, then appearance resources and the configuration skeleton, then six feature batches (small utilities, message enhancements, privacy locks and the profile page, the tab bar and folders, the icon pack, and voice and camera).

## 2. Features

### Appearance

- **Solar icon system:** 358 Solar-style icon resources imported wholesale; in-app icons render in Solar style by default, with a Default / Solar icon pack switch
- **Cherry themes:** all 8 Cherrygram themes (including AMOLED pure black and Monet color extraction) replace the default theme set; 56 Cherry-specific theme color keys were added on top of the base set
- **Main tab bar:** the bottom main tab bar was rebuilt with Cherrygram semantics; tabs can be added, removed, and reordered

### Everyday utilities

- **Sleep timer:** audio playback can be set to stop automatically
- **vCard export:** contacts can be exported in batch as vCard files
- **QR name card:** generate and scan Telegram QR name cards
- **Settings backup:** all settings can be exported to a local file and restored on another device — fully local, with no cloud relay

### Messaging

- **Customizable message menu:** items in the long-press message menu can be added, removed, and reordered, with an optional compact menu view
- **Message filtering:** a local keyword filtering engine (merged with the base client's local filter into a single engine); all matching runs entirely on the device
- **Gemini assistant:** in-chat AI summaries, Q&A, and voice transcription; the API key is supplied by the user and connects directly to Google's official API — the client ships with no built-in key
- **Braille spoilers:** spoiler content can be rendered in Braille form, entirely on the device

### Privacy and chats

- **Per-scenario biometric locks:** opening the app, deleting messages, viewing specific chats, and other scenarios can each require fingerprint or face verification
- **Profile page enhancements:** account registration date, data center (DC), and similar information derived entirely from local data, with no external service involved
- **Chat toggle group:** more than twenty fine-grained switches, including hiding "send as channel," archive gestures, and unread-first sorting

### Folders

- **Folder bar customization:** folder bar style, counting rules (muted chats can be excluded), and the top folder name display are all adjustable
- **Folder icon picker:** custom icons can be chosen for folders (note: the base data model has no server-side sync field, so custom icons are stored locally only and must be set again after a reinstall)

### Voice and camera

- **Voice enhancements:** system sounds are muted while recording, voice messages can play continuously, and voice-to-text transcription is supported (optionally through the Gemini channel)
- **CameraX basics:** the camera settings page and CameraX base components are in place (add-on camera switching is still being wired; in the current version selecting CameraX has no practical effect yet, and the app falls back to the original camera)

### Free by principle

Features that required a donation upstream (menu customization, message filtering, folder bar customization, and more) have had their paywalls removed here and are **enabled for free by default**. Upstream payment card numbers, affiliate links, and payment components were not ported at all.

## 3. Privacy

For a third-party client, the greatest risk is often not a lack of features — it is the remote controls a developer leaves in the code. Before any stitching began, both upstreams were put through a source-level privacy audit. The findings and how they were handled are as follows.

### Design principles

1. **Zero remote delivery:** the client must never pull configuration, lists, proxies, or update packages that can change its behavior from a developer's server
2. **Zero ads and analytics:** no advertising SDKs, and no behavioral analytics or crash reporting that carries user identifiers
3. **Zero built-in credentials:** no hardcoded third-party API keys, proxies, or payment details; any feature that needs a key is filled in by the user
4. **Local first:** backup, filtering, locking, icons, and similar features all run entirely on the device

### Removed: Mango's remote-control plane (10 mechanisms)

| Mechanism | Original behavior | Handling in this project |
| --- | --- | --- |
| Built-in proxy | On first install, a developer-hardcoded MTProto proxy was enabled by default; the proxy list re-synced from the developer's cloud storage every 30 minutes and could switch automatically | Hardcoded proxy and all default sources removed; proxies are user-supplied only |
| Auto-join channels | After sign-in, the account was automatically joined to public channels listed in a remote file, with no off switch | Entire module deleted |
| Remote pinning / promotion | Remotely delivered pinned dialogs, promoted accounts, and channel pages | Deleted |
| Remote announcements | The developer could pop up an announcement to all users at any time | Deleted |
| Remote lists | Scam flags, block lists, and anti-fraud keyword lists were all delivered from the developer's cloud storage | List sources emptied; anti-fraud protection rewritten as purely local rules |
| Cloud updates | APK updates were downloaded directly from the developer's cloud storage, with no signature verification in code | Entire module deleted |

All of these mechanisms were hosted on a single developer's 123Pan cloud storage account. After the strip-down, a full-project search for that provider's domains and account identifiers returns zero hits.

### Removed: Cherrygram's remote and monetization components

- **Hourly remote configuration:** could remotely deliver feature switches, donation card numbers, affiliate links, and a hardcoded proxy — the entire channel was cut
- **AdsGram advertising SDK:** sent the user's Telegram ID and IP address to an advertising platform — not ported
- **Firebase Analytics / Crashlytics:** behavioral analytics and crash reporting carrying user IDs and usernames — not ported
- **OTA updates:** update packages pulled from its Telegram channel — not ported
- **Obfuscated self-check module:** unauditable signature / tamper self-check code — the module was not ported at all
- **Donation checks:** several features validated against a remote donor list locally (including one hidden inside the appearance configuration) — all cut and made free

### Signing and placeholder notes

- The signing credentials in `gradle.properties` (`ghostgramci` / `ghostgram-ci`) are **public CI placeholder values**; the build workflow generates a throwaway keystore at build time, solely so the cloud build can complete its signing step
- Release packages distributed by the maintainer are re-signed locally with the project's dedicated key, which is **not** in this repository
- `TMessagesProj_App/google-services.json` is an obvious placeholder file (no real Firebase project); replace it with your own configuration if you need the related functionality

## 4. Acknowledgements

GhostGram stands on the shoulders of the following open-source projects, and thanks them all:

- **[Telegram](https://github.com/DrKLO/Telegram):** the official open-source Android client, the origin of everything in this project
- **[Mango](https://github.com/2492866096/Mango)** by [@2492866096](https://github.com/2492866096): provided the complete, compilable codebase; its upstream documentation is preserved in this repository as `README_UPSTREAM.md`
- **[Cherrygram](https://github.com/arslan4k1390/Cherrygram)** by [@arslan4k1390](https://github.com/arslan4k1390): provided the Solar icon system, the Cherry themes, and the original implementations of most of the features stitched into this project
- **Open-source components:** AndroidX CameraX, the Google Gemini SDK, and others (see each module's `build.gradle` for the full dependency list)

This project is not affiliated with any of the projects above. All modifications to upstream code — in particular the strip-downs listed in Section 3 — are the decisions of this project's maintainer. If any upstream author has concerns about attribution or licensing, please open an issue.
