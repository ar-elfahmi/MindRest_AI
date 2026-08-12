# **Section 1\. INTRODUCTION**

## **1.1 Purpose**

Dokumen ini mendefinisikan kebutuhan perangkat lunak MindRest AI — aplikasi mobile Android yang membantu pengguna meningkatkan kualitas tidur melalui integrasi Sleep Therapy, AI, dan konsep Ikigai. Dokumen ini menjadi acuan bagi tim pengembang, UI/UX designer, tester, QA, dosen pembimbing, dan penilai GEMASTIK.

## **1.2 Scope**

MindRest AI adalah AI-Powered Sleep Therapy Assistant yang memadukan tiga komponen utama:

1. Sleep Therapy: Pemantauan dan analisis kualitas tidur.  
2. Daily Journal: Refleksi diri dan pencatatan aktivitas serta emosi.  
3. Ikigai: Pendekatan untuk menemukan tujuan hidup guna meningkatkan kesejahteraan psikologis dan kualitas tidur.

AI menganalisis seluruh data pengguna untuk menghasilkan rekomendasi personal.  
Ruang Lingkup Sistem:

1. Manajemen akun dan profil  
2. Sleep Tracking, Mood Tracking, Daily Journal  
3. Relaxation Hub  
4. AI Sleep Insight & Ikigai Personalized Recommendation  
5. Dashboard dan riwayat aktivitas

Di Luar Ruang Lingkup:

1. Diagnosis klinis gangguan tidur atau kesehatan mental  
2. Konsultasi langsung dengan tenaga kesehatan profesional  
3. Penanganan kondisi darurat psikologis  
4. Penggantian layanan medis/psikoterapi  
   

   ## **1.3 Overview**

Dokumen terdiri dari tiga bagian:

1. Section 1: Tujuan, ruang lingkup, definisi, dan referensi.  
2. Section 2: Deskripsi umum sistem, arsitektur, fungsi, karakteristik pengguna, user stories, use case, batasan, asumsi, dan kebutuhan nonfungsional.  
3. Section 3: Appendix (glosarium, business rules, data dictionary, error messages, dan future enhancement).

   ## **Definitions, Acronyms, and Abbreviations**

| Istilah | Definisi |
| :---- | :---- |
| AI | Artificial Intelligence untuk analisis data dan rekomendasi personal |
| Sleep Therapy | Pendekatan pemantauan dan rekomendasi perilaku tidur |
| Sleep Tracking | Pencatatan waktu tidur, bangun, durasi, dan kualitas tidur |
| Mood Tracking | Pencatatan kondisi emosional harian |
| Daily Journal | Pencatatan aktivitas, refleksi, pengalaman, dan rencana harian |
| Ikigai | Konsep penemuan tujuan hidup melalui minat, kemampuan, kebutuhan sosial, dan profesi |
| AI Sleep Insight | Fitur AI untuk analisis kualitas tidur dan rekomendasi |
| Ikigai Recommendation | Fitur AI untuk rekomendasi aktivitas, milestone, dan sumber belajar |
| Dashboard | Halaman utama ringkasan perkembangan pengguna |
| MVP | Minimum Viable Product |
| API | Application Programming Interface |
| SRS | Software Requirements Specification |
| UML | Unified Modeling Language |
| CRUD | Create, Read, Update, Delete |

   ## **References**

* ISO/IEC/IEEE 29148:2018 — Requirements Engineering  
* IEEE 830-1998 — Recommended Practice for Software Requirements Specifications  
* ISO/IEC 25010:2011 — System and Software Quality Models

# **Section 2\. General Description**

## **2.1 Product Perspective**

MindRest AI adalah aplikasi mobile Android baru (standalone) yang tidak menggantikan sistem existing. Aplikasi memanfaatkan data historis (sleep tracking, mood tracking, daily journal, dan profil) untuk menghasilkan rekomendasi personal menggunakan AI.  
Layanan Eksternal:

