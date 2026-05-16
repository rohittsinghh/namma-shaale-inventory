# Contributing to Namma-Shaale Inventory

Thank you for your interest in contributing to Namma-Shaale Inventory! This document explains how to set up the project for development, our coding standards, and the pull request process.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Coding Standards](#coding-standards)
- [Branching Strategy](#branching-strategy)
- [Commit Message Format](#commit-message-format)
- [Pull Request Process](#pull-request-process)
- [Reporting Bugs](#reporting-bugs)
- [Feature Requests](#feature-requests)

---

## Code of Conduct

By participating in this project, you agree to maintain a respectful, inclusive, and welcoming environment for all contributors.

---

## Getting Started

1. **Fork** the repository on GitHub
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/<your-username>/namma-shaale-inventory.git
   cd namma-shaale-inventory
   ```
3. **Set upstream** to track the original repo:
   ```bash
   git remote add upstream https://github.com/rohittsinghh/namma-shaale-inventory.git
   ```

---

## Development Setup

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11 or higher
- Android emulator or device running API 28+

### Build the Project
```bash
./gradlew assembleDebug
```

### Run Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

### Sync Dependencies
After pulling changes, always sync Gradle:
- In Android Studio: **File → Sync Project with Gradle Files**
- CLI: `./gradlew dependencies`

---

## Project Structure

The app uses **MVVM architecture** with a **Repository pattern**. Before adding features, familiarise yourself with the layer separation:

```
ui/          → Compose screens and ViewModels (UI state only)
data/        → Room entities, DAOs, and Repositories
di/          → Hilt dependency injection modules
network/     → Retrofit API service interfaces
utils/       → Standalone utilities (no Android framework dependencies where possible)
```

---

## Coding Standards

### Kotlin Style
- Follow the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use `val` over `var` wherever possible
- Prefer extension functions over utility classes
- Name Compose functions in `PascalCase` with no suffix (e.g., `AssetCard`, not `AssetCardComposable`)

### Jetpack Compose
- Keep `@Composable` functions **stateless** where possible — hoist state to ViewModels
- Use `collectAsStateWithLifecycle()` instead of `collectAsState()` for lifecycle awareness
- Extract repeated UI patterns into reusable composables in `ui/common/`
- Use `key()` in `LazyColumn`/`LazyRow` items to improve recomposition efficiency

### ViewModel
- Expose state as a single `StateFlow<UiState>` data class per feature
- Never expose mutable state directly — use `private val _state = MutableStateFlow(...)`
- Use `viewModelScope.launch` for coroutines; handle exceptions with `try/catch`

### Repository
- Repositories return `Flow<T>` for live data or `suspend fun` for one-shot operations
- Keep business logic in the repository, not in ViewModels or DAOs
- DAOs should contain only SQL operations — no business logic

---

## Branching Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Stable, release-ready code |
| `develop` | Integration branch for features |
| `feature/<name>` | New features (branch from `develop`) |
| `fix/<name>` | Bug fixes |
| `docs/<name>` | Documentation changes only |

Always branch from `develop` for new features:
```bash
git checkout develop
git pull upstream develop
git checkout -b feature/your-feature-name
```

---

## Commit Message Format

Use [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(<scope>): <short description>

[optional body]

[optional footer]
```

**Types:**

| Type | When to use |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Formatting, no logic change |
| `refactor` | Code restructuring, no behavior change |
| `test` | Adding or updating tests |
| `chore` | Build scripts, dependencies |

**Examples:**
```bash
feat(assets): add QR code scanning for asset lookup
fix(auth): prevent OTP resend before 60-second cooldown expires
docs(readme): add architecture diagram and setup steps
refactor(reports): extract AI insights section into separate composable
```

---

## Pull Request Process

1. **Update your fork** before submitting:
   ```bash
   git fetch upstream
   git rebase upstream/develop
   ```

2. **Run all checks** locally:
   ```bash
   ./gradlew test
   ./gradlew lint
   ```

3. **Create the PR** against the `develop` branch (not `main`)

4. **Fill out the PR template** with:
   - What changed and why
   - Screenshots for UI changes
   - Testing steps

5. **Respond to review feedback** promptly

6. PRs are merged by maintainers after at least one approval and passing CI checks

---

## Reporting Bugs

When reporting a bug, include:

1. **Device and Android version** (e.g., Pixel 7, Android 14)
2. **App version** (from About screen or build config)
3. **Steps to reproduce** — be specific
4. **Expected vs. actual behavior**
5. **Logcat output** if available (filter by `NammaShalli`)
6. **Screenshots or screen recordings** if the issue is visual

---

## Feature Requests

Open a GitHub Issue with:
- A clear description of the problem you're trying to solve
- Why this feature would benefit school asset managers
- Any relevant mockups or wireframes

Features are prioritized based on alignment with the Namma-Shaale initiative's goals and user impact.

---

Thank you for helping improve school asset management across Karnataka!
