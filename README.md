# DevPA

A personal AI assistant Android app built for solo indie game developers. Features a home screen widget, AI-powered daily briefings, habit streak tracking, portfolio checklist, Gmail triage, and job search — all in one dark-themed native app.

## Features

- **Home Screen Widget** — shows your best habit streak, days tracked, and portfolio % at a glance
- **AI Morning Briefing** — powered by Claude API, prioritizes your tasks and gives a daily power tip
- **Habit Tracker** — tracks current streak, days since you started, and personal best for each habit. 14-day visual dot history
- **Portfolio Checklist** — 15 career milestones from GitHub setup to your first job application
- **Gmail Triage** — pulls your inbox, detects forwarded mail, sorts by urgency
- **Job Board** — live Unity & Indie game dev listings

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Architecture | MVVM |
| Local DB | Room (SQLite) |
| Dependency Injection | Hilt |
| Networking | Retrofit + OkHttp |
| Background Jobs | WorkManager |
| Widget | AppWidgetProvider + RemoteViews |
| AI | Anthropic Claude API |
| Email | Gmail API (OAuth 2.0) |

## Project Structure

```
app/src/main/java/com/devpa/app/
├── data/
│   ├── db/          # Room entities, DAOs, Database
│   └── repository/  # Claude API, Briefing logic
├── di/              # Hilt dependency injection module
├── ui/
│   ├── briefing/    # Briefing Fragment + ViewModel
│   ├── habits/      # Habits Fragment + ViewModel + Adapter
│   ├── portfolio/   # Portfolio Fragment + ViewModel + Adapter
│   ├── email/       # Email Fragment + ViewModel
│   └── jobs/        # Jobs Fragment + ViewModel
├── util/            # StreakCalculator
└── widget/          # AppWidgetProvider, WorkManager, BootReceiver
```

## Setup

### 1. Clone and open
```bash
git clone https://github.com/yourusername/devpa.git
cd devpa
# Open in VS Code or Android Studio
```

### 2. Add your API key
Create or edit `local.properties` in the project root:
```
CLAUDE_API_KEY=your_anthropic_api_key_here
sdk.dir=/path/to/your/android/sdk
```
> ⚠️ `local.properties` is in `.gitignore` — never commit your API key

### 3. Set up Gmail API
1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project → Enable Gmail API
3. Create OAuth 2.0 credentials (Android app type)
4. Add your SHA-1 fingerprint (run `./gradlew signingReport`)

### 4. Build and run
```bash
./gradlew assembleDebug
# Or press Run in Android Studio / use the Android emulator extension in VS Code
```

### 5. Add the widget
Long-press your home screen → Widgets → DevPA → drag to home screen

## Roadmap

- [ ] Google Calendar integration
- [ ] Game jam deadline tracker
- [ ] Push notifications for daily briefing
- [ ] Dark/light theme toggle
- [ ] Export habit data to CSV

## Architecture Notes

This app uses MVVM throughout:
- **Model** — Room database entities + Retrofit API calls
- **ViewModel** — business logic, exposes `StateFlow` to the UI
- **View** — Fragments observe `StateFlow`, update UI reactively

The home screen widget uses `AppWidgetProvider` + `RemoteViews` (standard Android widget system) refreshed daily by `WorkManager`. This is the correct, battery-efficient approach — not a service running continuously.

## Portfolio Note

This project demonstrates:
- MVVM architecture (industry standard for Android roles)
- Room + Retrofit + Hilt (the standard Android stack)
- Advanced feature: home screen widget with WorkManager
- Real API integrations (Claude, Gmail, Indeed)
- Solving a genuine personal problem (great interview story)