1. Google Gemini API (mesin AI)  
2. Supabase (cloud database)

**2.2 System Description**  
Alur Kerja Sistem: User → Authentication → User Profile → Sleep Tracking / Mood Tracking / Daily Journal → Cloud Database → AI Engine → AI Sleep Insight / Ikigai Recommendation → Dashboard  
Semakin banyak data yang dikumpulkan, semakin akurat rekomendasi AI. 

**2.1 Product Perspective**  
MindRest AI adalah aplikasi mobile Android baru (standalone) yang tidak menggantikan sistem existing. Aplikasi memanfaatkan data historis (sleep tracking, mood tracking, daily journal, dan profil) untuk menghasilkan rekomendasi personal menggunakan AI.  
Layanan Eksternal:

1. Google Gemini API (mesin AI)  
2. Supabase (cloud database)

**2.2 System Description**  
Alur Kerja Sistem: User → Authentication → User Profile → Sleep Tracking / Mood Tracking / Daily Journal → Cloud Database → AI Engine → AI Sleep Insight / Ikigai Recommendation → Dashboard  
Semakin banyak data yang dikumpulkan, semakin akurat rekomendasi AI.

* Memberikan akses inklusif terhadap layanan kesehatan mental yang aman, murah, dan bebas stigma.  
* Menyediakan dukungan psikologis awal melalui chatbot dan komunitas, dengan fallback ke profesional bila dibutuhkan.  
* Meningkatkan kesadaran kesehatan mental melalui edukasi dan gamifikasi.  
* Mendukung SDGs poin 3 (Good Health and Well-Being).

**2.3 Product Functions**

| Modul | Deskripsi |
| :---- | :---- |
| Authentication | Registrasi, login, logout, pengelolaan akun |
| User Profile | Informasi dasar pengguna (umur, TB, BB, aktivitas, keluhan tidur) |
| Sleep Tracking | Pencatatan jam tidur, jam bangun, durasi, dan kualitas tidur |
| Mood Tracking | Pencatatan kondisi emosional harian |
| Daily Journal | Pencatatan aktivitas, refleksi, pengalaman, dan rencana harian |
| Relaxation Hub | Media relaksasi (musik, meditasi, video, ambient sound) |
| AI Sleep Insight | Analisis data tidur dan rekomendasi personal |
| Ikigai Recommendation | Rekomendasi aktivitas, milestone, dan sumber belajar berbasis konsep Ikigai |
| Dashboard | Ringkasan perkembangan pengguna |
| History | Riwayat aktivitas pengguna |

**2.4 System Architecture**  
Arsitektur Client–Server:  
![][image1]  
Komponen Utama: Android Mobile App, Backend API, Supabase Database, Google Gemini API.

**2.5 User Characteristics**

| Karakteristik | Deskripsi |
| :---- | :---- |
| Umur | 18–35 tahun |
| Pendidikan | SMA, Mahasiswa, Pekerja |
| Pengalaman Teknologi | Mampu menggunakan aplikasi Android sehari-hari |
| Pengalaman AI | Tidak diperlukan |
| Kondisi | Pernah mengalami gangguan tidur ringan–sedang, overthinking, atau kesulitan menentukan arah pengembangan diri |

Pengguna tidak wajib memahami terapi tidur atau konsep Ikigai karena rekomendasi diberikan secara otomatis. 

**2.6 User Stories**

| ID | User Story |
| :---- | :---- |
| US-01 | Sebagai pengguna, saya ingin membuat akun agar data perkembangan saya tersimpan. |
| US-02 | Sebagai pengguna, saya ingin mencatat kualitas tidur setiap hari agar mengetahui perkembangan tidur saya. |
| US-03 | Sebagai pengguna, saya ingin mencatat mood harian agar memahami kondisi emosional saya. |
| US-04 | Sebagai pengguna, saya ingin menulis jurnal harian agar dapat melakukan refleksi diri. |
| US-05 | Sebagai pengguna, saya ingin memperoleh rekomendasi tidur yang dipersonalisasi agar dapat memperbaiki kualitas tidur. |
| US-06 | Sebagai pengguna, saya ingin memperoleh rekomendasi aktivitas berbasis Ikigai agar memiliki arah pengembangan diri. |
| US-07 | Sebagai pengguna, saya ingin melihat perkembangan diri saya agar mengetahui perubahan kualitas tidur dan kebiasaan. |

