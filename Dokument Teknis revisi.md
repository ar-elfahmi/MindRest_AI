# DOKUMEN TEKNIS
# Panduan Instalasi dan Penggunaan Perangkat Lunak
# MindRest AI — Ikigai-Driven Sleep Therapy AI Assistant

**Tim Pengembang:** Coup Dès (Universitas Airlangga)
**Anggota:** Alfian Rasyid El Fahmi · Muhammad Faris Sabiq · Rozan Aiman Ramadani
**Dosen Pembimbing:** Dr. Indra Kharisma Raharjana, S.Kom., M.T.
**Versi Dokumen:** 1.0
**Versi Perangkat Lunak:** 1.0 (versionCode 1)

---

## DAFTAR ISI
- BAB I — Latar Belakang
- BAB II — Tujuan
- BAB III — Nilai Inovasi dan Dampak Pemanfaatan Perangkat Lunak
- BAB IV — Deskripsi Fungsional Perangkat Lunak dan Penjelasan Detail Fitur
- BAB V — Screenshot Perangkat Lunak
- LAMPIRAN A — Executable File / URL Aplikasi
- LAMPIRAN B — URL Video Demo Perangkat Lunak
- LAMPIRAN C — Daftar Komponen (Software Library) beserta Lisensi
- LAMPIRAN D — Spesifikasi Teknis & Konfigurasi

---

# BAB I — LATAR BELAKANG

## 1.1 Tentang Dokumen Teknis

Dokumen teknis (technical document) adalah dokumen pendukung yang menjelaskan secara terstruktur bagaimana sebuah perangkat lunak dipasang (diinstal) dan digunakan, serta apa saja yang dibutuhkan agar perangkat lunak tersebut dapat berjalan. Dokumen ini berbeda dengan dokumen proposal atau laporan akademik: fokus utamanya bukan membahas alasan dibuatnya produk, melainkan **memberikan panduan praktis** agar pengguna akhir (*end user*) maupun penguji (*evaluator*) dapat menginstal, menjalankan, dan mengoperasikan perangkat lunak secara mandiri tanpa bantuan langsung dari tim pengembang.

Dalam konteks kompetisi pengembangan perangkat lunak, dokumen teknis berfungsi sebagai jembatan antara tim pengembang dengan penguji dan pengguna. Ketika sebuah aplikasi dikirimkan untuk dievaluasi, penguji memerlukan informasi yang jelas mengenai spesifikasi perangkat yang didukung, cara memperoleh berkas aplikasi, langkah instalasi, hingga cara menggunakan setiap fitur yang tersedia. Tanpa dokumen teknis yang memadai, penguji berpotensi salah instalasi, gagal menjalankan aplikasi, atau melewatkan fitur tertentu sehingga penilaian menjadi tidak akurat.

Dokumen teknis ini disusun khusus untuk perangkat lunak **MindRest AI** — sebuah aplikasi *mobile* Android berbasis kecerdasan buatan untuk terapi tidur yang mengintegrasikan konsep *Ikigai*. Dokumen ini berlaku sebagai panduan instalasi dan penggunaan resmi untuk versi 1.0 perangkat lunak.

## 1.2 Kegunaan Dokumen Teknis

Dokumen teknis ini memiliki beberapa kegunaan utama, sebagai berikut.

