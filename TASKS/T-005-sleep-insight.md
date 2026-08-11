# T-005 — Sleep Insight Recommendation (FR-014)

**Priority**: 🟢 Sedang (setelah T-003 sukses — pakai pattern Gemini yang sama)
**Estimated effort**: 60–90 menit
**FR terkait**: FR-014 (rekomendasi aktivitas/makanan dari sleep)
**Dependencies**: T-003 (pattern Gemini call sudah established)
**Blocks**: none

---

## 🎯 Goal

Generate rekomendasi personal berdasarkan riwayat `sleep_logs` (7–30 hari terakhir) via Edge Function Gemini. Tampilkan di `LifestyleScreen` atau widget Home (sesuai T-004).

---

## 📖 Context

- FR-014 spec: "Berdasarkan riwayat sleep_logs, sistem menampilkan Sleep Insight berupa rekomendasi aktivitas, makanan, dan musik relaksasi."
- Pattern Gemini call sudah ada dari T-003 (`chat-gemini`) dan `generate-ikigai-report`. T-005 tinggal buat edge function khusus + UI display.
- LifestyleScreen ada (`features/lifestyle/presentation/screen/LifestyleScreen.kt`) — cek apakah sudah dipakai atau masih placeholder.

---

## 📚 Read First

1. `CHANGELOG.md` — entry T-003 harus sudah ada (pattern Gemini confirmed)
2. `supabase/functions/chat-gemini/index.ts` — copy structure
3. `supabase/functions/generate-ikigai-report/index.ts` — lihat return pattern (kalau JSON parse, pertimbangkan)
4. `app/src/main/java/com/example/features/lifestyle/presentation/screen/LifestyleScreen.kt`
5. `app/src/main/java/com/example/features/sleep/data/repository/SleepRepository.kt` — method get logs

---

## ✅ Scope

- `supabase/functions/generate-sleep-insight/index.ts` (**baru**)
- `supabase/migrations/005_sleep_insights.sql` (**baru** — tabel untuk cache insight)
- `app/src/main/java/com/example/features/lifestyle/**`
- `app/src/main/java/com/example/features/sleep/data/repository/SleepRepository.kt` — tambah `getRecentSleepLogs(days: Int)`
- `CHANGELOG.md`

## ❌ DON'T Touch

- File Ikigai, Mood, Home (sesuai T-001..T-004)
- File `core/**`
- File Journal (T-003)

---

## 🛠️ Implementation Steps

### Step 1: Schema migration

`supabase/migrations/005_sleep_insights.sql`:

```sql
CREATE TABLE IF NOT EXISTS public.sleep_insights (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  period_days INT NOT NULL,
  recommendations JSONB NOT NULL,  -- { activities: [...], foods: [...], music: [...] }
  summary TEXT,
  generated_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.sleep_insights ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users manage own insights" ON public.sleep_insights
  FOR ALL USING (auth.uid() = user_id);

CREATE INDEX idx_sleep_insights_user ON public.sleep_insights(user_id, generated_at DESC);
```

### Step 2: Edge Function `generate-sleep-insight`

Input: `userId` (dari auth), `periodDays` (default 7).
Logic:
1. Query `sleep_logs` last N days for user.
2. Build prompt: "Berdasarkan data tidur ini: [summary stats + last N entries], berikan rekomendasi aktivitas, makanan, dan musik relaksasi dalam format JSON."
3. Call Gemini, parse response.
4. Save to `sleep_insights` table.
5. Return JSON ke client.

### Step 3: Repository method

`SleepRepository.getRecentSleepLogs(days: Int): Result<List<SleepLog>>` (kalau belum ada).

### Step 4: LifestyleScreen wire

- Tombol "Generate Insight" → call Edge Function
- Loading state
- Display hasil: 3 section (Aktivitas, Makanan, Musik)
- Refresh button

### Step 5: Build & commit

```bash
./gradlew assembleDebug
git add app/src/main/java/com/example/features/lifestyle/ \
        app/src/main/java/com/example/features/sleep/data/repository/SleepRepository.kt \
        supabase/functions/generate-sleep-insight/ \
        supabase/migrations/005_sleep_insights.sql
git commit -m "feat(sleep-insight): Gemini-powered recommendations from sleep history (FR-014)

- New Edge Function generate-sleep-insight: query sleep_logs → Gemini → save to sleep_insights
- Migration 005: sleep_insights table with RLS
- SleepRepository.getRecentSleepLogs(days)
- LifestyleScreen: generate button + 3-section display (activities/foods/music)"
```

### Step 6: Update CHANGELOG

- FR-014: 🔴 → 🟢 (kalau end-to-end sukses) atau 🟡 (kalau wiring done tapi test butuh GEMINI_API_KEY)

---

## ✔️ Acceptance

- [ ] Build sukses
- [ ] Edge function `generate-sleep-insight/index.ts` ada
- [ ] Migration `005_sleep_insights.sql` ada
- [ ] LifestyleScreen bisa trigger generate + tampil hasil
- [ ] Commit + CHANGELOG entry

---

## 📝 Reporting (sama format T-001)
