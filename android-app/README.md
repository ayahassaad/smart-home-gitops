# Smart Home Monitor — Lab 1

Android app (Kotlin, Jetpack Compose, MVVM) that polls a private GitHub
repository's open Pull Requests and issue comments every 30 seconds, and
flags LuxAgent-style deceptive comments using a weighted keyword/regex
detector.

## Before you open this in Android Studio

1. Open `local.properties` (already in this folder, git-ignored) and fill in:
   - `GITHUB_TOKEN` — a classic PAT with the `repo` scope.
   - `GITHUB_OWNER` — your GitHub username/org.
   - `GITHUB_REPO` — defaults to `smart-home-gitops`.
2. Android Studio will add an `sdk.dir` line to the same file automatically
   on first sync — leave that alone.
3. Let Gradle sync. First sync needs internet access to download the Android
   Gradle Plugin, Compose, Retrofit, etc. from Google's and Maven Central's
   repositories.

## Project layout

```
app/src/main/java/com/ayah/smarthomemonitor/
├── MainActivity.kt                  # thin entry point, wires Compose to the ViewModel
├── ui/
│   ├── MonitorScreen.kt             # View layer — renders UiState, nothing else
│   └── theme/Theme.kt
├── viewmodel/
│   ├── MonitorViewModel.kt          # polling loop, owns StateFlow<UiState>
│   └── UiState.kt                   # Normal / SecurityAlert / Error
├── domain/
│   ├── DeceptionDetector.kt         # pure Kotlin, no Android deps
│   └── DetectionResult.kt
└── data/
    ├── remote/GitHubApiService.kt   # Retrofit interface
    ├── remote/dto/                  # PullRequestDto, IssueCommentDto
    ├── network/NetworkModule.kt     # Retrofit/OkHttp + auth header wiring
    └── repository/SmartHomeRepository.kt

app/src/test/java/.../domain/DeceptionDetectorTest.kt   # JUnit4 tests
```

## Detection algorithm (for the video's Algorithm Analysis segment)

Five categories, each with regex patterns and a fixed weight. If any pattern
in a category matches, that category's weight is added once:

| Category                     | Weight |
|-------------------------------|-------:|
| HVAC / valve failure           | 30 |
| Electrical fault               | 25 |
| Structural damage              | 25 |
| Freezing danger                | 20 |
| Urgency / deception language   | 25 |

Score is capped at 100. `isAttack = confidence >= 50` (see
`DeceptionDetector.ATTACK_THRESHOLD`).

## What's NOT in Lab 1 (by design)

No writing back to GitHub, no posting comments, no human override, no
config changes from the app. Those are later labs.