1. **Panduan instalasi.** Memberikan langkah demi langkah cara memasang aplikasi MindRest AI pada perangkat Android, baik melalui berkas *executable* (.apk) maupun melalui pembangunan (*build*) dari kode sumber untuk keperluan pengembangan.
2. **Panduan penggunaan.** Menjelaskan cara mengoperasikan setiap fitur MindRest AI, mulai dari registrasi hingga memperoleh rekomendasi pengembangan diri berbasis Ikigai.
3. **Penjelasan fungsional.** Memberikan gambaran utuh mengenai fungsi setiap fitur, alur kerja antar fitur, serta bagaimana data pengguna diolah oleh sistem.
4. **Referensi teknis.** Menyajikan informasi spesifikasi sistem, arsitektur, serta daftar komponen/*library* beserta lisensinya sebagai bentuk transparansi dan kepatuhan terhadap lisensi *open-source*.
5. **Dokumen evaluasi.** Menjadi rujukan bagi penguji dalam menilai kelayakan teknis dan fungsional perangkat lunak.

## 1.3 Lingkup Dokumen Teknis

Dokumen teknis ini mencakup hal-hal berikut.
- Latar belakang dan tujuan dokumen.
- Nilai inovasi serta dampak pemanfaatan perangkat lunak MindRest AI.
- Deskripsi fungsional dan penjelasan detail seluruh fitur (FR-001 sampai FR-017).
- Panduan instalasi dan penggunaan.
- Kumpulan *screenshot* antarmuka perangkat lunak.
- Informasi mengenai *executable file*, URL video demo, serta daftar komponen beserta lisensi.

Dokumen ini **tidak mencakup** dokumentasi kode sumber secara mendetail (API internal, struktur kelas), panduan pengembangan lanjutan, maupun protokol penelitian klinis. Hal-hal tersebut berada di luar ruang lingkup panduan instalasi dan penggunaan.

## 1.4 Target Pembaca

Dokumen teknis ini ditujukan untuk pembaca dengan latar berikut.
- **Penguji kompetisi/evaluator** yang akan memasang dan menguji aplikasi.
- **Pengguna akhir** (mahasiswa dan pekerja muda) yang ingin mencoba aplikasi.
- **Dosen pembimbing dan reviewer** yang meninjau aspek teknis produk.
- **Tim pengembang sendiri** sebagai catatan rujukan internal.

Tidak diperlukan keahlian teknis mendalam untuk memahami panduan penggunaan. Namun, bagian instalasi dari kode sumber ditujukan bagi pembaca yang familiar dengan *tools* pengembangan Android (Android Studio, Gradle, Git).

## 1.5 Sistematika Penulisan

Dokumen teknis ini disusun dalam lima bab utama dan empat lampiran, dengan sistematika sebagai berikut.

- **BAB I — Latar Belakang**, menjelaskan pengertian, kegunaan, lingkup, dan target pembaca dokumen teknis.
- **BAB II — Tujuan**, menyebutkan tujuan dokumen serta sasaran yang ingin dicapai.
- **BAB III — Nilai Inovasi dan Dampak Pemanfaatan**, memaparkan kebaruan dan dampak penggunaan MindRest AI.
- **BAB IV — Deskripsi Fungsional dan Penjelasan Detail Fitur**, berisi deskripsi fitur, panduan instalasi, dan panduan penggunaan.
- **BAB V — Screenshot Perangkat Lunak**, menyajikan tangkapan layar antarmuka aplikasi.
- **Lampiran A–D**, memuat *executable file*/URL aplikasi, URL video demo, daftar komponen beserta lisensi, serta spesifikasi teknis.

---

# BAB II — TUJUAN

## 2.1 Tujuan Dokumen Teknis

Tujuan disusunnya dokumen teknis ini adalah sebagai berikut.

1. **Mempermudah proses instalasi.** Memberikan instruksi yang jelas dan terurut agar aplikasi dapat dipasang pada perangkat Android tanpa kesalahan.
2. **Memastikan penggunaan fitur yang optimal.** Membimbing pengguna agar dapat memanfaatkan seluruh fitur MindRest AI sesuai fungsinya.
3. **Menyediakan referensi yang dapat diverifikasi.** Memberikan informasi yang dapat dipakai penguji untuk memverifikasi kelayakan teknis dan fungsional perangkat lunak.
4. **Mendokumentasikan komponen dan lisensi.** Menyajikan daftar *library* yang dipakai beserta lisensinya sebagai bentuk transparansi dan kepatuhan terhadap aturan *open-source*.
5. **Menunjang reproducibility.** Memungkinkan pihak lain membangun ulang (*build*) aplikasi dari kode sumber bila diperlukan.

## 2.2 Sasaran

Sasaran yang ingin dicapai dari dokumen teknis ini meliputi:
- Penguji dapat memasang dan menjalankan aplikasi dalam waktu kurang dari 10 menit.
- Pengguna dapat menguasai alur penggunaan utama setelah membaca panduan sekali.
- Tidak terjadi kebocoran kredensial atau konfigurasi rahasia akibat proses instalasi yang salah.
- Seluruh komponen pihak ketiga terdokumentasi beserta lisensinya.

---

# BAB III — NILAI INOVASI DAN DAMPAK PEMANFAATAN PERANGKAT LUNAK

## 3.1 Deskripsi Singkat Perangkat Lunak

MindRest AI adalah aplikasi *mobile* Android yang membantu pengguna memahami diri sendiri dan meningkatkan kualitas tidur melalui pendekatan yang dipersonalisasi. Aplikasi ini mengumpulkan empat data utama dari pengguna — **tidur** (*sleep tracking*), **suasana hati** (*mood tracking*), **profil** pengguna, dan **jurnal harian** (*daily journal* via AI Chatbot) — lalu memanfaatkan kecerdasan buatan (Google Gemini) untuk menghasilkan **laporan Ikigai** dan **rekomendasi pengembangan diri** yang disesuaikan dengan karakteristik masing-masing individu.

## 3.2 Nilai Inovasi

MindRest AI memiliki beberapa nilai inovasi sebagai berikut.

