-- =============================================================================
-- MindRest_AI — Backfill profiles untuk user yang dibuat SEBELUM trigger
-- on_auth_user_created terpasang.
-- =============================================================================
-- KONTEKS:
--   Trigger on_auth_user_created (di schema.sql) hanya fire pada INSERT baru
--   ke auth.users. User yang signup SEBELUM trigger terpasang (mis.
--   alfian@mail.com) tidak punya row di public.profiles. Script ini menutup
--   celah tersebut dengan replikasi logika handle_new_user() untuk semua user
--   yang belum punya profile.
--
-- CARA PAKAI:
--   1. Pastikan supabase/schema.sql SUDAH dijalankan (profil tabel + trigger
--      terpasang). Kalau belum, jalankan schema.sql dulu di SQL Editor.
--   2. Jalankan script ini di: Supabase Dashboard → SQL Editor → New query.
--   3. Idempoten — aman dijalankan berulang (ON CONFLICT DO NOTHING).
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Langkah 1 — Verifikasi: apakah trigger & function terpasang? (opsional)
-- ---------------------------------------------------------------------------
-- (a) Trigger ada & enabled?  (tgenabled 'O' = enabled)
select tgname, tgrelid::regclass as on_table, tgenabled
from pg_trigger
where tgname in ('on_auth_user_created', 'profiles_touch_updated_at');

-- (b) Function handle_new_user ada & SECURITY DEFINER?
select proname, prosecdef, proowner::regrole as owner
from pg_proc where proname = 'handle_new_user';

-- (c) RLS aktif di profiles?
select relname, relrowsecurity, relforcerowsecurity
from pg_class where relname = 'profiles';

-- (d) Selisih auth.users vs profiles (harus ada gap → perlu backfill)
select (select count(*) from auth.users)      as auth_users_count,
       (select count(*) from public.profiles) as profiles_count;

-- ---------------------------------------------------------------------------
-- Langkah 2 — Backfill (jalankan jika auth_users_count > profiles_count)
-- ---------------------------------------------------------------------------
insert into public.profiles (id, email, display_name, avatar_url)
select
    u.id,
    coalesce(u.email, u.raw_user_meta_data ->> 'email') as email,
    coalesce(
        u.raw_user_meta_data ->> 'full_name',
        u.raw_user_meta_data ->> 'name',
        split_part(coalesce(u.email, u.raw_user_meta_data ->> 'email', ''), '@', 1)
    ) as display_name,
    u.raw_user_meta_data ->> 'avatar_url' as avatar_url
from auth.users u
where not exists (
    select 1 from public.profiles p where p.id = u.id
)
on conflict (id) do nothing;

-- Konfirmasi hasil:
select id, email, display_name, created_at
from public.profiles
order by created_at desc;
