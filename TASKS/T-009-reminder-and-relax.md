# T-009 — Reminder Scheduling + Relaxation Audio (FR-016 + FR-017)

## 🎯 Goal

Wire **2 fitur** supaya user bisa:
1. **FR-016** — Pasang pengingat waktu tidur (notification fired tiap malam)
2. **FR-017** — Putar audio relaksasi di RelaxScreen

Setelah T-009 → **14/17 ✅ hijau**, tinggal 3 🟡 (semua runtime E2E test phase).

---

## 📖 Context

### State existing (sudah di-scout)

| FR | Komponen | Status | File |
|---|---|---|---|
| FR-016 | `BedtimeNotificationHelper` | ✅ Real impl (AlarmManager + NotificationManager) | `features/reminder/BedtimeNotificationHelper.kt` |
| FR-016 | `BedtimeNotificationReceiver` | ✅ Real impl (handle alarm trigger) | `features/reminder/BedtimeNotificationReceiver.kt` |
| FR-016 | Manifest receiver registration | ✅ `exported=false`, declared | `AndroidManifest.xml` line 38-39 |
| FR-016 | `ReminderScreen` UI | ⚠️ Ada, perlu verifikasi wiring | `features/reminder/presentation/screen/ReminderScreen.kt` |
| FR-016 | Permissions | ✅ `POST_NOTIFICATIONS` + `RECEIVE_BOOT_COMPLETED` declared | `AndroidManifest.xml` |
| FR-017 | `RelaxViewModel` | ❌ **STUB ONLY** (34 baris, `getDummyMediaItems`, `onPlayClicked` no-op) | `features/relaxation/presentation/viewmodel/RelaxViewModel.kt` |
| FR-017 | `RelaxScreen` UI | ⚠️ Ada, perlu wiring ke playback real | `features/relaxation/presentation/screen/RelaxScreen.kt` |
| FR-017 | `AdvancedRelaxationScreen` | ⚠️ Ada, parallel screen | `features/relaxation/presentation/screen/AdvancedRelaxationScreen.kt` |
| FR-017 | **Audio dependency** | ❌ **TIDAK ADA** (no media3/exoplayer/MediaPlayer) | `gradle/libs.versions.toml` |
| FR-017 | **Audio assets** | ❌ **TIDAK ADA** (no `res/raw/`, no `assets/`) | filesystem |

### Apa yang HARUS dibuat / diubah

**FR-016 (Reminder):**
1. ReminderScreen: time picker UI (jam + menit)
2. Save ke `SharedPreferences` / `DataStore` (preferensi lokal, tidak perlu DB)
3. Panggil `BedtimeNotificationHelper.scheduleBedtimeNotification(context, hour, minute)`
4. Permission request flow untuk `POST_NOTIFICATIONS` (API 33+ / Android 13+)
5. BOOT_COMPLETED receiver untuk reschedule setelah reboot (penting!)

**FR-017 (Relax):**
1. **Tambah dependency** `androidx.media3:media3-exoplayer` di `libs.versions.toml` + `app/build.gradle.kts`
2. **Tambah audio assets** — LIHAT "Audio Source" di bawah untuk pilihan
3. Refactor `RelaxViewModel` untuk benar-benar play/pause/seek dengan ExoPlayer
4. Lifecycle handling (pause saat app background)
5. UI: tombol play/pause + progress bar

---

## 📚 Read First (WAJIB)

1. `features/reminder/BedtimeNotificationHelper.kt` — schedule API sudah lengkap
2. `features/reminder/BedtimeNotificationReceiver.kt` — receiver callback
3. `features/reminder/presentation/screen/ReminderScreen.kt` — current UI state
4. `features/relaxation/presentation/viewmodel/RelaxViewModel.kt` — STUB state
5. `features/relaxation/presentation/screen/RelaxScreen.kt` — current UI
6. `features/relaxation/presentation/state/RelaxUiState.kt` — state model
7. `app/src/main/AndroidManifest.xml` — current permissions
8. `gradle/libs.versions.toml` — current dependencies

**Wajib Context7** (training data out-of-date):
- `resolve-library-id "androidx media3 exoplayer"` → query "media3 exoplayer kotlin basic playback"
- `resolve-library-id "androidx datastore"` → query "datastore preferences kotlin basic read write"
- `resolve-library-id "accompanist permissions"` → query "accompanist permissions requestPOST_NOTIFICATIONS"

---

## ✅ Scope

### A. FR-016 — Reminder Scheduling

**A1. ReminderViewModel (BARU kalau belum ada):**
- `features/reminder/presentation/viewmodel/ReminderViewModel.kt`
- Method: `load()`, `setReminderTime(hour, minute)`, `cancelReminder()`
- Persistence: `DataStore<Preferences>` (preferred) atau `SharedPreferences` (fallback)

