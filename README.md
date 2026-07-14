# App Locker

A minimal Android app (Kotlin) that locks **WhatsApp**, **Instagram**, **Gallery**, and **Chrome**
behind a 4-digit PIN, using an `AccessibilityService` to detect which app is currently
in the foreground.

## How it works

1. **`LockAccessibilityService`** listens for `TYPE_WINDOW_STATE_CHANGED` accessibility
   events, which fire whenever a new window (i.e. a new foreground app) appears.
2. When the foreground package matches one of the locked apps (and it hasn't been
   unlocked in the last few seconds), the service launches **`PinEntryActivity`**
   on top of it.
3. **`PinEntryActivity`** asks for the 4-digit PIN. A correct PIN marks that app as
   "unlocked" for a short grace period (`AppConstants.UNLOCK_GRACE_PERIOD_MS`) so the
   user isn't re-prompted the instant the underlying app resumes.
4. **`MainActivity`** lets the user set/change the PIN and toggle which of the four
   apps are locked. **`PinSetupActivity`** handles PIN creation (and re-verifies the
   old PIN before allowing a change).
5. The PIN is never stored in plain text — `PrefsHelper` stores a salted SHA-256 hash
   in `SharedPreferences` (`HashUtils`).

### Package names watched

| App        | Package(s) watched                                                                 |
|------------|--------------------------------------------------------------------------------------|
| WhatsApp   | `com.whatsapp`                                                                       |
| Instagram  | `com.instagram.android`                                                              |
| Gallery    | `com.google.android.apps.photos`, `com.android.gallery3d`, `com.sec.android.gallery3d`, `com.miui.gallery`, `com.oneplus.gallery` |
| Chrome     | `com.android.chrome`                                                                 |

Gallery watches several common OEM/Google gallery packages since there is no single
universal "Gallery" package across Android devices. Add more package names in
`AppConstants.kt` if your device uses a different gallery app.

## Project structure

```
AppLocker/
├── build.gradle.kts                 (project-level)
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts             (app module)
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/applocker/
        │   ├── AppConstants.kt          (lockable app definitions)
        │   ├── HashUtils.kt             (PIN salt + SHA-256 hashing)
        │   ├── PrefsHelper.kt           (SharedPreferences access)
        │   ├── LockState.kt             (in-memory unlock/grace-period state)
        │   ├── AccessibilityUtils.kt    (checks if the service is enabled)
        │   ├── LockAccessibilityService.kt
        │   ├── PinEntryActivity.kt      (the PIN lock screen)
        │   ├── PinSetupActivity.kt      (create/change PIN)
        │   └── MainActivity.kt          (app list + toggles)
        └── res/
            ├── layout/ (activity_main.xml, activity_pin_setup.xml, activity_pin_entry.xml)
            ├── values/ (strings.xml, colors.xml, themes.xml)
            ├── drawable/ (ic_launcher.xml, ic_lock.xml, bg_pin_input.xml)
            └── xml/accessibility_service_config.xml
```

## Opening the project

1. Open Android Studio (Koala/2024.1+ recommended) → **Open** → select the `AppLocker`
   folder.
2. Android Studio will detect there is no Gradle wrapper jar checked in (binary files
   can't be generated in a text-only environment) and will offer to **create the
   Gradle wrapper automatically** — accept that prompt. Alternatively, if you have a
   local Gradle 8.7+ install, run:
   ```
   gradle wrapper --gradle-version 8.7
   ```
   from the project root once, which will generate `gradlew`, `gradlew.bat`, and
   `gradle/wrapper/gradle-wrapper.jar`.
3. Let Gradle sync (it will download AGP 8.5.2 and the dependencies listed in
   `app/build.gradle.kts`).
4. Run the `app` configuration on a device or emulator (minSdk 24 / Android 7.0+).

## Using the app

1. Launch **App Locker**, tap **Set PIN**, and create a 4-digit PIN.
2. Tap **Enable Accessibility Service** — this opens system Accessibility settings.
   Find **App Locker** in the list and turn it on (Android will show a warning dialog
   about the permissions an accessibility service has; this is standard for any
   app-locker style app).
3. Back in App Locker, toggle on the apps you want locked (WhatsApp, Instagram,
   Gallery, Chrome are all locked by default once a PIN is set).
4. Open any locked app — App Locker will intercept it and show the PIN screen before
   you can use it.

## Notes / limitations

- This app needs the **Accessibility Service** permission, which is a sensitive
  Android permission. Google Play has specific policy requirements for apps that use
  it — this project is intended as a learning/personal-use reference, not a
  Play-Store-ready submission.
- Gallery app package names vary a lot between manufacturers; edit the `gallery`
  entry in `AppConstants.kt` if your device's gallery isn't being detected.
- No native app icon PNGs are bundled — the launcher icon is a simple vector
  drawable (`res/drawable/ic_launcher.xml`) referenced directly from the manifest,
  so the project compiles without needing external image assets.
