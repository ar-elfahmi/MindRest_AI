# Dokumen Teknis — Proposal & Spesifikasi Pengembangan Perangkat Lunak
# MindRest AI: Ikigai-Driven Sleep Therapy AI Assistant

> **Catatan Penyelarasan (Revisi).** Dokumen ini adalah versi yang telah dirapikan dan diselaraskan dari *srs revisi.docx* agar konsisten dengan (1) implementasi aplikasi yang sebenarnya dan (2) daftar 17 Functional Requirement (FR-001–FR-017) yang ditetapkan sebagai ruang lingkup pengembangan. Beberapa inkonsistensi pada versi sebelumnya telah diselesaikan sebagai berikut:
>
> - **Arsitektur diseragamkan menjadi Supabase BaaS.** Versi lama masih menyebut "Node.js sebagai backend pada server terpisah". Implementasi aktual tidak menggunakan server Node.js terpisah; seluruh backend dilayani oleh Supabase (PostgreSQL + PostgREST + Auth + Edge Functions). Aplikasi Android berkomunikasi langsung dengan database melalui PostgREST yang dilindungi *Row-Level Security* (RLS), sedangkan panggilan AI (Google Gemini) diteruskan melalui *Edge Function* Supabase sebagai *proxy* aman.
> - **Daily Journal hanya melalui AI Chatbot.** Tidak ada formulir "buat jurnal" terpisah; seluruh aktivitas jurnal berlangsung dalam antarmuka percakapan (FR-009). AI Chatbot merupakan bagian dari modul Daily Journal, bukan modul pendukung tersendiri.
> - **Sleep Insight (FR-014)** adalah fitur rekomendasi aktivitas, makanan, dan minuman yang mendukung kualitas tidur — bukan label "AI Sleep" generik.
> - **Riwayat diperjelas:** FR-006 (riwayat tidur) dan FR-008 (riwayat suasana hati) merupakan layar riwayat eksplisit.
> - **Catatan goresan/scratch note** yang tersebar di dokumen lama (mis. "untuk crud langsung ke supabase", "apakah backend dipisah", "pake fitur ikigai") telah dihapus dan diganti dengan deskripsi definitif.

---

## DAFTAR ISI
- BAB I — Latar Belakang
- BAB II — Batasan Perangkat Lunak yang Dikembangkan
- BAB III — Metodologi Pengembangan
- BAB IV — Analisis Kebutuhan dan Desain Solusi
- BAB V — Implementasi Perangkat Lunak
- BAB VI — Antarmuka (Mockup) Perangkat Lunak
- BAB VII — Dokumentasi Penggunaan
- BAB VIII — Penutup
- Daftar Pustaka

---

# BAB I — LATAR BELAKANG

## 1.1 Latar Belakang Ide Perangkat Lunak

Gangguan tidur menjadi salah satu permasalahan yang semakin banyak dialami oleh masyarakat, khususnya mahasiswa dan pekerja muda. Padatnya aktivitas, tekanan akademik maupun pekerjaan, serta kebiasaan penggunaan gawai sebelum tidur menyebabkan penurunan kualitas dan durasi tidur. Kondisi ini berdampak pada menurunnya produktivitas, ketidakstabilan emosi, dan peningkatan risiko gangguan kesehatan mental jangka panjang.

Berbagai solusi digital telah dikembangkan untuk membantu mengatasi gangguan tidur, seperti *sleep tracker*, aplikasi meditasi, maupun *Digital Cognitive Behavioral Therapy for Insomnia* (dCBT-I). Salah satu contoh pendekatan yang terbukti efektif adalah dCBT-I yang menunjukkan penurunan gejala insomnia, depresi, dan kecemasan secara signifikan (Lee dkk., 2023; Espie dkk., 2019). Namun, sebagian besar aplikasi yang tersedia masih berfokus pada satu aspek, misalnya hanya mencatat waktu tidur atau hanya menyediakan audio relaksasi. Solusi yang holistik — yang menghubungkan pola tidur, suasana hati, refleksi harian, serta tujuan hidup — masih jarang ditemukan.

Menurut hasil studi literatur, observasi terhadap aplikasi *sleep wellness*, serta proses brainstorming yang dilakukan tim, ditemukan bahwa kualitas tidur tidak hanya dipengaruhi oleh kebiasaan tidur, tetapi juga oleh kondisi psikologis, tingkat stres, dan rasa memiliki tujuan hidup. Konsep **Ikigai** — yaitu alasan untuk bangun di pagi hari — terbukti berasosiasi dengan kesejahteraan kesehatan jangka panjang (Chen dkk., 2022) serta berpotensi memprediksi tingkat kecemasan, depresi, dan kesejahteraan secara umum (Wilkes dkk., 2023). Studi juga menyebutkan bahwa gangguan tidur berhubungan dengan kebiasaan merenung (*overthinking*) dan ketidakjelasan arah hidup, sehingga pendekatan yang mengintegrasikan refleksi diri dengan pemantauan tidur dinilai menjanjikan (Springer, 2015).

Berdasarkan kondisi tersebut, tim mengembangkan sebuah aplikasi terapi tidur berbasis *Artificial Intelligence* yang mengintegrasikan **Sleep Therapy** dengan pendekatan **Ikigai** dalam satu platform bernama **MindRest AI**. Aplikasi ini membantu pengguna memahami diri sendiri melalui data tidur, suasana hati, profil, dan jurnal harian, kemudian memanfaatkan kecerdasan buatan untuk menghasilkan rekomendasi pengembangan diri yang dipersonalisasi.

## 1.2 Tujuan dan Manfaat Dikembangkannya Perangkat Lunak

Tujuan dari pengembangan MindRest AI adalah menghadirkan sebuah solusi digital yang membantu pengguna meningkatkan kualitas tidur melalui pendekatan yang lebih personal. Dengan mengintegrasikan Sleep Tracking, Mood Tracking, Daily Journal berbasis AI Chatbot, dan refleksi Ikigai, aplikasi ini bertujuan untuk memberikan wawasan yang utuh mengenai kondisi pengguna serta rekomendasi yang relevan dengan karakteristik masing-masing individu.

Manfaat yang diharapkan dari pengembangan MindRest AI adalah membantu meningkatkan kualitas tidur serta mendukung kesejahteraan pengguna melalui layanan pendampingan yang lebih personal. Integrasi Sleep Therapy dan Ikigai memungkinkan pengguna tidak hanya mencatat kebiasaan tidurnya, tetapi juga memahami keterkaitan antara tidur, suasana hati, dan tujuan hidupnya, sehingga dapat membentuk kebiasaan yang lebih sehat secara berkelanjutan.

