-- =============================================================================
-- Migration 001 — Extend profiles (untuk Sleep Therapy & onboarding kesehatan)
-- =============================================================================
-- Tujuan: Menambah kolom profil kesehatan agar bisa dipakai onboarding
--         dan input AI (Sleep Therapy / Ikigai).
--
-- Kolom baru:
--   height_cm   INT         — tinggi badan (cm)
--   weight_kg   INT         — berat badan (kg)
--   occupation  TEXT        — pekerjaan / aktivitas utama
--   complaints  TEXT[]      — array keluhan, mis. {'insomnia','overthinking'}
--
-- Aman untuk dijalankan berulang (IF NOT EXISTS).
-- =============================================================================


-- ---------------------------------------------------------------------------
-- 1. Tambah kolom ke profiles
-- ---------------------------------------------------------------------------
ALTER TABLE profiles
  ADD COLUMN IF NOT EXISTS height_cm INT,
  ADD COLUMN IF NOT EXISTS weight_kg INT,
  ADD COLUMN IF NOT EXISTS occupation TEXT,
  ADD COLUMN IF NOT EXISTS complaints TEXT[] DEFAULT '{}';


-- ---------------------------------------------------------------------------
-- 2. Verifikasi cepat (opsional, boleh di-comment)
-- ---------------------------------------------------------------------------
-- SELECT column_name, data_type
--   FROM information_schema.columns
--  WHERE table_schema = 'public' AND table_name = 'profiles'
--  ORDER BY ordinal_position;


-- =============================================================================
-- Selesai. profiles sekarang punya kolom:
--   id, email, display_name, avatar_url, created_at, updated_at,
--   height_cm, weight_kg, occupation, complaints
-- =============================================================================
