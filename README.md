# Sir Azeem — Android AI Student Agent

A GitHub-ready Android foundation for the Sir Azeem autonomous student communication agent.

## Included in this release

- Native Android app
- Premium dark glass-inspired UI
- Distinct tinted dashboard cards
- Persistent local knowledge store
- "Teach Agent" screen
- Student database
- Sequential Student IDs
- WhatsApp notification monitoring
- Notification RemoteInput reply support when WhatsApp exposes a reply action
- Optional OpenAI-compatible chat endpoint
- Agent enable/disable controls
- Auto-reply / approval mode
- Orders and files data models
- Android notification/accessibility permission screens

## Important Android/WhatsApp limitation

A normal Android app cannot be guaranteed to have unrestricted programmatic control over WhatsApp. This project therefore uses Android-supported notification access for incoming messages and attempts replies only when WhatsApp exposes a notification RemoteInput action.

Fully automated attachment/file sending through WhatsApp is not implemented as a blind UI automation because WhatsApp's private UI is version-dependent and such automation can break after updates. The architecture leaves an extension point for a carefully tested accessibility workflow or an official/authorized messaging API.

## Build on GitHub

1. Create a GitHub repository.
2. Upload the entire project folder.
3. Use Android Studio or add a GitHub Actions workflow.
4. Set up JDK 17 and Android SDK 35.
5. Run:

```bash
gradle :app:assembleDebug
```

The debug APK will be:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## GitHub Actions

A workflow is included under `.github/workflows/build-apk.yml`.

## First-run setup

1. Install the APK.
2. Grant Contacts permission if you want student/contact features.
3. Open Android Settings → Notification Access → enable "Sir Azeem".
4. Optional: enable the accessibility service if a future UI-automation extension is installed.
5. Open Settings in Sir Azeem.
6. Configure an OpenAI-compatible AI endpoint, API key, and model if you want generative responses.
7. Start in Approval Mode before enabling automatic replies.
8. Teach the agent your real business rules through "Teach Agent".

## Safety

Do not store payment credentials or secrets in source code. API keys are stored locally by this starter and should be protected further before production deployment.

## Build fix included
The latest project includes the required `kotlinx.coroutines.cancel` import that fixes the `Unresolved reference: cancel` compiler error in the WhatsApp notification listener.
