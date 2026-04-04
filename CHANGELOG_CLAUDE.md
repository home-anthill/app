# CHANGELOG_CLAUDE.md

This file tracks significant changes made with AI assistance, organized by type.

---

## 🔒 Security

- **Credential storage migrated to `EncryptedSharedPreferences`** — All SharedPreferences access now uses `Context.securePrefs()` for AES256-GCM encryption. All credential reads/writes go through this helper. The preferences file is excluded from cloud backup and device-transfer to prevent leaking auth tokens via Android backup.

- **Per-build-type network security config** — Release blocks all cleartext HTTP; debug and staging allow it. User-added CA certificates are trusted in debug/staging for MITM-proxy inspection.

- **Sensitive Logcat output removed** — OAuth deep-link query strings (containing `token`, `session_cookie`, `refresh_token`) removed from logs. All FCM, DI, and debug log calls guarded by `BuildConfig.DEBUG`.

- **`HttpLoggingInterceptor` scoped to debug builds** — Level is `HEADERS` in debug/staging, `NONE` in release. `Authorization` and `Cookie` headers never appear in Logcat on production.

- **Token refresh on 401** — `AppAuthenticator` silently refreshes the JWT on first 401, retries the request, and only falls back to logout when the refresh itself fails. Prior-response guard fixed from `!= null` (true on 3xx redirects too) to `?.code == 401`.

- **Refresh token flow implemented** — Full JWT refresh token cycle with `RefreshTokenServices`, `TokenResponse`, `SendRefreshTokenCookieInterceptor` (adds cookie **only** on `/token/refresh` endpoint), and `RefreshTokenRepository`.

- **Thread-safe date formatting** — `SimpleDateFormat` (mutable, not thread-safe) replaced with immutable `DateTimeFormatter` to prevent coroutine data races.

- **`MasterKey` cache made thread-safe** — `@Volatile` + double-checked locking ensures the key is built at most once even under concurrent access.

- **Koin logger guarded by build type** — Debug logs only in `BuildConfig.DEBUG` to avoid leaking DI resolution info in release.

- **Refresh token cookie scoping** — Path check changed from `contains` to `endsWith("/token/refresh")` so the token is never sent to unrelated endpoints.

---

## 🐛 Bug Fixes

- **First-install OAuth deep-link crash loop** — `onCreate()` now handles the deep-link when the process was killed while the browser was open (`singleTask` + process death).

- **Duplicate OAuth callback** — Both `onCreate()` and `onNewIntent()` discard a second callback if a JWT is already stored, preventing valid session overwrite.

- **Missing `return` after JWT redirect** — `onCreate()` fell through to render the login UI after launching `MainActivity`.

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

## Added

- **`SecurePrefs.kt`** — New `Context.securePrefs()` extension for AES256-GCM encrypted preferences.

- **Refresh token support** — `PreferencesKeys.kt` (two new constants), `LoginActivity.onNewIntent` (reads `refresh_token` from deep link), `LoginRepository` (new methods: `setJWT()`, `getRefreshToken()`, `setRefreshToken()`), `RefreshTokenServices`, `TokenResponse`, `SendRefreshTokenCookieInterceptor`, `RefreshTokenRepository`.
