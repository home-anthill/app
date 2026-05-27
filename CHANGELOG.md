# Changelog

## 3.0.0

### Features

- **In-app FCM notifications via snackbar** — Added `FCMNotificationBus` singleton (`SharedFlow<RemoteMessage>`). `FCMService.onMessageReceived` emits every foreground message to the bus, and `AppNavGraph` collects it to show a snackbar from any screen.
- **Encrypted preferences helper** — Added `Context.securePrefs()` for AES256-GCM encrypted SharedPreferences access.
- **Mobile refresh token support** — Added `PreferencesKeys` refresh-token constants, `RefreshTokenServices`, `TokenResponse`, `RefreshTokenRepository`, and `LoginRepository` helpers for reading and writing rotated refresh tokens.
- **Mobile OAuth PKCE code exchange** — App login now opens `/api/oauth/app/login` with a PKCE challenge, receives a one-time app code through the deep link, and exchanges it at `/api/oauth/app/exchange-code` using the stored verifier.
- **Public pre-login Retrofit client** — Added a separate `named("public")` OkHttp/Retrofit client for unauthenticated flows such as mobile app-code exchange.
- **Dedicated refresh Retrofit client** — Added a separate `named("refresh")` OkHttp/Retrofit client without `AppAuthenticator` to avoid recursive refresh calls.
- **Mobile logout endpoint integration** — Added `LogoutServices` / `LogoutRepository` wiring for `/api/oauth/app/logout`, submitting the stored mobile refresh token while still clearing local state if server logout fails.
- **Default FCM notification channel** — Added `com.google.firebase.messaging.default_notification_channel_id` manifest metadata and string resource so Android 8.0+ background notifications use the app's named channel.
- **Mobile authentication is token-based instead of cookie-based** — The app no longer parses, stores, or sends `oauth_session`; authenticated API calls use Bearer access JWTs and JSON refresh tokens only.
- **Mobile refresh/logout use JSON bodies** — Android refresh and logout now call `/api/oauth/app/refresh` and `/api/oauth/app/logout` with `{ refreshToken }`; the web-only refresh-cookie flow remains isolated from the app.
- **OAuth callback logic deduplicated** — Shared callback parsing and validation moved to `LoginActivity.handleOAuthCallback(Uri): Boolean`.
- **Mobile OAuth app-state contract updated** — `/api/oauth/app/login` now expects app clients to provide `app_state`, and `/app/postlogin` echoes it as `state` with the one-time app code.
- **FCM token refresh scheduled after login** — After successful mobile OAuth code exchange and credential persistence, `LoginActivity` schedules immediate FCM token refresh so device registration runs with the new authenticated session.
- **ViewModel async API simplified** — ViewModel mutation and async functions now launch internally on `viewModelScope` instead of exposing `suspend` APIs to composables via `rememberCoroutineScope`.
- **Controller feature value flows clarified** — `getValues` was renamed to `loadValues`, value loading emits through `StateFlow`, and `sendCommands` emits results through `SharedFlow<SendValueResult>`.
- **`AppDrawer` decoupled from Keystore** — `AppDrawer` now receives `profile: Profile?`; `MainViewModel` is hoisted to the `AppNavGraph` scope to avoid repeated Keystore decrypt and JSON parse work during recomposition.
- **`LoginRepository` registered as singleton** — Koin registration changed from `factory` to `single`.
- **Model and UI data made immutable** — `Profile`, `Home`, `Room`, `SensorValue`, `SpinnerItemObj`, and related classes now use `val` fields, with mutation sites refactored to `.copy(...)`.
- **Model number types made explicit** — Generic `Number` fields were replaced with concrete `Int`, `Long`, or `Double` fields.
- **Collection types tightened** — `MyDevicesList.homeDevices` uses `List`, and assigned device lookups now use `Set` membership.
- **Kotlin idioms cleaned up** — Range construction, side-effect iteration, controller/sensor lookup logic, null-check branches, override modifiers, empty constructors, trailing semicolons, explicit lambdas, and redundant conversions were simplified.
- **Magic numbers named** — Extracted constants including `SESSION_EXPIRY_SECONDS`, `LOAD_DELAY_MS`, `FCM_REGISTER_DELAY_MS`, and `OFFLINE_THRESHOLD_MS`.
- **`scheduleMonthlyRefresh` renamed to `schedulePeriodically`** — The method runs daily, so the name now matches the actual behavior.

