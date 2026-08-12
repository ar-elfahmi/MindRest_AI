# T-007 — Auth Flow End-to-End (FR-001 + FR-002 + FR-003)

## 🎯 Goal

Verifikasi dan lengkapi **3 fitur autentikasi sekaligus** supaya user bisa:
1. **FR-001** — daftar akun dengan email + password
2. **FR-002** — login + logout
3. **FR-003** — lengkapi/update profil (full_name, date_of_birth, dll)

Setelah T-007, **SEMUA fitur lain (FR-009 chat, FR-014 insight, FR-015 dashboard)** baru benar-benar testable end-to-end karena butuh user session valid.

---

## 📖 Context

### Yang SUDAH ada dari agent sebelumnya

| Layer | File | Status |
|---|---|---|
| Repository | `features/authentication/data/repository/AuthRepository.kt` | ✅ Interface + Impl (signIn, signUp, signOut returning `Result<Unit>`) |
| ViewModel | `features/authentication/presentation/viewmodel/AuthViewModel.kt` | ✅ `LoginViewModel` + `RegisterViewModel` dengan friendly error mapper |
| Compose UI | `features/authentication/presentation/screen/AuthScreens.kt` | ✅ SplashScreen, OnboardingScreen, LoginScreen, RegisterScreen (1558 baris) |
| Profile | `features/profile/presentation/screen/ProfileScreen.kt` | ✅ Ada, perlu verifikasi wiring update |
| Supabase | `supabase/schema.sql` | ✅ Trigger `on_auth_user_created` auto-create profile row |
| Migration | `supabase/migrations/001_extend_profiles.sql` | ✅ Extends profiles dengan kolom tambahan |

### Yang BELUM jelas / kemungkinan bug

1. **Navigation routing based on session** — Siapa yang observe `auth.sessionStatus`? SplashScreen harus decide "Home (kalau ada session) vs Onboarding/Login (kalau belum)".
2. **Email confirmation flow** — Supabase default = `confirm email ON`. Setelah signUp, session = null sampai user klik link di email. `LoginViewModel` perlu handle error ini dengan pesan jelas ("Cek inbox email Anda untuk konfirmasi").
3. **Profile auto-create trigger** — Apakah trigger `on_auth_user_created` sudah aktif di remote DB? Cek dengan `SELECT id FROM profiles LIMIT 1`.
4. **Profile edit di ProfileScreen** — Apakah ada `ProfileViewModel.updateProfile(...)` yang call `profiles.update(...)`? Atau masih read-only display?
5. **Register form fields** — Apakah RegisterScreen mengumpulkan `full_name` + `confirm_password`? AuthViewModel sudah punya `onFullNameChange`, jadi seharusnya iya — perlu verifikasi di UI.
6. **Logout button** — ProfileScreen harus ada tombol logout yang call `AuthRepository.signOut()` lalu navigate ke LoginScreen.

---

## 📚 Read First (WAJIB Context7 + repo)

1. `app/src/main/java/com/example/features/authentication/presentation/screen/AuthScreens.kt` — semua composables (1558 baris, baca pelan)
2. `app/src/main/java/com/example/features/authentication/presentation/viewmodel/AuthViewModel.kt` — LoginViewModel + RegisterViewModel
3. `app/src/main/java/com/example/features/authentication/data/repository/AuthRepository.kt` — kontrak
4. `app/src/main/java/com/example/MainActivity.kt` — entry point, cek `setContent { ... }` apa yang dibungkus
5. `app/src/main/java/com/example/core/navigation/` — NavGraph, cek apakah ada composable destination untuk Splash → Login → Home
6. `app/src/main/java/com/example/features/profile/presentation/screen/ProfileScreen.kt` — cek apakah sudah ada tombol save/update
7. `supabase/schema.sql` (cari `trigger on_auth_user_created`) — pastikan trigger aktif
8. `supabase/README.md` section 5 — Email + Google Auth setup di Dashboard

**Wajib Context7** (training data out-of-date):
- `resolve-library-id "supabase auth kotlin"` → query "supabase auth kotlin listen session status"
- `resolve-library-id "supabase auth kotlin"` → query "email confirmation flow signUp returns null session"

---

## ✅ Scope

### Wajib dibuat / diubah

1. **Session observer** (kalau belum ada):
   - File baru: `features/authentication/presentation/state/SessionState.kt`
   - Observe `auth.sessionStatus` dari `SupabaseClient`
   - Emit: `Loading | SignedIn | SignedOut`
   - Dipakai oleh `MainActivity` untuk route ke Splash → (Home / Login)

