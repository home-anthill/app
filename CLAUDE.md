# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**app** is the Android mobile client for the home-anthill IoT platform. It allows users to authenticate via GitHub OAuth2, manage homes/rooms/devices, view sensor readings, and receive push notifications for device offline events via Firebase Cloud Messaging (FCM). Written in Kotlin with Jetpack Compose.

## Build & Development Commands

```bash
./gradlew assembleDebug                          # Build debug APK
./gradlew assembleStaging                        # Build staging APK (debuggable, -staging suffix)
./gradlew assembleRelease                        # Build release APK (minified, shrunk)
./gradlew testDebugUnitTest                      # Run all unit tests
./gradlew testDebugUnitTest --tests "eu.homeanthill.repository.HomesRepositoryTest"  # Run single test class
./gradlew testDebugUnitTest -q --tests "HomesRepositoryTest"                         # Single test, quiet output
./gradlew connectedAndroidTest                   # Run instrumented tests on device/emulator
./gradlew lintDebug                              # Run Android Lint analysis
```

**Quick start**: Copy `secrets.defaults.properties` → `secrets.properties` and `google-services.json_template` → `google-services.json`, then `./gradlew assembleDebug`.

**Agent test requirement**: Before running any Gradle build/test/lint command, set `JAVA_HOME` to Android Studio's bundled JBR:

```bash
JAVA_HOME="/Users/ks89/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest
```

The shell default Java is Corretto 11, which is too old for the Gradle wrapper. The JetBrains Toolbox runtime under `/Applications/JetBrains Toolbox.app/Contents/jre/Contents/Home` is also unsuitable because it is missing `java.instrument`; use the Android Studio JBR path above.

## File Organization

```
app/src/main/java/eu/homeanthill/
├── api/
│   ├── requests/           # Retrofit service interfaces
│   ├── model/              # Data classes (@Parcelize for navigation)
│   ├── AuthInterceptor.kt
│   ├── AppAuthenticator.kt
│   └── SendSavedCookieInterceptor.kt
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
├── SecurePrefs.kt          # EncryptedSharedPreferences extension (securePrefs())
├── PreferencesKeys.kt      # SharedPreferences key constants
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
Refresh OkHttpClient chain: LoggingInterceptor → POST /api/oauth/app/refresh with JSON refreshToken
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
LoggingInterceptor
```
Used exclusively by `RefreshTokenServices` / `RefreshTokenRepository`. Has no `AppAuthenticator` (prevents infinite recursion), no `AuthInterceptor` (refresh endpoint does not require a Bearer token), and no refresh-token cookie interceptor because mobile refresh tokens are sent in the JSON request body.

**Public client** (`named("public")`):
```
LoggingInterceptor
```
Used by unauthenticated pre-login flows such as `/api/oauth/app/exchange-code`.

`HttpLoggingInterceptor` is set to `Level.HEADERS` in debug/staging builds and `Level.NONE` in release builds to prevent credentials from appearing in Logcat on production devices.

### Repository Pattern

Repositories wrap Retrofit service calls in `suspend` functions. They:
1. Call the service
2. Check `isSuccessful` on the `Response<T>`
3. Return `body()!!` on success or throw `IOException` on failure

Exceptions bubble to the ViewModel where they're caught and mapped to `Error(message)` state. All `LoginRepository` instances are registered as Koin `single` (not `factory`).

### Authentication Flow

- `LoginActivity` handles GitHub OAuth2; stores JWT token and refresh token via `EncryptedSharedPreferences`
- `AuthInterceptor` attaches JWT as Bearer token on every request
- `AppAuthenticator` intercepts 401 responses: attempts silent token refresh first; only logs out and redirects to `LoginActivity` if the refresh also fails
- `SendSavedCookiesInterceptor` adds the `oauth_session` session cookie to every request
- Mobile refresh and logout send the stored refresh token as JSON to `/api/oauth/app/refresh` and `/api/oauth/app/logout`; they do not use the web refresh-token cookie endpoint.

#### First-Install Deep-Link Handling

Two edge cases apply only on a fresh install (process has never run before):

1. **`onCreate()` must handle `intent.data`**: With `launchMode="singleTask"`, a returning deep link normally triggers `onNewIntent()`. But if the process was killed while the user was in the browser (Android kills background processes under memory pressure), the OS recreates `LoginActivity` and the callback URL arrives in `onCreate()` via `intent.data`, not `onNewIntent()`. `onCreate()` checks for OAuth parameters in `intent.data` before rendering the login UI.

2. **Duplicate `LoginMobileAppCallback` calls**: On first install the server fires `LoginMobileAppCallback` twice in quick succession (~400 ms apart, caused by Chrome following the deep-link redirect). Each invocation carries a different server session. `onNewIntent()` (and the `intent.data` path in `onCreate()`) checks for an existing JWT in `EncryptedSharedPreferences` at entry; if one is already stored the second callback is discarded with `finish()` to prevent a valid session from being overwritten.