### Bug fixes

- **First-install OAuth deep-link crash loop** — `onCreate()` now handles deep links after process death while the browser was open.
- **Duplicate OAuth callback handling** — `onCreate()` and `onNewIntent()` now discard a second callback when a JWT is already stored, preventing valid sessions from being overwritten.
- **Missing `return` after JWT redirect** — `onCreate()` no longer falls through to render the login UI after launching `MainActivity`.
- **Mobile login rejected after code exchange** — Client cookie naming was updated from stale `mysession` to server-side `oauth_session` for the previous cookie-based exchange flow.
- **Android refresh called the web endpoint** — The app now posts the stored refresh token to `/api/oauth/app/refresh` instead of sending an empty request to `/api/oauth/refresh`.
- **Token refresh retry guard** — `AppAuthenticator` now retries only after a 401 prior response instead of treating any prior response, including redirects, as a retry.
- **Concurrent 401 refresh race** — `AppAuthenticator` serializes refresh attempts so multiple requests do not submit the same rotating refresh token.
- **Koin dependency cycle after logout wiring** — Server logout calls moved from `LoginRepository` to `LogoutRepository`, breaking the `LoginRepository -> LogoutServices -> Retrofit -> AuthInterceptor -> LoginRepository` cycle.
- **Failed OAuth exchange left stale PKCE state** — Failed or invalid callbacks now clear pending verifier and app-state values from encrypted preferences.
- **Default callback ports accepted** — Callback validation treats omitted HTTPS and HTTP ports as `443` and `80`, respectively.
- **Web OAuth redirect intercepted by Android App Links** — Mobile App Links now validate `/app/postlogin` only, so mobile Chrome web login/logout no longer collides with the installed app.
- **Out-of-bounds controller feature access** — All four `getXxxByFeatureUuid` functions now bounds-check before array access.
- **Reference equality on `String` and `null`** — Incorrect `!==` checks were replaced with `!isNullOrEmpty()` or `!= null`.
- **Broken string template** — Corrected `profileUiState.profile?.id}` to use the missing `${...}` syntax.
- **Lexicographic feature sorting** — Feature ordering now sorts by numeric `order` instead of `order.toString()`.
- **`FCMService` not declared in manifest** — Added the Firebase messaging service declaration so `onMessageReceived` and `onNewToken` are invoked.
- **`FCMService.onNewToken` no-op** — Rotated FCM tokens are now persisted and re-registered.
- **`FCMService` cancellation handling** — Replaced broad `catch (Exception)` handling with `catch (IOException)` where appropriate.
- **Uncaught Firebase token failure** — `MainViewModel` now catches `Exception` when Firebase token retrieval fails, preventing coroutine crashes.
- **ViewModel mutation error states** — `createHome`, `deleteHome`, `createRoom`, and related mutations now emit `Error` state on `IOException`.
- **`LoginRepository` double-error emission** — `registerDeviceToFirebase()` now throws instead of returning null, so `init()` handles failures in one catch path.

### Security fixes

