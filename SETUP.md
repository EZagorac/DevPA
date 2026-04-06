# Setup Guide — DevPA

Follow these steps in order to get the project building in VS Code.

---

## Step 1 — Install Android Studio (required even if using VS Code)

Download from: https://developer.android.com/studio
Run the installer and let it install the Android SDK.
Note the SDK path — you'll need it for `local.properties`.

---

## Step 2 — Install VS Code Extensions

Open VS Code → Extensions (Ctrl+Shift+X) → Install each:

- **Kotlin** by fwcd
- **Gradle for Java** by Microsoft
- **Android iOS Emulator** by DiemasMichiels
- **GitLens** by GitKraken

---

## Step 3 — Clone and configure

```bash
git clone https://github.com/yourusername/devpa.git
cd devpa
```

Edit `local.properties` in the project root:
```
CLAUDE_API_KEY=sk-ant-your-key-here
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk   # Windows
# sdk.dir=/Users/YourName/Library/Android/sdk               # Mac
# sdk.dir=/home/YourName/Android/Sdk                        # Linux
```

---

## Step 4 — Get your Anthropic API key

1. Go to https://console.anthropic.com
2. Sign up / log in
3. API Keys → Create Key
4. Paste into `local.properties` as shown above

---

## Step 5 — Build the project

In VS Code terminal:
```bash
# Windows (PowerShell)
.\gradlew.bat assembleDebug

# Windows (CMD)
gradlew.bat assembleDebug

# Mac / Linux
chmod +x gradlew
./gradlew assembleDebug
```

The APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Step 6 — Install on your phone

Enable USB debugging on your Android phone:
- Settings → About Phone → tap Build Number 7 times
- Settings → Developer Options → USB Debugging → ON

Add `adb` to your PATH (one-time setup, then restart terminal):
```powershell
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Users\$env:USERNAME\AppData\Local\Android\Sdk\platform-tools", "User")
```

Connect phone via USB, then:
```bash
# Windows
adb install app\build\outputs\apk\debug\app-debug.apk

# Mac/Linux
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or just copy the APK to your phone and open it (allow installs from unknown sources).

---

## Step 7 — Add the home screen widget

1. Long-press your Android home screen
2. Tap **Widgets**
3. Find **DevPA** in the list
4. Drag it to your home screen
5. Resize between 3×2 and 4×2 cells

---

## Step 8 — Gmail integration (Phase 5)

1. Go to https://console.cloud.google.com
2. Create a new project → Enable the **Gmail API**
3. Credentials → Create OAuth 2.0 Client ID → Android
4. Get your SHA-1 fingerprint:
   ```bash
   ./gradlew signingReport
   ```
5. Add the SHA-1 to your Google Cloud OAuth client

---

## Common Issues

**Gradle sync fails:**
- Make sure `sdk.dir` in `local.properties` points to your actual SDK path
- Run `./gradlew clean` then try again

**API key not found:**
- Check `local.properties` is in the root folder (same level as `settings.gradle`)
- Make sure there are no spaces around the `=` sign

**Widget not showing data:**
- Make sure you've opened the app at least once so Room can seed the database
- Long-press the widget → try removing and re-adding it

**Build error on Hilt:**
- Make sure you're using JDK 17, not 11 or 21
- In VS Code: Ctrl+Shift+P → "Java: Configure Java Runtime"