---

# BAB II — BATASAN PERANGKAT LUNAK YANG DIKEMBANGKAN

## 2.1 Batasan Fungsional

### 2.1.1 Fitur yang Diimplementasikan

MindRest AI menyediakan fitur **Authentication & Profile**, **Sleep Tracking**, **Mood Tracking**, **Daily Journal** (berbasis AI Chatbot), **Ikigai**, **Sleep Insight**, **Dashboard**, **Notification**, dan **Relaxation**. Seluruh fitur tersebut dirumuskan ke dalam 17 Functional Requirement (FR-001–FR-017) yang dijelaskan pada Bab IV. Fitur AI (rekomendasi pengembangan diri berbasis Ikigai, respons AI Chatbot pada jurnal, dan Sleep Insight) menggunakan data pengguna sebagai bahan dasar pemrosesan agar rekomendasi bersifat personal.

### 2.1.2 Fitur yang Tidak Diimplementasikan

MindRest AI tidak dirancang untuk melakukan **diagnosis klinis** terhadap gangguan tidur maupun gangguan kesehatan mental. Sistem tidak menyediakan konsultasi darurat, konsultasi dengan tenaga medis profesional, maupun layanan krisis kesehatan mental. Aplikasi juga tidak menggantikan peran pengobatan atau terapi profesional. Rekomendasi yang diberikan bersifat pendukung (*supportive*) dan tidak boleh dijadikan satu-satunya dasar pengambilan keputusan medis. Selain itu, aplikasi tidak mengintegrasikan perangkat *wearable* maupun perangkat medis.

## 2.2 Batasan Teknis

### 2.2.1 Platform dan Teknologi Aplikasi

MindRest AI dikembangkan sebagai aplikasi *mobile* pada platform **Android** menggunakan bahasa **Kotlin** dan toolkit UI deklaratif **Jetpack Compose**. Implementasi dibatasi pada sistem operasi Android sehingga belum mencakup pengembangan khusus untuk iOS maupun platform web. Aplikasi didistribusikan melalui berkas *Android Package* (APK) yang dipasang secara *sideload* pada perangkat Android yang didukung.

### 2.2.2 Backend dan Penyimpanan Data

Backend MindRest AI sepenuhnya dilayani oleh **Supabase** sebagai *Backend-as-a-Service* (BaaS). Supabase menyediakan:
- **Database** PostgreSQL yang di-*host* dan dikelola oleh Supabase.
- **Autentikasi** berbasis email/sandi dengan sesi JWT.
- **PostgREST API** sebagai jalur *Create/Read/Update/Delete* (CRUD) yang diakses langsung oleh aplikasi Android.
- **Row-Level Security (RLS)** yang membatasi setiap pengguna hanya dapat mengakses data miliknya sendiri.

Aplikasi Android berperan sebagai *client* dan mengakses database secara langsung melalui PostgREST API yang dilindungi RLS. Tidak terdapat server Node.js terpisah untuk menangani logika aplikasi.

### 2.2.3 Layanan Kecerdasan Buatan

Fitur berbasis AI menggunakan **Google Gemini API** sebagai layanan AI eksternal. Layanan tersebut mendukung tiga fungsi utama: (1) respons empatik pada **AI Chatbot** dalam Daily Journal (FR-009/FR-011), (2) rekomendasi pengembangan diri berbasis **Ikigai** dan data Daily Journal (FR-013), serta (3) materi pendukung untuk **Sleep Insight** (FR-014). Penggunaan Gemini dibatasi oleh kuota dan kebijakan layanan, sehingga hasil yang dihasilkan perlu dipahami sebagai dukungan dan bukan diagnosis.

Panggilan ke Gemini **tidak dilakukan langsung dari aplikasi Android**. Sebagai gantinya, aplikasi memanggil **Supabase Edge Function** (berbasis Deno/TypeScript) yang berperan sebagai *proxy* aman: Edge Function menerima *request* dari *client*, memverifikasi identitas pengguna melalui JWT, memanggil Gemini API dengan *service role key*, lalu menyimpan hasilnya ke database. Pola ini memastikan kunci API Gemini tidak pernah terekspos di sisi *client*.

Pengembangan fitur AI mempertimbangkan pendekatan **Cognitive Behavioral Therapy for Insomnia (CBT-I)** sebagai salah satu acuan dalam penyusunan rekomendasi terkait tidur. Pendekatan tersebut digunakan untuk memandu arah prompt dan struktur rekomendasi, bukan sebagai protokol klinis formal.

### 2.2.4 Batasan Integrasi

MindRest AI tidak mengintegrasikan perangkat *wearable* maupun perangkat medis untuk memperoleh data tidur secara otomatis. Data tidur diperoleh melalui **input manual** pengguna pada aplikasi (waktu tidur, waktu bangun, kualitas tidur), sehingga pengambilan data bergantung pada kesadaran dan ketelitian pengguna. Aplikasi juga tidak melakukan integrasi dengan kalender eksternal, layanan musik pihak ketiga, maupun media sosial.

---

# BAB III — METODOLOGI PENGEMBANGAN

## 3.1 Metode Pengembangan Perangkat Lunak

Pengembangan MindRest AI menggunakan pendekatan **Agile** dengan *framework* **Scrum**. Pendekatan ini dipilih karena memungkinkan produk dikembangkan secara bertahap dan disesuaikan berdasarkan hasil dari setiap siklus. Dalam Scrum, **Sprint** menjadi wadah pengembangan untuk menghasilkan bertahap (*increment*) produk yang dapat digunakan. Tim secara berkala meninjau hasil dan menyesuaikan rencana sesuai prioritas.

Pada pengembangan MindRest AI, proses dimulai dengan **analisis kebutuhan** dan **perancangan produk** sebelum dilanjutkan ke pengembangan fungsi aplikasi. Pekerjaan yang telah dirumuskan kemudian dikelola melalui *Product Backlog* dan dikembangkan secara bertahap dalam beberapa Sprint. Setiap Sprint menghasilkan *increment* yang dapat dievaluasi melalui *Sprint Review*, sementara cara kerja tim dievaluasi melalui *Sprint Retrospective*.

## 3.2 Tahapan Pengembangan Perangkat Lunak

Pengembangan MindRest AI mengikuti empat tahapan utama: **Requirement Analysis**, **Product Backlog**, **Sprint Planning**, dan **Sprint**. Tahapan tersebut digunakan untuk menyusun kebutuhan, menentukan pekerjaan, serta mengatur pelaksanaan pengembangan fitur. Hasil pengembangan kemudian dievaluasi melalui *Daily Scrum*, *Sprint Review*, dan *Sprint Retrospective*.