**A2. Wire `ReminderScreen.kt`:**
- TimePicker Material3 (atau 2 NumberPicker untuk hour/minute)
- Tombol "Simpan Pengingat" → call `viewModel.setReminderTime(...)`
- Tombol "Matikan Pengingat" → call `viewModel.cancelReminder()` (hilang kalau belum ada reminder)
- Permission request flow untuk `POST_NOTIFICATIONS` (API 33+):
  - Pakai `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`
  - Request permission sebelum simpan reminder
  - Kalau ditolak → tampilkan snackbar "Izin notifikasi diperlukan untuk pengingat"
- Display current state: "Pengingat aktif: 22:00" / "Pengingat nonaktif"

**A3. Reschedule on boot:**
- Buat receiver baru: `BootCompletedReceiver` extends `BroadcastReceiver`
- Listen `BOOT_COMPLETED` action
- Baca hour/minute dari DataStore → call `BedtimeNotificationHelper.scheduleBedtimeNotification(...)`
- Register di AndroidManifest:
  ```xml
  <receiver
      android:name=".features.reminder.BootCompletedReceiver"
      android:exported="true">
      <intent-filter>
          <action android:name="android.intent.action.BOOT_COMPLETED" />
          <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
      </intent-filter>
  </receiver>
  ```

**A4. CHANGELOG entry** — Master Status FR-016 🟡 → 🟢

### B. FR-017 — Relaxation Audio Playback

**B1. Tambah dependency media3:**

Di `gradle/libs.versions.toml`:
```toml
[versions]
media3 = "1.4.1"  # cek versi terbaru via Context7

[libraries]
androidx-media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
androidx-media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
androidx-media3-common = { group = "androidx.media3", name = "media3-common", version.ref = "media3" }
```

Di `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)
}
```

**B2. Audio source — pilih salah satu (rekomendasi: B):**

| Opsi | Audio Source | Pros | Cons |
|---|---|---|---|
| A | Bundle 2-3 small MP3 di `res/raw/` (≤ 2MB each, royalty-free) | Offline, no network | Legal risk (cek lisensi!), APK size +6MB |
| **B** | **Public URL dari sound CDN (mis. mixkit.co / pixabay)**, fetch runtime | No APK size impact, easy update | Network needed, latency first load |
| C | Procedurally-generated tone (sine wave 432Hz / white noise) via AudioTrack | Zero deps, zero assets | Bukan "musik", monoton |

