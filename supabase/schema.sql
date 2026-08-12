-- =============================================================================
-- MindRest_AI — Supabase database schema
-- =============================================================================
-- Jalankan script ini SATU KALI di:
--   Supabase Dashboard → Project → SQL Editor → New query → paste → Run
--
-- Output script:
--   1. Tabel: profiles, mood_logs, sleep_logs, journal_entries
--   2. Indexes untuk query yang sering dipakai
--   3. Row Level Security (RLS) — user hanya bisa baca/tulis datanya sendiri
--   4. Trigger: created_at otomatis terisi + profile auto-create saat user sign up
-- =============================================================================


-- ---------------------------------------------------------------------------
-- 1. Tabel profiles
-- ---------------------------------------------------------------------------
-- Tabel ini menyimpan data profil app-spesifik per user (display_name,
-- avatar_url, email). Di-backup oleh trigger on_auth_user_created agar
-- otomatis terisi setiap kali ada row baru di auth.users — jadi berlaku
-- untuk semua provider (email, Google, Apple, dll).
--
-- id di sini = auth.users.id (1:1). Tidak pakai FK terpisah untuk id agar
-- trigger cukup INSERT tanpa perlu UPDATE ketika email user berubah.
-- ---------------------------------------------------------------------------
create table if not exists public.profiles (
    id            uuid        primary key references auth.users(id) on delete cascade,
    email         text,
    display_name  text,
    avatar_url    text,
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now()
);
create index if not exists profiles_created_at_idx on public.profiles (created_at desc);


-- ---------------------------------------------------------------------------
-- 2. Tabel mood_logs
-- ---------------------------------------------------------------------------
create table if not exists public.mood_logs (
    id          uuid        primary key default gen_random_uuid(),
    user_id     uuid        not null references auth.users(id) on delete cascade,
    mood_score  int         not null check (mood_score between 1 and 5),
    created_at  timestamptz not null default now()
);
create index if not exists mood_logs_user_id_created_at_idx
    on public.mood_logs (user_id, created_at desc);


-- ---------------------------------------------------------------------------
-- 2. Tabel sleep_logs
-- ---------------------------------------------------------------------------
-- Catatan: bed_time / wake_up_time / sleep_quality disimpan sebagai string
-- untuk konsistensi dengan Android DTO. Format:
--   bed_time      = "HH:mm"        contoh: "22:00"
--   wake_up_time  = "HH:mm"        contoh: "06:00"
--   sleep_quality = "POOR|FAIR|GOOD|EXCELLENT"
-- (bisa di-migrate ke tipe native nanti — lihat catatan di README.md)
-- ---------------------------------------------------------------------------
create table if not exists public.sleep_logs (
    id             uuid        primary key default gen_random_uuid(),
    user_id        uuid        not null references auth.users(id) on delete cascade,
    bed_time       text        not null,
    wake_up_time   text        not null,
    sleep_quality  text        not null
                              check (sleep_quality in ('POOR','FAIR','GOOD','EXCELLENT')),
    created_at     timestamptz not null default now()
);
create index if not exists sleep_logs_user_id_created_at_idx
    on public.sleep_logs (user_id, created_at desc);


-- ---------------------------------------------------------------------------
-- 3. Tabel journal_entries
-- ---------------------------------------------------------------------------
create table if not exists public.journal_entries (
    id          uuid        primary key default gen_random_uuid(),
    user_id     uuid        not null references auth.users(id) on delete cascade,
    content     text        not null,
    created_at  timestamptz not null default now()
);
create index if not exists journal_entries_user_id_created_at_idx
    on public.journal_entries (user_id, created_at desc);


-- ---------------------------------------------------------------------------
-- 4. Trigger: auto-create profile saat user baru daftar
-- ---------------------------------------------------------------------------
-- Jalankan setiap kali row baru dibuat di auth.users. Mengambil metadata
-- dari raw_user_meta_data (diisi otomatis oleh Supabase sesuai provider):
--   - Email provider        → { "email": "..." }
--   - Google (ID Token)     → { "email": "...", "full_name": "...", "avatar_url": "...",
--                                "provider_id": "...", "name": "..." }
--   - Apple                 → { "email": "...", "full_name": "...", "provider_id": "..." }
-- ---------------------------------------------------------------------------
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_email        text;
    v_display_name text;
    v_avatar_url   text;