### 3.2.1 Requirement Analysis

Tahap *Requirement Analysis* dilakukan untuk mengidentifikasi permasalahan dan kebutuhan calon pengguna. Informasi dikumpulkan melalui studi literatur, *brainstorming* tim, dan kuesioner kepada 30 responden yang merupakan mahasiswa berusia 18–25 tahun, pernah mengalami gangguan tidur, dan aktif menggunakan *smartphone*. Hasil analisis disusun dalam *Product Requirements Document* (PRD) yang berisi tujuan produk, kebutuhan pengguna, dan gambaran fitur. Kebutuhan tersebut digambarkan melalui Use Case, kemudian diterjemahkan ke dalam rancangan UI/UX menggunakan Figma.

### 3.2.2 Product Backlog

Kebutuhan dan fitur yang telah dirumuskan selanjutnya disusun ke dalam *Product Backlog*. Daftar tersebut digunakan untuk memetakan urutan pengembangan berdasarkan prioritas dan ketergantungan antarfitur.

**Tabel 3.1 Product Backlog MindRest AI**

| ID | Product Backlog Item | Prioritas | Sprint |
|----|----------------------|-----------|--------|
| PB-01 | Persiapan produk, penyusunan PRD, perancangan Figma, dan implementasi awal UI | Tinggi | Sprint 0 |
| PB-02 | Pencatatan waktu tidur dan waktu bangun | Tinggi | Sprint 1 |
| PB-03 | Perhitungan dan riwayat data tidur | Tinggi | Sprint 1 |
| PB-04 | Pencatatan suasana hati | Tinggi | Sprint 2 |
| PB-05 | Journaling melalui AI Chatbot dan riwayat jurnal | Tinggi | Sprint 3 |
| PB-06 | Pengisian empat aspek Ikigai | Tinggi | Sprint 4 |
| PB-07 | Rekomendasi pengembangan diri berbasis Ikigai dan Daily Journal | Tinggi | Sprint 4 |
| PB-08 | Pembuatan Sleep Insight | Sedang | Sprint 5 |
| PB-09 | Dashboard, Notifikasi, dan Relaxation | Sedang | Sprint 5 |

### 3.2.3 Sprint Planning

*Sprint Planning* digunakan untuk menentukan pekerjaan yang akan dikerjakan dalam suatu Sprint. Tim memilih fitur berdasarkan prioritas yang telah ditetapkan dalam *Product Backlog*, kemudian menguraikannya menjadi tugas yang lebih spesifik agar target dapat dicapai. Pada MindRest AI, Use Case, desain UI/UX, dan struktur database menjadi acuan dalam menguraikan tugas.

### 3.2.4 Sprint

*Sprint* merupakan siklus kerja untuk menyelesaikan pekerjaan yang telah direncanakan. Pada MindRest AI, kegiatan di dalamnya meliputi implementasi *logic*, pengembangan fungsi, integrasi dengan Supabase, dan pengujian sesuai target. Setiap Sprint ditinjau melalui tiga kegiatan berikut.

1. **Daily Scrum** — memantau perkembangan pekerjaan selama Sprint. Anggota tim menyampaikan pekerjaan yang telah dikerjakan dan kendala yang ditemukan. Hasil koordinasi digunakan untuk menyesuaikan pekerjaan agar tetap sesuai target.
2. **Sprint Review** — meninjau hasil pekerjaan pada akhir Sprint. Tim mendemonstrasikan hasil pengembangan untuk melihat kesesuaiannya dengan target. Masukan yang diperoleh menjadi bahan pertimbangan untuk pekerjaan selanjutnya.
3. **Sprint Retrospective** — meninjau cara kerja tim selama Sprint. Tim membahas kendala, koordinasi, pembagian tugas, dan hal-hal yang berjalan dengan baik untuk memperbaiki cara kerja pada Sprint berikutnya.

---

# BAB IV — ANALISIS KEBUTUHAN DAN DESAIN SOLUSI

## 4.1 Analisis Kebutuhan

Pengumpulan kebutuhan dilakukan melalui studi literatur, *brainstorming* tim, dan kuesioner terhadap 30 responden dengan kriteria mahasiswa berusia 18–25 tahun, pernah mengalami gangguan tidur, dan aktif menggunakan *smartphone*. Studi literatur mencakup hubungan tidur, kesehatan mental, *journaling*, AI, dan konsep Ikigai. Kuesioner disusun dalam empat bagian: kondisi kualitas tidur, Daily Journal, pengembangan diri (Ikigai), dan konsep aplikasi.

**Tabel 4.1 Ringkasan Hasil Kuesioner Analisis Kebutuhan**

| Bagian | Tema | Contoh Pernyataan |
|--------|------|-------------------|
| 1. Kualitas Tidur | Persepsi kualitas tidur yang rendah, kelelahan saat bangun, kesulitan mengetahui penyebab, kebutuhan rekomendasi yang dipersonalisasi | "Saya membutuhkan rekomendasi tidur yang disesuaikan dengan kondisi pribadi saya." |
| 2. Daily Journal | Jarang menuliskan pengalaman harian, lupa kondisi emosi sebelumnya, butuh media refleksi yang sederhana | "Saya bersedia melakukan journaling apabila prosesnya sederhana dan hanya membutuhkan waktu singkat." |
| 3. Pengembangan Diri (Ikigai) | Bingung menentukan arah hidup, aktivitas kurang bermakna, butuh rekomendasi yang sesuai potensi | "Saya tertarik apabila AI dapat memberikan rekomendasi pengembangan diri berdasarkan aktivitas dan catatan harian saya." |
| 4. Konsep Aplikasi | Keyakinan bahwa psikologis, stres, dan tujuan hidup memengaruhi tidur; ketertarikan pada platform terintegrasi | "Saya tertarik menggunakan aplikasi yang menggabungkan pemantauan tidur, journaling, AI, dan rekomendasi pengembangan diri." |

Berdasarkan hasil kuesioner, kebutuhan pengguna dikelompokkan menjadi empat permasalahan utama: **kualitas tidur**, **refleksi diri**, **pengembangan diri**, dan **kebutuhan terhadap integrasi beberapa fitur dalam satu aplikasi**. Hasil tersebut menghasilkan tiga kebutuhan utama sebagai dasar pengembangan MindRest AI: (1) rekomendasi kualitas tidur yang dipersonalisasi, (2) media refleksi diri yang sederhana, dan (3) pendampingan pengembangan diri melalui konsep Ikigai. Ketiga kebutuhan tersebut diterjemahkan menjadi modul-modul aplikasi yang dirinci pada subbab berikut.

