# Project Overview — Namma-Shaale Inventory

## What is Namma-Shaale?

**Namma-Shaale** (meaning "Our School" in Kannada) is a Karnataka government initiative to improve the quality and management of government-run schools across the state. Under this initiative, School Development and Monitoring Committees (SDMC) — comprising parents, teachers, and local representatives — are responsible for the overall administration and welfare of their schools.

One critical but largely neglected area is **physical asset management**: tracking furniture, electronics, sports equipment, lab instruments, and library books — ensuring they are maintained, repaired, and replaced on time.

---

## The Problem

Current asset management in government schools suffers from:

- **Paper-based registers** that are lost, damaged, or outdated
- **No photo evidence** of asset condition, leading to disputes
- **Delayed maintenance** because there is no proactive inspection system
- **No reporting infrastructure** for SDMC meetings or government audits
- **Zero data analytics** — no way to identify patterns or prioritize spending

The result: classrooms with broken furniture, malfunctioning computers, and unusable lab equipment — all directly impacting student learning quality.

---

## Our Solution

**Namma-Shaale Inventory** is a purpose-built Android application for school stakeholders. It provides:

### For Teachers
- Quick asset lookup and status check
- Log issues with photo evidence during normal school hours
- Receive notifications when repair requests are updated

### For SDMC Members
- Complete asset inventory at a glance
- Monthly health check workflows to inspect all assets systematically
- Generate PDF reports for SDMC meetings and government submissions
- AI-powered recommendations for maintenance prioritization

### For School Principals
- Dashboard overview of all asset health metrics
- Repair request tracking from issue to resolution
- Historical data on asset lifecycle and costs

---

## Design Philosophy

### Offline-First
School internet connectivity in rural Karnataka is unreliable. Every core feature — asset registration, health checks, issue logging, repair requests — works entirely offline. Data is stored locally in Room/SQLite and only the AI insights feature requires connectivity.

### Minimal Onboarding
The app uses phone number + OTP authentication to avoid dependency on email accounts. Any teacher with an Android phone can start using it in under 2 minutes.

### Purposeful AI
AI is not a gimmick here. The Groq API integration provides actionable, school-specific maintenance recommendations — not generic summaries. The AI prompt is built from real asset data: counts, conditions, repair backlog, and open issues.

### Security Without Friction
- Passwords use SHA-256 + salt (secure, but no extra user steps)
- API keys are encrypted transparently with Android Keystore
- No sensitive data leaves the device except to authorised API endpoints

---

## Technology Justification

| Choice | Why |
|--------|-----|
| **Kotlin** | Modern, concise, null-safe — the standard for new Android development |
| **Jetpack Compose** | Declarative UI reduces boilerplate 40–60% vs XML Views; easier to maintain |
| **MVVM + Repository** | Google's recommended Android architecture; separates UI from data concerns |
| **Room over SQLite direct** | Type-safe queries, compile-time verification, built-in Flow support |
| **Hilt over manual DI** | Compile-time dependency graph verification eliminates runtime DI errors |
| **KSP over KAPT** | 2–3x faster incremental build times for annotation processing |
| **Groq API** | Free tier with fast inference; llama-3.1-8b-instant is accurate for structured analysis |
| **Android Keystore** | Hardware-backed key storage — the most secure option available on Android |
| **DataStore over SharedPreferences** | Coroutine-native, no ANR risk from synchronous disk reads |

---

## Feature Matrix

| Feature | Authentication | Asset Management | Health Checks | Repairs | Issues | Reports |
|---------|:---:|:---:|:---:|:---:|:---:|:---:|
| Create | Phone+OTP | ✅ | ✅ | ✅ | ✅ | Auto-generated |
| Read | Session check | ✅ | ✅ | ✅ | ✅ | ✅ |
| Update | — | Status only | — | Status | — | Refresh |
| Delete | Logout | — | — | — | — | — |
| Filter | — | By status | — | By status | By asset | By category |
| Photo | — | ✅ Camera + Gallery | ✅ Camera | — | ✅ Camera | — |
| Export | — | — | — | — | — | ✅ PDF + Share |
| AI Analysis | — | — | — | — | — | ✅ Groq API |

---

## Screens Inventory

| Screen | Route | Description |
|--------|-------|-------------|
| Sign Up | `signup` | Register with name, phone, school, role, password |
| Sign In | `signin` | Phone number input → OTP flow |
| OTP Verify | `otp/{phone}/{otp}` | Enter 6-digit OTP with countdown timer |
| Dashboard | `dashboard` | Asset metrics, quick actions, alerts |
| Asset List | `assets` | All assets with filter tabs and search |
| Asset Register | `assets/register` | Multi-field form with camera upload |
| Asset Details | `assets/{id}` | Full asset info + health check history |
| Health Check Select | `healthcheck/select` | Choose assets for inspection batch |
| Health Check | `healthcheck` | Step through each asset — update condition |
| Health Check Summary | `healthcheck/summary` | Review results before finalising |
| Issue Log | `issues/log` | Report a new issue with photo |
| Issue List | `issues` | View all open and resolved issues |
| Repair Requests | `repairs` | All repair tickets with status |
| Reports | `reports` | Summary metrics, PDF export, AI insights |
| Profile | `profile` | Personal info, API key, preferences |

---

## Data Model

```
User (1) ──── (N) Asset
Asset (1) ──── (N) HealthCheck
Asset (1) ──── (N) IssueLog
Asset (1) ──── (N) RepairRequest
```

All records are scoped to a `schoolId` which is the logged-in user's associated school. Multi-school support (where one user manages multiple schools) is planned for a future release.