**2.7 Use Case Diagram**  
Aktor User dapat melakukan:

* Register, Login, Manage Profile  
* Sleep Tracking, Mood Tracking, Daily Journal  
* Relaxation Hub, View Dashboard  
* AI Sleep Insight, Ikigai Recommendation  
* History

**2.8 Use Case Specification**

#### **UC-01 Register Account**

| Item | Deskripsi |
| :---- | :---- |
| ID | UC-01 |
| Nama | Register Account |
| Aktor | User |
| Precondition | Pengguna belum memiliki akun; aplikasi terhubung internet |
| Trigger | Pengguna menekan tombol Register |
| Main Flow | 1\. Sistem menampilkan form registrasi |
| 2\. User mengisi nama, email, password |  |
| 3\. Sistem validasi format email dan kompleksitas password |  |
| 4\. Sistem membuat akun baru dan menyimpan data |  |
| 5\. Sistem menampilkan pesan sukses dan mengarahkan ke Login |  |
| Alt. Flow | A1: Email sudah digunakan → sistem minta email lain. |
| A2: Password \< 8 karakter → sistem tampilkan pesan kesalahan. |  |
| Exception | E1: Tidak ada koneksi → sistem tampilkan "Koneksi internet tidak tersedia." |
| Postcondition | Akun berhasil dibuat; data tersimpan di database. |
| Business Rules | BR-01: Email unik. BR-02: Password minimal 8 karakter. BR-03: Password di-hash (Argon2/BCrypt). |

**UC-02 Login**

| Item | Deskripsi |
| :---- | :---- |
| ID | UC-02 |
| Nama | Login |
| Aktor | User |
| Precondition | Pengguna memiliki akun; perangkat terhubung internet |
| Trigger | Pengguna menekan tombol Login |
| Main Flow | 1\. User memasukkan email dan password |
| 2\. Sistem validasi akun di server |  |
| 3\. Server membuat session login |  |
| 4\. Dashboard ditampilkan |  |
| Alt. Flow | A1: Email salah → "Email tidak ditemukan." |
| A2: Password salah → "Password tidak sesuai." |  |
| A3: Lupa password → sistem kirim tautan reset. |  |
| Exception | E1: Server gagal dihubungi → "Server tidak dapat dihubungi." |
| Postcondition | Session login berhasil; Dashboard dapat diakses. |
| Business Rules | BR-04: Login hanya dengan akun valid. BR-05: Session berakhir setelah Logout. |

**UC-03 Manage Profile**

| Item | Deskripsi |
| :---- | :---- |
| ID | UC-03 |
| Nama | Manage User Profile |
| Aktor | User |
| Precondition | User telah login |
| Trigger | Pengguna memilih menu Profile |
| Main Flow | 1\. Sistem ambil dan tampilkan data profil |
| 2\. User mengubah informasi |  |
| 3\. Sistem validasi dan simpan perubahan |  |
| 4\. Sistem tampilkan pesan berhasil |  |
| Data Dikelola | Nama, umur, jenis kelamin, tinggi badan, berat badan, aktivitas harian, keluhan tidur, target tidur |
| Alt. Flow | A1: Data tidak lengkap → sistem minta melengkapi. |
| A2: Nilai tidak valid (umur negatif, TB/BB tidak logis) → sistem tampilkan validasi. |  |
| Exception | E1: Gagal menyimpan → "Terjadi kesalahan saat menyimpan data." |
| Postcondition | Profil diperbarui; AI gunakan data terbaru. |
| Business Rules | BR-06: Umur 13–100 tahun. BR-07–08: Berat dan tinggi badan positif. BR-09: Keluhan tidur dapat dipilih \>1. |