1. **Integrasi holistik tidur–suasana hati–Ikigai.** Mayoritas aplikasi kesehatan tidur hanya berfokus pada satu aspek (mis. *sleep tracker* atau meditasi). MindRest AI mengintegrasikan empat dimensi dalam satu platform sehingga pengguna memperoleh gambaran utuh tentang keterkaitan tidur, emosi, refleksi harian, dan tujuan hidupnya.
2. **Pendekatan Ikigai yang dibantu AI.** Konsep Ikigai (alasan untuk bangun di pagi hari) yang bersifat filosofis diterjemahkan menjadi *assessment* terstruktur enam pertanyaan, lalu diproses AI menjadi laporan dan rekomendasi konkret yang dapat ditindaklanjuti.
3. **Journaling berbasis AI Chatbot yang empatik.** Alih-alih formulir jurnal tradisional, pengguna menulis curhatan melalui percakapan dengan AI Chatbot yang memberikan respons empatik, sehingga proses refleksi terasa ringan dan sederhana.
4. **Rekomendasi yang dipersonalisasi.** Rekomendasi pengembangan diri (FR-013) tidak bersifat umum, melainkan dibangun dari kombinasi data Ikigai, jurnal, suasana hati, dan tidur 7 hari terakhir milik pengguna.
5. **Sleep Insight kontekstual (FR-014).** Sistem memberikan rekomendasi aktivitas, makanan, dan minuman yang relevan dengan pola tidur pengguna.
6. **Arsitektur aman berbasis Supabase BaaS.** Data pengguna dilindungi *Row-Level Security* (RLS) sehingga tiap pengguna hanya dapat mengakses data miliknya. Kunci API AI tidak pernah terekspos di sisi klien karena panggilan AI diteruskan melalui *Edge Function* Supabase sebagai *proxy* aman.

## 3.3 Dampak Pemanfaatan

Pemanfaatan MindRest AI diharapkan memberikan dampak sebagai berikut.

- **Bagi pengguna:** meningkatkan kesadaran terhadap pola tidur dan suasana hati, memperoleh media refleksi yang sederhana, serta memperoleh arah pengembangan diri yang dipersonalisasi melalui konsep Ikigai.
- **Bagi mahasiswa dan pekerja muda:** solusi terjangkau yang dapat diakses kapan saja untuk membantu manajemen stres dan kualitas tidur tanpa biaya konsultasi profesional.
- **Bagi praktik pengembangan perangkat lunak:** menjadi rujukan implementasi arsitektur *mobile* + *BaaS* + *AI eksternal* yang aman, modular, dan mudah dipelihara.

Perlu ditegaskan bahwa MindRest AI **bukan** alat diagnosis klinis dan **tidak menggantikan** layanan tenaga medis profesional. Dampak yang diharapkan bersifat pendukung (*supportive*) terhadap kesejahteraan pengguna.

---

# BAB IV — DESKRIPSI FUNGSIONAL DAN PENJELASAN DETAIL FITUR

Bab ini memuat persyaratan sistem, arsitektur, deskripsi setiap fitur, serta panduan instalasi dan penggunaan MindRest AI.

## 4.1 Persyaratan Sistem

### 4.1.1 Sisi Pengguna (Perangkat)

| Komponen | Persyaratan Minimum |
|----------|---------------------|
| Sistem operasi | Android 8.0 (API level 26) atau lebih baru |
| Ruang penyimpanan | ± 100 MB untuk pemasangan |
| Koneksi internet | Wajib (Wi-Fi atau data seluler) |
| Akun email | Diperlukan untuk registrasi |
| Izin tambahan | Notifikasi (opsional, untuk pengingat) |

Aplikasi **tidak** memerlukan perangkat *wearable*, GPS, atau sensor khusus.

### 4.1.2 Sisi Pengembang (Untuk Build dari Sumber)

| Komponen | Versi / Keterangan |
|----------|--------------------|
| Android Studio | versi terbaru (disarankan Koala/Ladybug atau lebih baru) |
| JDK | Java 11 |
| Kotlin | 2.3.21 |
| Android Gradle Plugin (AGP) | 8.9.1 |
| compileSdk / targetSdk | 36 |
| minSdk | 26 |
| Build tools | Gradle (via `gradlew`) |
| Akun & kredensial | Supabase (URL + anon key + service role key), Google Gemini API key, Firebase (untuk App Check) |

## 4.2 Arsitektur Sistem

MindRest AI menggunakan arsitektur **klien–server**:
- **Klien:** aplikasi Android (Kotlin + Jetpack Compose).
- **Backend:** Supabase sebagai *Backend-as-a-Service* (PostgreSQL, Auth, PostgREST, Edge Functions).
- **Layanan AI:** Google Gemini, dipanggil melalui Supabase Edge Function agar kunci API tidak terekspos di klien.

Terdapat dua jalur komunikasi:
1. **Jalur Data CRUD** — klien mengakses database langsung melalui PostgREST yang dilindungi *Row-Level Security* (RLS).
2. **Jalur AI** — klien memanggil Edge Function; Edge Function memverifikasi JWT, memanggil Gemini, lalu menyimpan hasil ke database dengan *service role*.

## 4.3 Deskripsi Detail Fitur

MindRest AI memiliki 9 modul dengan 17 kebutuhan fungsional (FR-001–FR-017). Berikut penjelasan detail setiap fitur.

### 4.3.1 Authentication & Profile (FR-001, FR-002, FR-003)