## 4.2 Kebutuhan Sistem

### 4.2.1 Daftar Modul dan Functional Requirement

Functional Requirement disusun berdasarkan modul utama yang terdapat pada MindRest AI. Setiap kebutuhan dirumuskan secara spesifik untuk menggambarkan fungsi yang dapat dilakukan oleh pengguna maupun sistem. Prioritas kebutuhan ditentukan menggunakan metode **MoSCoW** yang terdiri atas **Must**, **Should**, dan **Could**. Total terdapat 17 kebutuhan fungsional yang menjadi ruang lingkup pengembangan.

**Tabel 4.2 Daftar Modul dan Functional Requirement**

| Modul | ID | Functional Requirement | Prioritas |
|-------|----|------------------------|-----------|
| Authentication & Profile | FR-001 | Pengguna dapat melakukan registrasi menggunakan email. | Must |
| Authentication & Profile | FR-002 | Pengguna dapat melakukan login dan logout. | Must |
| Authentication & Profile | FR-003 | Pengguna dapat melengkapi dan memperbarui profil pengguna. | Should |
| Sleep Tracking | FR-004 | Pengguna dapat mencatat waktu tidur, waktu bangun, dan durasi tidur. | Must |
| Sleep Tracking | FR-005 | Sistem dapat menghitung durasi tidur berdasarkan data yang dicatat. | Must |
| Sleep Tracking | FR-006 | Pengguna dapat melihat riwayat data tidur. | Must |
| Mood Tracking | FR-007 | Pengguna dapat mencatat kondisi suasana hati. | Must |
| Mood Tracking | FR-008 | Pengguna dapat melihat riwayat suasana hati. | Must |
| Daily Journal | FR-009 | Pengguna dapat melakukan journaling melalui AI Chatbot. | Must |
| Daily Journal | FR-010 | Pengguna dapat melihat riwayat jurnal. | Must |
| Daily Journal | FR-011 | Sistem dapat mengolah data dari jurnal yang dapat digunakan untuk analisis AI. | Must |
| Ikigai | FR-012 | Pengguna dapat mengisi empat aspek Ikigai. | Must |
| Ikigai | FR-013 | Sistem dapat menghasilkan rekomendasi pengembangan diri berdasarkan data Ikigai dan data Daily Journal. | Must |
| Sleep Insight | FR-014 | Sistem dapat memberikan rekomendasi aktivitas, makanan, dan minuman yang mendukung kualitas tidur pengguna. | Should |
| Dashboard | FR-015 | Sistem dapat menampilkan ringkasan sleep trends, mood trends, sleep insight, daily journal, dan daily reminders. | Must |
| Notification | FR-016 | Sistem dapat menampilkan notifikasi kepada pengguna. | Could |
| Relaxation | FR-017 | Pengguna dapat mengakses fitur relaksasi. | Could |

### 4.2.2 Non-Functional Requirement

Kebutuhan non-fungsional disusun berdasarkan karakteristik kualitas perangkat lunak **ISO/IEC 25010:2011** yang relevan dengan MindRest AI, meliputi *Functional Suitability*, *Performance Efficiency*, *Usability*, *Reliability*, *Security*, *Compatibility*, *Maintainability*, dan *Portability*. Aspek *Security*, *Usability*, dan *Performance Efficiency* mendapat penekanan khusus karena aplikasi menangani data pribadi dan memanfaatkan layanan AI.

**Tabel 4.3 Non-Functional Requirement**

| Kategori | ID | Non-Functional Requirement |
|----------|----|----------------------------|
| Functional Suitability | NFR-001 | Fitur inti tersedia sesuai kebutuhan yang ditetapkan. |
| Functional Suitability | NFR-002 | Rekomendasi AI menggunakan data pengguna sebagai dasar pemrosesan. |
| Performance Efficiency | NFR-003 | Proses utama aplikasi memberikan waktu respons yang sesuai untuk penggunaan normal. |
| Usability | NFR-004 | Antarmuka menggunakan navigasi dan komponen yang konsisten. |
| Usability | NFR-005 | Proses journaling melalui AI Chatbot dibuat sederhana dan mudah dipahami. |
| Reliability | NFR-006 | Data yang telah tersimpan tetap tersedia saat aplikasi digunakan kembali. |
| Security | NFR-007 | Data pribadi pengguna hanya dapat diakses melalui akun yang sesuai (RLS). |
| Security | NFR-008 | Komunikasi aplikasi dengan server menggunakan koneksi yang aman (HTTPS + JWT). |
| Compatibility | NFR-009 | Aplikasi dapat berjalan pada perangkat Android yang menjadi target pengembangan. |
| Maintainability | NFR-010 | Sistem menggunakan struktur kode yang modular (feature-based). |
| Portability | NFR-011 | Aplikasi dapat dipasang melalui APK pada perangkat Android yang didukung. |

## 4.3 Traceability Matrix

Traceability Matrix digunakan untuk memetakan kebutuhan pengguna dengan Functional Requirement yang telah ditetapkan. Pemetaan ini menunjukkan hubungan antara permasalahan pengguna dan kebutuhan fungsional yang mendukung penyelesaiannya, sehingga setiap kebutuhan pengguna memiliki jejak yang dapat ditelusuri hingga implementasi.

**Tabel 4.4 Traceability Matrix**

| Kebutuhan Pengguna | FR Terkait |
|--------------------|------------|
| Membutuhkan akses dan pengelolaan akun | FR-001–FR-003 |
| Sulit mengetahui kondisi dan riwayat tidur | FR-004–FR-006 |
| Membutuhkan pemantauan suasana hati | FR-007–FR-008 |
| Membutuhkan media refleksi diri | FR-009–FR-011 |
| Bingung menentukan arah hidup | FR-012–FR-013 |
| Membutuhkan rekomendasi untuk mendukung kualitas tidur | FR-014 |
| Ingin melihat ringkasan kondisi dan aktivitas | FR-015 |
| Membutuhkan informasi dan aktivitas pendukung | FR-016–FR-017 |

## 4.4 Desain Solusi

MindRest AI dirancang dengan mengintegrasikan pemantauan tidur, pencatatan suasana hati, *journaling* melalui AI Chatbot, dan refleksi Ikigai dalam satu aplikasi. Data yang dihasilkan dari aktivitas pengguna digunakan sebagai dasar pengolahan dan pemberian rekomendasi melalui fitur berbasis AI, yaitu rekomendasi pengembangan diri (FR-013), Sleep Insight (FR-014), serta respons AI Chatbot pada Daily Journal (FR-009/FR-011).

