# IMPLEMENTASI PERANGKAT LUNAK — MindRest AI

> Dokumen ini menjelaskan rencana implementasi low-level aplikasi **MindRest AI** dengan fokus pada fitur inti. Ditujukan untuk pembaca yang sudah sedikit paham IT namun tidak perlu menjadi programmer.

---

## 1. Pendahuluan

MindRest AI adalah aplikasi mobile Android yang membantu pengguna memahami diri sendiri lewat tiga data utama: **catatan tidur**, **suasana hati**, dan **jurnal harian**. Ketiganya kemudian dipakai sebagai bahan bakar bagi kecerdasan buatan (AI) untuk menghasilkan laporan **Ikigai** — yaitu ringkasan tentang "apa yang kamu cintai", "apa yang kamu kuasai", "apa yang bisa jadi profesimu", dan "apa yang ingin kamu kontribusikan ke dunia". Implementasi disusun berdasarkan 17 kebutuhan fungsional (FR-001 sampai FR-017) yang tercantum di tabel acuan.

## 2. Gambaran Umum Aplikasi

Aplikasi dibangun dengan **Jetpack Compose** (bahasa Kotlin) untuk sisi Android, dan **Supabase** untuk sisi server (database, autentikasi, dan Edge Functions berbasis Deno/TypeScript). AI yang dipakai adalah **Google Gemini**, namun kuncinya disimpan di server — bukan di aplikasi — sehingga tidak bisa dicuri dari sisi pengguna. Komunikasi antara aplikasi dan server menggunakan HTTPS dengan token login (JWT), sehingga setiap permintaan terlindungi.

## 3. Manajemen Arsitektur

### 3.1 Manajemen Folder

Struktur kode dipisah berdasarkan **fitur** (feature-based), bukan berdasarkan tipe file. Tujuannya agar satu fitur — misalnya tidur — semua berkas terkait (tampilan, logika, dan akses data) terkumpul dalam satu folder. Pola ini memudahkan tim saat menambah atau memperbaiki fitur tanpa menyentuh fitur lain.

```
app/src/main/java/com/example/
├── core/                  # komponen bersama (navigasi, jaringan, desain)
├── features/
│   ├── authentication/    # login, register, profil
│   ├── home/              # dashboard & check-in harian
│   ├── sleep/             # pencatatan & riwayat tidur
│   ├── mood/              # pencatatan & riwayat mood
│   ├── journal/           # jurnal AI & riwayatnya
│   ├── ikigai/            # assessment + laporan Ikigai
│   ├── relaxation/        # fitur relaksasi
│   └── notification/      # pusat notifikasi
└── MainActivity.kt        # titik masuk aplikasi
```

### 3.2 Manajemen Route (Navigasi)

Setiap layar punya **route** berupa string unik, didefinisikan di `core/navigation/Screen.kt`. Alur dibungkus dalam **Navigation Compose**:

- **Autentikasi**: `splash → onboarding → login/register`
- **Tab utama (bottom bar)**: `home`, `sleep`, `relaxation`, `ikigai`, `profile`
- **Layar turunan**: `mood_tracking`, `sleep_tracking`, `ai_journal`, `journal_history`, `ikigai_assessment`, `ikigai_report_loading`, `ikigai_report`, `notifications`

Pemisahan ini mencegah tab "naik-turun" sendiri saat pengguna berpindah layar, sehingga tombol **Back** di HP terasa alami.

### 3.3 Manajemen Database

Database utama adalah **PostgreSQL** di Supabase. Terdapat dua jenis akses:

| Akses | Digunakan oleh | Tujuan |
|---|---|---|
| **User-level (JWT)** | Aplikasi Android | CRUD data milik sendiri |
| **Service role** | Edge Function server | Tulis laporan hasil AI (tidak bisa dipalsukan dari klien) |

Setiap tabel punya **Row Level Security (RLS)** — artinya walaupun tabel sama, pengguna A tidak akan pernah bisa membaca data pengguna B. Skema disimpan di `supabase/schema.sql` dan migrasi tambahan di folder `supabase/migrations/`.

> 📷 **[TAMPILKAN DI SINI: Diagram relasi antar-tabel (ERD) MindRest AI]**
> ```
> ![ERD Database](docs/images/erd-mindrest.png)
> ```
> 📷 **[TAMPILKAN DI SINI: Skema tabel profiles, mood_logs, sleep_logs, journal_entries, ikigai_assessments, ikigai_reports]**

## 4. Implementasi Per Fitur

### 4.1 Autentikasi & Profil (FR-001, FR-002, FR-003)

Pengguna mendaftar dengan email dan kata sandi melalui layar **Register**; Supabase mengirim tautan verifikasi dan otomatis membuat baris di tabel `profiles`. Layar **Login** memvalidasi kredensial, lalu aplikasi menyimpan sesi (JWT) agar tidak perlu login ulang. Tombol **Logout** menghapus sesi lokal dan kembali ke layar login. Pembaruan profil (nama tampilan, tinggi, berat, pekerjaan, keluhan) dilakukan di layar **Profile** dan disimpan ke kolom tambahan pada tabel `profiles`.

