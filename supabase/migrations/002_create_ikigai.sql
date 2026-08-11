-- =============================================================================
-- Migration 002 — Buat tabel Ikigai (assessments + reports)
-- =============================================================================
-- Tujuan: Mendukung fitur Ikigai discovery (MINGGU 2–3 ROADMAP).
--
-- Tabel baru:
--   ikigai_assessments
--     - 6 pertanyaan onboarding (Q1-Q4 text bebas, Q5 text overthinking,
--       Q6 skala 1-10)
--   ikigai_reports
--     - laporan AI terstruktur: markdown + 4 lingkaran + rekomendasi
--     - INSERT hanya via Edge Function (service role), user SELECT-only
--
-- Ringkasan RLS:
--   ikigai_assessments  — user CRUD own (full)
--   ikigai_reports       — user SELECT own only (read-only untuk client)
-- =============================================================================


-- ---------------------------------------------------------------------------
-- 1. Tabel ikigai_assessments
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ikigai_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  q1_passion TEXT,        -- "3 hal yang paling kamu nikmati"
  q2_skill TEXT,          -- "hal yang kamu jago / sering dipuji"
  q3_profession TEXT,     -- "pekerjaan / aktivitas utama"
  q4_mission TEXT,        -- "bentuk kontribusi ke dunia"
  q5_overthinking TEXT,   -- "paling sering overthinking soal"
  q6_satisfaction INT,    -- 1-10 skala kepuasan hidup
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- ---------------------------------------------------------------------------
-- 2. Tabel ikigai_reports
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ikigai_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  assessment_id UUID NOT NULL REFERENCES ikigai_assessments(id) ON DELETE CASCADE,
  report_markdown TEXT NOT NULL,         -- laporan text dari AI
  ikigai_circles JSONB NOT NULL,         -- {passion, skill, profession, mission}
  recommendations JSONB NOT NULL,        -- [{id, text, done}] array 3-5 item
  version INT NOT NULL DEFAULT 1,        -- 1=baseline, 2+=refreshed
  generated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- ---------------------------------------------------------------------------
-- 3. Indexes
-- ---------------------------------------------------------------------------
create index if not exists idx_ikigai_assessments_user
  on ikigai_assessments(user_id, created_at desc);

create index if not exists idx_ikigai_reports_user
  on ikigai_reports(user_id, generated_at desc);


-- ---------------------------------------------------------------------------
-- 4. Row Level Security
-- ---------------------------------------------------------------------------
ALTER TABLE ikigai_assessments ENABLE ROW LEVEL SECURITY;
ALTER TABLE ikigai_reports       ENABLE ROW LEVEL SECURITY;

-- ikigai_assessments: user CRUD own
DROP POLICY IF EXISTS "Users can CRUD own assessments" ON ikigai_assessments;
CREATE POLICY "Users can CRUD own assessments"
  ON ikigai_assessments
  FOR ALL
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

-- ikigai_reports: user SELECT own only.
-- INSERT hanya dari Edge Function (service role key bypasses RLS),
-- jadi user tidak bisa inject report palsu.
DROP POLICY IF EXISTS "Users can read own reports" ON ikigai_reports;
CREATE POLICY "Users can read own reports"
  ON ikigai_reports
  FOR SELECT
  USING (auth.uid() = user_id);


-- ---------------------------------------------------------------------------
-- 5. Verifikasi cepat (opsional, boleh di-comment)
-- ---------------------------------------------------------------------------
-- SELECT tablename, rowsecurity
--   FROM pg_tables
--  WHERE schemaname = 'public'
--    AND tablename IN ('ikigai_assessments', 'ikigai_reports');
--
-- SELECT policyname, cmd, qual
--   FROM pg_policies
--  WHERE schemaname = 'public'
--    AND tablename IN ('ikigai_assessments', 'ikigai_reports');


-- =============================================================================
-- Selesai. Tabel ikigai_assessments & ikigai_reports sudah live dengan RLS.
-- Lanjut: Edge Function generate-ikigai-report (TASK 3.1).
-- =============================================================================