### 4.4.1 Use Case Diagram

Use Case Diagram menggambarkan interaksi **User** dengan fungsi yang tersedia pada MindRest AI. Aktor utama adalah pengguna terdaftar yang telah melakukan autentikasi. Sistem eksternal yang berinteraksi meliputi Supabase (autentikasi, database, dan Edge Functions) serta Google Gemini API (layanan AI). Use Case yang tersedia meliputi:

- **Autentikasi & Profil:** Register (FR-001), Login/Logout (FR-002), Kelola Profil (FR-003).
- **Sleep Tracking:** Catat Tidur (FR-004), Hitung Durasi Tidur (FR-005), Lihat Riwayat Tidur (FR-006).
- **Mood Tracking:** Catat Suasana Hati (FR-007), Lihat Riwayat Mood (FR-008).
- **Daily Journal (AI Chatbot):** Journaling via AI Chatbot (FR-009), Lihat Riwayat Jurnal (FR-010), Pengolahan Data Jurnal (FR-011).
- **Ikigai:** Isi Aspek Ikigai (FR-012), Hasilkan Rekomendasi Pengembangan Diri (FR-013).
- **Sleep Insight:** Tampilkan Rekomendasi Tidur (FR-014).
- **Dashboard:** Tampilkan Ringkasan (FR-015).
- **Notification:** Tampilkan Notifikasi (FR-016).
- **Relaxation:** Akses Fitur Relaksasi (FR-017).

Catatan: pembuatan jurnal hanya tersedia melalui AI Chatbot (FR-009); tidak terdapat use case "Buat Jurnal" terpisah. AI Chatbot merupakan bagian dari modul Daily Journal.

### 4.4.2 Arsitektur Sistem

Arsitektur MindRest AI menggunakan pendekatan **client–server** dengan aplikasi Android sebagai *client* dan **Supabase sebagai Backend-as-a-Service (BaaS)**. Tidak terdapat server Node.js terpisah. Komponen utama arsitektur adalah:

1. **Client (Android).** Dikembangkan menggunakan **Kotlin + Jetpack Compose**. Mengelola UI, *state management*, navigasi, dan *local repository* yang berkomunikasi dengan Supabase.
2. **Database (Supabase PostgreSQL).** Menyimpan seluruh data pengguna. Diakses langsung oleh *client* melalui **PostgREST API**. Setiap tabel dilindungi **Row-Level Security (RLS)** sehingga pengguna hanya dapat membaca/menulis data miliknya sendiri.
3. **Autentikasi (Supabase Auth).** Menangani registrasi, login, logout, dan verifikasi email. Mengeluarkan **JWT** yang disimpan *client* dan dikirim pada setiap permintaan.
4. **Edge Functions (Deno/TypeScript).** Berjalan di sisi server Supabase sebagai *proxy* aman untuk memanggil **Google Gemini API**. Edge Function memverifikasi JWT pengguna, menerapkan *rate-limit*, membangun prompt, memanggil Gemini dengan *service role key*, lalu menyimpan hasil ke database.
5. **Google Gemini API.** Layanan AI eksternal yang menghasilkan respons AI Chatbot, laporan Ikigai, dan materi Sleep Insight.

Terdapat **dua jalur komunikasi** yang memisahkan jalur data dari jalur AI:
- **Jalur Data CRUD (RLS-protected):** *client* ↔ database secara langsung untuk operasi *create/read/update/delete* (sleep logs, mood logs, journal entries, profil, dsb.).
- **Jalur AI (Edge Function intermediary):** *client* ↔ Edge Function ↔ Gemini, dengan hasil ditulis kembali ke database oleh Edge Function menggunakan *service role*.

Pemisahan ini menjamin kunci API Gemini tidak terekspos di sisi *client*, sekaligus menjaga agar data sensitif tetap terisolasi per pengguna melalui RLS.

**Tabel 4.5 Tingkat Akses Komunikasi**

| Akses | Digunakan oleh | Tujuan |
|-------|----------------|--------|
| User-level (JWT) | Aplikasi Android | CRUD data milik sendiri (RLS) |
| Service role | Edge Function (server) | Menjalankan Gemini & menulis hasil AI ke database (tidak dapat dipalsukan dari *client*) |

### 4.4.3 Swimlane Diagram

Swimlane Diagram menggambarkan alur lintas-aktor untuk dua proses berbasis AI: **Rekomendasi Ikigai (FR-013)** dan **Sleep Insight (FR-014)**. Aktor yang terlibat: **User**, **Android App**, **Supabase (Auth/DB/Edge Function)**, dan **Google Gemini API**.

**Alur Rekomendasi Ikigai (FR-013):**
1. **User** membuka layar Ikigai Assessment dan mengisi enam pertanyaan (passion, skill, profesi, misi, *overthinking*, kepuasan hidup) → FR-012.
2. **Android App** menyimpan jawaban ke tabel `ikigai_assessments` melalui jalur CRUD.
3. **Android App** memanggil Edge Function `generate-ikigai-report` dengan menyertakan JWT.
4. **Edge Function** memverifikasi JWT, mengecek *rate-limit* (maksimal 1× per hari), merangkai prompt berisi jawaban assessment + data mood/sleep/jurnal 7 hari terakhir, lalu memanggil Gemini.
5. **Gemini** mengembalikan JSON terstruktur (`report_markdown`, `ikigai_circles`, dan 3–5 `recommendations`).
6. **Edge Function** menyimpan hasil ke tabel `ikigai_reports` menggunakan *service role*.
7. **Android App** menampilkan empat lingkaran Ikigai dan daftar rekomendasi yang dapat dicentang saat sudah dikerjakan.

**Alur Sleep Insight (FR-014):**
1. **User** mencatat waktu tidur, waktu bangun, dan kualitas tidur → FR-004.
2. **Android App** menyimpan data ke `sleep_logs` dan menghitung durasi tidur otomatis → FR-005.
3. Berdasarkan riwayat `sleep_logs`, **Android App** menampilkan Sleep Insight berupa rekomendasi aktivitas, makanan, dan minuman yang mendukung kualitas tidur. Pada tahap saat ini rekomendasi berbasis aturan lokal; ke depan diperkaya dengan ringkasan dari AI via Edge Function.

### 4.4.4 Desain Modul Utama

Desain modul utama MindRest AI dikelompokkan menjadi tiga bagian yang saling mendukung:

1. **Modul Input & Pemantauan** — Sleep Tracking (FR-004–FR-006), Mood Tracking (FR-007–FR-008), Daily Journal via AI Chatbot (FR-009–FR-011), serta pengisian aspek Ikigai (FR-012). Modul ini berfungsi mengumpulkan data mentah dari pengguna.
2. **Modul Analisis AI** — mengolah data dari modul input menjadi wawasan: rekomendasi pengembangan diri berbasis Ikigai (FR-013) dan Sleep Insight (FR-014). Pemrosesan dilakukan di Edge Function dengan Gemini.
3. **Modul Penyajian & Dukungan** — Dashboard (FR-015) yang merangkum tren, Notification (FR-016), dan Relaxation (FR-017).

### 4.4.5 Desain Database

Database utama adalah PostgreSQL di Supabase dengan enam tabel inti berikut (semua dilindungi RLS berbasis `user_id`):

**Tabel 4.6 Skema Database Inti**

| Tabel | Kolom Utama | Keterangan |
|-------|-------------|------------|
| `profiles` | `id` (FK ke `auth.users`), `email`, `display_name`, `avatar_url`, `height_cm`, `weight_kg`, `occupation`, `complaints[]`, `created_at`, `updated_at` | Data profil & onboarding kesehatan (FR-003). |
| `mood_logs` | `id`, `user_id`, `mood_score` (1–5), `created_at` | Catatan suasana hati (FR-007–FR-008). |
| `sleep_logs` | `id`, `user_id`, `bed_time`, `wake_up_time`, `sleep_quality` (POOR/FAIR/GOOD/EXCELLENT), `created_at` | Catatan tidur (FR-004–FR-006). |
| `journal_entries` | `id`, `user_id`, `content`, `created_at` | Isi jurnal dari percakapan AI Chatbot (FR-009–FR-011). |
| `ikigai_assessments` | `id`, `user_id`, `q1_passion`, `q2_skill`, `q3_profession`, `q4_mission`, `q5_overthinking`, `q6_satisfaction` | Jawaban enam aspek Ikigai (FR-012). |
| `ikigai_reports` | `id`, `user_id`, `assessment_id`, `report_markdown`, `ikigai_circles` (JSONB), `recommendations` (JSONB), `version` | Hasil laporan & rekomendasi AI (FR-013). |

---

# BAB V — IMPLEMENTASI PERANGKAT LUNAK

## 5.1 Gambaran Umum Implementasi

MindRest AI adalah aplikasi *mobile* Android yang membantu pengguna memahami diri sendiri melalui empat data utama: *sleep tracker*, *mood tracker*, profil, dan *daily journal* (via AI Chatbot). Keempatnya dipakai sebagai "bahan bakar" bagi kecerdasan buatan untuk menghasilkan laporan Ikigai — ringkasan tentang minat, kemampuan, profesi, dan misi pengguna — beserta rekomendasi pengembangan diri yang dipersonalisasi.

Implementasi disusun berdasarkan **17 kebutuhan fungsional** (FR-001–FR-017) pada Tabel 4.2. Aplikasi dibangun dengan **Jetpack Compose (Kotlin)** untuk sisi Android dan **Supabase** untuk sisi server (database, autentikasi, dan Edge Functions berbasis Deno/TypeScript). AI yang digunakan adalah **Google Gemini**, dipanggil melalui Edge Function agar kunci API tetap aman.

## 5.2 Manajemen Arsitektur

### 5.2.1 Manajemen Folder

Struktur kode dipisah berdasarkan fitur (*feature-based*), bukan berdasarkan tipe *file*. Tujuannya agar satu fitur — misalnya *sleep tracking* — seluruh berkas terkait (tampilan, logika, dan akses data) terkumpul dalam satu folder. Pola ini memudahkan tim saat menambah atau memperbaiki fitur tanpa menyentuh fitur lain. Struktur utama:

- `app/src/main/java/com/example/core/` — `base/`, `common/`, `designsystem/`, `navigation/`, `network/`, `utils/`.
- `app/src/main/java/com/example/features/` — `authentication/`, `home/`, `sleep/`, `mood/`, `journal/`, `ikigai/`, `relaxation/`, `notification/`, `profile/`, `reminder/`, `statistics/`, `lifestyle/`, `settings/`, `achievements/`.
- `supabase/` — `schema.sql`, `migrations/`, `functions/` (Edge Functions).

### 5.2.2 Manajemen Route (Navigasi)

Setiap layar memiliki *route* berupa *string* unik yang didefinisikan di `core/navigation/Screen.kt`. Alur dibungkus dalam *Navigation Compose* dengan tiga tingkat:

- **Autentikasi:** `splash` → `onboarding` → `login`/`register`.
- **Tab utama (bottom bar):** `home`, `sleep`, `relaxation`, `ikigai`, `profile`.
- **Layar turunan (spokes):** `journal`, `ai_journal`, `mood_tracking`, `sleep_tracking`, `lifestyle`, `notifications`, `reminder`, `statistics`, `settings`, `achievements`, `ikigai_assessment`, `ikigai_report_loading`, `ikigai_report`.

Pemisahan ini mencegah tab "naik-turun" sendiri saat pengguna berpindah layar, sehingga tombol *Back* di perangkat terasa alami.

### 5.2.3 Manajemen Database

Database utama adalah PostgreSQL di Supabase. Terdapat dua jenis akses (lihat Tabel 4.5): akses **user-level (JWT)** untuk CRUD data milik sendiri, dan akses **service role** yang hanya digunakan oleh Edge Function untuk menulis hasil AI. Setiap tabel memiliki **Row-Level Security (RLS)** — artinya walaupun tabel sama, pengguna A tidak akan pernah bisa membaca data pengguna B. Skema disimpan di `supabase/schema.sql` dan migrasi tambahan di `supabase/migrations/`.

## 5.3 Implementasi per Fitur

### 5.3.1 Autentikasi & Profil (FR-001, FR-002, FR-003)

Pengguna mendaftar dengan email dan kata sandi melalui layar Register (**FR-001**). Supabase mengirim tautan verifikasi email dan secara otomatis membuat baris di tabel `profiles`. Layar Login memvalidasi kredensial, lalu aplikasi menyimpan sesi (JWT) agar tidak perlu login ulang (**FR-002**). Tombol Logout menghapus sesi lokal dan kembali ke layar Splash. Pengguna dapat melengkapi dan memperbarui profil — nama tampilan, foto, tinggi/berat badan, pekerjaan, serta keluhan kesehatan — melalui layar Profile (**FR-003**); perubahan disimpan ke tabel `profiles`.

### 5.3.2 Sleep Tracking (FR-004, FR-005, FR-006)