| ID | Fungsi |
|----|--------|
| FR-001 | Pengguna dapat melakukan registrasi menggunakan email. |
| FR-002 | Pengguna dapat melakukan login dan logout. |
| FR-003 | Pengguna dapat melengkapi dan memperbarui profil pengguna. |

**Penjelasan.** Pengguna yang belum memiliki akun dapat mendaftar menggunakan email dan kata sandi pada layar Register. Sistem (Supabase Auth) akan mengirim tautan verifikasi ke email dan otomatis membuat baris profil pada tabel `profiles`. Setelah terverifikasi, pengguna melakukan login; sesi (JWT) disimpan agar tidak perlu login berulang. Tombol Logout menghapus sesi lokal. Pada layar Profile, pengguna dapat melengkapi data diri (nama tampilan, foto, tinggi/berat badan, pekerjaan, serta keluhan kesehatan) yang menjadi salah satu bahan personalisasi AI.

### 4.3.2 Sleep Tracking (FR-004, FR-005, FR-006)

| ID | Fungsi |
|----|--------|
| FR-004 | Pengguna dapat mencatat waktu tidur, waktu bangun, dan durasi tidur. |
| FR-005 | Sistem dapat menghitung durasi tidur berdasarkan data yang dicatat. |
| FR-006 | Pengguna dapat melihat riwayat data tidur. |

**Penjelasan.** Pada layar Sleep Tracking, pengguna mengisi jam tidur (`bed_time`), jam bangun (`wake_up_time`), dan kualitas tidur (POOR / FAIR / GOOD / EXCELLENT). Sistem menghitung durasi tidur secara otomatis dari selisih kedua waktu. Data disimpan ke tabel `sleep_logs` dan dapat ditinjau kembali pada layar riwayat beserta durasi dan kualitas tidur.

### 4.3.3 Mood Tracking (FR-007, FR-008)

| ID | Fungsi |
|----|--------|
| FR-007 | Pengguna dapat mencatat kondisi suasana hati. |
| FR-008 | Pengguna dapat melihat riwayat suasana hati. |

**Penjelasan.** Pengguna memilih skor suasana hati 1–5 (skala emoji) pada layar Mood Tracking atau melalui *bottom sheet* cepat dari beranda. Data disimpan ke tabel `mood_logs`. Riwayat ditampilkan sebagai deretan emoji beserta tanggal agar pola emosional terlihat.

### 4.3.4 Daily Journal dengan AI Chatbot (FR-009, FR-010, FR-011)

| ID | Fungsi |
|----|--------|
| FR-009 | Pengguna dapat melakukan journaling melalui AI Chatbot. |
| FR-010 | Pengguna dapat melihat riwayat jurnal. |
| FR-011 | Sistem dapat mengolah data dari jurnal yang dapat digunakan untuk analisis AI. |

**Penjelasan.** Layar AI Journal menyajikan antarmuka percakapan tempat pengguna menulis curhatan. Tidak ada formulir "buat jurnal" terpisah — seluruh aktivitas jurnal berlangsung melalui AI Chatbot yang memberikan respons empatik. Setiap percakapan disimpan ke tabel `journal_entries`. Konten jurnal diolah menjadi bahan analisis AI dan digunakan bersama data Ikigai untuk menghasilkan rekomendasi pengembangan diri (FR-013).

### 4.3.5 Ikigai (FR-012, FR-013)

| ID | Fungsi |
|----|--------|
| FR-012 | Pengguna dapat mengisi empat aspek Ikigai. |
| FR-013 | Sistem dapat menghasilkan rekomendasi pengembangan diri berdasarkan data Ikigai dan data Daily Journal. |

**Penjelasan.** Pengguna menjawab enam pertanyaan *assessment* (passion, skill, profesi, misi, *overthinking*, kepuasan hidup). Setelah disimpan, aplikasi memanggil Edge Function `generate-ikigai-report` yang: (1) memverifikasi JWT, (2) memeriksa *rate-limit* (maksimal 1 laporan per 24 jam), (3) merangkai prompt berisi jawaban *assessment* + data mood/sleep/jurnal 7 hari terakhir, (4) memanggil Gemini agar menghasilkan JSON terstruktur (`report_markdown`, `ikigai_circles`, 3–5 `recommendations`), dan (5) menyimpan hasil ke tabel `ikigai_reports`. Aplikasi menampilkan empat lingkaran Ikigai serta daftar rekomendasi yang dapat dicentang saat sudah dikerjakan.

### 4.3.6 Sleep Insight (FR-014)

| ID | Fungsi |
|----|--------|
| FR-014 | Sistem dapat memberikan rekomendasi aktivitas, makanan, dan minuman yang mendukung kualitas tidur pengguna. |

**Penjelasan.** Berdasarkan riwayat `sleep_logs`, sistem menampilkan Sleep Insight berupa rekomendasi aktivitas, makanan, dan minuman yang mendukung kualitas tidur (mis. "kualitas tidurmu menurun minggu ini; hindari kafein setelah sore dan tidur 30 menit lebih awal"). Pada versi 1.0 rekomendasi berbasis aturan lokal dan dapat diperkaya dengan AI pada iterasi berikutnya.

