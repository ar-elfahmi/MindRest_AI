-- =============================================================================
-- Migration 003 — Policy UPDATE untuk ikigai_reports (checkbox rekomendasi)
-- =============================================================================
-- Tujuan: Mengizinkan user meng-update row report miliknya sendiri agar
--         fitur "centang rekomendasi" (checkbox toggle) di IkigaiReportScreen
--         (TASK 3.3) bisa persist ke DB.
--
-- KONTEKS:
--   Migration 002 membuat ikigai_reports SELECT-only (untuk mencegah user
--   inject report palsu). Tapi TASK 3.3 butuh update field
--   `recommendations[].done` saat user centang aktivitas.
--
-- TRADEOFF & KEAMANAN:
--   - RLS Postgres bersifat row-level (bukan field-level), jadi policy ini
--     mengizinkan UPDATE seluruh row milik user, termasuk report_markdown.
--   - Risiko: user BISA memodifikasi report_markdown miliknya sendiri.
--   - Mitigasi: user hanya bisa touch row MILIKNYA (auth.uid() = user_id).
--     Mereka tidak bisa baca/modifikasi report user lain. Untuk MVP ini
--     acceptable — report adalah milik user tersebut.
--   - INSERT tetap diblokir (tidak ada policy INSERT) → user tidak bisa
--     inject report palsu. Hanya Edge Function (service role) yang INSERT.
-- =============================================================================

DROP POLICY IF EXISTS "Users can update own reports" ON ikigai_reports;
CREATE POLICY "Users can update own reports"
  ON ikigai_reports
  FOR UPDATE
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);


-- ---------------------------------------------------------------------------
-- Verifikasi cepat (opsional)
-- ---------------------------------------------------------------------------
-- SELECT policyname, cmd
--   FROM pg_policies
--  WHERE schemaname = 'public'
--    AND tablename = 'ikigai_reports';
-- Expected:
--   Users can read own reports     | SELECT
--   Users can update own reports   | UPDATE