Pengguna membuka layar Sleep Tracking, mengisi jam tidur (`bed_time`), jam bangun (`wake_up_time`), dan kualitas tidur (`POOR`/`FAIR`/`GOOD`/`EXCELLENT`) (**FR-004**). Aplikasi menghitung durasi tidur secara otomatis dari selisih kedua waktu tersebut (**FR-005**). Data tersimpan ke tabel `sleep_logs`. Riwayat tidur ditampilkan pada layar riwayat beserta durasi dan kualitas, sehingga pengguna dapat memantau pola tidurnya (**FR-006**).

### 5.3.3 Mood Tracking (FR-007, FR-008)

Pengguna memilih skor suasana hati 1–5 (skala emoji) pada layar Mood Tracking atau melalui *bottom sheet* cepat dari beranda (**FR-007**). Data tersimpan ke tabel `mood_logs`. Riwayat mood ditampilkan sebagai deretan emoji beserta tanggal agar pengguna dapat melihat pola emosionalnya (**FR-008**).

### 5.3.4 Daily Journal dengan AI Chatbot (FR-009, FR-010, FR-011)

Layar AI Journal menyajikan antarmuka percakapan tempat pengguna menulis curhatannya (**FR-009**). Tidak terdapat formulir jurnal terpisah — seluruh *journaling* berlangsung melalui AI Chatbot. Pesan pengguna dikirim ke Edge Function yang memanggil Gemini, lalu mengembalikan balasan empatik. Setiap percakapan disimpan ke tabel `journal_entries` agar riwayat dapat dibuka kembali (**FR-010**). Konten jurnal inilah yang diolah menjadi bahan analisis AI — dikombinasikan dengan data Ikigai untuk menghasilkan rekomendasi pengembangan diri (**FR-011**, lihat FR-013).

### 5.3.5 Ikigai (FR-012, FR-013)

Pengguna menjawab enam pertanyaan *assessment* (passion, skill, profesi, misi, *overthinking*, kepuasan hidup) di layar Ikigai Assessment (**FR-012**); jawaban disimpan ke tabel `ikigai_assessments`. Setelah disimpan, aplikasi memanggil Edge Function `generate-ikigai-report` yang melakukan langkah berikut (**FR-013**):

1. Memverifikasi token login (JWT).
2. Mengecek *rate-limit* (maksimal 1× per hari).
3. Merangkai prompt berisi jawaban *assessment* + data mood/sleep/jurnal 7 hari terakhir.
4. Meminta Gemini mengembalikan JSON terstruktur: `report_markdown`, `ikigai_circles`, dan 3–5 `recommendations`.
5. Menyimpan hasil ke `ikigai_reports` menggunakan *service role*.

Aplikasi kemudian menampilkan empat lingkaran Ikigai dan daftar rekomendasi yang dapat dicentang saat sudah dikerjakan.

### 5.3.6 Sleep Insight (FR-014)

Berdasarkan riwayat `sleep_logs`, aplikasi memberikan rekomendasi aktivitas, makanan, dan minuman yang mendukung kualitas tidur pengguna, misalnya "kualitas tidurmu menurun minggu ini, coba hindari kafein setelah sore dan tidur 30 menit lebih awal". Pada tahap saat ini rekomendasi berbasis aturan lokal; ke depan akan diperkaya dengan ringkasan dari AI melalui Edge Function.

### 5.3.7 Dashboard / Beranda (FR-015)

Layar Home menggabungkan ringkasan: tren tidur 7 hari (*sleep trends*), tren mood 7 hari (*mood trends*), Sleep Insight terbaru, jurnal terakhir (*daily journal*), dan daftar pengingat harian (*daily reminders*). Seluruh *widget* memuat datanya sendiri dari *repository* terkait, sehingga kegagalan satu *widget* tidak mengganggu *widget* lain.

### 5.3.8 Notifikasi (FR-016)

Notifikasi dihasilkan untuk menampilkan informasi dan pengingat kepada pengguna, disimpan pada tabel/pesan notifikasi, dan ditampilkan pada layar Notifications. Aplikasi juga mengirim *push notification* lokal saat ada item baru. (Fitur prioritas *Could*; cakupan penjadwalan otomatis dapat diperluas di iterasi berikutnya.)

### 5.3.9 Relaksasi (FR-017)

Layar Relaxation memutar audio atau menampilkan panduan relaksasi sederhana (mis. *breathing exercise* dan musik relaksasi) yang telah disiapkan secara statis di dalam aplikasi. Fitur ini tidak membutuhkan AI, sehingga dapat diakses kapan saja, termasuk saat *offline*.

## 5.4 Penutup Implementasi

Seluruh 17 kebutuhan fungsional tercakup dalam implementasi di atas. Pendekatan pemisahan fitur (*feature-based*), *route* terpusat, dan database berlapis RLS memastikan aplikasi mudah dikembangkan, aman, dan siap melayani pengguna dalam menemukan Ikigai mereka.

---

# BAB VI — ANTARMUKA (MOCKUP) PERANGKAT LUNAK

Antarmuka MindRest AI dirancang dengan navigasi *bottom bar* (Home, Sleep, Relaxation, Ikigai, Profile) sebagai titik masuk utama, dilengkapi layar turunan untuk pencatatan dan riwayat. Berikut ringkasan layar utama (lihat *Gambar 6.1–6.9* pada lampiran dokumen sumber):

- **Splash & Login/Register (6.1–6.2)** — layar pembuka dan autentikasi (FR-001, FR-002).
- **Home/Dashboard (6.3)** — ringkasan tren tidur, tren mood, Sleep Insight, jurnal terakhir, dan pengingat harian (FR-015).
- **Notification (6.4)** — daftar notifikasi dan pengingat (FR-016).
- **Mood Tracking (6.5)** — pencatatan dan riwayat suasana hati (FR-007, FR-008).
- **Journal & AI Journaling (6.6)** — antarmuka percakapan AI Chatbot dan riwayat jurnal (FR-009, FR-010).
- **Sleep Tracking & Sleep Insight (6.7)** — input tidur, perhitungan durasi, riwayat, dan rekomendasi tidur (FR-004–FR-006, FR-014).
- **Relax (6.8)** — panduan dan audio relaksasi (FR-017).
- **Profile (6.9)** — kelola profil pengguna (FR-003).

---

# BAB VII — DOKUMENTASI PENGGUNAAN

## 7.1 Instalasi

Pengguna mengaktifkan fitur *Developer Mode* (mode pengembang) pada perangkat Android, mengizinkan instalasi dari sumber tidak dikenal, lalu memasang berkas aplikasi (.apk). Setelah terpasang, pengguna melakukan registrasi menggunakan email dan login dengan akun yang dimiliki.

## 7.2 Cara Penggunaan