#### Refresh Token Flow (mobile)

The api-server returns an app code to the deep link from `/api/oauth/app/callback`. Android then exchanges that code through `/api/oauth/app/exchange-code` and stores the returned access token and refresh token in `EncryptedSharedPreferences`.

When `AppAuthenticator` receives a 401:
1. It calls `RefreshTokenRepository.repoRefreshToken()` synchronously (using OkHttp `execute()`)
2. `RefreshTokenRepository` reads `refreshTokenKey` from secure prefs and uses a **dedicated OkHttp client** (no `AppAuthenticator`, no `AuthInterceptor`) to `POST /api/oauth/app/refresh` with body `{ "refreshToken": "..." }`
3. On success: stores the new JWT via `LoginRepository.setJWT()`, stores the rotated `refreshToken` from the JSON response via `LoginRepository.setRefreshToken()`, then retries the original request
4. On failure (refresh 401 or network error): calls `LoginRepository.logoutAndRedirect()`

The dedicated OkHttp/Retrofit pair is registered in Koin under the `named("refresh")` qualifier to keep it separate from the main client.

#### Logout Flow (mobile)

`ProfileViewModel.logout()` calls `LogoutRepository.logoutWithServerAndRedirect()`. The repository reads `refreshTokenKey` and posts `{ "refreshToken": "..." }` to `/api/oauth/app/logout` so the api-server can revoke the mobile refresh-token family. Local logout and redirect still happen if the token is missing or the server call fails.

#### Credential storage (`PreferencesKeys.kt` + `SecurePrefs.kt`)

All credentials are stored in `EncryptedSharedPreferences` (AES256-GCM) via the `Context.securePrefs()` extension in `SecurePrefs.kt`. The underlying file is excluded from cloud backup and device-transfer in `backup_rules.xml` / `data_extraction_rules.xml`.

| Key | Value stored |
|-----|-------------|
| `jwtKey` | Access JWT |
| `refreshTokenKey` | Refresh token value |
| `cookieKey` | `oauth_session` cookie value |
| `loginTimestampKey` | Unix timestamp of last login |
| `fcmTokenKey` | Firebase Cloud Messaging token |
| `profileKey` | Serialised `Profile` JSON |

Never call `Context.getSharedPreferences()` directly — always use `Context.securePrefs()`.

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

`ControllerFeatureValuesViewModel` exposes:
- `getValueUiState: StateFlow<ValuesUiState>` — load state collected via `LaunchedEffect`
- `sendValueResult: SharedFlow<SendValueResult>` — one-shot send result collected in a `LaunchedEffect(Unit)` collector; triggers the parent snackbar

### Mutation functions are regular funs, not `suspend`

All ViewModel mutation functions (`createHome`, `deleteHome`, `updateRoom`, etc.) are **regular functions** (not `suspend`) that launch their coroutine internally on `viewModelScope`. Screens call them directly from event handlers — no `rememberCoroutineScope().launch { … }` wrapper is needed or correct. Binding the work to the composable's coroutine scope risks cancellation before the result is emitted if the screen is destroyed mid-operation.

`LoginRepository` holds a single `private val gson = Gson()` instance. Do not call `Gson()` per function — JSON serialisation of `Profile` goes through this cached instance.

`SensorFeatureValuesViewModel` and `OnlineFeatureValuesViewModel` use `private val dtf = DateTimeFormatter.ofPattern(…).withZone(ZoneId.systemDefault())` for date formatting. Do not use `SimpleDateFormat` — it is not thread-safe. `DateTimeFormatter` is immutable and safe for concurrent use from multiple coroutines.

All ViewModels declare delay durations as `private const val LOAD_DELAY_MS` (and additional named constants where needed, e.g. `FCM_REGISTER_DELAY_MS`, `OFFLINE_THRESHOLD_MS`) in their `companion object`. Do not use inline `delay(500)` or other literal millisecond values.

`OnlineFeatureValuesViewModel` uses `private const val OFFLINE_THRESHOLD_MS = 60 * 1000L` for the device-offline detection threshold. Do not inline `60 * 1000` directly in `isOffline`.

`AppAuthenticator` uses `response.priorResponse?.code == 401` (not `priorResponse != null`) to detect retry loops, so a 401 following an HTTP redirect still attempts a token refresh.

`PostSetFeatureDeviceValue.value` is typed `Double`, consistent with all other value fields in the model layer. Do not use `Number` as a field type in models.

`SendSavedCookiesInterceptor` expresses the 28-day session expiry as `private const val SESSION_EXPIRY_SECONDS = 28 * 24 * 60 * 60`. Do not use the raw magic number inline.

