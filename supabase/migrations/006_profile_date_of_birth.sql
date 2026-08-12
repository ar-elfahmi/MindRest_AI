-- =============================================================================
-- Migration 006 — Add date_of_birth column to profiles
-- =============================================================================
-- Tujuan: Mendukung fitur FR-003 "lengkapi profil" dengan field tanggal lahir.
--         Ditambahkan terpisah dari migration 001 karena saat itu scope
--         onboarding kesehatan hanya height/weight/occupation/complaints.
--
-- Kolom baru:
--   date_of_birth DATE  — tanggal lahir (nullable; diisi di ProfileScreen
--                         melalui DatePicker)
--
-- Aman untuk dijalankan berulang (IF NOT EXISTS).
-- =============================================================================


-- ---------------------------------------------------------------------------
-- 1. Tambah kolom ke profiles
-- ---------------------------------------------------------------------------
ALTER TABLE profiles
  ADD COLUMN IF NOT EXISTS date_of_birth DATE;


-- ---------------------------------------------------------------------------
-- 2. Verifikasi cepat (opsional, boleh di-comment)
-- ---------------------------------------------------------------------------
-- SELECT column_name, data_type
--   FROM information_schema.columns
--  WHERE table_schema = 'public' AND table_name = 'profiles'
--  ORDER BY ordinal_position;


-- =============================================================================
-- Selesai. profiles sekarang punya tambahan kolom:
--   date_of_birth DATE  (nullable)
-- =============================================================================