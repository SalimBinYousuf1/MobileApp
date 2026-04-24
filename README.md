# FluidMusic (Android Kotlin + Gradle)

FluidMusic is a modern Android music player foundation with a fluid premium UX and command-line (CMD) workflow.

## Features included

- Online + offline catalog mode
- Play / pause / next / previous controls
- Queue management
- Favorite tracks
- Playlist management
- Offline save/download simulation + clear downloads
- Sleep timer, repeat mode, shuffle mode
- Dynamic search with quick play/download actions
- Compose screens: Home, Search, Library, Now Playing, Settings, Downloads, Profile

## Build and create app from CMD

```bash
scripts/cmd-build.sh env
scripts/cmd-build.sh clean
scripts/cmd-build.sh test
scripts/cmd-build.sh create
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

> If Java 17 is at a different location, set `JAVA17_HOME=/path/to/jdk17` before running commands.
