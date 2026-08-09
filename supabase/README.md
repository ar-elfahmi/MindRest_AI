# Setup Supabase untuk MindRest_AI

Panduan ini membuat project Supabase dari nol dan menghubungkannya ke
aplikasi Android.

> **Estimasi waktu:** 10–15 menit (tidak termasuk setup akun pertama kali).

---

## 1. Buat akun & project Supabase

1. Buka <https://supabase.com> → klik **Start your project**.
2. Sign up via GitHub (paling cepat).
3. Klik **New project**:
   - **Name:** `mindrest-ai` (atau apa saja)
   - **Database password:** buat password kuat → **simpan** di password manager
   - **Region:** pilih yang paling dekat dengan pengguna (mis. Singapore)
   - **Plan:** Free sudah cukup
4. Tunggu ±2 menit sampai project siap.

## 2. Jalankan schema SQL

1. Di dashboard project → sidebar kiri klik **SQL Editor**.
2. Klik **New query**.
3. Buka file `schema.sql` di repo ini (folder `supabase/`),
   **copy semua isinya**, paste ke SQL editor.
4. Klik **Run** (atau Ctrl+Enter).
5. Harus muncul `Success. No rows returned`. Itu artinya 3 tabel + RLS sudah dibuat.

Verifikasi cepat: buka **Table Editor** di sidebar. Akan ada tabel:
`mood_logs`, `sleep_logs`, `journal_entries`.

## 3. Ambil kredensial

1. Sidebar → **Settings** → **API**.
2. Catat dua nilai ini:
   - **Project URL** → contoh: `https://abcdefghij.supabase.co`
   - **anon public** key → JWT panjang yang dimulai dengan `eyJ...`

> ⚠️ Pakai **anon public**, BUKAN `service_role` (service_role bypass RLS,
> bahaya kalau bocor).

## 4. Konfigurasi Android

1. Di root project Android, salin template env:

   ```bash
   cp .env.example .env
   ```

2. Edit `.env` dan isi:

   ```dotenv
   SUPABASE_URL=https://abcdefghij.supabase.co
   SUPABASE_ANON_KEY=eyJhbGciOi...   # JWT panjang dari anon public
   GEMINI_API_KEY=...                # opsional, untuk tahap berikutnya
   ```

3. Rebuild project di Android Studio
   (**Build → Make Project** atau jalankan ulang).
   Gradle tidak auto-reload `.env` — *wajib rebuild*.

## 5. (Sangat disarankan) Aktifkan Email Auth

Sampai sini, semua Repository butuh `currentSessionOrNull()?.user?.id`.
Tanpa login → semua fitur Mood/Sleep/Journal akan menampilkan
error `"User not logged in"`.

1. Dashboard Supabase → **Authentication** → **Providers**.
2. Aktifkan **Email** (default sudah aktif di project baru).
3. (Opsional) Aktifkan juga Google / Apple sesuai kebutuhan.

Login UI di Android (`AuthPlaceholders.kt`) **belum diimplementasikan**
— lihat TODO di langkah 6.

## 6. Langkah berikutnya (di luar setup Supabase)

Setelah app bisa konek ke Supabase:

- [x] ~~Implementasi Login/Register screen di `features/authentication/`~~ ✅
- [ ] Tambah operasi READ di tiap Repository (history/statistik)
- [ ] Integrasi Gemini AI untuk `AiJournalScreen`

---

## 7. Konfirmasi Email (untuk testing cepat)

Supabase secara **default mengaktifkan konfirmasi email** untuk sign-up baru
(`mailer_autoconfirm: false`). Artinya setelah register, user **harus klik link
konfirmasi** di inbox sebelum bisa sign in. Kalau tidak, login akan selalu
gagal dengan error `Email not confirmed` (atau `Invalid login credentials`).

### Opsi A: Disable konfirmasi email (disarankan untuk MVP/testing)

1. Dashboard → **Authentication** → **Providers** → **Email**.
2. Scroll ke bawah → matikan toggle **"Confirm email"**.
3. Klik **Save**.

Sekarang sign-up → langsung aktif, bisa langsung sign-in.

### Opsi B: Keep konfirmasi ON (production-grade)

1. Jalankan `Sign Up` di app → cek inbox (termasuk folder Spam).
2. Klik link konfirmasi dari Supabase.
3. Baru jalankan `Sign In`.

---

## Troubleshooting

| Gejala | Penyebab | Solusi |
|--------|----------|--------|
| App crash / "Supabase belum dikonfigurasi" | `.env` masih placeholder atau belum rebuild | Isi `.env`, lalu **Build → Make Project** |
| `401 Unauthorized` | `SUPABASE_ANON_KEY` salah / pakai service_role | Salin ulang **anon public** dari Settings → API |
| `new row violates row-level security policy` | RLS aktif tapi `auth.uid()` null (user belum login) | Login dulu, atau cek policy dengan **SQL Editor** |
| Data tidak muncul di Table Editor | INSERT pakai anon tanpa login + RLS blocking | Pastikan ada `auth.uid()` = `user_id` saat insert |

---

## Catatan teknis

- DTO `SleepLogInsert` mengirim `bedTime` & `wakeUpTime` sebagai **ISO 8601 string**
  (contoh `2026-08-09T22:00:00Z`), bukan `HH:mm` mentah. Schema menyimpan
  keduanya sebagai `text` agar fleksibel — bisa di-migrate ke `timestamptz`
  di kemudian hari.
- `sleep_quality` disimpan sebagai `text` dengan CHECK constraint
  (`POOR|FAIR|GOOD|EXCELLENT`).
- RLS policy: setiap user hanya bisa baca/tulis baris dengan
  `user_id = auth.uid()`. Service role bypass RLS (server-side only).
