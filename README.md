# MITS Parent — Android app (Phase one)

Native Android parent app for **MITS Schools** (Innovation • Creativity • Values), built with Kotlin and Jetpack Compose (Material 3). It follows the approved flowchart:

```
Parent Login  →  Select Child's Class  →  Parent Dashboard
                                            ├─ Notifications Bar  → Announcements & Reminders
                                            ├─ Yearly Calendar    → Events, Holidays & Exams
                                            ├─ Weekly Diary       → Weekly Syllabus & Homework
                                            └─ Daily Diary        → Daily Classwork & Homework
```

## Screens

| Screen | What it does |
|---|---|
| Splash | MITS maple-leaf mark on launch (Android 12+ splash API) |
| Login | Registered mobile number + password, show/hide password, inline errors, "Forgot password" help with school contact |
| Select class | Play Group, Nursery, KG, Grade 1–6 grouped as Early Years / Primary; remembered on the device |
| Dashboard | Greeting, class switcher, Notifications bar (latest + unread count), "Today at a glance" (homework due, subjects today, next event), Daily / Weekly / Yearly cards, Coming-up list, pull to refresh |
| Daily Diary | Mon–Sat date strip, teacher's note, classwork and homework per subject, homework tick-off with progress bar, holiday/weekend states |
| Weekly Diary | Week navigation, weekly focus, per-subject syllabus, learning objectives and homework (expandable) |
| Yearly Calendar | Month grid with colour-coded holidays / exams / events / parent meetings, filters, event details, **Add to my calendar** |
| Notifications | Grouped by Today / Yesterday / This week / Earlier, category filters, unread markers, mark all read |
| Profile & settings | Change class, Light / Dark / System theme, call or email the school office, sign out |

## Improvements beyond the flowchart

- **Bottom navigation** (Home, Daily, Weekly, Calendar, Alerts) so every flowchart branch is one tap away, not just from the dashboard.
- **"Today at a glance"** summary so parents see what matters without opening each section.
- **Homework tracker** — parents tick homework off; progress is saved on the device.
- **Add school events to the phone's own calendar** (no extra permissions needed).
- **Class is remembered** and can be changed any time from the dashboard header or Profile.
- **Dark mode**, edge-to-edge layout, themed (monochrome) launcher icon for Android 13+.
- Accessible touch targets and content descriptions; works on phones and tablets.

## Open & run

1. Install **Android Studio** (Ladybug or newer).
2. *File → Open…* and choose the `MITSParentApp` folder. Let Gradle sync (it downloads Gradle 8.13 and dependencies).
3. Pick an emulator or a connected phone and press **Run ▶**.

Sign in with **any mobile number (7+ digits) and any password of 4+ characters** — phase one uses sample data.

### Build an installable APK / Play Store bundle

- Test APK: *Build → Build App Bundle(s) / APK(s) → Build APK(s)* → `app/build/outputs/apk/debug/app-debug.apk`
- Release: *Build → Generate Signed App Bundle / APK…*, create a keystore (keep it safe — you need it for every update), choose **Android App Bundle** for Google Play.
- Store icon (512×512): `branding/playstore_icon_512.png`.

## Before release — checklist

- [ ] Put the real school phone, email and office hours in `SchoolConfig.kt` (currently placeholders).
- [ ] Connect the real backend (below) and remove the sample-data login rule.
- [ ] Confirm the academic-year dates in the calendar (sample assumes an August → June year).
- [ ] Add a privacy policy URL in the Play Console (required for apps used by families).

## Connecting the school's backend (phase two)

All data flows through one interface: `data/SchoolRepository.kt`

```kotlin
interface SchoolRepository {
    suspend fun login(mobile: String, password: String): Result<ParentSession>
    suspend fun dailyDiary(schoolClass: SchoolClass, date: LocalDate): DailyDiary
    suspend fun weeklyDiary(schoolClass: SchoolClass, weekStart: LocalDate): WeeklyDiary
    suspend fun yearlyCalendar(schoolClass: SchoolClass): List<SchoolEvent>
    suspend fun notifications(schoolClass: SchoolClass): List<SchoolNotification>
}
```

`MockSchoolRepository` implements it with realistic sample content. To go live, write e.g. `ApiSchoolRepository` (Retrofit/Ktor calling the school's server or a Firebase project) and swap it in `MitsApplication.kt`. No screen code needs to change.

Suggested phase-two additions: push notifications (Firebase Cloud Messaging) for new diaries and announcements, a teacher/admin web portal to publish diaries, multiple children per parent account, and OTP login.

## Project structure

```
app/src/main/java/com/mitsschools/parent/
├─ MainActivity.kt            Splash, edge-to-edge, theme
├─ MitsApplication.kt         Wires the repository (swap mock → API here)
├─ SchoolConfig.kt            School contact details
├─ data/
│  ├─ model/Models.kt         Classes, subjects, diary, events, notifications
│  ├─ SchoolRepository.kt     Data interface
│  ├─ MockSchoolRepository.kt Sample data for phase one
│  └─ SessionStore.kt         Login, chosen class, read/done state, theme
└─ ui/
   ├─ AppViewModel.kt         Shared app state
   ├─ navigation/             Routes + bottom navigation
   ├─ screens/                Login, Class select, Dashboard, Daily, Weekly, Calendar, Notifications, Profile
   ├─ components/             Shared cards, icons, date helpers
   └─ theme/                  MITS colours (from the logo), typography, light/dark
```

Tech: Kotlin 2.1, Jetpack Compose (BOM 2025.06), Material 3, Navigation Compose, AndroidX SplashScreen. `minSdk 26` (Android 8.0+), `targetSdk 36`.
