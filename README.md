# Namma-Shaale Inventory
### School Asset Management System for Government Schools

> A mobile-first Android app that digitizes asset tracking, health inspections, and maintenance reporting for government schools under the **Namma-Shaale** initiative — powered by AI insights.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android API](https://img.shields.io/badge/Min%20SDK-API%2028%20(Android%209.0)-brightgreen)](https://developer.android.com/about/versions/pie)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Repository-orange)](https://developer.android.com/topic/architecture)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## Table of Contents

- [Problem Statement](#problem-statement)
- [Solution Overview](#solution-overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Folder Structure](#folder-structure)
- [Screenshots](#screenshots)
- [Setup Instructions](#setup-instructions)
- [AI Features](#ai-features)
- [Security](#security)
- [Future Improvements](#future-improvements)
- [Contributing](#contributing)
- [License](#license)

---

## Problem Statement

Karnataka government schools manage hundreds of physical assets — classroom furniture, computers, lab equipment, sports gear, and library books. Under the **Namma-Shaale** initiative, School Development and Monitoring Committees (SDMC) are responsible for this inventory, but face critical challenges:

| Challenge | Impact |
|-----------|--------|
| Paper-based registers | No real-time visibility into asset condition |
| Manual reporting | Delayed maintenance decisions |
| No photographic evidence | Disputes over asset damage and accountability |
| No analytics | SDMC unable to make data-driven budget decisions |
| Siloed information | Teachers and administrators work from outdated records |

This results in assets deteriorating past repair, wasted government funds, and poor learning environments for students.

---

## Solution Overview

**Namma-Shaale Inventory** is an offline-first Android application that digitizes the complete school asset management lifecycle:

```
Register Asset → Monthly Health Check → Log Issues → Raise Repairs → Generate Reports
      ↑                                                                      ↓
      └──────────────────── AI-Powered Insights ←────────────────────────────┘
```

Every stakeholder — teachers, SDMC members, and school principals — gets a single source of truth for all school assets, accessible anytime from their Android device.

---

## Features

### Authentication
- Phone number-based login with simulated OTP verification
- SHA-256 password hashing with unique random salt per user
- Session persistence via Jetpack DataStore
- OTP expires after 5 minutes with 3 maximum resend attempts

### Asset Management
- Register assets with name, category, location, serial number, purchase date, and estimated cost
- Capture asset photos using the device camera (CameraX) or gallery
- View all assets with filter by condition status (Good / Fair / Needs Repair / Lost)
- Detailed asset view with complete modification history

### Health Checks
- Conduct monthly condition inspections across all registered assets
- Multi-asset batch health check workflow
- Log issues with photo evidence directly during a check
- Session summary screen after each inspection

### Repair Requests
- Raise repair requests with four priority levels: Low / Medium / High / Critical
- Track lifecycle: Pending → In Progress → Completed
- Badge count on bottom navigation shows pending items at a glance

### Reports & Export
- Auto-generated asset summary report with school details
- Asset status breakdown and category distribution
- Total estimated asset value calculation
- Export to PDF and share via any installed app

### AI-Powered Insights
- Maintenance recommendations based on current asset health data
- Powered by **Groq API** (Llama 3.1 8B Instant model)
- Analysis includes repair vs. replace recommendations and priority action items

### Profile Management
- Edit name, email, phone, school, and role
- Groq API key management with masked display and show/hide toggle
- API key stored encrypted via Android Keystore (AES-256-GCM)
- Test API key connectivity before saving

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Kotlin | 2.0.21 |
| UI Framework | Jetpack Compose | BOM 2024.09 |
| Design System | Material 3 | via Compose BOM |
| Navigation | Navigation Compose | 2.8.5 |
| Architecture | MVVM + Repository Pattern | — |
| Dependency Injection | Hilt (Dagger) | 2.59.2 |
| Local Database | Room (SQLite) | 2.7.1 |
| Annotation Processing | KSP | 2.0.21-1.0.28 |
| Session Storage | DataStore Preferences | 1.1.1 |
| Networking | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| AI Integration | Groq API (Llama 3.1 8B) | REST |
| Encryption | Android Keystore AES-256-GCM | Platform |
| Camera | CameraX + FileProvider | Platform |
| PDF Export | Android PdfDocument API | Platform |
| Image Loading | Coil | 2.7.0 |
| Splash Screen | Core SplashScreen | 1.0.1 |

---

## Architecture

The app follows **MVVM (Model-View-ViewModel)** with a **Repository pattern**, ensuring clean separation of concerns and testability.

### Layer Diagram

```
┌──────────────────────────────────────────────────────────┐
│                       UI Layer                            │
│                                                          │
│   Compose Screens  ←──  ViewModels  ←── StateFlow/State  │
│   (Stateless UI)        (UI Logic)      (Reactive State) │
└──────────────────────────┬───────────────────────────────┘
                           │  calls
┌──────────────────────────▼───────────────────────────────┐
│                   Repository Layer                        │
│                                                          │
│   AssetRepository  │  UserRepository  │  ReportRepository│
│   HealthRepository │  IssueRepository │  RepairRepository │
└──────┬─────────────────────────┬────────────────────────┘
       │                         │
┌──────▼──────────┐   ┌──────────▼────────────────────────┐
│   Local Data    │   │         Remote Data                │
│                 │   │                                    │
│  Room Database  │   │  Groq API (Retrofit)               │
│  (SQLite)       │   │  Groq AI Insights                  │
│                 │   │                                    │
│  DataStore      │   └────────────────────────────────────┘
│  (Session/Prefs)│
└─────────────────┘
```

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Offline-first** | School networks are unreliable; all core features work without internet |
| **Single Activity** | Compose Navigation manages all screen transitions within one activity |
| **StateFlow + collectAsStateWithLifecycle** | Lifecycle-aware state collection prevents memory leaks |
| **Hilt for DI** | Compile-time verified dependency graph, less boilerplate than manual DI |
| **KSP over KAPT** | Faster incremental builds for Room and Hilt annotation processing |
| **FileProvider for camera** | Secure file URI sharing between app and camera intent |

---

## Folder Structure

```
app/src/main/java/com/example/nammashalli/
│
├── data/
│   ├── local/
│   │   ├── dao/                    # Room Data Access Objects
│   │   │   ├── AssetDao.kt
│   │   │   ├── HealthCheckDao.kt
│   │   │   ├── IssueLogDao.kt
│   │   │   ├── RepairRequestDao.kt
│   │   │   └── UserDao.kt
│   │   ├── entities/               # Room database table definitions
│   │   │   ├── AssetEntity.kt
│   │   │   ├── HealthCheckEntity.kt
│   │   │   ├── IssueLogEntity.kt
│   │   │   ├── RepairRequestEntity.kt
│   │   │   └── UserEntity.kt
│   │   └── AppDatabase.kt          # Room database + migration setup
│   └── repository/                 # Repository implementations (data access abstraction)
│       ├── AssetRepository.kt
│       ├── HealthCheckRepository.kt
│       ├── IssueRepository.kt
│       ├── RepairRepository.kt
│       └── UserRepository.kt
│
├── di/                             # Hilt dependency injection modules
│   ├── DatabaseModule.kt           # Room + DAO bindings
│   └── NetworkModule.kt            # Retrofit + OkHttp + Groq bindings
│
├── network/                        # API service interfaces
│   └── GroqApiService.kt           # Groq LLM REST client
│
├── ui/
│   ├── assets/                     # Asset CRUD screens
│   │   ├── AssetDetailsScreen.kt
│   │   ├── AssetListScreen.kt
│   │   ├── AssetRegisterScreen.kt
│   │   └── AssetViewModel.kt
│   ├── auth/                       # Login / OTP screens
│   │   ├── AuthViewModel.kt
│   │   ├── OtpScreen.kt
│   │   ├── SignInScreen.kt
│   │   └── SignUpScreen.kt
│   ├── common/                     # Shared, reusable Compose components
│   │   ├── AssetStatus.kt          # Status enum with colors
│   │   └── CommonComponents.kt     # AppTopBar, MetricCard, EmptyState, etc.
│   ├── dashboard/                  # Home dashboard screen
│   │   ├── DashboardScreen.kt
│   │   └── DashboardViewModel.kt
│   ├── healthcheck/                # 3-screen health check flow
│   │   ├── HealthCheckScreen.kt
│   │   ├── HealthCheckSelectScreen.kt
│   │   ├── HealthCheckSummaryScreen.kt
│   │   └── HealthCheckViewModel.kt
│   ├── issues/                     # Issue logging and list
│   │   ├── IssueListScreen.kt
│   │   ├── IssueLogScreen.kt
│   │   └── IssueViewModel.kt
│   ├── navigation/                 # Compose NavGraph and route definitions
│   │   ├── MainViewModel.kt        # Cross-screen state (e.g., badge counts)
│   │   ├── NavGraph.kt             # Full nav graph with bottom bar
│   │   └── Screen.kt               # Route string definitions
│   ├── profile/                    # User profile and settings
│   │   ├── ProfileScreen.kt
│   │   └── ProfileViewModel.kt
│   ├── repairs/                    # Repair request management
│   │   ├── RepairRequestsScreen.kt
│   │   └── RepairViewModel.kt
│   ├── reports/                    # PDF reports + AI insights
│   │   ├── ReportScreen.kt
│   │   └── ReportViewModel.kt
│   └── theme/                      # Material 3 design tokens
│       ├── Color.kt                # Brand and status colors
│       ├── Theme.kt                # MaterialTheme setup
│       └── Type.kt                 # Typography scale
│
├── utils/                          # Standalone utility classes
│   ├── EncryptionUtil.kt           # Android Keystore AES-256-GCM
│   ├── ImageUtil.kt                # CameraX file creation + FileProvider URI
│   ├── OtpManager.kt               # OTP generation and time-based expiry
│   ├── PdfGenerator.kt             # PDF document creation with PdfDocument API
│   ├── SessionManager.kt           # DataStore-backed session persistence
│   └── ValidationUtil.kt           # Form input validation rules
│
├── MainActivity.kt                 # Single activity, Compose entry point
└── NammaShalliApplication.kt       # @HiltAndroidApp application class
```

---

## Screenshots

> Screenshots are captured from a development build on a Pixel 7 emulator (Android 14).

| Sign In | Dashboard | Asset List |
|:-------:|:---------:|:----------:|
| Phone OTP login with form validation | Overview of all assets with status metrics | Filterable asset list with status badges |

| Register Asset | Health Check | AI Report |
|:--------------:|:------------:|:---------:|
| Camera + gallery photo upload, dropdown selectors | Multi-asset batch inspection flow | PDF export + Groq AI maintenance insights |

---

## Setup Instructions

### Prerequisites

| Tool | Required Version |
|------|-----------------|
| Android Studio | Hedgehog (2023.1.1) or newer |
| JDK | 11 or higher |
| Android device / emulator | Android 9.0+ (API 28+) |
| Internet connection | Required only for AI insights feature |

### Step 1: Clone the Repository

```bash
git clone https://github.com/rohittsinghh/namma-shaale-inventory.git
cd namma-shaale-inventory
```

### Step 2: Open in Android Studio

1. Launch **Android Studio**
2. Click **File → Open**
3. Select the cloned project folder
4. Wait for Gradle sync to complete (first sync downloads dependencies — allow 2–5 minutes)

### Step 3: Build the Project

**Option A — Android Studio:**
- Press `Ctrl+F9` (Windows/Linux) or `Cmd+F9` (macOS), or click **Build → Make Project**

**Option B — Command line:**
```bash
# macOS / Linux
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Step 4: Run on a Device

1. Connect an Android phone (USB debugging enabled) or start an AVD emulator
2. In Android Studio, click the **Run** button or press `Shift+F10`
3. Select your device from the device list

### Step 5: Add Groq API Key (Optional)

To enable AI-powered maintenance insights:

1. Create a free account at [console.groq.com](https://console.groq.com)
2. Navigate to **API Keys** and generate a new key
3. Open the app → go to **Profile** tab
4. Scroll to **Groq API Key** section
5. Paste your key → tap **Test** to verify → tap **Update Key** to save

The key is encrypted with Android Keystore before storage and is never transmitted except to Groq's API endpoint.

---

## AI Features

The app uses **Groq API** with the `llama-3.1-8b-instant` model to analyze current school asset data and generate:

- Prioritized maintenance action plan
- Assets recommended for replacement vs. repair
- Estimated cost savings from proactive maintenance
- Monthly inspection checklist based on asset age and condition

**AI insight generation flow:**

```
User taps "Get AI Insights"
    → ReportViewModel collects current asset stats
    → Builds structured prompt with asset counts, repair backlog, condition data
    → Sends to Groq API via Retrofit
    → Streams response back to UI
    → Displayed in a formatted card on the Reports screen
```

---

## Security

| Feature | Implementation |
|---------|---------------|
| Password hashing | SHA-256 with per-user random 16-byte salt |
| API key storage | AES-256-GCM via Android Keystore hardware-backed store |
| OTP validity | 5-minute expiry, 3 resend attempts maximum |
| Camera file sharing | FileProvider — no public external storage access |
| Database | Local-only Room/SQLite, no cloud sync |
| Network | HTTPS-only via OkHttp (Groq API) |

---

## Future Improvements

| Feature | Description | Priority |
|---------|-------------|----------|
| Gemini AI Integration | Google Gemini for multimodal asset photo analysis | High |
| QR Code Scanning | Scan printed asset tags for instant lookup | High |
| Push Notifications | Alerts for overdue health checks and repairs | Medium |
| Offline Sync | Background data sync when connectivity is restored | Medium |
| Multi-School Support | SDMC members managing multiple campuses | Medium |
| Excel Export | Spreadsheet format for district-level reporting | Low |
| Dark Mode | System-adaptive dark theme support | Low |
| ML Kit Damage Detection | Auto-detect damage severity from asset photos | Low |

---

## Contributing

Contributions, bug reports, and feature requests are welcome!

Please read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting a pull request.

**Quick start for contributors:**

```bash
# 1. Fork and clone
git clone https://github.com/rohittsinghh/namma-shaale-inventory.git

# 2. Create a feature branch
git checkout -b feature/your-feature-name

# 3. Make changes and commit
git commit -m "feat: add your feature description"

# 4. Push and open a PR
git push origin feature/your-feature-name
```

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for full details.

```
MIT License — Copyright (c) 2024 Namma-Shaale Inventory Contributors
```

---

<div align="center">

Built for Karnataka Government Schools under the **Namma-Shaale** initiative.

Empowering teachers and SDMC members to protect every school asset.

</div>