`LoginActivity` has a private `handleOAuthCallback(data: Uri): Boolean` helper that extracts the app login `code`, exchanges it with the stored PKCE verifier, writes the returned credentials to `EncryptedSharedPreferences`, and starts `PermissionActivity`. Both `onCreate` and `onNewIntent` delegate to this single function — do not duplicate the callback handling logic.

### Error Handling

- Repositories throw `IOException` on API failure
- ViewModels catch exceptions in coroutines and emit `Error(message)` state — this applies to both read (`init()`) and mutation functions (`createHome`, `deleteDevice`, etc.)
- Screens display error messages via Material UI snackbars or error cards

### FCM Token Lifecycle

- On first launch, `MainViewModel.init()` fetches a token from Firebase and registers it with the server via `FCMTokenRepository`
- When Firebase rotates the token, `FCMService.onNewToken()` persists the new token via `LoginRepository` and immediately re-registers it with the server (only if a JWT is present — i.e. the user is logged in). This is wired via Koin `inject()` inside `FCMService`.

## Testing

Unit tests use **MockK** (not Mockito) and JUnit 4. No external infrastructure is required — all dependencies are mocked.

**Repository tests** (`app/src/test/…/repository/`): mock the Retrofit service interface directly, run with `runBlocking`, assert `IOException` on non-`isSuccessful` responses.

```kotlin
class HomesRepositoryTest {
    private val mockHomesService = mockk<HomesServices>()
    private lateinit var homesRepository: HomesRepository

    @Before fun setUp() { homesRepository = HomesRepository(mockHomesService) }
    @After fun tearDown() { clearAllMocks() }

    @Test
    fun `repoGetHomes returns list on success`() = runBlocking {
        coEvery { mockHomesService.getHomes() } returns Response.success(listOf(testHome))
        val result = homesRepository.repoGetHomes()
        assertEquals(1, result.size)
    }
}
```

**ViewModel tests** (`app/src/test/…/ui/screens/`): use `TestCoroutineScheduler` + `StandardTestDispatcher` + `Dispatchers.setMain`; run with `runTest(testScheduler)`; call `advanceUntilIdle()` after triggering async work. Annotate with `@OptIn(ExperimentalCoroutinesApi::class)`.

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class HomesListViewModelTest {
    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val mockHomesRepo = mockk<HomesRepository>()

    @Before fun setUp() { Dispatchers.setMain(mainDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain(); clearAllMocks() }

    @Test
    fun `init emits Idle with homes list on success`() = runTest(testScheduler) {
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)
        val vm = HomesListViewModel(mockHomesRepo)
        advanceUntilIdle()
        assertTrue(vm.homesUiState.value is HomesListViewModel.HomesUiState.Idle)
    }
}
```

## Tech Stack

- **Kotlin** (Android), Jetpack Compose, Material3
- **Koin** for DI (`koin-core`, `koin-android`, `koin-androidx-compose`)
- **Retrofit** + Gson + OkHttp; `API_BASE_URL` injected via `secrets.properties` → `BuildConfig`
- **Firebase BOM** — FCM (push notifications), Firestore, Analytics
- **Jetpack Security** (`security-crypto`) — `EncryptedSharedPreferences` for credential storage
- **Coil** (image loading with OkHttp backend)
- **Accompanist** (runtime permissions)
- **Coroutines** (`viewModelScope` for VM scope, `suspend` for async)
- **MockK** + JUnit 4 + `kotlinx.coroutines.test` for unit tests
- Min SDK: 33 (Android 13), Target/Compile SDK: 36, JVM target: 11

## Build Variants

| Variant | Debuggable | Minified | Version Suffix | Cleartext HTTP | Log level |
|---------|-----------|---------|------|------|------|
| debug   | Yes       | No      | `-debug` | Allowed | `HEADERS` |
| staging | Yes       | No      | `-staging` | Allowed | `HEADERS` |
| release | No        | Yes     | none (TODO: real signing) | Blocked | `NONE` |

Cleartext traffic is controlled per build type via `network_security_config.xml` files in `src/main/res/xml/` (release), `src/debug/res/xml/`, and `src/staging/res/xml/`. Use `staging` for testing real backend without minification (easier debugging). Release signing is TODO — currently uses debug keystore.

## Configuration Files

- `secrets.properties` (gitignored) — API keys, OAuth IDs. Copy from `secrets.defaults.properties`
- `google-services.json` (gitignored) — Firebase config. Copy from `_template`
- `staging.properties` / `release.properties` — environment-specific overrides (injected via build variants)
- `local.properties` — SDK path (auto-generated)

## AI Changelog

Append significant changes (new features, bug fixes, refactors) to `CHANGELOG_CLAUDE.md` in the repo root.
