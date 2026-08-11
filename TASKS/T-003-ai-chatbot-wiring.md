# T-003 — AI Chatbot Wiring (FR-009, FR-011)

**Priority**: 🟠 Tinggi — critical path untuk FR-014 (sleep insight)
**Estimated effort**: 60–90 menit
**FR terkait**: FR-009 (journaling via chatbot), FR-011 (olah data jurnal)
**Dependencies**: T-001 (Edge Function Ikigai sudah committed, schema rapi)
**Blocks**: T-005 (Sleep Insight butuh Gemini pattern yang sama)

---

## 🎯 Goal

Wire `AiJournalScreen` agar benar-benar memanggil Edge Function Gemini untuk menghasilkan respons AI (bukan mock/hardcode), dan persist conversation ke `journal_entries`.

---

## 📖 Context

- Edge function `test-gemini` dan `generate-ikigai-report` sudah ada di `supabase/functions/`. **Test-gemini paling cocok dipake di sini** (general-purpose Gemini call).
- `journal_entries` table ada dengan kolom `content` saja (per schema.sql). Untuk conversation history perlu extend schema: tambah `role` (user/assistant) dan `session_id`.
- `AiJournalScreen` punya UI siap (per `JournalPlaceholders`), tapi belum ada call ke Edge Function.

**PENTING — user perlu isi `GEMINI_API_KEY` di `.env` SEBELUM task ini**. Kalau belum, agent akan error runtime saat testing. Agent harus tulis ini sebagai blocker di CHANGELOG dan tidak commit kode yang tidak dites.

---

## 📚 Read First (urutan)

1. `CHANGELOG.md` — entry T-001 + T-002 harus sudah ada
2. `app/src/main/java/com/example/features/journal/presentation/screen/AiJournalScreen.kt`
3. `app/src/main/java/com/example/features/journal/presentation/viewmodel/JournalViewModel.kt`
4. `app/src/main/java/com/example/features/journal/data/repository/JournalRepository.kt`
5. `app/src/main/java/com/example/features/journal/presentation/state/JournalUiState.kt`
6. `supabase/functions/test-gemini/index.ts` — lihat pattern pemanggilan Gemini
7. `supabase/functions/generate-ikigai-report/index.ts` — lihat pattern response handling

---

## ✅ Scope (boleh diedit/dibuat)

- `app/src/main/java/com/example/features/journal/**` (semua file journal)
- `supabase/functions/chat-gemini/index.ts` (**buat baru** — Edge Function khusus chatbot)
- `supabase/migrations/004_journal_conversation.sql` (**buat baru** — extend `journal_entries` dengan `role` + `session_id`)
- `core/network/SupabaseClient.kt` — **hanya** kalau perlu tambah helper call Edge Function
- `CHANGELOG.md`

## ❌ DON'T Touch

- File Ikigai, Sleep, Mood (sesuai T-001/T-002)
- `core/navigation/Screen.kt` — route AI Journal sudah ada
- File `core/base/**`, `core/designsystem/**`
- `.env`, `.env.example` — **JANGAN tulis API key langsung**. Pakai env var di Supabase dashboard.

---

## 🛠️ Implementation Steps

### Step 1: Schema migration

Buat `supabase/migrations/004_journal_conversation.sql`:

```sql
-- Extend journal_entries untuk conversation history
ALTER TABLE public.journal_entries
  ADD COLUMN IF NOT EXISTS session_id UUID,
  ADD COLUMN IF NOT EXISTS role TEXT CHECK (role IN ('user', 'assistant')),
  ADD COLUMN IF NOT EXISTS parent_id UUID REFERENCES public.journal_entries(id);

-- Index untuk query by session
CREATE INDEX IF NOT EXISTS idx_journal_session
  ON public.journal_entries(user_id, session_id, created_at);

-- Backfill: baris lama tanpa role dianggap 'entry' (full journal)
-- (kalau ada data existing, biarkan role NULL untuk backward compat)
```

### Step 2: Edge Function `chat-gemini`

Buat `supabase/functions/chat-gemini/index.ts` — pattern: ambil conversation history dari `journal_entries` by `session_id`, kirim ke Gemini dengan system prompt CBT-style, return AI response.

Referensi: copy structure dari `supabase/functions/test-gemini/index.ts` dan `generate-ikigai-report/index.ts`.

### Step 3: Repository method

Tambah di `JournalRepository.kt`:
```kotlin
suspend fun callChatGemini(sessionId: String, userMessage: String): Result<String>
suspend fun saveJournalEntry(entry: JournalEntry): Result<Unit>
suspend fun getConversationHistory(sessionId: String): Result<List<JournalEntry>>
```

### Step 4: ViewModel wire

`JournalViewModel`:
- `sendMessage(text: String)` — panggil `callChatGemini`, save user message, save AI response, update state
- `loadHistory(sessionId)` — panggil `getConversationHistory`
- `startNewSession()` — generate UUID baru

### Step 5: UI binding

`AiJournalScreen`:
- Input field → ViewModel.sendMessage
- Loading indicator saat menunggu response
- Tampilkan conversation history (bukan `messages by remember {...}` mock)
- Error state kalau Gemini call gagal

### Step 6: Build & test manual

```bash
./gradlew assembleDebug
# Manual test (idealnya pakai emulator):
# 1. Login → buka AI Journal
# 2. Kirim pesan → pastikan response AI muncul
# 3. Cek journal_entries di Supabase dashboard: ada 2 row baru (user + assistant)
```

**Catatan**: kalau `GEMINI_API_KEY` di `.env` belum diisi, **test manual tidak bisa**. Agent tulis ini sebagai blocker.

### Step 7: Commit

```bash
git add app/src/main/java/com/example/features/journal/ \
        supabase/functions/chat-gemini/ \
        supabase/migrations/004_journal_conversation.sql
git commit -m "feat(journal): wire AI chatbot to Gemini Edge Function (FR-009, FR-011)

- New Edge Function chat-gemini: conversation history → Gemini → response
- Migration 004: extend journal_entries with session_id, role, parent_id
- JournalRepository: add callChatGemini, saveJournalEntry, getConversationHistory
- JournalViewModel: sendMessage flow with loading/error states
- AiJournalScreen: replace mock messages with real conversation

Requires: GEMINI_API_KEY set in Supabase Edge Function secrets
(see supabase/README.md)"
```

### Step 8: Update CHANGELOG

- FR-009: 🟡 → 🟢 (kalau end-to-end test sukses) atau 🟡 (kalau hanya wired tapi belum test runtime)
- FR-011: 🔴 → 🟡 (data flow jadi ada, insight extraction masuk T-005)

---

## ✔️ Acceptance

- [ ] Build sukses
- [ ] Edge function `chat-gemini/index.ts` ada
- [ ] Migration `004_journal_conversation.sql` ada
- [ ] Commit baru dengan message jelas
- [ ] CHANGELOG entry lengkap + status updated
- [ ] **Kalau GEMINI_API_KEY tersedia**: end-to-end test sukses (kirim pesan → dapat respons). **Kalau tidak**: tulis blockers di CHANGELOG

---

## 📝 Reporting (sama format T-001)

Tambah catatan spesifik:
- Apakah Edge Function dites via `supabase functions deploy chat-gemini` + curl test?
- Apakah user sudah isi `GEMINI_API_KEY`?
- Apakah `journal_entries` terverifikasi punya row baru setelah test?