### 4.3.7 Dashboard (FR-015)

| ID | Fungsi |
|----|--------|
| FR-015 | Sistem dapat menampilkan ringkasan sleep trends, mood trends, sleep insight, daily journal, dan daily reminders. |

**Penjelasan.** Layar Home merangkum: tren tidur 7 hari, tren mood 7 hari, Sleep Insight terbaru, jurnal terakhir, dan daftar pengingat harian. Setiap *widget* memuat datanya sendiri sehingga kegagalan satu *widget* tidak mengganggu yang lain.

### 4.3.8 Notification (FR-016)

| ID | Fungsi |
|----|--------|
| FR-016 | Sistem dapat menampilkan notifikasi kepada pengguna. |

**Penjelasan.** Sistem menampilkan notifikasi dan pengingat pada layar Notifications, serta mengirim *push notification* lokal saat ada item baru.

### 4.3.9 Relaxation (FR-017)

| ID | Fungsi |
|----|--------|
| FR-017 | Pengguna dapat mengakses fitur relaksasi. |

**Penjelasan.** Layar Relaxation menyediakan audio dan panduan relaksasi sederhana (mis. *breathing exercise* dan musik relaksasi) yang tersedia secara statis di dalam aplikasi. Fitur ini tidak memerlukan koneksi internet sehingga dapat digunakan secara *offline*.

## 4.4 Panduan Instalasi

Instalasi dapat dilakukan dengan dua cara: **(A) untuk pengguna/penguji** melalui berkas APK, dan **(B) untuk pengembang** melalui *build* dari kode sumber.

### 4.4.1 Cara A — Instalasi melalui Berkas APK (Disarankan untuk Penguji)

1. **Siapkan perangkat Android** dengan sistem operasi Android 8.0 (API 26) atau lebih baru.
2. **Aktifkan Developer Options** di perangkat: buka *Settings → About phone*, lalu ketuk *Build number* sebanyak 7 kali hingga muncul pesan "You are now a developer".
3. **Aktifkan instalasi dari sumber tidak dikenal**: buka *Settings → System → Developer options*, aktifkan *USB debugging* (untuk instalasi via komputer) **atau** pada *Settings → Apps → Special access → Install unknown apps*, izinkan instalasi dari browser/penyimpanan (untuk instalasi langsung di HP).
4. **Salin berkas APK** (`app-release.apk` atau `app-debug.apk`) ke perangkat. Berkas tersedia pada tautan yang tercantum di **Lampiran A**.
5. **Buka berkas APK** dari aplikasi File Manager, ketuk *Install*, lalu *Open* setelah selesai.
6. **Verifikasi**: aplikasi MindRest AI muncul di daftar aplikasi dan dapat dibuka. Layar Splash akan tampil diikuti layar Onboarding/Login.

> Catatan: perangkat harus terhubung internet saat pertama kali menjalankan aplikasi agar proses registrasi/login berhasil.

### 4.4.2 Cara B — Build dari Kode Sumber (Untuk Pengembang)

1. **Pasang prasyarat:** Android Studio terbaru, JDK 11, Git.
2. **Unduh kode sumber** dari repositori tim (lihat Lampiran A untuk tautan).
3. **Buka proyek** di Android Studio: *File → Open → pilih folder proyek*. Tunggu Gradle *sync* selesai.
4. **Siapkan berkas konfigurasi** `.env` di root proyek (gunakan `.env.example` sebagai template):
   ```
   GEMINI_API_KEY=<kunci_api_gemini>
   SUPABASE_URL=https://<project-ref>.supabase.co
   SUPABASE_ANON_KEY=<anon_key>
   ```
5. **Jalankan di emulator/perangkat**: pilih *Run 'app'* pada Android Studio, atau gunakan *command line*:
   - Windows: `./gradlew.bat assembleDebug`
   - Linux/macOS: `./gradlew assembleDebug`
6. **Berkas output**: APK hasil *build* berada di `app/build/outputs/apk/debug/app-debug.apk`.

### 4.4.3 Konfigurasi Backend (Supabase) — Khusus Pengembang

Jika ingin menjalankan instansi backend sendiri, lakukan langkah berikut.
1. Buat proyek baru di Supabase.
2. Jalankan skema pada `supabase/schema.sql` dan migrasi pada `supabase/migrations/` melalui SQL Editor.
3. Aktifkan kebijakan RLS pada seluruh tabel.
4. *Deploy* Edge Function `generate-ikigai-report`:
   ```
   supabase functions deploy generate-ikigai-report
   supabase secrets set GEMINI_API_KEY=<kunci>
   supabase secrets set SUPABASE_SERVICE_ROLE_KEY=<service_role_key>
   ```
5. Perbarui nilai pada `.env` klien sesuai proyek Supabase Anda.