begin
    v_email := coalesce(
        new.email,
        (new.raw_user_meta_data ->> 'email')
    );

    -- Display name: coba beberapa key yang umum dipakai provider.
    v_display_name := coalesce(
        new.raw_user_meta_data ->> 'full_name',
        new.raw_user_meta_data ->> 'name',
        split_part(coalesce(v_email, ''), '@', 1)
    );

    -- Avatar URL hanya tersedia dari beberapa provider (Google).
    v_avatar_url := new.raw_user_meta_data ->> 'avatar_url';

    insert into public.profiles (id, email, display_name, avatar_url)
    values (new.id, v_email, v_display_name, v_avatar_url)
    on conflict (id) do nothing;

    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();


-- ---------------------------------------------------------------------------
-- 5. Trigger: bump updated_at di profiles saat row di-update
-- ---------------------------------------------------------------------------
create or replace function public.touch_updated_at()
returns trigger
language plpgsql
as $$
begin
    new.updated_at := now();
    return new;
end;
$$;

drop trigger if exists profiles_touch_updated_at on public.profiles;
create trigger profiles_touch_updated_at
    before update on public.profiles
    for each row execute function public.touch_updated_at();


-- ---------------------------------------------------------------------------
-- 6. Row Level Security
-- ---------------------------------------------------------------------------
-- Aktifkan RLS di setiap tabel lalu beri policy agar user hanya bisa
-- baca/tulis baris miliknya sendiri (user_id = auth.uid()).
-- ---------------------------------------------------------------------------

-- profiles
alter table public.profiles enable row level security;

drop policy if exists "Users can read own profile"   on public.profiles;
drop policy if exists "Users can update own profile" on public.profiles;

create policy "Users can read own profile"
    on public.profiles for select
    using (auth.uid() = id);

create policy "Users can update own profile"
    on public.profiles for update
    using (auth.uid() = id)
    with check (auth.uid() = id);

-- Insert & delete hanya via trigger (security definer) — tidak ada policy
-- INSERT/DELETE sehingga client tidak bisa menyisipkan/menghapus profile
-- secara langsung.


-- mood_logs
alter table public.mood_logs enable row level security;

drop policy if exists "Users can read own mood logs"   on public.mood_logs;
drop policy if exists "Users can insert own mood logs" on public.mood_logs;
drop policy if exists "Users can update own mood logs" on public.mood_logs;
drop policy if exists "Users can delete own mood logs" on public.mood_logs;

create policy "Users can read own mood logs"
    on public.mood_logs for select
    using (auth.uid() = user_id);

create policy "Users can insert own mood logs"
    on public.mood_logs for insert
    with check (auth.uid() = user_id);

create policy "Users can update own mood logs"
    on public.mood_logs for update
    using (auth.uid() = user_id);

create policy "Users can delete own mood logs"
    on public.mood_logs for delete
    using (auth.uid() = user_id);


-- sleep_logs
alter table public.sleep_logs enable row level security;

drop policy if exists "Users can read own sleep logs"   on public.sleep_logs;
drop policy if exists "Users can insert own sleep logs" on public.sleep_logs;
drop policy if exists "Users can update own sleep logs" on public.sleep_logs;
drop policy if exists "Users can delete own sleep logs" on public.sleep_logs;

create policy "Users can read own sleep logs"
    on public.sleep_logs for select
    using (auth.uid() = user_id);

create policy "Users can insert own sleep logs"
    on public.sleep_logs for insert
    with check (auth.uid() = user_id);

create policy "Users can update own sleep logs"
    on public.sleep_logs for update
    using (auth.uid() = user_id);

create policy "Users can delete own sleep logs"
    on public.sleep_logs for delete
    using (auth.uid() = user_id);


-- journal_entries
alter table public.journal_entries enable row level security;

drop policy if exists "Users can read own journal entries"   on public.journal_entries;
drop policy if exists "Users can insert own journal entries" on public.journal_entries;
drop policy if exists "Users can update own journal entries" on public.journal_entries;
drop policy if exists "Users can delete own journal entries" on public.journal_entries;

create policy "Users can read own journal entries"
    on public.journal_entries for select
    using (auth.uid() = user_id);

create policy "Users can insert own journal entries"
    on public.journal_entries for insert
    with check (auth.uid() = user_id);

create policy "Users can update own journal entries"
    on public.journal_entries for update
    using (auth.uid() = user_id);

create policy "Users can delete own journal entries"
    on public.journal_entries for delete
    using (auth.uid() = user_id);


