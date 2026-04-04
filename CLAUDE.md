# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**app** is the Android mobile client for the home-anthill IoT platform. It allows users to authenticate via GitHub OAuth2, manage homes/rooms/devices, view sensor readings, and receive push notifications for device offline events via Firebase Cloud Messaging (FCM). Written in Kotlin with Jetpack Compose.

## Build & Development Commands

```bash
./gradlew assembleDebug                          # Build debug APK
./gradlew assembleStaging                        # Build staging APK (debuggable, -staging suffix)
./gradlew assembleRelease                        # Build release APK (minified, shrunk)
./gradlew test                                   # Run all unit tests
./gradlew testDebugUnitTest --tests "eu.homeanthill.ExampleUnitTest"  # Run single unit test class
./gradlew testDebugUnitTest -q --tests "ExampleUnitTest"             # Single test, quiet output
./gradlew connectedAndroidTest                   # Run instrumented tests on device/emulator
./gradlew lintDebug                              # Run Android Lint analysis
```

**Quick start**: Copy `secrets.defaults.properties` → `secrets.properties` and `google-services.json_template` → `google-services.json`, then `./gradlew assembleDebug`.

## File Organization

```
app/src/main/java/eu/homeanthill/
├── api/
│   ├── requests/           # Retrofit service interfaces
│   ├── model/              # Data classes (@Parcelize for navigation)
│   ├── AuthInterceptor.kt
│   ├── AppAuthenticator.kt
│   ├── SendSavedCookieInterceptor.kt
│   └── SendRefreshTokenCookieInterceptor.kt
├── repository/             # Data repositories (single source of truth)
├── ui/
│   ├── screens/
│   │   ├── main/           # MainScreen + MainViewModel
│   │   ├── homes/          # Home, room, device hierarchy
│   │   ├── devices/        # Device list, edit, feature values
│   │   ├── profile/        # User profile
│   ├── components/         # Reusable composables (CircleAsyncImage, MaterialSpinner, etc.)
│   ├── navigation/         # NavHost, Destinations, Drawer
│   └── theme/
├── di/KoinModules.kt       # All DI module registration
├── MainActivity.kt
└── LoginActivity.kt
```

## Architecture

Follows MVVM with Koin for dependency injection. All modules registered in `di/KoinModules.kt`: `viewModelModule`, `repositoryModule`, `apiModule`, `retrofitModule`.

### Data Flow

```
Composable Screen → koinViewModel<XViewModel>()
    ↓ (via repo.repoXxx() suspend functions)
XViewModel updates StateFlow<UiState> (sealed class with Loading/Idle/Error)
    ↓
XRepository calls Retrofit XServices interface
    ↓
Main OkHttpClient chain: LoggingInterceptor → SendSavedCookiesInterceptor → AuthInterceptor → AppAuthenticator
    ↓ (on 401: AppAuthenticator calls RefreshTokenRepository using the refresh OkHttpClient)
Refresh OkHttpClient chain: LoggingInterceptor → SendRefreshTokenCookieInterceptor → POST /api/token/refresh
    ↓
api-server REST endpoints
```

### State Management Pattern

Each ViewModel uses a sealed `UiState` class with three states:
- `Loading` — initial/refresh state
- `Idle(data)` — success with data
- `Error(message)` — exception occurred

Example from `DevicesListViewModel`:
```kotlin
sealed class DevicesUiState {
  data class Idle(val deviceList: MyDevicesList?) : DevicesUiState()
  data object Loading : DevicesUiState()
  data class Error(val errorMessage: String) : DevicesUiState()
}
```

Screens collect via `collectAsStateWithLifecycle()` to avoid collecting when in background.

### OkHttp Client Chain

Two separate OkHttp clients are registered in Koin:

**Main client** (default, no qualifier):
```
LoggingInterceptor → SendSavedCookiesInterceptor → AuthInterceptor → AppAuthenticator
```
Used by all standard API services (homes, devices, profile, etc.).

**Refresh client** (`named("refresh")`):
```
LoggingInterceptor → SendRefreshTokenCookieInterceptor
```
Used exclusively by `RefreshTokenServices` / `RefreshTokenRepository`. Has no `AppAuthenticator` (prevents infinite recursion) and no `AuthInterceptor` (refresh endpoint does not require a Bearer token).

### Repository Pattern

Repositories wrap Retrofit service calls in `suspend` functions. They:
1. Call the service
2. Check `isSuccessful` on the `Response<T>`
3. Return `body()!!` on success or throw `IOException` on failure

Error handling is minimal — exceptions bubble to the ViewModel where they're caught and mapped to `Error(message)` state.

### Authentication Flow