### 4.2 Sleep Tracking (FR-004, FR-005, FR-006)

Pengguna membuka layar **Sleep Tracking**, mengisi jam tidur (`bed_time`), jam bangun (`wake_up_time`), dan kualitas tidur (POOR/FAIR/GOOD/EXCELLENT). Aplikasi menghitung **durasi tidur** secara otomatis dengan selisih dua waktu tersebut (FR-005). Data tersimpan ke tabel `sleep_logs`. Riwayat ditampilkan di layar **Sleep Hub** dalam bentuk daftar kronologis dan grafik ringkas.

> 📷 **[TAMPILKAN DI SINI: Visualisasi tabel sleep_logs & grafik riwayat tidur]**

### 4.3 Mood Tracking (FR-007, FR-008)

Pengguna memilih skor suasana hati 1–5 (😢 sampai 😁) di layar **Mood Tracking** atau lewat *bottom sheet* cepat dari beranda. Data tersimpan ke tabel `mood_logs`. Riwayat mood ditampilkan sebagai deretan emoji beserta tanggal agar pengguna bisa melihat pola emosionalnya.

### 4.4 Daily Journal dengan AI Chatbot (FR-009, FR-010, FR-011)

Layar **AI Journal** menyajikan antarmuka percakapan tempat pengguna menulis curhat. Pesan dikirim ke **Edge Function** server yang memanggil Gemini, lalu mengembalikan balasan empatik. Setiap percakapan disimpan ke tabel `journal_entries` agar riwayat bisa dibuka kembali (FR-010). Konten jurnal inilah yang nanti dipakai AI sebagai data pasif untuk memperkaya laporan Ikigai (FR-011).

### 4.5 Ikigai (FR-012, FR-013)

Pengguna menjawab **6 pertanyaan assessment** (passion, skill, profesi, misi, overthinking, kepuasan hidup) di layar **Ikigai Assessment**, disimpan ke tabel `ikigai_assessments`. Setelah disimpan, aplikasi memanggil Edge Function `generate-ikigai-report` yang:

1. Memverifikasi token login.
2. Mengecek rate-limit (maksimal 1× per hari).
3. Merangkai prompt berisi jawaban assessment + data mood/sleep/journal 7 hari terakhir.
4. Meminta Gemini mengembalikan JSON terstruktur: `report_markdown`, `ikigai_circles`, dan 3–5 rekomendasi.
5. Menyimpan hasilnya ke `ikigai_reports`.

Aplikasi lalu menampilkan **4 lingkaran Ikigai** dan rekomendasi yang bisa di-centang saat sudah dikerjakan.

> 📷 **[TAMPILKAN DI SINI: Skema tabel ikigai_assessments & ikigai_reports]**
> 📷 **[TAMPILKAN DI SINI: Mockup tampilan 4 lingkaran Ikigai]**

### 4.6 Sleep Insight (FR-014)

Berdasarkan riwayat `sleep_logs`, aplikasi memberikan saran sederhana (misalnya "kualitas tidurmu menurun minggu ini, coba tidur 30 menit lebih awal"). Saat ini berbasis aturan lokal; ke depan akan diperkaya dengan ringkasan dari AI.

### 4.7 Dashboard / Beranda (FR-015)

Layar **Home** menggabungkan ringkasan: tren tidur 7 hari, tren mood 7 hari, *sleep insight* terbaru, jurnal terakhir, dan daftar pengingat harian. Semua widget memuat datanya sendiri dari repository terkait, sehingga kegagalan satu widget tidak menggangu yang lain.

### 4.8 Notifikasi (FR-016)

Notifikasi dihasilkan oleh **Edge Function** terjadwal (pg_cron) yang memanggil Gemini untuk membuat pesan harian berdasarkan profil dan mood terkini, lalu menyimpannya ke tabel `notifications`. Aplikasi menampilkan di tab **Notifications** dan mengirim *push notification* lokal saat ada baris baru.

### 4.9 Relaksasi (FR-017)

Layar **Relaxation** memutar audio atau menampilkan panduan relaksasi sederhana yang sudah disiapkan secara statis di dalam aplikasi. Tidak membutuhkan AI, sehingga dapat diakses kapanpun termasuk saat offline.

## 5. Penutup

Seluruh 17 kebutuhan fungsional tercakup dalam implementasi di atas. Pendekatan **pemisahan fitur**, **route terpusat**, dan **database berlapis RLS** memastikan aplikasi mudah dikembangkan, aman, dan siap melayani pengguna dalam menemukan Ikigai mereka.

> 📷 **[TAMPILKAN DI SINI: Arsitektur lengkap aplikasi + alur data Android ↔ Supabase ↔ Gemini]**
