# UI Migration — Fase 2 (Token Migration 20 Screens)

> **Status source of truth untuk migrasi UI.** Fase 1 (foundation) sudah selesai.
> Fase 2 ini = migrasi mekanis 20 screen existing ke token + core components.

## Konteks singkat

- **Fase 1 (DONE):** design tokens (`Spacing`, `Typography`, `Color`, `Shape`,
  `Elevation`, `Motion`) + 9 core components + `DesignSystemShowcaseScreen`.
  Lihat commit/PR sebelumnya. **Tidak ada screen existing yang disentuh.**
- **Fase 2 (INI):** 20 feature screen, ~9350 LOC, ~560 hardcoded `dp`. Tiap
  screen = 1 tiket, kerjakan blockers-first, **clear context antar tiket**.

## Baca dulu

1. **[`MIGRATION_GUIDE.md`](./MIGRATION_GUIDE.md)** — resep 9 langkah, dp→token
   mapping table, acceptance criteria. **WAJIB sebelum mulai tiket apapun.**
2. [`issues/00-TEMPLATE.md`](./issues/00-TEMPLATE.md) — format tiket.
3. [`issues/01-home-screen.md`](./issues/01-home-screen.md) — **tracer bullet**
   (pilot). Kerjakan PERTAMA untuk validasi resep.

## Urutan eksekusi (6 wave)

Tiap wave = independen, tidak ada blocking antar-wave. Urutan = user-value +
complexity (pilot ringan dulu, heavy terakhir).

| Wave | Tema | Screen (LOC / dp) | Effort | Tiket |
|---|---|---|---|---|
| **1** | 🎯 Pilot | `HomeScreen` (1021 / 76) | XL ~3-5h | `01-home-screen` |
| **2** | Daily core loop | `ProfileScreen` (601/33), `MoodTrackingScreen` (278/21), `JournalHistoryScreen` (473/33), `JournalScreen` (88/6) | M ~4-6h | `02`-`05` |
| **3** | Sleep & relax | `SleepHubScreen` (382/23), `SleepTrackingScreen` (181/8), `RelaxScreen` (292/13), `AdvancedRelaxationScreen` (336/15) | M ~3-5h | `06`-`09` |
| **4** | Ikigai flow | `IkigaiDashboardScreen` (328/35), `IkigaiReportScreen` (747/51), `IkigaiAssessmentScreen` (323/19), `IkigaiReportLoadingScreen` (87/5) | L ~5-7h | `10`-`13` |
| **5** | Tracking & habits (HEAVY) | `StatisticsScreen` (772/49), `LifestyleScreen` (1167/78), `AchievementsScreen` (768/55), `ReminderScreen` (497/24) | XL ~8-12h | `14`-`17` |
| **6** | Secondary & chat | `AiJournalScreen` (422/42), `NotificationScreen` (192/9), `SettingsScreen` (266/12) | M ~3-4h | `18`-`20` |

**Total:** 20 tiket, ~26-38h effort. Selesaikan 1 wave per sesi (clear context antar tiket).

## Blocking edges

- **Foundation siap** ✅ (prasyarat semua tiket — done di fase 1).
- Antar-screen: **tidak ada blocking** (tiap screen independen).
- **Exception:** kalau screen butuh komponen baru yang belum ada di foundation →
  buat tiket ekstraksi dulu, jangan inline. Tandai di field `Blocking:` tiket itu.

## Status board

| Tiket | Screen | Status |
|---|---|---|
| 01 | HomeScreen | ⬜ todo |
| 02 | ProfileScreen | ⬜ todo |
| 03 | MoodTrackingScreen | ⬜ todo |
| 04 | JournalHistoryScreen | ⬜ todo |
| 05 | JournalScreen | ⬜ todo |
| 06 | SleepHubScreen | ⬜ todo |
| 07 | SleepTrackingScreen | ⬜ todo |
| 08 | RelaxScreen | ⬜ todo |
| 09 | AdvancedRelaxationScreen | ⬜ todo |
| 10 | IkigaiDashboardScreen | ⬜ todo |
| 11 | IkigaiReportScreen | ⬜ todo |
| 12 | IkigaiAssessmentScreen | ⬜ todo |
| 13 | IkigaiReportLoadingScreen | ⬜ todo |
| 14 | StatisticsScreen | ⬜ todo |
| 15 | LifestyleScreen | ⬜ todo |
| 16 | AchievementsScreen | ⬜ todo |
| 17 | ReminderScreen | ⬜ todo |
| 18 | AiJournalScreen | ⬜ todo |
| 19 | NotificationScreen | ⬜ todo |
| 20 | SettingsScreen | ⬜ todo |

Legend: ⬜ todo · 🟡 in-progress · ✅ done · ⛔ blocked

## Cara kerja (per tiket)

1. Pilih tiket dengan status `⬜ todo`, set ke `🟡 in-progress`.
2. **Baca `MIGRATION_GUIDE.md`** (resep) + tiket tersebut.
3. Ambil screenshot SEBELUM.
4. Eksekusi 9 langkah resep.
5. Build + screenshot SESUDAH + bandingkan.
6. Jalankan `reviewer` subagent untuk correctness + cleanup.
7. Set status `✅ done`, commit dengan pesan `refactor(ui): migrate <Screen> to design tokens`.