**UC-05 Mood Tracking**

| Item | Deskripsi |
| :---- | :---- |
| ID | UC-05 |
| Nama | Mood Tracking |
| Aktor | User |
| Precondition | User telah login |
| Trigger | Pengguna membuka Mood Tracking |
| Main Flow | 1\. Sistem tampilkan pilihan mood |
| 2\. User pilih mood \+ catatan opsional |  |
| 3\. User tekan Save |  |
| 4\. Data disimpan; riwayat diperbarui |  |
| Mood Category | 😄 Sangat Bahagia, 🙂 Bahagia, 😐 Netral, 🙁 Sedih, 😢 Sangat Sedih |
| Alt. Flow | A1: Tidak ada catatan → sistem tetap simpan mood. |
| A2: Mood diganti sebelum save → sistem simpan yang terakhir. |  |
| Exception | E1: Penyimpanan gagal → sistem minta coba lagi. |
| Postcondition | Mood tersimpan; AI dapat menggunakannya. |
| Business Rules | BR-13: Hanya 1 mood/hari. BR-14: Catatan opsional. |

**UC-06 Daily Journal**

| Item | Deskripsi |
| :---- | :---- |
| ID | UC-06 |
| Nama | Daily Journal |
| Aktor | User |
| Precondition | User telah login |
| Trigger | Pengguna memilih menu Daily Journal |
| Main Flow | 1\. Sistem tampilkan editor |
| 2\. User tulis jurnal (judul opsional, isi, aktivitas hari ini, rencana besok) |  |
| 3\. Sistem simpan draft sementara (opsional) |  |
| 4\. User tekan Save → sistem validasi dan simpan ke database |  |
| 5\. Jurnal masuk histori; AI tandai siap dianalisis |  |
| Alt. Flow | A1: Judul kosong → sistem buat judul otomatis berdasarkan tanggal. |
| A2: Hanya beberapa kalimat → sistem tetap simpan. |  |
| Exception | E1: Isi jurnal kosong → "Isi jurnal tidak boleh kosong." |
| Postcondition | Jurnal tersimpan; AI dapat menganalisisnya. |
| Business Rules | BR-15: 1 jurnal/hari. BR-16: Minimal 20 karakter. BR-17: Hanya pemilik akun yang dapat akses. BR-18: Jurnal menjadi bagian personalisasi AI. |

**UC-08 AI Sleep Insight**

| Item | Deskripsi |
| :---- | :---- |
| ID | UC-08 |
| Nama | AI Sleep Insight |
| Aktor | User |
| Precondition | User login; tersedia data Sleep Tracking, Mood Tracking, dan Daily Journal |
| Trigger | Pengguna membuka menu AI Sleep Insight |
| Main Flow | 1\. Sistem ambil histori pengguna |
| 2\. Sistem kirim data ke AI Engine |  |
| 3\. AI analisis pola tidur, mood, aktivitas, dan keluhan |  |
| 4\. AI hasilkan Sleep Score dan Sleep Insight |  |
| 5\. AI susun rekomendasi personal |  |
| 6\. Sistem tampilkan hasil analisis |  |
| Output | Sleep Score, ringkasan pola tidur, faktor pengaruh, kebiasaan yang perlu diperbaiki, rekomendasi jam tidur, estimasi peningkatan kualitas tidur |
| Alt. Flow | A1: Data belum cukup → "Data belum cukup untuk analisis akurat. Silakan lanjutkan pencatatan aktivitas harian." |
| Exception | E1: AI Service gagal → sistem tampilkan error dan tawarkan coba lagi. |
| Postcondition | Analisis tersimpan dalam riwayat; Dashboard diperbarui. |
| Business Rules | BR-20: AI hanya gunakan data pengguna yang login. BR-21: Analisis berdasarkan data historis, bukan hanya hari yang sama. BR-22: Hasil bersifat rekomendasi, bukan diagnosis medis. |

