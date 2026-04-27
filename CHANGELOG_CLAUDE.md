# CHANGELOG_CLAUDE.md

This file tracks significant changes made with AI assistance, organized by type.

---

## 🔒 Security

- **Credential storage migrated to `EncryptedSharedPreferences`** — All SharedPreferences access now uses `Context.securePrefs()` for AES256-GCM encryption. All credential reads/writes go through this helper. The preferences file is excluded from cloud backup and device-transfer to prevent leaking auth tokens via Android backup.
- **Per-build-type network security config** — Release and staging block cleartext HTTP and trust only system CAs; debug allows cleartext and user-added CA certificates for local/MITM inspection.
- **Sensitive Logcat output removed** — OAuth deep-link query strings (containing `token`, `session_cookie`, `refresh_token`) removed from logs. All FCM, DI, and debug log calls guarded by `BuildConfig.DEBUG`.
- **`HttpLoggingInterceptor` scoped to debug builds** — Level is `HEADERS` in debug, `NONE` in staging/release. `Authorization` and `Cookie` headers never appear in Logcat on production-like builds.
- **OAuth callback validation hardened** — `LoginActivity.handleOAuthCallback(Uri)` now validates callback scheme and `/postlogin` path before exchanging an app code. Debug additionally accepts `http` callbacks for local development; host and port remain in manifest/property configuration.
- **Localhost OAuth callback moved to debug-only manifest** — `http://localhost:8082/postlogin` is no longer declared in the main manifest, so staging/release only expose the HTTPS app-link callback.
- **Profile screenshots blocked** — `ProfileScreen` sets `FLAG_SECURE` while mounted and clears it on dispose to prevent screenshots/screen recording while the regenerated API token can be visible.
- **Staging hardened** — Staging APKs are now non-debuggable, minified, resource-shrunk, cleartext-blocked, and use `HttpLoggingInterceptor.Level.NONE`.
- **Token refresh on 401** — `AppAuthenticator` silently refreshes the JWT on first 401, retries the request, and only falls back to logout when the refresh itself fails. Prior-response guard fixed from `!= null` (true on 3xx redirects too) to `?.code == 401`.
- **Mobile refresh token flow implemented** — Full JWT refresh token cycle with `RefreshTokenServices`, `TokenResponse`, and `RefreshTokenRepository`. Mobile refresh now posts `{ refreshToken }` to `/api/oauth/app/refresh` and persists the rotated `refreshToken` from the JSON response.
- **Mobile OAuth login migrated to PKCE code exchange** — App login now opens `/api/oauth/app/login` with `code_challenge` / `code_challenge_method=S256`, receives an app code through the deep link, and exchanges it at `/api/oauth/app/exchange-code` using the stored code verifier instead of receiving tokens directly in the callback URL.
- **Thread-safe date formatting** — `SimpleDateFormat` (mutable, not thread-safe) replaced with immutable `DateTimeFormatter` to prevent coroutine data races.
- **`MasterKey` cache made thread-safe** — `@Volatile` + double-checked locking ensures the key is built at most once even under concurrent access.
- **Koin logger guarded by build type** — Debug logs only in `BuildConfig.DEBUG` to avoid leaking DI resolution info in release.
- **Mobile refresh tokens kept out of cookies** — Android refresh/logout use JSON bodies on `/api/oauth/app/refresh` and `/api/oauth/app/logout`; the web-only `/api/oauth/refresh` cookie flow is not used by the app.

---

## 🐛 Bug Fixes

