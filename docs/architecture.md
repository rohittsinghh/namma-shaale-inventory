# Architecture Guide — Namma-Shaale Inventory

This document describes the architectural decisions, patterns, and data flow used in the Namma-Shaale Inventory Android application.

---

## Overview

The app follows **MVVM (Model-View-ViewModel)** architecture with a **Repository pattern** for data access, consistent with the [official Android architecture recommendations](https://developer.android.com/topic/architecture).

### Architectural Principles

1. **Separation of concerns** — each layer has a single, well-defined responsibility
2. **Unidirectional data flow** — state flows down from ViewModel to UI; events flow up from UI to ViewModel
3. **Offline-first** — all core functionality works without internet access
4. **Testability** — repositories and ViewModels can be unit tested independently

---

## Layer Structure

```
┌──────────────────────────────────────────────────────────┐
│                     Presentation Layer                    │
│                                                          │
│   @Composable Screens                                    │
│        │ observes StateFlow                              │
│        ▼                                                 │
│   ViewModels (HiltViewModel)                             │
│        │ updates state via MutableStateFlow              │
│        │ calls suspend functions                         │
└──────────────────┬───────────────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────────────┐
│                     Domain / Repository Layer             │
│                                                          │
│   AssetRepository       UserRepository                   │
│   HealthCheckRepository  RepairRepository                 │
│   IssueRepository        (+ ReportRepository)            │
│                                                          │
│   Each repository decides: Room DB or Retrofit API       │
└────────┬────────────────────────┬────────────────────────┘
         │                        │
┌────────▼──────────┐   ┌─────────▼──────────────────────┐
│    Local Data      │   │         Remote Data             │
│                   │   │                                 │
│   Room Database   │   │   Groq API (Retrofit)           │
│   (5 entities)    │   │   llama-3.1-8b-instant          │
│                   │   │                                 │
│   DataStore       │   └─────────────────────────────────┘
│   (session)       │
└───────────────────┘
```

---

## Presentation Layer

### Compose Screens

Each screen is a `@Composable` function that:
- Accepts only callbacks and the ViewModel as parameters
- Collects state via `collectAsStateWithLifecycle()`
- Sends user events to the ViewModel via function calls
- Contains **no business logic**

```kotlin
@Composable
fun AssetListScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    viewModel: AssetViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()
    // UI renders state; user actions call viewModel.xxx()
}
```

### ViewModels

Each feature module has one ViewModel that:
- Exposes a single `StateFlow<UiState>` sealed class or data class
- Handles all UI-related logic and state transformations
- Delegates data operations to repositories via `viewModelScope`
- Never holds Context references directly

```kotlin
@HiltViewModel
class AssetViewModel @Inject constructor(
    private val assetRepository: AssetRepository
) : ViewModel() {
    private val _listState = MutableStateFlow(AssetListState())
    val listState: StateFlow<AssetListState> = _listState.asStateFlow()
}
```

### Navigation

All navigation is handled by **Navigation Compose** with a single `NavGraph` composable. Route definitions live in `Screen.kt` as sealed classes or objects to prevent typo-related runtime crashes.

The `Scaffold` with `NavigationBar` wraps the `NavHost`, with the bottom bar only visible on the 5 root tab routes.

---

## Repository Layer

Repositories are the **single source of truth** for each data domain. They:

- Abstract the data source (Room, Retrofit, DataStore) from the ViewModel
- Return `Flow<T>` for reactive streams or `suspend fun` for one-shot operations
- Handle caching decisions (local DB is always the primary source)

```kotlin
class AssetRepository @Inject constructor(
    private val assetDao: AssetDao,
    private val sessionManager: SessionManager
) {
    fun getAssets(schoolId: Long): Flow<List<AssetEntity>> =
        assetDao.getAllBySchool(schoolId)

    suspend fun insertAsset(asset: AssetEntity): Long =
        assetDao.insert(asset)
}
```

---

## Data Layer

### Room Database

Five entities with corresponding DAOs:

| Entity | Description |
|--------|-------------|
| `UserEntity` | Registered users with hashed password and salt |
| `AssetEntity` | School assets with status, category, location, photo path |
| `HealthCheckEntity` | Inspection records linked to assets |
| `IssueLogEntity` | Issue reports with photo evidence |
| `RepairRequestEntity` | Repair tickets with priority and status |

All entities use `autoGenerate = true` primary keys. Relations use foreign key `Long` references without Room `@ForeignKey` constraints (to simplify schema migrations).

### DataStore Preferences

`SessionManager` wraps DataStore to persist:
- Logged-in user ID
- School name
- User role

DataStore is preferred over SharedPreferences for its coroutine-native API and type safety.

### Groq API (Retrofit)

`GroqApiService` is a Retrofit interface pointing to `https://api.groq.com/`. The API key is injected at runtime from `EncryptionUtil` (Android Keystore) — never hardcoded or stored in `local.properties`.

---

## Dependency Injection (Hilt)

Two Hilt modules wire the entire dependency graph:

| Module | Provides |
|--------|---------|
| `DatabaseModule` | `AppDatabase`, all 5 DAOs, all 5 repositories |
| `NetworkModule` | OkHttpClient, Retrofit, `GroqApiService`, `SessionManager`, `EncryptionUtil` |

All ViewModels are annotated with `@HiltViewModel` and injected automatically by `hiltViewModel()` in Compose.

---

## Security Architecture

```
User enters API key
     │
     ▼
EncryptionUtil.encrypt()
     │ generates random IV
     ▼
Android Keystore (AES-256-GCM)
     │ produces ciphertext + IV
     ▼
Stored in DataStore as Base64 strings
     │
     ▼  (on next API call)
EncryptionUtil.decrypt()
     │ retrieves key from Keystore
     ▼
Plaintext key passed to OkHttp Authorization header
```

Password hashing uses SHA-256 with a 16-byte random salt generated per user at registration. The salt is stored alongside the hash in `UserEntity`.

---

## State Management Pattern

Each screen's state is modeled as a Kotlin `data class`:

```kotlin
data class AssetListState(
    val isLoading: Boolean = false,
    val assets: List<AssetEntity> = emptyList(),
    val selectedFilter: String = "All",
    val error: String? = null
)
```

This pattern:
- Makes state changes predictable (only one StateFlow to observe)
- Enables easy debugging (state is fully serializable)
- Simplifies testing (assert on state data class fields)
- Prevents partial state updates causing inconsistent UI

---

## Build Configuration

| Config | Value |
|--------|-------|
| `compileSdk` | 35 |
| `minSdk` | 28 (Android 9.0) |
| `targetSdk` | 35 |
| `kotlinJvmTarget` | 11 |
| Annotation processor | KSP (faster than KAPT) |
| Compose Compiler | Kotlin plugin (no separate version needed with Kotlin 2.x) |
