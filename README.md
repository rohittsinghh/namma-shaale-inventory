# Namma-Shaale — School Asset Inventory

A lightweight Android app for school teachers and SDMC members to track, manage, and maintain school assets. Built for Karnataka government schools under the **Namma-Shaale** initiative.

---

## Features

### Authentication
- Phone number based login with OTP verification
- Secure password hashing (SHA-256 with salt)
- Session persistence via DataStore

### Asset Management
- Register assets with name, category, location, purchase date, and estimated cost
- Capture asset photos using the camera
- View and filter assets by status (Good, Fair, Needs Repair, Lost)
- Asset detail view with full history

### Health Checks
- Monthly asset condition inspections
- Multi-asset batch health check flow
- Issue logging during checks (with photo evidence)
- Summary screen after each check session

### Repair Requests
- Raise repair requests with priority (Low, Medium, High, Critical)
- Track request status (Pending, In Progress, Completed)
- View all open and resolved repairs

### Reports
- Auto-generated asset summary report
- PDF export with school name, asset counts, and repair details
- Share PDF via any installed app
- AI-powered maintenance recommendations via Groq API

### Profile
- Edit personal info (name, email, phone, role, school)
- Groq API key management with masked display and show/hide toggle
- API key encrypted with Android Keystore (AES-256-GCM)
- Test API key validity before saving
- Theme and notification preferences

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation with Bottom Nav Bar |
| Architecture | MVVM + Repository pattern |
| DI | Hilt (Dagger) |
| Local DB | Room (SQLite) with KSP |
| Session | DataStore Preferences |
| Networking | Retrofit + OkHttp |
| AI | Groq API (`llama-3.1-8b-instant`) |
| Encryption | Android Keystore AES-256-GCM |
| PDF | Android PdfDocument API |
| Image | CameraX + FileProvider |

---

## Screenshots

> Coming soon

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26+
- A physical or virtual device running Android 8.0+

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/rohittsinghh/namma-shaale-inventory.git
   cd namma-shaale-inventory
   ```

2. **Open in Android Studio**
   - File → Open → select the project folder
   - Let Gradle sync complete

3. **Build and run**
   - Connect a device or start an emulator
   - Click **Run** or press `Shift+F10`

### Groq API Key (optional)
To enable AI maintenance insights:
1. Get a free API key at [console.groq.com](https://console.groq.com)
2. Enter it in the app under **Profile → Groq API Key**
3. Tap **Test** to verify, then **Update Key** to save

The key is encrypted with the Android Keystore and never stored in plain text.

---

## Project Structure

```
app/src/main/java/com/example/nammashalli/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs
│   │   └── entities/     # Room entities
│   └── repository/       # Repository layer
├── di/                   # Hilt modules
├── network/              # Retrofit + Groq API service
├── ui/
│   ├── auth/             # Sign up, sign in, OTP screens
│   ├── assets/           # Asset list, register, details
│   ├── dashboard/        # Home dashboard
│   ├── healthcheck/      # Health check flow (3 screens)
│   ├── issues/           # Issue log screens
│   ├── navigation/       # NavGraph, Screen routes, MainViewModel
│   ├── profile/          # Profile screen + ViewModel
│   ├── repairs/          # Repair requests screen
│   ├── reports/          # Report screen + PDF + AI
│   └── theme/            # Material 3 theme
└── utils/                # Encryption, session, validation, OTP, PDF
```

---

## Security

- Passwords are never stored in plain text — SHA-256 with random salt
- Groq API keys are encrypted at rest using Android Keystore (AES-256-GCM)
- OTP expires after 5 minutes with a maximum of 3 resend attempts
- FileProvider used for secure camera and PDF file sharing

---

## License

This project is licensed under the [MIT License](LICENSE).

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