- **First-install OAuth deep-link crash loop** — `onCreate()` now handles the deep-link when the process was killed while the browser was open (`singleTask` + process death).
- **Duplicate OAuth callback** — Both `onCreate()` and `onNewIntent()` discard a second callback if a JWT is already stored, preventing valid session overwrite.
- **Missing `return` after JWT redirect** — `onCreate()` fell through to render the login UI after launching `MainActivity`.
- **Mobile login rejected after successful code exchange** — Client cookie name updated from stale `mysession` to server-side `oauth_session`, so the session cookie returned by `/api/oauth/app/exchange-code` is persisted and sent with authenticated API requests.
- **Koin dependency cycle after logout endpoint wiring** — Server logout call moved out of `LoginRepository` into `LogoutRepository`, breaking the `LoginRepository → LogoutServices → Retrofit → AuthInterceptor → LoginRepository` resolution loop.
- **Android refresh called web endpoint** — The app was posting an empty request to `/api/oauth/refresh`, which validates web refresh cookies and returned 401 for mobile tokens. It now calls `/api/oauth/app/refresh` with the stored refresh token in JSON.
- **Out-of-bounds crash on controller feature values** — All four `getXxxByFeatureUuid` functions now bounds-check before array access.
- **Reference equality on `String` and `null`** — `!==` replaced with `!isNullOrEmpty()` / `!= null` throughout.
- **Broken string template** — `profileUiState.profile?.id}` missing `${` corrected.
- **Lexicographic feature sort** — `sortedBy { it.order.toString() }` (1, 10, 2, …) changed to `sortedBy { it.order }` (numeric).
- **`FCMService.onNewToken` was a no-op stub** — Now persists and re-registers the rotated FCM token.
- **`FCMService` exception handling** — `catch (Exception)` swallowed `CancellationException`; changed to `catch (IOException)`. Scope upgraded from raw `CoroutineScope` to `lifecycleScope`.
- **Error handling missing from ViewModel mutation functions** — `createHome`, `deleteHome`, `createRoom`, etc. now emit `Error` state on `IOException` instead of propagating.
- **`LoginRepository` double-error emission** — `registerDeviceToFirebase()` changed from `suspend`/null-return to throw, so a single `catch` in `init()` handles all failures.
- **Dead code removed** — `SendUiState` sealed class was declared but never exposed or collected.

---

## 🏗️ Architecture / Design

- **Dual OkHttp/Retrofit instances** — Main client has `AppAuthenticator`; refresh client intentionally does not (prevents recursion). Registered under `named("refresh")` in Koin.
- **Public OkHttp/Retrofit client added** — Unauthenticated pre-login flows use a separate `named("public")` Retrofit instance for app code exchange, keeping them outside the authenticated client/authenticator chain.
- **Mobile logout endpoint wired** — `LogoutServices` now posts the stored refresh token to `/api/oauth/app/logout`; `LogoutRepository` still clears local state if server logout cannot complete.
- **`LoginRepository` as `single`** — Changed from `factory` to consistent singleton pattern.
- **All ViewModel mutation and async functions converted to non-`suspend`** — Functions called from composables via `rememberCoroutineScope().launch { … }` are now regular functions that launch on `viewModelScope` internally, making them resilient to screen destruction. `rememberCoroutineScope` removed from all affected screens.
- **`ControllerFeatureValuesViewModel` result emission** — `getValues` renamed `loadValues`, emits to `StateFlow`. `sendCommands` emits to `SharedFlow<SendValueResult>`. Composable uses separate `LaunchedEffect` blocks for each concern.
- **`AppDrawer` de-coupled from Keystore** — Now receives `profile: Profile?` as a parameter. `MainViewModel` hoisted to `AppNavGraph` scope. Eliminates repeated Keystore decrypt + JSON parse on recomposition.
- **OAuth callback logic deduplicated** — Shared extraction logic moved to `handleOAuthCallback(Uri): Boolean`.

---

## 🧹 Idiomatic Kotlin

- **`var` → `val` across all model and UI classes** — `Profile`, `Home`, `Room`, `SensorValue`, `SpinnerItemObj`, all immutable now. Mutation sites refactored to use `.copy(…)`.
- **`Number` → concrete types in all models** — All fields explicitly typed as `Int`, `Long`, or `Double`. Consistent type safety.
- **`MutableList` → `List`** — `MyDevicesList.homeDevices` constructed fully before assignment.
- **O(n²) → O(1) device lookup** — `devicesIds: List<String>` changed to `assignedIds: Set<String>` with `!in`.
- **`IntRange(…).step(1).toList().toIntArray()` → `(n..m).toIntArray()`** — Idiomatic range syntax removes intermediate `List`.
- **Spinner option lists pre-computed once** — `getSetpoints()`, `getModes()`, `getFanSpeeds()`, `getTolerances()` computed at construction time, not on every call.
- **`MasterKey` built only once** — Moved behind cached field so Keystore lookup happens at most once per process.
- **`LoginRepository.gson` cached** — Instance reused instead of recreated on every call.
- **`map {}` for side-effects → `forEach`** — Correct iteration idiom.
- **`getControllers` / `getSensors` simplified** — `find + != null` pattern replaced with `any` / `none`.
- **Magic numbers extracted to named constants** — `SESSION_EXPIRY_SECONDS`, `LOAD_DELAY_MS`, `FCM_REGISTER_DELAY_MS`, `OFFLINE_THRESHOLD_MS` in companion objects.
- **Redundant code removed** — Unnecessary safe-calls inside null-check blocks (`?.` → `.`), `return@launch`, `public` on override, empty constructor `()`, trailing semicolons, explicit `it ->`, unused `TAG` constants, redundant `.toDouble()`.