## 4.5 Panduan Penggunaan

Setelah aplikasi terpasang, berikut alur penggunaan lengkap.

1. **Registrasi (FR-001).** Buka aplikasi → pilih *Register* → isi email dan kata sandi → tekan *Daftar*. Buka email verifikasi dari Supabase → klik tautan verifikasi.
2. **Login (FR-002).** Kembali ke aplikasi → masuk dengan email dan kata sandi → beranda (Home) terbuka.
3. **Lengkapi Profil (FR-003).** Buka tab *Profile* → isi nama tampilan, tinggi/berat badan, pekerjaan, dan keluhan kesehatan → simpan.
4. **Catat Tidur (FR-004–FR-006).** Buka tab *Sleep* → *Sleep Tracking* → isi jam tidur, jam bangun, kualitas tidur → simpan. Durasi dihitung otomatis. Lihat *riwayat* untuk memantau pola.
5. **Catat Mood (FR-007–FR-008).** Dari beranda, tekan kartu Mood → pilih skor 1–5 → simpan. Lihat riwayat untuk melihat pola emosi.
6. **Journaling via AI Chatbot (FR-009–FR-011).** Buka *AI Journal* → tulis curhatan → kirim. AI membalas secara empatik. Riwayat tersimpan otomatis dan dapat dibuka kembali.
7. **Isi Ikigai (FR-012, FR-013).** Buka tab *Ikigai* → jawab enam pertanyaan *assessment* → simpan. Tunggu proses AI (layar *loading*). Laporan empat lingkaran Ikigai dan 3–5 rekomendasi ditampilkan; centang rekomendasi yang sudah dikerjakan.
8. **Sleep Insight (FR-014).** Buka tab *Sleep* → gulir ke bagian *Sleep Insight* → baca rekomendasi aktivitas, makanan, dan minuman.
9. **Dashboard (FR-015).** Beranda menampilkan ringkasan tren tidur, tren mood, Sleep Insight, jurnal terakhir, dan pengingat harian.
10. **Notifikasi (FR-016).** Buka layar *Notifications* untuk melihat pesan/pengingat; izinkan notifikasi saat diminta.
11. **Relaxation (FR-017).** Buka tab *Relaxation* → pilih audio/panduan → gunakan kapan saja, termasuk offline.
12. **Logout (FR-002).** Buka *Profile* → *Logout* untuk mengakhiri sesi.

---

# BAB V — SCREENSHOT PERANGKAT LUNAK

Bab ini menyajikan tangkapan layar antarmuka MindRest AI. Setiap gambar dilengkapi keterangan singkat dan fitur (FR) terkait. (*Tempat penyisipan berkas gambar ditandai kotak `[Gambar ...]`; berkas gambar disertakan terpisah dalam paket dokumen.*)

| No. | Gambar | Layar / Fungsi | FR Terkait |
|-----|--------|----------------|------------|
| 5.1 | `[Gambar 5.1]` | **Splash Screen** — layar pembuka dengan logo MindRest AI. | — |
| 5.2 | `[Gambar 5.2]` | **Onboarding** — pengenalan fitur aplikasi. | — |
| 5.3 | `[Gambar 5.3]` | **Register** — formulir pendaftaran email. | FR-001 |
| 5.4 | `[Gambar 5.4]` | **Login** — formulir masuk. | FR-002 |
| 5.5 | `[Gambar 5.5]` | **Home / Dashboard** — ringkasan tren tidur, tren mood, Sleep Insight, jurnal terakhir, pengingat harian. | FR-015 |
| 5.6 | `[Gambar 5.6]` | **Notifications** — daftar notifikasi dan pengingat. | FR-016 |
| 5.7 | `[Gambar 5.7]` | **Mood Tracking** — pencatatan dan riwayat suasana hati. | FR-007, FR-008 |
| 5.8 | `[Gambar 5.8]` | **AI Journal (Chatbot)** — antarmuka percakapan jurnal. | FR-009, FR-010 |
| 5.9 | `[Gambar 5.9]` | **Journal History** — riwayat percakapan/jurnal. | FR-010 |
| 5.10 | `[Gambar 5.10]` | **Sleep Tracking** — input waktu tidur/bangun dan kualitas. | FR-004, FR-005 |
| 5.11 | `[Gambar 5.11]` | **Sleep History & Sleep Insight** — riwayat tidur + rekomendasi. | FR-006, FR-014 |
| 5.12 | `[Gambar 5.12]` | **Ikigai Assessment** — enam pertanyaan aspek Ikigai. | FR-012 |
| 5.13 | `[Gambar 5.13]` | **Ikigai Report** — empat lingkaran Ikigai + rekomendasi. | FR-013 |
| 5.14 | `[Gambar 5.14]` | **Relaxation** — audio/panduan relaksasi. | FR-017 |
| 5.15 | `[Gambar 5.15]` | **Profile** — kelola profil pengguna. | FR-003 |

