# Cursor Agents for Android

Unofficial Android client that wraps the **official** Cursor Agents web app (`https://cursor.com/agents`) in a native shell.

This is **not** an Anysphere / Cursor product. Cursor’s own Android app is planned and has no public release date. The iOS app and this wrapper both talk to the same cloud-agent backend as the website.

## What you get (same as the web / close to iOS)

Cursor documents that the iOS app is a mobile controller for Cloud Agents, not a mobile IDE. This app loads that same workflow:

- Sign in with your Cursor account (including SSO when the provider allows it in a WebView)
- Start and monitor cloud agents
- Follow chats, review diffs, and merge PRs in the official web UI
- Camera / file attachments via the system picker
- Microphone for voice features the web app already supports
- Downloads, pull-to-refresh, hardware back, and `cursor.com/agents` deep links
- Session cookies persist between launches

## What stays native-iOS-only

These need Cursor’s own app + OS APIs, not a website wrapper:

- Lock-screen Live Activities / Dynamic Island
- First-party push notifications from Cursor
- Remote Control handoff chrome that lives in the desktop Agents Window
- Pixel-identical native iOS UI

## Requirements

- Android 8.0 (API 26) or later
- Android Studio Ladybug+ or command-line SDK 35
- A Cursor account that can use Cloud Agents (paid plans for starting runs)

## Build

```bash
./gradlew :app:assembleDebug
```

Install the APK from `app/build/outputs/apk/debug/` onto a device or emulator.

Open the project root in Android Studio and run the `app` configuration if you prefer the IDE.

## Why a WebView, not a UI clone

Cursor’s mobile product **is** the agents website plus a native shell. Rebuilding their screens and private APIs would copy a proprietary product. Loading the official site means you always get the current agents experience, sign-in, and security checks.

Google or other identity providers sometimes block sign-in inside embedded WebViews. If that happens, complete login at [cursor.com/agents](https://cursor.com/agents) in Chrome, or add that site to the home screen as a PWA until you can sign in here.

## Disclaimer

Not affiliated with, endorsed by, or supported by Anysphere, Inc. Cursor is a trademark of its owner. Use this wrapper with your own account and at your own risk.
