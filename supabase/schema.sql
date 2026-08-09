-- =============================================================================
-- MindRest_AI — Supabase database schema
-- =============================================================================
-- Jalankan script ini SATU KALI di:
--   Supabase Dashboard → Project → SQL Editor → New query → paste → Run
--
-- Output script:
--   1. Tabel: mood_logs, sleep_logs, journal_entries
--   2. Indexes untuk query yang sering dipakai
--   3. Row Level Security (RLS) — user hanya bisa baca/tulis datanya sendiri
--   4. Trigger: created_at otomatis terisi
-- =============================================================================


-- ---------------------------------------------------------------------------
-- 1. Tabel mood_logs
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
-- 4. Row Level Security
-- ---------------------------------------------------------------------------
-- Aktifkan RLS di setiap tabel lalu beri policy agar user hanya bisa
-- baca/tulis baris miliknya sendiri (user_id = auth.uid()).
-- ---------------------------------------------------------------------------

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
--   where schemaname = 'public' and tablename in ('mood_logs','sleep_logs','journal_entries');
-- =============================================================================