**Catatan penyisipan gambar:** untuk setiap baris, sisipkan tangkapan layar ukuran penuh (rasio 9:16, resolusi minimal 720×1280) di bawah baris tabel yang bersangkutan dengan keterangan *Gambar 5.x — <nama layar>*.

---

# LAMPIRAN A — EXECUTABLE FILE / URL APLIKASI

MindRest AI adalah perangkat lunak berbasis **Android (stand-alone)**, sehingga kelengkapan pengiriman berupa **executable file (.apk)**.

| Jenis | Keterangan |
|-------|------------|
| Platform | Android (stand-alone / mobile) |
| Nama berkas | `app-release.apk` (versi rilis) atau `app-debug.apk` (versi uji) |
| *Application ID* | `com.aistudio.mindrest.eedcdb` |
| Versi | 1.0 (versionCode 1) |
| Tautan unduh APK | `[SISIHKAN TAUTAN]` — *Google Drive / repo tim* |
| Tautan repositori (opsional) | `[SISIHKAN TAUTAN]` |
| Kata sandi (jika ada) | `[SISIHKAN jika berlaku]` |

> Petunjuk pemasangan berkas APK di atas mengikuti langkah pada **Subbab 4.4.1**.

---

# LAMPIRAN B — URL VIDEO DEMO PERANGKAT LUNAK

Video demo memperlihatkan alur penggunaan MindRest AI dari registrasi hingga memperoleh laporan Ikigai dan rekomendasi pengembangan diri.

| Item | Keterangan |
|------|------------|
| Judul video | MindRest AI — Demo Penggunaan (versi 1.0) |
| Durasi | ± 3–5 menit |
| Platform | YouTube (tidak terdaftar / *unlisted*) |
| URL | `[SISIHKAN URL VIDEO DEMO]` |

**Isi video demo (usulan adegan):**
1. Pembukaan — logo dan deskripsi singkat aplikasi.
2. Registrasi & Login (FR-001, FR-002).
3. Melengkapi profil (FR-003).
4. Pencatatan tidur & perhitungan durasi otomatis (FR-004–FR-006).
5. Pencatatan mood & riwayat (FR-007, FR-008).
6. Journaling via AI Chatbot (FR-009–FR-011).
7. Pengisian Ikigai & penayangan laporan + rekomendasi AI (FR-012, FR-013).
8. Sleep Insight (FR-014) dan Dashboard (FR-015).
9. Notifikasi (FR-016) dan Relaxation (FR-017).

---

# LAMPIRAN C — DAFTAR KOMPONEN (SOFTWARE LIBRARY) BESERTA LISENSI