2. **Wire navigation** di `MainActivity.kt`:
   - Observe SessionState
   - `Loading` → `SplashScreen`
   - `SignedIn` → `HomeScreen` (atau NavGraph `home` route)
   - `SignedOut` → `OnboardingScreen` (pertama kali) atau `LoginScreen`

3. **Email confirmation handler** di `LoginViewModel`:
   - Tambah case di `friendlySignInMessage()` untuk exception `UserNotConfirmedException` (atau pola kode error `email_not_confirmed`)
   - Pesan: "Email belum dikonfirmasi. Cek inbox untuk link aktivasi."

4. **Profile update wiring** di ProfileScreen:
   - Tambah `ProfileViewModel.kt` kalau belum ada
   - Method `updateProfile(fullName, dateOfBirth, dll)` yang call `profiles.update { ... }`
   - UI: tombol "Save" yang enabled hanya kalau field berubah
   - Success/error snackbar

5. **Logout button** di ProfileScreen:
   - Panggil `AuthRepository.signOut()` lewat ViewModel
   - Navigate ke LoginScreen setelah success
   - Confirmation dialog sebelum logout ("Yakin ingin keluar?")

6. **README update**:
   - `supabase/README.md` section 5: cara disable email confirmation untuk testing cepat (Dashboard → Auth → Providers → Email → toggle OFF), ATAU cara cek inbox confirmation
   - Tambah catatan: "Untuk E2E test lokal, disable email confirmation lebih cepat."

### Migration / DB (cek, jangan buat baru kecuali perlu)

- **Tidak perlu migration baru** kecuali ada kolom profile yang belum ada di `001_extend_profiles.sql`
- Verifikasi trigger `on_auth_user_created` aktif: `SELECT tgname FROM pg_trigger WHERE tgname = 'on_auth_user_created';`

---

## ❌ DON'T Touch

- ❌ `AuthRepository.kt` (interface tetap, jangan ubah signature — ViewModel bergantung)
- ❌ `AuthViewModel.kt` core logic (fungsi signIn/signUp sudah benar via Result<Unit>)
- ❌ File di luar folder `features/authentication/` dan `features/profile/`
- ❌ `MainActivity.kt` SELAIN navigation logic yang sudah di-scope di atas
- ❌ `supabase/schema.sql` (jangan edit langsung — pakai migration kalau ada perubahan)
- ❌ Edge Functions (T-007 pure auth flow, tidak pakai AI)
- ❌ File di `features/{sleep,journal,ikigai,lifestyle,mood,reminder,relaxation,achievements,statistics,settings,home}/` — fitur lain, tunggu giliran
- ❌ Jangan install APK di emulator (orchestrator handle E2E runtime test terpisah setelah integration selesai)

---

## 🛠️ Implementation Steps

### Step 1: Diagnosa state awal (5-10 menit)

Cek apakah navigation sudah ada atau belum:
```bash
grep -rE "NavHost|NavController|navigation/" app/src/main/java/com/example/
ls app/src/main/java/com/example/core/navigation/ 2>&1
grep -E "sessionStatus|currentSessionOrNull" app/src/main/java/com/example/ -r
```

Output kerangka:
- Kalau `core/navigation/` ada NavGraph dengan destinations Splash/Login/Home/Profile → step 2 langsung
- Kalau ada `sessionStatus` observer → tinggal wire ke MainActivity
- Kalau belum ada keduanya → bikin dari nol (Scope #1)

### Step 2: Implementasi sesuai gap

#### A. Kalau session observer BELUM ada

Buat `features/authentication/presentation/state/SessionState.kt`:

```kotlin
data class SessionState(
    val status: Status = Status.Loading,
    val userId: String? = null,
    val email: String? = null,
) {
    enum class Status { Loading, SignedIn, SignedOut }

    companion object {
        fun fromSession(session: SessionStatus): SessionState = when (session) {
            is SessionStatus.Authenticated -> SessionState(
                status = Status.SignedIn,
                userId = session.session.user?.id,
                email = session.session.user?.email,
            )
            is SessionStatus.NotAuthenticated -> SessionState(Status.SignedOut)
            is SessionStatus.LoadingFromStorage,
            is SessionStatus.Loading -> SessionState(Status.Loading)
            else -> SessionState(Status.Loading)
        }
    }
}

class SessionObserver @Inject constructor(
    private val client: SupabaseClient,
) {
    val sessionState: StateFlow<SessionState> = client.auth.sessionStatus
        .map { SessionState.fromSession(it) }
        .stateIn(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionState(),
        )
}
```

Register di Hilt module (kalau project pakai Hilt) atau pass manual dari MainActivity.

#### B. Wire MainActivity navigation

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
            MindRestApp(sessionState = sessionState)
        }
    }
}

