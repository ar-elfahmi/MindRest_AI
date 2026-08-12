-- =============================================================================
-- Migration 005 — Tabel sleep_insights (FR-014 / T-005)
-- =============================================================================
-- Tujuan: Cache rekomendasi personal (activities / foods / music) hasil
--         Edge Function `generate-sleep-insight` sehingga user tidak perlu
--         regenerate setiap buka screen (mirip pola ikigai_reports).
--
-- Catatan arsitektur:
--   - INSERT hanya via Edge Function (service role) → user tidak bisa
--     inject insight palsu.
--   - User SELECT own rows via RLS.
--   - Tidak ada UPDATE / DELETE dari client (insight immutable per versi).
--     Regenerasi selalu INSERT row baru dengan generated_at baru.
--
-- Kolom:
--   id              UUID PK
--   user_id         FK auth.users (cascade)
--   period_days     INT  — window analisis (default 7, max 30)
--   recommendations JSONB — { activities:[...], foods:[...], music:[...] }
--                          masing-masing array of { id, text }
--   summary         TEXT  — 1-2 kalimat ringkasan (untuk preview HomeScreen)
--   generated_at    TIMESTAMPTZ
-- =============================================================================


-- ---------------------------------------------------------------------------
-- 1. Tabel sleep_insights
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sleep_insights (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  period_days INT NOT NULL DEFAULT 7 CHECK (period_days BETWEEN 1 AND 30),
  recommendations JSONB NOT NULL,
  summary TEXT,
  generated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- ---------------------------------------------------------------------------
-- 2. Indexes
-- ---------------------------------------------------------------------------
-- Lookup "latest insight for user" → pakai composite index (user_id, generated_at DESC).
CREATE INDEX IF NOT EXISTS idx_sleep_insights_user
  ON sleep_insights(user_id, generated_at DESC);


-- ---------------------------------------------------------------------------
-- 3. Row Level Security
-- ---------------------------------------------------------------------------
ALTER TABLE sleep_insights ENABLE ROW LEVEL SECURITY;

-- User SELECT own rows.
-- INSERT hanya dari Edge Function (service role bypasses RLS) sehingga user
-- tidak bisa insert insight palsu — sama dengan pola ikigai_reports.
DROP POLICY IF EXISTS "Users can read own sleep insights" ON sleep_insights;
CREATE POLICY "Users can read own sleep insights"
  ON sleep_insights
  FOR SELECT
  USING (auth.uid() = user_id);


-- ---------------------------------------------------------------------------
-- 4. Verifikasi cepat (opsional)
-- ---------------------------------------------------------------------------
-- SELECT tablename, rowsecurity FROM pg_tables
--   WHERE schemaname = 'public' AND tablename = 'sleep_insights';
--
-- SELECT policyname, cmd, qual FROM pg_policies
--   WHERE schemaname = 'public' AND tablename = 'sleep_insights';


-- =============================================================================
-- Selesai. Tabel sleep_insights siap dipakai oleh Edge Function
-- generate-sleep-insight (T-005).
-- =============================================================================