**Rekomendasi: Opsi B** dengan URL public dari [mixkit.co/free-sound-effects/relax](https://mixkit.co/free-sound-effects/relax/) atau [pixabay.com/sound-effects/search/relax](https://pixabay.com/sound-effects/search/relax/) — keduanya gratis tanpa login untuk personal/educational use. Simpan URL di `RelaxMediaItem` data class, fetch via ExoPlayer.

**B3. Refactor `RelaxViewModel`:**

```kotlin
class RelaxViewModel : ViewModel() {
    private var exoPlayer: ExoPlayer? = null

    private val _uiState = MutableStateFlow(RelaxUiState())
    val uiState: StateFlow<RelaxUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RelaxEvent>()
    val events: SharedFlow<RelaxEvent> = _events.asSharedFlow()

    init {
        loadMediaItems()
    }

    private fun loadMediaItems() {
        // List of (id, title, duration, url) from public CDN
        val items = listOf(
            RelaxMediaItem("ocean_waves", "Ocean Waves", "10:00", "https://assets.mixkit.co/active_storage/sfx/2515/2515-preview.mp3"),
            RelaxMediaItem("rain_ambient", "Rain Ambient", "08:30", "https://assets.mixkit.co/active_storage/sfx/2394/2394-preview.mp3"),
            RelaxMediaItem("forest_birds", "Forest Birds", "12:00", "https://assets.mixkit.co/active_storage/sfx/2434/2434-preview.mp3"),
        )
        _uiState.update { it.copy(mediaItems = items, isLoading = false) }
    }

    fun onPlayClicked(item: RelaxMediaItem) {
        if (_uiState.value.currentItem?.id == item.id && exoPlayer?.isPlaying == true) {
            // Toggle pause
            exoPlayer?.pause()
            _uiState.update { it.copy(isPlaying = false) }
        } else {
            // Start new playback
            exoPlayer?.release()
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(item.url))
                prepare()
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            _uiState.update { it.copy(currentItem = item, isPlaying = true) }
                        }
                    }
                })
            }
        }
    }

    fun onPauseClicked() {
        exoPlayer?.pause()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun onSeekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    override fun onCleared() {
        exoPlayer?.release()
        exoPlayer = null
        super.onCleared()
    }
}
```

**Catatan:** kalau tidak ada `Context` di ViewModel (kalau project pakai Hilt tanpa Activity context), pakai `Application` via `AndroidViewModel`. Lihat pola ViewModel lain di project (HomeViewModel, LifestyleViewModel) untuk konsistensi.

**B4. Wire `RelaxScreen.kt`:**
- Pakai `state.mediaItems` (bukan hardcoded list)
- Tombol play → `viewModel.onPlayClicked(item)`
- Player controller di bawah (Material3 + media3-ui `PlayerView` atau custom Compose dengan progress bar)
- Lifecycle: `DisposableEffect` untuk pause saat `LocalLifecycleOwner` di `ON_PAUSE`

**B5. Permission untuk `INTERNET`:**
- Sudah ada di manifest ✅ (no change needed)

**B6. CHANGELOG entry** — Master Status FR-017 🟡 → 🟢

---

## ❌ DON'T Touch

- ❌ File di luar `features/reminder/` + `features/relaxation/` + `gradle/libs.versions.toml` + `app/build.gradle.kts` + `AndroidManifest.xml` + `CHANGELOG.md`
- ❌ File di `features/{authentication,profile,home,ikigai,journal,lifestyle,sleep,mood,statistics,achievements,notification,settings}/`
- ❌ `BedtimeNotificationHelper.kt` (sudah lengkap)
- ❌ `BedtimeNotificationReceiver.kt` (sudah lengkap)
- ❌ Supabase migrations / Edge Functions (T-009 purely client-side, tidak pakai AI)
- ❌ Backend (T-009 tidak butuh DB changes)
- ❌ **JANGAN upload audio copyright-protected ke `res/raw/`** — pakai URL public domain atau non-bundled

---

## 🛠️ Implementation Steps

### Step 1: Diagnosa state awal (5 menit)

```bash
# Cek permission dependencies
grep -E "(accompanist|media3)" gradle/libs.versions.toml app/build.gradle.kts
# Cek ada audio assets
ls app/src/main/res/raw/ 2>/dev/null
ls app/src/main/assets/ 2>/dev/null
# Cek existing ReminderViewModel
find features/reminder -name "ReminderViewModel*"
# Cek pattern VM lain untuk Hilt vs manual DI
grep -E "(AndroidViewModel|@HiltViewModel)" features/*/presentation/viewmodel/*.kt | head -10
```

### Step 2: Implementasi FR-016 (Reminder)

Mulai dari FR-016 karena lebih cepat:

1. Cek apakah `ReminderViewModel` sudah ada. Kalau belum, buat dengan DataStore.
2. Wire `ReminderScreen` ke ViewModel — TimePicker + tombol Simpan/Matikan.
3. Tambah permission launcher untuk `POST_NOTIFICATIONS`.
4. Buat `BootCompletedReceiver.kt`.
5. Register receiver di `AndroidManifest.xml`.
6. Update `ReminderViewModel.schedule()` panggil `BedtimeNotificationHelper.scheduleBedtimeNotification(...)` dengan intent PendingIntent → `BedtimeNotificationReceiver`.

### Step 3: Implementasi FR-017 (Relax)

1. Tambah media3 dependency di `libs.versions.toml` + `app/build.gradle.kts`.
2. Update `RelaxViewModel` dengan ExoPlayer (template di B3 atas).
3. Update `RelaxScreen` pakai `state.mediaItems` + lifecycle handling.
4. Verify URL audio accessible (HEAD request sebelum commit).

### Step 4: Build verify

```bash
export JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
./gradlew :app:assembleDebug --no-daemon
```

### Step 5: Update CHANGELOG

Format entry:

```markdown
### [YYYY-MM-DD HH:MM] T-009 | agent: <id> | FR: FR-016, FR-017
**Goal**: Wire reminder scheduling (AlarmManager → Notification) + relaxation audio playback (ExoPlayer)
**Files changed**: ...
**Acceptance**:
- [✅] Reminder: TimePicker UI + DataStore persist + scheduleBedtimeNotification wired
- [✅] Reminder: Permission flow POST_NOTIFICATIONS (API 33+)
- [✅] Reminder: BootCompletedReceiver untuk reschedule
- [✅] Relax: media3 dependency ditambah
- [✅] Relax: ExoPlayer + lifecycle handling
- [✅] Relax: RelaxScreen wired ke ViewModel
- [✅] Build sukses
- [⚠️] Runtime E2E test deferred (audio playback + notification fire butuh APK install)
**Build**: ✅ sukses | ❌ <error>
**Risks/Notes**: ...
**Next blocker**: ...
```

Update **Master Status**:
- FR-016 🟡 → 🟢
- FR-017 🟡 → 🟢

---

## 🧪 Acceptance Criteria

- [ ] ReminderScreen punya TimePicker UI (Material3)
- [ ] User bisa pilih jam + menit → Simpan → notification scheduled
- [ ] DataStore persist reminder time (survive app restart)
- [ ] Permission `POST_NOTIFICATIONS` diminta dengan benar untuk API 33+
- [ ] BootCompletedReceiver reschedule reminder setelah reboot
- [ ] RelaxScreen pakai `state.mediaItems` (bukan hardcoded)
- [ ] Tombol play di RelaxScreen trigger ExoPlayer playback
- [ ] Pause/play toggle works
- [ ] Audio pause otomatis saat app ke background (lifecycle)
- [ ] ExoPlayer.release() di onCleared() (no leak)
- [ ] Audio URL accessible (HEAD 200 OK)
- [ ] Build sukses: `./gradlew :app:assembleDebug`
- [ ] CHANGELOG entry + Master Status updated
- [ ] Commit messages conventional

---

## ⚠️ Gotchas

1. **ExoPlayer butuh Context** — kalau ViewModel constructor tidak punya Application context, pakai `AndroidViewModel` atau inject via Hilt (`@HiltViewModel` + `@ApplicationContext`).
2. **POST_NOTIFICATIONS permission** — hanya untuk API 33+ (Android 13). Untuk API < 33, permission auto-granted, tapi tetep perlu check `Build.VERSION.SDK_INT >= 33` sebelum request.
3. **Audio URL availability** — test dengan `curl -I <url>` dulu sebelum commit. Kalau URL 404 / private, ganti.
4. **AlarmManager precision** — `setInexactRepeating` hemat baterai tapi bisa delay ±10 menit. `setExactAndAllowWhileIdle` lebih tepat tapi perlu permission `SCHEDULE_EXACT_ALARM` (API 31+) → user harus grant manual di Settings. Untuk bedtime reminder, **inexact acceptable** (target ~22:00, fired 21:50-22:10 OK).
5. **BootCompletedReceiver exported=true** — required untuk `BOOT_COMPLETED` dari system. Tambahkan `<intent-filter>` di manifest.
6. **ExoPlayer `release()` di onCleared()** — kalau tidak, leak media codec. Pastikan pattern ada di template B3.
7. **Media3 vs MediaPlayer** — MediaPlayer API lama, banyak bug. Pakai media3-exoplayer (recommended).
8. **DATABASE migration tidak perlu** — Reminder time pakai DataStore (lokal), bukan DB.
9. **No background service needed** — AlarmManager + BroadcastReceiver cukup untuk "fire notification once". Tidak perlu WorkManager / ForegroundService.
10. **Jangan upload audio copyright-protected** — legal risk. Pakai public CDN URLs saja.

---

## 📤 Output

Buat **3 commit atomic**:
1. `feat(reminder): wire TimePicker UI + DataStore persist + boot reschedule (FR-016)`
2. `feat(relax): add media3 dependency + ExoPlayer playback lifecycle (FR-017)`
3. `docs(changelog): T-009 entry + FR-016/017 status → 🟢`

Kalau ada perubahan `AndroidManifest.xml` (untuk receiver baru), gabung ke commit #1.

---

## 🛑 Stop Conditions

Tulis di CHANGELOG dengan `❌` lalu **jangan commit code**, kalau:
- Build gagal dan error tidak bisa fix dalam 30 menit
- media3 dependency conflict dengan Compose / Kotlin version
- Audio URL test gagal (ganti URL atau escalate ke user)
- BootCompletedReceiver test gagal (cek kembali manifest + receiver pattern)
- `RelaxScreen` refactor terlalu besar (>500 baris) → escalate untuk split T-009a (reminder) + T-009b (relax)

---

## 🎯 Definition of Done

- ✅ FR-016 🟡 → 🟢 (reminder scheduling works end-to-end, persist + boot reschedule)
- ✅ FR-017 🟡 → 🟢 (audio playback works di RelaxScreen)
- ✅ Total **3 commit** di-push
- ✅ Build sukses
- ✅ CHANGELOG entry lengkap + Master Status updated
- ✅ Runtime E2E test DI-DEFER (orchestrator handle post-integration phase)
- ✅ Tidak ada file di luar Scope yang ter-edit
- ✅ Tidak ada audio copyright-protected

---

## 📞 Reporting

Setelah selesai, paste ke orchestrator (Pi sesi saat ini):
```
T-009 DONE
- Files: <list>
- Commits: <hash1>, <hash2>, <hash3>
- FR-016: 🟡 → 🟢
- FR-017: 🟡 → 🟢
- Build: ✅/❌
- Audio source: URL / bundled / procedural
- Notes: <any>
```