**UC-09 Ikigai Personalized Recommendation**

| Item | Deskripsi |
| :---- | :---- |
| ID | UC-09 |
| Nama | Ikigai Personalized Recommendation |
| Aktor | User |
| Precondition | User login; tersedia riwayat Daily Journal; data profil lengkap |
| Trigger | Pengguna membuka menu Ikigai Recommendation |
| Main Flow | 1\. Sistem ambil data pengguna |
| 2\. AI analisis jurnal, mood, dan histori tidur |  |
| 3\. AI identifikasi kecenderungan minat dan potensi |  |
| 4\. AI susun rekomendasi aktivitas harian |  |
| 5\. AI buat milestone pengembangan diri |  |
| 6\. AI tampilkan rekomendasi karier dan sumber belajar |  |
| Output | Aktivitas harian, milestone mingguan/bulanan, pengembangan karakter, rekomendasi karier, pembelajaran, kontribusi sosial |
| Alt. Flow | A1: Riwayat jurnal belum cukup → sistem berikan rekomendasi umum dan sarankan journaling rutin. |
| Exception | E1: AI Service tidak tersedia → sistem tampilkan rekomendasi terakhir yang tersimpan. |
| Postcondition | Rekomendasi Ikigai tersimpan; Dashboard diperbarui. |
| Business Rules | BR-23: Rekomendasi harus mempertimbangkan 4 komponen Ikigai (What You Love, What You Are Good At, What The World Needs, What You Can Be Paid For). BR-24: Rekomendasi diperbarui jika ada data baru yang signifikan. |

**UC-10 Dashboard & History**

| Item | Deskripsi |
| :---- | :---- |
| ID | UC-10 |
| Nama | Dashboard & History |
| Aktor | User |
| Precondition | User telah login |
| Trigger | Pengguna membuka halaman Home |
| Main Flow | 1\. Sistem ambil data terbaru |
| 2\. Sistem hitung ringkasan statistik |  |
| 3\. Dashboard tampilkan Sleep Score, Mood, AI Recommendation, Progress |  |
| 4\. User pilih histori → sistem tampilkan detail |  |
| Informasi | Sleep Score, mood hari ini, ringkasan AI Recommendation, progress mingguan, riwayat tidur/mood/jurnal/rekomendasi AI |
| Alt. Flow | A1: Belum ada data → Dashboard tampilkan empty state dan arahkan ke Sleep/Mood Tracking atau Daily Journal. |
| Exception | E1: Data gagal dimuat → sistem tampilkan error dan opsi muat ulang. |
| Postcondition | Pengguna memperoleh informasi perkembangan terbaru. |
| Business Rules | BR-25: Dashboard selalu tampilkan data terbaru yang tersinkronisasi. BR-26: Riwayat diurutkan waktu terbaru. |

**2.9 Use Case Relationship**  
**Register → Login → Manage Profile → \[Sleep Tracking | Mood Tracking | Daily Journal\] → AI Recommendation Engine → \[AI Sleep Insight | Ikigai Recommendation\] → Dashboard & History → Relaxation Hub**  
**2.10 Traceability Matrix**

| Use Case | Functional Requirement | Modul |
| :---- | :---- | :---- |
| UC-01 | FR-001 | Authentication |
| UC-02 | FR-002 | Authentication |
| UC-03 | FR-004 | Profile |
| UC-04 | FR-005, FR-008 | Sleep Tracking |
| UC-05 | FR-006, FR-012 | Mood Tracking |
| UC-06 | FR-011, FR-013, FR-015 | Daily Journal |
| UC-07 | Relaxation Hub Module | Relaxation Hub |
| UC-08 | FR-007, FR-008, FR-009 | AI Sleep Insight |
| UC-09 | FR-016–FR-020 | Ikigai Recommendation |
| UC-10 | FR-021–FR-028 | Dashboard & History |

**2.11 General Constraints**