1. **Login / Registrasi** — buat akun dengan email, lalu login (FR-001, FR-002).
2. **Lengkapi Profil** — isi data diri dan informasi kesehatan dasar (FR-003).
3. **Sleep Tracking** — catat waktu tidur dan waktu bangun; sistem menghitung durasi otomatis. Lihat riwayat untuk memantau pola tidur (FR-004–FR-006).
4. **Mood Tracking** — catat suasana hati harian dan pantau pola emosional pada riwayat (FR-007, FR-008).
5. **Daily Journaling (AI Chatbot)** — curahkan isi pikiran melalui percakapan dengan AI Chatbot; sistem menyimpan jurnal dan mengolahnya untuk analisis AI (FR-009–FR-011).
6. **Ikigai** — isi enam aspek Ikigai, lalu peroleh rekomendasi pengembangan diri yang dipersonalisasi dari data Ikigai dan Daily Journal (FR-012, FR-013).
7. **Sleep Insight** — baca rekomendasi aktivitas, makanan, dan minuman untuk meningkatkan kualitas tidur (FR-014).
8. **Dashboard** — pantau ringkasan tren dan pengingat harian dari layar beranda (FR-015).
9. **Notification & Relaxation** — kelola pengingat (FR-016) dan gunakan fitur relaksasi saat dibutuhkan (FR-017).

## 7.3 Alur Penggunaan

Pengguna memulai dari **Home**, melakukan pencatatan aktivitas harian (tidur, mood, jurnal), mengisi aspek Ikigai, memperoleh analisis AI (rekomendasi pengembangan diri dan Sleep Insight), kemudian menerima rekomendasi dan pengingat untuk membantu meningkatkan kualitas tidur dan kesejahteraan secara berkelanjutan.

---

# BAB VIII — PENUTUP

## Kesimpulan

MindRest AI merupakan aplikasi *AI-Powered Sleep Therapy Assistant* yang membantu pengguna meningkatkan kualitas tidur melalui fitur **Sleep Tracking**, **Mood Tracking**, **Daily Journal** (via AI Chatbot), **Ikigai**, **Sleep Insight**, **Dashboard**, **Notification**, dan **Relaxation**. Dengan memanfaatkan *Artificial Intelligence* (Google Gemini) yang diakses secara aman melalui Supabase Edge Functions, aplikasi ini menghasilkan rekomendasi pengembangan diri yang dipersonalisasi berdasarkan data Ikigai dan Daily Journal. Implementasi 17 kebutuhan fungsional (FR-001–FR-017) telah menunjukkan bahwa integrasi pemantauan tidur, refleksi diri, dan pendekatan Ikigai dalam satu platform dapat menjawab kebutuhan pengguna secara holistik. Arsitektur Supabase BaaS dengan RLS dan Edge Function menjaga keamanan data sekaligus menjaga kerahasiaan kunci layanan AI.

## Evaluasi

Solusi yang dikembangkan menyelesaikan permasalahan utama pengguna: (1) ketidaktahuan terhadap pola tidur dan suasana hati, (2) ketiadaan media refleksi yang sederhana, dan (3) kebingungan dalam menentukan arah pengembangan diri. Keterbatasan saat ini mencakup ketergantungan pada input manual untuk data tidur, keterbatasan kuota layanan AI, serta cakupan Sleep Insight yang masih bersifat aturan lokal. Pengembangan selanjutnya dapat memperkaya Sleep Insight dengan AI, menambah penjadwalan notifikasi otomatis, serta memperluas evaluasi pengguna pada skala yang lebih luas.

---

# DAFTAR PUSTAKA

- Chen, Y., Kim, E. S., VanderWeele, T. J., Chen, S., Trudel-Fitzgerald, C., Kubzansky, L. D., & Kawachi, I. (2022). Ikigai and subsequent health and wellbeing among Japanese older adults: Longitudinal outcome-wide analysis. *The Lancet Regional Health – Western Pacific, 21*, 100391.
- Lee, S., Oh, J. W., Park, K. M., Lee, S., & Lee, E. (2023). Digital cognitive behavioral therapy for insomnia on depression and anxiety: A systematic review and meta-analysis. *npj Digital Medicine, 6*, Article 52.
- Espie, C. A., Emsley, R., Kyle, S. D., Gordon, C., Drake, C. L., Siriwardena, A. N., Cape, J., Ong, J. C., Sheaves, B., Foster, R., Freeman, D., Costa-Font, J., Marsden, A., & Luik, A. I. (2019). Effect of digital cognitive behavioral therapy for insomnia on health, psychological well-being, and sleep-related quality of life. *JAMA Psychiatry, 76*(4), 401.
- Spyridonidis, S., Lad, D., Peters, H., Ellis, J., & Robinson, L. J. (2025). Global prevalence of insomnia symptoms in undergraduate university students: A systematic review and meta-analysis. *Sleep Advances, 6*(4), zpaf083.
- Wilkes, J., Garip, G., Kotera, Y., & Fido, D. (2023). Can Ikigai Predict Anxiety, Depression, and Well-being? *International Journal of Mental Health and Addiction, 21*, 2941–2953.
- Schwaber, K., & Sutherland, J. (2020). *The Scrum Guide: The Definitive Guide to Scrum: The Rules of the Game*. Scrum.org.
- Gkintoni, E., Vassilopoulos, S. P., Nikolaou, G., & Boutsinas, B. (2025). Digital and AI-Enhanced Cognitive Behavioral Therapy for Insomnia: Neurocognitive Mechanisms and Clinical Outcomes. *Journal of Clinical Medicine, 14*, 2265.
- Guo, Z., Lai, A., Thygesen, J. H., Farrington, J., Keen, T., & Li, K. (2024). Large Language Models for Mental Health Applications: Systematic Review. *JMIR Mental Health, 11*, e57400.
- Mathunjwa, B. M., Kor, R. Y. J., Ngarnkuekool, W., & Hsu, Y.-L. (2025). A Comprehensive Review of Home Sleep Monitoring Technologies: Smartphone Apps, Smartwatches, and Smart Mattresses. *Sensors, 25*, 1771.
- Zhang, C., Liu, Y., Guo, X., Liu, Y., Shen, Y., & Ma, J. (2023). Digital Cognitive Behavioral Therapy for Insomnia Using a Smartphone Application in China: A Pilot Randomized Clinical Trial. *JAMA Network Open, 6*(3), e234866.
- Haq, Z. M., & Noprion, M. I. (2026). Cross Platform News Integration Using RESTful API Architecture for Web and Android Systems. *International Journal of Computing Research (IJCR), 1*(1), 20–29.