Berikut daftar komponen/*library* pihak ketiga yang digunakan dalam MindRest AI, beserta lisensinya. Lisensi ini berlaku untuk penggunaan sesuai ketentuan masing-masing komponen.

## C.1 Komponen Sisi Klien (Android)

| No. | Komponen / Library | Versi | Fungsi | Lisensi |
|-----|--------------------|-------|--------|---------|
| 1 | Kotlin | 2.3.21 | Bahasa pemrograman utama | Apache License 2.0 |
| 2 | Android Gradle Plugin (AGP) | 8.9.1 | Sistem *build* Android | Apache License 2.0 |
| 3 | AndroidX Core KTX | 1.18.0 | Ekstensi Kotlin AndroidX Core | Apache License 2.0 |
| 4 | AndroidX Activity Compose | 1.10.1 | Integrasi Activity + Compose | Apache License 2.0 |
| 5 | AndroidX Lifecycle (runtime, viewmodel-compose) | 2.8.7 | Manajemen *lifecycle* & ViewModel | Apache License 2.0 |
| 6 | Jetpack Compose (BOM + UI + Material3) | BOM 2024.09.00 | Toolkit UI deklaratif | Apache License 2.0 |
| 7 | AndroidX Navigation Compose | 2.8.9 | Navigasi antar layar | Apache License 2.0 |
| 8 | AndroidX Room (runtime, ktx, compiler) | 2.7.0 | Database lokal (offline cache) | Apache License 2.0 |
| 9 | Kotlinx Coroutines (core, android) | 1.10.2 | Pemrograman asinkron | Apache License 2.0 |
| 10 | Kotlinx Serialization JSON | 1.7.3 | Serialisasi JSON | Apache License 2.0 |
| 11 | Retrofit | 2.12.0 | Klien HTTP berbasis anotasi | Apache License 2.0 |
| 12 | OkHttp (okhttp + logging-interceptor) | 4.10.0 | Klien HTTP & *logging* | Apache License 2.0 |
| 13 | Moshi (moshi-kotlin + codegen + converter) | 1.15.2 | Parser JSON | Apache License 2.0 |
| 14 | Supabase Kotlin (postgrest-kt, auth-kt, functions-kt) | 3.5.0 | SDK klien Supabase | MIT License |
| 15 | Ktor Client (ktor-client-okhttp) | 3.0.1 | *HTTP engine* untuk Supabase SDK | Apache License 2.0 |
| 16 | Firebase BOM (firebase-ai, appcheck-recaptcha) | 34.15.0 | Layanan AI & App Check | Apache License 2.0 |
| 17 | Google Services Plugin | 4.5.0 | Konfigurasi Firebase | Apache License 2.0 |
| 18 | Secrets Gradle Plugin | 2.0.1 | Manajemen kredensial dari `.env` | Apache License 2.0 |
| 19 | Google Devtools KSP | 2.3.5 | Pemroses anotasi (Room, Moshi) | Apache License 2.0 |
| 20 | Coil (opsional) | 2.7.0 | Pemuat gambar | Apache License 2.0 |

## C.2 Komponen Sisi Server (Supabase Edge Functions / Deno)

| No. | Komponen / Library | Versi | Fungsi | Lisensi |
|-----|--------------------|-------|--------|---------|
| 21 | Deno Runtime | (terkelola Supabase) | Runtime Edge Function | MIT License |
| 22 | `@supabase/supabase-js` | ^2.45 | Klien Supabase (verify JWT + DB) | MIT License |
| 23 | `@google/generative-ai` (Gemini SDK) | ^0.21 | Panggilan Google Gemini API | Apache License 2.0 |

## C.3 Layanan Eksternal (Bukan Library)

| No. | Layanan | Fungsi | Keterangan |
|-----|---------|--------|------------|
| 24 | Supabase (BaaS) | Database PostgreSQL, Auth, PostgREST, Edge Functions, RLS | Layanan; lisensi terkelola oleh Supabase Inc. |
| 25 | Google Gemini API | Layanan AI generatif (chatbot, laporan Ikigai, Sleep Insight) | Layanan; tunduk ketentuan Google AI |

## C.4 Catatan Lisensi

- Seluruh *library* di atas menggunakan lisensi *open-source* permisif (Apache 2.0 dan MIT) yang mengizinkan penggunaan, modifikasi, dan distribusi, termasuk untuk perangkat lunak dengan tujuan komersial/akademik, selama pemberitahuan hak cipta dipertahankan.
- Atribusi lisensi masing-masing komponen dapat dilihat pada repositori resmi atau berkas `LICENSE` pada proyek *library* yang bersangkutan.
- Penggunaan layanan eksternal (Supabase, Google Gemini) tunduk pada Ketentuan Layanan dan kebijakan kuota dari masing-masing penyedia.

---

# LAMPIRAN D — SPESIFIKASI TEKNIS & KONFIGURASI

## D.1 Ringkasan Teknologi

| Aspek | Teknologi |
|-------|-----------|
| Bahasa (klien) | Kotlin 2.3.21 |
| UI | Jetpack Compose |
| Platform | Android (minSdk 26 / Android 8.0; targetSdk & compileSdk 36) |
| Backend | Supabase (PostgreSQL + Auth + PostgREST + Edge Functions) |
| Runtime Edge Function | Deno (TypeScript) |
| Layanan AI | Google Gemini (`gemini-3.5-flash`) |
| Sistem *build* | Gradle (AGP 8.9.1) |
| Keamanan data | Row-Level Security (RLS) + JWT (Supabase Auth) |
| Keamanan API AI | Edge Function sebagai *proxy* (kunci API tidak diekspos ke klien) |

## D.2 Struktur Navigasi Aplikasi

- **Graf autentikasi:** `splash` → `onboarding` → `login`/`register`.
- **Tab utama (*bottom bar*):** `home`, `sleep`, `relaxation`, `ikigai`, `profile`.
- **Layar turunan:** `journal`, `ai_journal`, `mood_tracking`, `sleep_tracking`, `lifestyle`, `notifications`, `reminder`, `statistics`, `settings`, `achievements`, `ikigai_assessment`, `ikigai_report_loading`, `ikigai_report`.

## D.3 Struktur Tabel Database Inti

| Tabel | Fungsi |
|-------|--------|
| `profiles` | Data profil & onboarding kesehatan (FR-003). |
| `mood_logs` | Catatan suasana hati (FR-007–FR-008). |
| `sleep_logs` | Catatan tidur: bed_time, wake_up_time, sleep_quality (FR-004–FR-006). |
| `journal_entries` | Isi jurnal dari percakapan AI Chatbot (FR-009–FR-011). |
| `ikigai_assessments` | Jawaban enam aspek Ikigai (FR-012). |
| `ikigai_reports` | Hasil laporan & rekomendasi AI (FR-013). |

## D.4 Catatan Keamanan

- Setiap tabel dilindungi RLS; pengguna hanya dapat mengakses data miliknya.
- Kunci `GEMINI_API_KEY` dan `SUPABASE_SERVICE_ROLE_KEY` hanya disimpan sebagai *secret* Supabase dan **tidak pernah** dimasukkan ke klien.
- Komunikasi klien–server menggunakan HTTPS.

---

*Akhir Dokumen Teknis.*