---

## 🔧 Build / Tooling

- **Deprecated `gradle.properties` entries removed** — `android.enableAppCompileTimeRClass=false` (removed in AGP 8), `android.uniquePackageNames=false`, `android.defaults.buildfeatures.resvalues=true`, `android.dependency.useConstraints=true`, `android.usesSdkInManifest.disallowed=false`, `android.sdk.defaultTargetSdkToCompileSdkIfUnset=false`.
- **Build performance improved** — `org.gradle.parallel=true` enabled, `org.gradle.caching=true` added, daemon heap `-Xmx2048m` → `-Xmx4096m`, `-XX:+HeapDumpOnOutOfMemoryError` added.

- **R8 optimised resource shrinking enabled** — Smaller release/staging APKs.
- **`secrets-gradle-plugin` `[libraries]` entry removed** — Leftover from old `buildscript classpath` approach.
- **`proguardFiles` added to `staging` build type** — Prepared for future `isMinifyEnabled=true`.
- **Deprecated API imports updated** — Koin `viewModel` DSL import to `org.koin.core.module.dsl`. `MenuAnchorType` → `ExposedDropdownMenuAnchorType`.
- **`@Suppress("DEPRECATION")` on `SecurePrefs`** — `MasterKey` and `EncryptedSharedPreferences` deprecated in `security-crypto:1.1.0` with no stable replacement yet.

---

## Fixed

- **`FCMService` not declared in manifest** — Added `<service>` entry with `com.google.firebase.MESSAGING_EVENT` intent filter so `onMessageReceived` and `onNewToken` are actually invoked by the Firebase SDK.
- **`FirebaseException` uncaught in `MainViewModel`** — Changed `catch (err: IOException)` to `catch (err: Exception)` so a Firebase failure to fetch a token does not escape the coroutine and crash the app.
- **Dead `serviceScope` removed from `FCMService`** — The unused `CoroutineScope(SupervisorJob() + Dispatchers.IO)` and its `onDestroy` cancel were removed; all async work goes through WorkManager.
- **No-op `handleNow()` removed from `FCMService`** — Removed placeholder `handleNow()` call and method; all incoming messages are forwarded to `FCMNotificationBus` regardless of whether they carry a data payload.
- **`scheduleMonthlyRefresh` renamed to `schedulePeriodically`** — Method runs on a 1-day interval; name now reflects actual behaviour. Updated callers in `App.kt` and `FCMService.kt`.
- **Default FCM notification channel declared** — Added `com.google.firebase.messaging.default_notification_channel_id` meta-data to manifest and `default_notification_channel_id` string resource so Android 8.0+ background notifications use a named channel instead of the Firebase fallback `"miscellaneous"` channel.

---

## Added

- **In-app FCM notifications via snackbar** — Added `FCMNotificationBus` singleton (`SharedFlow<RemoteMessage>`). `FCMService.onMessageReceived` emits every foreground message to the bus. `AppNavGraph` in `MainActivity` collects it in a `LaunchedEffect` and shows a `SnackbarDuration.Long` snackbar with the notification title/body, visible on whichever screen the user is on.
- **`SecurePrefs.kt`** — New `Context.securePrefs()` extension for AES256-GCM encrypted preferences.
- **Refresh token support** — `PreferencesKeys.kt` (refresh-token constants), `LoginActivity` PKCE app-code exchange, `LoginRepository` (new methods: `setJWT()`, `getRefreshToken()`, `setRefreshToken()`), `RefreshTokenServices`, `TokenResponse`, and `RefreshTokenRepository`.