@Composable
fun MindRestApp(sessionState: SessionState) {
    when (sessionState.status) {
        SessionState.Status.Loading -> SplashScreen()
        SessionState.Status.SignedOut -> {
            // Decide: OnboardingScreen first time, LoginScreen after
            // Simplest: always LoginScreen; user can navigate to Onboarding manual
            LoginScreen(...)
        }
        SessionState.Status.SignedIn -> HomeScreen(...)
    }
}
```

Catatan: Cek `MainActivity.kt` existing — kalau pakai NavHost dengan destinations, gunakan `startDestination` dinamis:

```kotlin
NavHost(
    navController = navController,
    startDestination = when (sessionState.status) {
        SessionState.Status.Loading -> "splash"
        SessionState.Status.SignedIn -> "home"
        SessionState.Status.SignedOut -> "login"
    },
)
```

#### C. Email confirmation handler di LoginViewModel

Cari fungsi `friendlySignInMessage()` di AuthViewModel.kt (line ~85). Tambah case:

```kotlin
private fun Throwable.friendlySignInMessage(): String = when {
    message?.contains("Email not confirmed", ignoreCase = true) == true ->
        "Email belum dikonfirmasi. Cek inbox Anda untuk link aktivasi."
    message?.contains("Invalid login credentials", ignoreCase = true) == true ->
        "Email atau password salah."
    message?.contains("network", ignoreCase = true) == true ->
        "Gagal terhubung ke server. Cek koneksi internet."
    else -> message ?: "Login gagal. Coba lagi."
}
```

#### D. ProfileViewModel + ProfileScreen update

File baru kalau belum ada: `features/profile/presentation/viewmodel/ProfileViewModel.kt`:

```kotlin
class ProfileViewModel @Inject constructor(
    private val client: SupabaseClient,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    fun load() = viewModelScope.launch {
        val userId = client.auth.currentUserOrNull()?.id ?: return@launch
        val profile = client.from("profiles").select { filter { eq("id", userId) } }
            .decodeSingleOrNull<Profile>()
        _uiState.update { it.copy(profile = profile, isLoading = false) }
    }

    fun updateProfile(fullName: String, dateOfBirth: LocalDate?) = viewModelScope.launch {
        val userId = client.auth.currentUserOrNull()?.id ?: return@launch
        runCatching {
            client.from("profiles").update({
                set("full_name", fullName)
                dateOfBirth?.let { set("date_of_birth", it.toString()) }
            }) { filter { eq("id", userId) } }
        }.onSuccess {
            _events.emit(ProfileEvent.UpdateSuccess)
        }.onFailure { e ->
            _events.emit(ProfileEvent.UpdateError(e.friendlyMessage()))
        }
    }

    fun signOut() = viewModelScope.launch {
        client.auth.signOut()
        // SessionState observer akan auto-redirect ke LoginScreen
    }
}

data class ProfileUiState(
    val profile: Profile? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
)

sealed interface ProfileEvent {
    object UpdateSuccess : ProfileEvent
    data class UpdateError(val message: String) : ProfileEvent
}
```

ProfileScreen UI update: tambahkan tombol "Save" yang enabled hanya kalau `fullName` field ≠ profile.full_name. Tambah tombol "Logout" dengan confirmation dialog.

### Step 3: Verify dengan curl + build

#### Backend verify (Supabase)

```bash
# Cek trigger aktif
supabase db remote exec "SELECT tgname FROM pg_trigger WHERE tgname = 'on_auth_user_created';"