- `LoginActivity` handles GitHub OAuth2; stores JWT token and refresh token via SharedPreferences
- `AuthInterceptor` attaches JWT as Bearer token on every request
- `AppAuthenticator` intercepts 401 responses: attempts silent token refresh first; only logs out and redirects to `LoginActivity` if the refresh also fails
- `SendSavedCookiesInterceptor` adds the `mysession` session cookie to every request
- `SendRefreshTokenCookieInterceptor` adds the `refresh_token` cookie **only** to requests targeting `token/refresh`

#### Refresh Token Flow (mobile)

The api-server sets `refresh_token` as an `HttpOnly` cookie in the `/api/app_callback` response, but Android's Intent system (which handles the deep link) does not expose `Set-Cookie` headers to the app. To work around this, the server also includes the raw refresh token value as a `refresh_token` query parameter in the deep link URL. `LoginActivity.onNewIntent` reads this value and stores it in SharedPreferences under `refreshTokenKey`.

When `AppAuthenticator` receives a 401:
1. It calls `RefreshTokenRepository.repoRefreshToken()` synchronously (using OkHttp `execute()`)
2. `RefreshTokenRepository` uses a **dedicated OkHttp client** (no `AppAuthenticator`, no `AuthInterceptor`) with `SendRefreshTokenCookieInterceptor` to `POST /api/token/refresh`
3. On success: stores the new JWT via `LoginRepository.setJWT()`, reads the rotated `refresh_token` from the `Set-Cookie` response header and stores it via `LoginRepository.setRefreshToken()`, then retries the original request
4. On failure (refresh 401 or network error): calls `LoginRepository.logoutAndRedirect()`

The dedicated OkHttp/Retrofit pair is registered in Koin under the `named("refresh")` qualifier to keep it separate from the main client.

#### Credential storage (SharedPreferences keys in `PreferencesKeys.kt`)

| Key | Value stored |
|-----|-------------|
| `jwtKey` | Access JWT |
| `refreshTokenKey` | Refresh token value |
| `cookieKey` | `mysession` cookie value |
| `loginTimestampKey` | Unix timestamp of last login |
| `fcmTokenKey` | Firebase Cloud Messaging token |
| `profileKey` | Serialised `Profile` JSON |

### Navigation

`MainActivity` sets up `ModalNavigationDrawer` with `NavHost`. Top-level destinations in `ui/navigation/Destinations.kt`:
- `Home` → `MainScreen` / `MainViewModel` (dashboard; triggers FCM token registration)
- `Profile` → `ProfileScreen` / `ProfileViewModel`
- `Homes` → `HomesScreen` (nested: homes list → rooms list)
- `Devices` → `DevicesScreen` (nested: devices list → edit device → feature values)

Navigation passes objects via `savedStateHandle`. All model classes (`Device`, `Home`, `Room`) are `@Parcelize` data classes for safe serialization.

### Device Feature Values

Devices have typed features. `FeaturesScreen` reads device from `savedStateHandle` and conditionally renders based on feature type:
- `sensor` + name `online` → `OnlineFeatureValues` (from `OnlineRepository`)
- `sensor` (other) → `SensorFeatureValues` (read-only sensor readings)
- `controller` → `ControllerFeatureValues` (read/write device state via `DevicesRepository`)

Each feature value composable:
- Gets its own ViewModel via `koinViewModel<T>()`
- Receives `refreshTrigger: Int` from parent to drive pull-to-refresh
- Uses its own StateFlow for UI state

### Error Handling

- Repositories throw `IOException` on API failure
- ViewModels catch exceptions in coroutines and emit `Error(message)` state
- Screens display error messages via Material UI snackbars or error cards

## Tech Stack

- **Kotlin** (Android), Jetpack Compose, Material3
- **Koin** for DI (`koin-core`, `koin-android`, `koin-androidx-compose`)
- **Retrofit** + Gson + OkHttp; `API_BASE_URL` injected via `secrets.properties` → `BuildConfig`
- **Firebase BOM** — FCM (push notifications), Firestore, Analytics
- **Coil** (image loading with OkHttp backend)
- **Accompanist** (runtime permissions)
- **Coroutines** (`viewModelScope` for VM scope, `suspend` for async)
- Min SDK: 33 (Android 13), Target/Compile SDK: 36, JVM target: 11

## Build Variants

| Variant | Debuggable | Minified | Version Suffix |
|---------|-----------|---------|------|
| debug   | Yes       | No      | `-debug` |
| staging | Yes       | No      | `-staging` |
| release | No        | Yes     | none (TODO: real signing) |

Use `staging` for testing real backend without minification (easier debugging). Release signing is TODO — currently uses debug keystore.

## Configuration Files

- `secrets.properties` (gitignored) — API keys, OAuth IDs. Copy from `secrets.defaults.properties`
- `google-services.json` (gitignored) — Firebase config. Copy from `_template`
- `staging.properties` / `release.properties` — environment-specific overrides (injected via build variants)
- `local.properties` — SDK path (auto-generated)