| Kategori | Batasan |
| :---- | :---- |
| Hardware | Smartphone Android; koneksi internet |
| Software | Android 8+; Kotlin; Jetpack Compose; Google Gemini API; Supabase |
| Operasional | AI memerlukan koneksi internet; sistem bukan alat diagnosis medis; rekomendasi AI bersifat pendamping |

### 2.12 Assumptions and Dependencies

Asumsi:

* Pengguna memiliki perangkat Android yang kompatibel.  
* Pengguna bersedia mengisi jurnal dan data tidur secara rutin.  
* Google Gemini API dan Supabase dapat diakses dengan stabil.  
* Pengguna memberikan data yang benar.

Dependensi:

* Google Gemini API  
* Supabase Cloud Database  
* Internet Connection  
* Android SDK, Jetpack Compose, Kotlin

### 2.13 Nonfunctional Requirements

| Kategori | Batasan |
| :---- | :---- |
| Hardware | Smartphone Android; koneksi internet |
| Software | Android 8+; Kotlin; Jetpack Compose; Google Gemini API; Supabase |
| Operasional | AI memerlukan koneksi internet; sistem bukan alat diagnosis medis; rekomendasi AI bersifat pendamping |

# **Section 3\. Appendixes**

## **Appendix A. Glossary**

| Istilah | Penjelasan |
| :---- | :---- |
| AI | Artificial Intelligence untuk analisis dan rekomendasi personal |
| Sleep Therapy | Pendekatan meningkatkan kualitas tidur melalui pemantauan dan rekomendasi |
| Sleep Insight | Hasil analisis AI terhadap histori tidur |
| Sleep Tracking | Pencatatan waktu tidur, bangun, durasi, kualitas |
| Mood Tracking | Pencatatan kondisi emosional harian |
| Daily Journal | Catatan aktivitas, refleksi, pengalaman, rencana harian |
| Ikigai | Konsep penemuan tujuan hidup |
| Dashboard | Halaman utama ringkasan perkembangan |
| Recommendation Engine | Modul AI penghasil rekomendasi |
| Sleep Score | Nilai kualitas tidur hasil analisis AI |
| Milestone | Target pengembangan diri yang direkomendasikan AI |
| Relaxation Hub | Modul media relaksasi |

## **Appendix B. Business Rules**

Authentication

* BR-01: Email harus unik.  
* BR-02: Password minimal 8 karakter.  
* BR-03: Password disimpan menggunakan algoritma hashing.

Sleep Tracking

* BR-04: Hanya dapat diisi 1 kali/hari.  
* BR-05: Durasi tidur dihitung otomatis dari jam tidur dan jam bangun.  
* Mood Tracking  
* BR-06: Hanya 1 kondisi mood/hari.

Daily Journal

* BR-07: Isi minimal 20 karakter.  
* BR-08: Hanya dapat diakses pemilik akun.

AI Recommendation

* BR-09: AI hanya gunakan data pengguna yang login.  
* BR-10: AI tidak menghasilkan diagnosis medis.  
* BR-11: Rekomendasi bersifat pendamping (decision support).  
* BR-12: Rekomendasi diperbarui jika ada data historis baru.

Ikigai

* BR-13: Rekomendasi harus mempertimbangkan 4 komponen Ikigai (What You Love, What You Are Good At, What The World Needs, What You Can Be Paid For).

## 

## **Appendix C. Data Dictionary**

User

| Field | Type | Description |
| :---- | :---- | :---- |
| user\_id | UUID | Primary Key |
| name | String | Nama pengguna |
| email | String | Email |
| password | String | Password terenkripsi |
| age | Integer | Umur |
| gender | Enum | Jenis kelamin |
| height | Float | Tinggi badan (cm) |
| weight | Float | Berat badan (kg) |
| activity\_level | Enum | Tingkat aktivitas |

Sleep Tracking