# Cek auth.users allow email signups
supabase db remote exec "SELECT value FROM auth.config WHERE parameter = 'SITE_URL';"
# (Optional) Disable email confirmation untuk testing cepat
# Via Dashboard: Auth → Providers → Email → "Confirm email" toggle OFF
```

#### Android build verify

```bash
export JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"  # Windows
# atau JAVA_HOME=/opt/android-studio/jbr/Contents/jre/Contents/Home  # macOS
./gradlew :app:assembleDebug --no-daemon
```

Kalau error: perbaiki, jangan commit, tulis di CHANGELOG dengan diagnosis.

### Step 4: Update CHANGELOG

Format entry:

```markdown
### [YYYY-MM-DD HH:MM] T-007 | agent: <id> | FR: FR-001, FR-002, FR-003
**Goal**: Wire auth flow end-to-end (sign-up, sign-in, sign-out, profile update)
**Files changed**: ...
**Acceptance**:
- [✅] Session observer + MainActivity navigation
- [✅] Email confirmation handler di LoginViewModel
- [✅] ProfileViewModel + ProfileScreen update wiring
- [✅] Logout button dengan confirmation dialog
- [✅] Build sukses (`./gradlew :app:assembleDebug`)
- [⚠️] Runtime E2E test deferred (perlu APK install + emulator/device — orchestrator handle)
**Build**: ✅ sukses | ❌ <error>
**Risks/Notes**: ...
**Next blocker**: ...
```

Juga update **Master Status** di CHANGELOG.md bagian atas:
- FR-001 🟡 → 🟢
- FR-002 🟡 → 🟢
- FR-003 🟡 → 🟢

---

## 🧪 Acceptance Criteria

- [ ] SessionState observer observe `auth.sessionStatus` (Loading/SignedIn/SignedOut)
- [ ] MainActivity route ke Splash (Loading), Home (SignedIn), Login (SignedOut)
- [ ] LoginViewModel handle `Email not confirmed` exception dengan pesan jelas
- [ ] ProfileScreen bisa update `full_name` + `date_of_birth` ke tabel `profiles`
- [ ] ProfileScreen ada tombol "Logout" dengan confirmation dialog
- [ ] Logout call `signOut()`, SessionState observer auto-redirect ke LoginScreen
- [ ] Trigger `on_auth_user_created` aktif di remote DB (verified via SQL query)
- [ ] `README` update: cara disable email confirmation untuk testing cepat
- [ ] Build sukses: `./gradlew :app:assembleDebug`
- [ ] CHANGELOG entry ditulis + Master Status updated
- [ ] Commit message conventional: `feat(auth): wire session observer + profile update (FR-001, 002, 003)`

---

## ⚠️ Gotchas

1. **JAVA_HOME** — wajib di-set sebelum `./gradlew`. Windows: `set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr`. macOS/Linux: `export JAVA_HOME=$(/usr/libexec/java_home)`.
2. **Gradle daemon** — kalau error aneh, tambahkan `--no-daemon`.
3. **Email confirmation ON** = default Supabase. Untuk testing lokal cepat: disable via Dashboard, atau cek inbox email testing (kalau pakai Mailtrap / inbucket).
4. **Hilt vs manual DI** — cek apakah project sudah pakai Hilt (`@HiltAndroidApp`, `@Inject`). Kalau iya, pakai pattern itu. Kalau belum, instantiate manual di MainActivity.
5. **Session observer lifecycle** — kalau pakai `viewModelScope`, akan auto-cleanup saat Activity destroy. Tapi kalau observe di composable, pastikan pakai `collectAsStateWithLifecycle()`.
6. **Migration 001** — `extend profiles` mungkin menambah kolom `full_name` dll. Cek isi migration untuk tahu kolom apa saja yang tersedia.
7. **EmailConfirmationException** — beda library beda nama exception. Supabase auth kotlin terbaru mungkin return `RestException` dengan message mengandung "Email not confirmed", bukan exception khusus. Pakai string matching di friendly mapper.

---

## 📤 Output

Buat **2 commit atomic**:
1. `feat(auth): wire session observer + navigation routing (FR-001, FR-002)`
2. `feat(profile): wire profile update + logout dialog (FR-003)`

Plus commit docs:
3. `docs(changelog): T-007 entry + FR-001/002/003 status → 🟢`

Kalau ada perubahan `supabase/README.md`, pisah jadi commit `docs(supabase): disable email confirmation note`.

---

## 🛑 Stop Conditions

Tulis di CHANGELOG dengan `❌` lalu **jangan commit code**, kalau:
- Build gagal dan error tidak bisa fix dalam 30 menit
- SessionState API beda dari yang di docs (cek Context7 lagi)
- ProfileScreen refactor terlalu besar (>500 baris changes) → escalate ke orchestrator untuk split jadi T-007a (auth) + T-007b (profile edit)
- Trigger `on_auth_user_created` TIDAK ADA di remote DB → tulis blocker, jangan migrate manual (orchestrator decide)

---

## 🎯 Definition of Done

- ✅ 3 FR (FR-001, FR-002, FR-003) berubah status dari 🟡 → 🟢
- ✅ Total **4 commit** (atau 3 + 1 docs supabase) di-push
- ✅ Build sukses
- ✅ CHANGELOG entry lengkap + Master Status updated
- ✅ Runtime E2E test DI-DEFER (orchestrator handle di fase post-integration)
- ✅ Tidak ada file di luar Scope yang ter-edit

---

## 📞 Reporting

Setelah selesai, paste ke orchestrator (Pi sesi saat ini):
```
T-007 DONE
- Files: <list>
- Commits: <hash1>, <hash2>, <hash3>
- FR-001: 🟡 → 🟢
- FR-002: 🟡 → 🟢
- FR-003: 🟡 → 🟢
- Build: ✅/❌
- E2E runtime test: deferred (per orchestrator plan)
- Notes: <any>
```
