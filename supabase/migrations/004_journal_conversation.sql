-- =============================================================================
-- Migration 004 — Journal conversation (FR-009, FR-011)
-- =============================================================================
-- Tujuan: extend `journal_entries` agar bisa menyimpan conversation history
-- untuk fitur AI Journaling Chatbot (FR-009) dan olah data jurnal (FR-011).
--
-- Schema lama (lihat schema.sql section 3):
--   id, user_id, content, created_at
--   -- dipakai untuk "full journal entry" (1 row = 1 entry panjang)
--
-- Schema baru (T-003):
--   + session_id  UUID  -- mengelompokkan pesan dalam 1 sesi chat
--   + role        TEXT  -- 'user' | 'assistant' (NULL = legacy full entry)
--   + parent_id   UUID  -- optional, link assistant reply ke user prompt
--
-- Backward compatibility:
--   - Semua kolom baru nullable / NULLable TEXT → row lama tetap valid.
--   - Baris lama tanpa role dianggap "full journal entry" (T-002 lihat UI).
--     Chat history query filter `WHERE role IS NOT NULL` agar tidak ikut.
--
-- Idempotent: pakai IF NOT EXISTS agar aman dijalankan berulang.
-- =============================================================================

ALTER TABLE public.journal_entries
  ADD COLUMN IF NOT EXISTS session_id UUID,
  ADD COLUMN IF NOT EXISTS role TEXT
    CHECK (role IS NULL OR role IN ('user', 'assistant')),
  ADD COLUMN IF NOT EXISTS parent_id UUID
    REFERENCES public.journal_entries(id) ON DELETE SET NULL;

-- Index untuk query conversation history by session (urut kronologis).
-- Komposit (user_id, session_id, created_at) supaya RLS-friendly: query
-- selalu filter user_id dulu → pakai idx_user_created_at existing, baru
-- filter session_id → index ini.
CREATE INDEX IF NOT EXISTS idx_journal_entries_session
  ON public.journal_entries (user_id, session_id, created_at)
  WHERE session_id IS NOT NULL;

-- Index tambahan untuk query "all chat messages" (WHERE role IS NOT NULL).
CREATE INDEX IF NOT EXISTS idx_journal_entries_role
  ON public.journal_entries (user_id, created_at)
  WHERE role IS NOT NULL;

-- =============================================================================
-- Verifikasi cepat (opsional, boleh di-comment):
-- SELECT column_name, data_type, is_nullable
--   FROM information_schema.columns
--  WHERE table_schema = 'public'
--    AND table_name = 'journal_entries'
--  ORDER BY ordinal_position;
--
-- Expected (akhir):
--   id, user_id, content, created_at, session_id, role, parent_id
-- =============================================================================