| Field | Type |
| :---- | :---- |
| sleep\_id | UUID |
| user\_id | UUID |
| sleep\_time | Time |
| wake\_time | Time |
| sleep\_duration | Float |
| sleep\_score | Integer |
| note | Text |

Mood Tracking

| Field | Type |
| :---- | :---- |
| mood\_id | UUID |
| user\_id | UUID |
| mood | Enum |
| note | Text |
| created\_at | Timestamp |

Daily Journal

| Field | Type |
| :---- | :---- |
| journal\_id | UUID |
| user\_id | UUID |
| title | String |
| content | Text |
| activity | Text |
| plan | Text |
| created\_at | Timestamp |

Recommendation

| Field | Type |
| :---- | :---- |
| recommendation\_id | UUID |
| user\_id | UUID |
| recommendation\_type | Enum |
| title | String |
| description | Text |
| generated\_at | Timestamp |

## **Appendix D. External Interface**

| Interface | Teknologi |
| :---- | :---- |
| Mobile App | Android (Kotlin \+ Jetpack Compose) |
| Database | Supabase PostgreSQL |
| AI Engine | Google Gemini API |

**AI Input:** Sleep Tracking, Mood Tracking, Daily Journal, User Profile  
**AI Output:** Sleep Insight, Ikigai Recommendation, Daily Activity Recommendation, Milestone, Learning Recommendation

## **Appendix E. Error Messages**

| Kode | Pesan |
| :---- | :---- |
| ERR-001 | Email sudah digunakan. |
| ERR-002 | Password tidak sesuai. |
| ERR-003 | Koneksi internet tidak tersedia. |
| ERR-004 | Data gagal disimpan. |
| ERR-005 | AI Service tidak tersedia. |
| ERR-006 | Data historis belum cukup untuk dianalisis. |
| ERR-007 | Isi jurnal tidak boleh kosong. |
| ERR-008 | Profil belum lengkap. |

## 

## **Appendix F. Future Enhancement**

* Integrasi smartwatch dan wearable device  
* Sinkronisasi otomatis dengan Google Fit / Health Connect  
* Voice Journal (Speech-to-Text)  
* AI Chat Therapist berbasis percakapan  
* Prediksi risiko gangguan tidur menggunakan Machine Learning  
* Integrasi kalender untuk penjadwalan aktivitas  
* Komunitas pengguna dan wellness challenge  
* Gamifikasi melalui sistem achievement

## **Appendix G. Requirement Traceability Matrix (RTM)**

| User Story | Use Case | Functional Requirement |
| :---- | :---- | :---- |
| US-01 | UC-01 | FR-001 |
| US-02 | UC-04 | FR-005, FR-008 |
| US-03 | UC-05 | FR-006, FR-012 |
| US-04 | UC-06 | FR-011, FR-013 |
| US-05 | UC-08 | FR-007, FR-008, FR-009 |
| US-06 | UC-09 | FR-016–FR-020 |
| US-07 | UC-10 | FR-021–FR-028 |

## **Appendix H. Document Revision History**

| Version | Date | Author | Description |
| :---- | ----- | :---- | :---- |
| 1.0 | Agustus 2026 | Tim Pengembang | Penyusunan awal SRS |
| 1.1 | \- | \- | Revisi hasil validasi kebutuhan |
| 2.0 | \- | \- | Penyesuaian setelah implementasi MVP |

## **Kesimpulan**

Dokumen SRS ini telah mencakup seluruh elemen inti sesuai standar IEEE 830 dan ISO/IEC/IEEE 29148, meliputi: tujuan dan ruang lingkup sistem, deskripsi umum, arsitektur, karakteristik pengguna, 10 use case specification lengkap dengan alur utama/alternatif/eksepsi, business rules, kebutuhan nonfungsional, data dictionary, external interface, error messages, serta requirement traceability matrix. Seluruh spesifikasi konsisten dengan proposal MindRest AI dan siap dijadikan acuan analisis, perancangan, implementasi, serta pengujian perangkat lunak.

[image1]: <data:image/png;base64,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>