-- =============================================================================
-- Selesai. Verifikasi cepat (opsional):
--   select tablename, rowsecurity from pg_tables
--   where schemaname = 'public'
--     and tablename in ('profiles','mood_logs','sleep_logs','journal_entries',
--                       'ikigai_assessments','ikigai_reports');
--
-- Pastikan juga di Supabase Dashboard → Authentication → Providers → Google
-- provider sudah diaktifkan dan Client ID / Secret Google Cloud sudah diisi
-- (lihat supabase/README.md langkah 5).
-- =============================================================================


-- =============================================================================
-- 7. Migration 001 — Extend profiles (untuk Sleep Therapy & onboarding)
-- =============================================================================
-- Tujuan: menambah kolom profil kesehatan untuk onboarding & input AI.
--   height_cm   INT
--   weight_kg   INT
--   occupation  TEXT
--   complaints  TEXT[]   -- mis. {'insomnia','overthinking','anxiety'}
-- Aman untuk dijalankan berulang (IF NOT EXISTS).
-- =============================================================================

ALTER TABLE profiles
  ADD COLUMN IF NOT EXISTS height_cm INT,
  ADD COLUMN IF NOT EXISTS weight_kg INT,
  ADD COLUMN IF NOT EXISTS occupation TEXT,
  ADD COLUMN IF NOT EXISTS complaints TEXT[] DEFAULT '{}';


-- =============================================================================
-- 8. Migration 002 — Tabel Ikigai (assessments + reports)
-- =============================================================================
-- Tujuan: fitur Ikigai discovery (MINGGU 2–3 ROADMAP).
--   ikigai_assessments — 6 pertanyaan onboarding (user CRUD own)
--   ikigai_reports      — laporan AI: markdown + 4 lingkaran + rekomendasi
--                         INSERT hanya via Edge Function (service role),
--                         user SELECT-only (anti-injection).
-- =============================================================================

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

create index if not exists idx_ikigai_assessments_user
  on ikigai_assessments(user_id, created_at desc);

create index if not exists idx_ikigai_reports_user
  on ikigai_reports(user_id, generated_at desc);

ALTER TABLE ikigai_assessments ENABLE ROW LEVEL SECURITY;
ALTER TABLE ikigai_reports       ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can CRUD own assessments" ON ikigai_assessments;
CREATE POLICY "Users can CRUD own assessments"
  ON ikigai_assessments
  FOR ALL
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can read own reports" ON ikigai_reports;
CREATE POLICY "Users can read own reports"
  ON ikigai_reports
  FOR SELECT
  USING (auth.uid() = user_id);
-- NOTE: INSERT ke ikigai_reports HANYA dari Edge Function (service role),
-- jadi user tidak bisa inject report palsu. Hanya user bisa SELECT.


-- ===========================================================================
-- ===========================================================================
-- 9. Migration 003 — Policy UPDATE untuk ikigai_reports (checkbox rekomendasi)
-- ===========================================================================
-- Tujuan: Mengizinkan user meng-update row report miliknya sendiri agar
--         fitur "centang rekomendasi" (checkbox toggle) di IkigaiReportScreen
--         (TASK 3.3) bisa persist ke DB.
--
-- KONTEKS:
--   Migration 002 (section 8) membuat ikigai_reports SELECT-only (untuk
--   mencegah user inject report palsu). Tapi TASK 3.3 butuh update field
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
-- ===========================================================================

DROP POLICY IF EXISTS "Users can update own reports" ON ikigai_reports;
CREATE POLICY "Users can update own reports"
  ON ikigai_reports
  FOR UPDATE
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

-- Verifikasi cepat (opsional, boleh di-comment):
-- SELECT policyname, cmd
--   FROM pg_policies
--  WHERE schemaname = 'public'
--    AND tablename = 'ikigai_reports';
-- Expected:
--   Users can read own reports     | SELECT
--   Users can update own reports   | UPDATE


-- =============================================================================
-- Akhir schema.sql — semua tabel + RLS siap dipakai.
-- Tabel live: profiles, mood_logs, sleep_logs, journal_entries,
--             ikigai_assessments, ikigai_reports.
--
-- CARA PAKAI: Copy-paste SELURUH file ini ke Supabase SQL Editor → Run.
-- File ini adalah single source of truth (idempotent: semua CREATE pakai
-- IF NOT EXISTS, semua POLICY pakai DROP IF EXISTS sebelum CREATE).
-- =============================================================================