- **Credential storage encrypted** — SharedPreferences access now goes through `Context.securePrefs()` backed by `EncryptedSharedPreferences`; the encrypted preferences file is excluded from cloud backup and device transfer.
- **Per-build-type network security config** — Release and staging block cleartext HTTP and trust only system CAs; debug allows cleartext and user-added CA certificates for local inspection.
- **Sensitive Logcat output removed** — OAuth deep-link query strings containing `token`, `session_cookie`, or `refresh_token` are no longer logged, and FCM, DI, and debug logs are guarded by `BuildConfig.DEBUG`.
- **`HttpLoggingInterceptor` scoped to debug builds** — Debug uses `HEADERS`; staging and release use `NONE`, and production-like builds never log `Authorization` or `Cookie` headers.
- **OAuth callback validation hardened** — Callback scheme, host, port, and path are validated from Gradle properties before app-code exchange; real callback hosts and local ports live in gitignored property files.
- **Mobile OAuth callback isolated from web post-login** — Android App Links now target `/app/postlogin`, leaving web `/postlogin#token=...` in the browser.
- **Mobile OAuth app-state validation added** — Android generates an app-owned state, sends it as `app_state`, requires the callback `state` to match, and clears pending state/PKCE data on invalid callbacks.
- **OAuth verifier, state, and code entropy increased** — Android PKCE verifiers and app states now use 96 random bytes, producing maximum-length 128-character verifier/state strings; api-server OAuth states and mobile app codes are also 128 characters.
- **Mobile app-code format validation** — `/api/oauth/app/exchange-code` rejects malformed app login codes before Mongo lookup.
- **Local OAuth callback moved to debug-only manifest** — Staging and release expose only the configured HTTPS App Link callback.
- **Profile screenshots blocked** — `ProfileScreen` sets `FLAG_SECURE` while mounted and clears it on dispose.
- **Staging build hardened** — Staging APKs are non-debuggable, minified, resource-shrunk, cleartext-blocked, and use `HttpLoggingInterceptor.Level.NONE`.
- **Thread-safe date formatting** — Replaced mutable `SimpleDateFormat` with immutable `DateTimeFormatter`.
- **Thread-safe `MasterKey` cache** — Added `@Volatile` and double-checked locking so the key is built at most once under concurrent access.
- **Koin logger guarded by build type** — Koin DI logs are emitted only in debug builds.

### Performance

- **Device lookup made O(1)** — `devicesIds: List<String>` became `assignedIds: Set<String>` with `!in` membership checks.
- **Spinner options precomputed** — `getSetpoints()`, `getModes()`, `getFanSpeeds()`, and `getTolerances()` are computed at construction time instead of on every call.
- **`MasterKey` built once per process** — Keystore lookup now happens at most once.
- **`LoginRepository.gson` cached** — The Gson instance is reused instead of recreated on every call.

### Chores

- **Mobile session cookie plumbing removed** — Removed stale cookie interceptors and Koin wiring for mobile session cookies.
- **Dead `SendUiState` removed** — Removed unused sealed class that was never exposed or collected.
- **Dead `serviceScope` removed from `FCMService`** — Removed unused `CoroutineScope(SupervisorJob() + Dispatchers.IO)` and `onDestroy` cancellation.
- **No-op `handleNow()` removed from `FCMService`** — Removed placeholder method and call; all incoming messages are forwarded to `FCMNotificationBus`.
- **Redundant code and constants removed** — Removed unused `TAG` constants and other redundant Kotlin syntax during cleanup.
- **Deprecated `gradle.properties` entries removed** — Removed obsolete Android Gradle Plugin flags including `android.enableAppCompileTimeRClass=false`, `android.uniquePackageNames=false`, `android.defaults.buildfeatures.resvalues=true`, `android.dependency.useConstraints=true`, `android.usesSdkInManifest.disallowed=false`, and `android.sdk.defaultTargetSdkToCompileSdkIfUnset=false`.
- **Gradle build performance improved** — Enabled `org.gradle.parallel=true`, added `org.gradle.caching=true`, increased daemon heap from `-Xmx2048m` to `-Xmx4096m`, and added `-XX:+HeapDumpOnOutOfMemoryError`.
- **R8 optimized resource shrinking enabled** — Release and staging APKs are smaller.
- **Removed stale `secrets-gradle-plugin` version-catalog entry** — Deleted leftover `[libraries]` entry from the old `buildscript classpath` setup.
- **`proguardFiles` added to staging** — Staging build type is ready for future minification.
- **Deprecated API imports updated** — Updated Koin `viewModel` DSL import to `org.koin.core.module.dsl` and `MenuAnchorType` to `ExposedDropdownMenuAnchorType`.
- **`SecurePrefs` deprecation suppression added** — Suppressed `MasterKey` and `EncryptedSharedPreferences` deprecations while `security-crypto:1.1.0` has no stable replacement.
