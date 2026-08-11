# Supabase Edge Functions — MindRest_AI

Pipeline deploy & test Edge Functions untuk project **MindRest_AI**.
Dibuat di TASK 1.3 (validasi infra sebelum bangun pipeline Gemini/Ikigai),
diperluas di TASK 1.4 (smoke test Gemini API).

---

## 1. Identitas Project

| Item | Nilai |
|---|---|
| Project name | `MindRest_AI` |
| **Project ref** | `twaphoalrrgujnbshpez` |
| Organization | `wtmadbdvavvnifubobnw` |
| Region | _(cek Dashboard > Settings > General)_ |
| Base URL function | `https://twaphoalrrgujnbshpez.functions.supabase.co/<FUNCTION_NAME>` |

---

## 2. Struktur Folder

```
supabase/
├── README.md                      # setup DB (existing)
├── schema.sql                     # skema Postgres (existing)
└── functions/
    ├── README.md                  # ← file ini
    ├── _shared/
    │   ├── cors.ts                # CORS headers bersama (dipakai semua function)
    │   └── prompts/
    │       └── ikigai.ts          # TASK 3.2 — prompt library Ikigai + JSON schema
    ├── hello/
    │   └── index.ts               # TASK 1.3 — smoke test pipeline deploy
    ├── test-gemini/
    │   └── index.ts               # TASK 1.4 — smoke test pipeline AI (Gemini)
    └── generate-ikigai-report/
        └── index.ts               # TASK 3.1 — Edge Function pipeline Ikigai
```

> **Pola:** tiap function = folder sendiri berisi `index.ts`. Shared helper
> (CORS, prompt library) taruh di `_shared/` lalu `import` dengan path relatif
> (`../_shared/cors.ts`).

---

## 3. Function `hello` (TASK 1.3 — smoke test pipeline)

**Tujuan:** Validasi pipeline deploy. Bukan logic bisnis (Gemini/Ikigai di
function terpisah).

- Method: `GET` (smoke) atau `POST` (dengan `{ "name": "..." }`).
- Deploy flag: `--no-verify-jwt` (anon key cukup, tidak butuh JWT user).

### Deploy

```bash
# Wajib pakai --use-api (bundle server-side tanpa Docker).
# Tanpa --use-api, Docker bundling sering timeout di mesin lokal.
supabase functions deploy hello \
  --no-verify-jwt \
  --use-api \
  --project-ref twaphoalrrgujnbshpez
```

### Test

```bash
URL="https://twaphoalrrgujnbshpez.functions.supabase.co/hello"
KEY="<SUPABASE_ANON_KEY dari .env>"

# GET (smoke test)
curl -s -w "\n[HTTP %{http_code}]\n" "$URL" \
  -H "Authorization: Bearer $KEY" -H "apikey: $KEY"

# POST dengan body
curl -s -w "\n[HTTP %{http_code}]\n" -X POST "$URL" \
  -H "Authorization: Bearer $KEY" -H "apikey: $KEY" \
  -H "Content-Type: application/json" \
  -d '{"name":"MindRest"}'
```

**Respon yang diharapkan (HTTP 200):**

```json
{"message":"hello from edge, MindRest!","function":"hello","project":"mindrest-ai","received_method":"POST","timestamp":"..."}
```

### Latency teramati (TASK 1.3)

| Skenario | Latency |
|---|---|
| GET (cold start) | ~2.3s |
| POST (warm) | ~1.1s |

> Catatan untuk UX: cold start Edge Function ~2-3 detik. UI pipeline Ikigai
> (TASK 3.x) wajib punya loading state yang menampung delay ini.

---

## 4. Function `test-gemini` (TASK 1.4 — AI smoke test)

**Tujuan:** Validasi pipeline AI (call Gemini API dari Edge Function).
Bukan logic produksi — pipeline Ikigai ada di function `generate-ikigai-report`
(TASK 3.1).

- Method: `GET` (default prompt) atau `POST` (custom `{prompt, model}`).
- Pakai SDK `@google/generative-ai@^0.21` (resolved → 0.24.x).
- Pakai model `gemini-3.5-flash-lite` (lihat section **Pemilihan Model** di bawah).
- Secret yang dipakai: `GEMINI_API_KEY`.
- Deploy flag: `--no-verify-jwt` (anon key cukup untuk smoke test).

### Deploy

```bash
supabase functions deploy test-gemini \
  --no-verify-jwt \
  --use-api \
  --project-ref twaphoalrrgujnbshpez
```

### Test

```bash
URL="https://twaphoalrrgujnbshpez.functions.supabase.co/test-gemini"
KEY="<SUPABASE_ANON_KEY dari .env>"

# GET (default prompt: "Sebut 3 warna primer dalam 1 kalimat.")
curl -s -w "\n[HTTP %{http_code}]\n" "$URL" \
  -H "Authorization: Bearer $KEY" -H "apikey: $KEY"

# POST dengan prompt custom
curl -s -w "\n[HTTP %{http_code}]\n" -X POST "$URL" \
  -H "Authorization: Bearer $KEY" -H "apikey: $KEY" \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Jawab 1 kata: apa ibukota Jepang?"}'

# POST dengan model lebih besar (TASK 3.1 mungkin perlu ini)
curl -s -w "\n[HTTP %{http_code}]\n" -X POST "$URL" \
  -H "Authorization: Bearer $KEY" -H "apikey: $KEY" \
  -H "Content-Type: application/json" \
  -d '{"prompt":"...","model":"gemini-3.5-flash"}'
```

### Bentuk Respon

Selalu `{ok: boolean, data?: {...}, error?: {...}}` agar client (curl / test e2e)
bisa branch berdasarkan `ok` tanpa parse pesan.

**Sukses (HTTP 200):**

```json
{
  "ok": true,
  "data": {
    "text": "Tiga warna primer adalah merah, kuning, dan biru.",
    "model": "gemini-3.5-flash-lite",
    "prompt": "Sebut 3 warna primer dalam 1 kalimat.",
    "latency_ms": 571,
    "usage": { "promptTokenCount": 12, "candidatesTokenCount": 12, "totalTokenCount": 24 }
  }
}
```

**Error (HTTP 4xx / 5xx):**

```json
{
  "ok": false,
  "error": {
    "code": "model_not_found",
    "message": "[GoogleGenerativeAI Error]: ... [404 Not Found] ..."
  }
}
```

| `error.code`      | HTTP | Penyebab umum                                       |
|-------------------|------|-----------------------------------------------------|
| `missing_api_key` | 500  | `GEMINI_API_KEY` belum di-set di secrets             |
| `invalid_key`     | 500  | API key ditolak (401/403 / typo)                    |
| `rate_limited`    | 429  | Free-tier quota Gemini habis / RPM exceeded         |
| `model_not_found` | 400  | Model id salah / deprecated (mis. `gemini-1.5-flash`) |
| `safety_block`    | 400  | Prompt ditolak filter keamanan Gemini               |
| `upstream_error`  | 502  | 5xx / network / timeout dari Google API             |
| `prompt_too_long` | 400  | Prompt >1000 karakter                               |
| `sdk_init_failed` | 500  | Inisialisasi SDK gagal (key format salah)           |
| `unknown`         | 500  | Fallback — lihat `error.message`                    |

### Latency teramati (TASK 1.4, model `gemini-3.5-flash-lite`)

Pengukuran via curl dari mesin lokal ke function URL.

| Skenario                  | Server (`latency_ms`) | Total round-trip (curl) |
|---------------------------|-----------------------|-------------------------|
| First call (cold-ish)     | 571 ms                | 1.94 s                  |
| Warm #1                   | 670 ms                | 2.01 s                  |
| Warm #2                   | 543 ms                | 0.83 s                  |
| Warm #3                   | 603 ms                | 0.82 s                  |
| After 30s idle            | 663 ms                | 1.02 s                  |

Catatan:
- **`latency_ms`** di body = waktu eksekusi di dalam Deno (Gemini call +
  parsing). Server ini stabil di **~550-700 ms** untuk prompt pendek.
- **Total round-trip** = latensi Deno + network (client → Supabase Edge
  → Google API → balik). Budget UX: **~1-2 detik** untuk prompt pendek.
- Edge Function Supabase nampaknya **tidak cold-start** setelah idle 30 detik
  (mungkin threshold cold-start ~5+ menit). Untuk UI pipeline Ikigai
  (TASK 3.x) tetap wajib punya loading state karena response AI penuh
  akan lebih lama (prompt besar + output markdown panjang).

### Pemilihan Model (kenapa `gemini-3.5-flash-lite`)

Hasil probing semua model pada SDK 0.24.x dengan API key yang aktif:

| Model                  | Status                                    | Catatan |
|------------------------|-------------------------------------------|---------|
| `gemini-1.5-flash`     | ❌ 404 Not Found                          | Sudah deprecated |
| `gemini-1.5-pro`       | ❌ 404 Not Found                          | Sudah deprecated |
| `gemini-2.0-flash`     | ⚠️ 429 quota habis                       | Free-tier quota exceeded |
| `gemini-2.0-flash-lite`| ⚠️ 429 quota habis                       | Free-tier quota exceeded |
| `gemini-2.5-flash`     | ❌ 404 "no longer available to new users" | |
| `gemini-2.5-flash-lite`| ❌ 404 "no longer available to new users" | |
| `gemini-3.5-flash`     | ✅ Works (8.6s cold, ~3s warm)            | Kualitas lebih tinggi, cocok untuk pipeline Ikigai |
| `gemini-3.5-flash-lite`| ✅ Works (~600ms warm)                    | **Dipakai sebagai default** — cukup untuk smoke test |

> **Untuk TASK 3.1 (`generate-ikigai-report`)**: pertimbangkan
> `gemini-3.5-flash` (bukan lite) karena output markdown panjang + JSON
> terstruktur butuh kualitas lebih. Uji kualitas output dulu sebelum
> commit ke default.

---

## 5. Function `generate-ikigai-report` (TASK 3.1 — pipeline Ikigai)

**Tujuan:** Pipeline utama Ikigai. Verifikasi JWT user, rate limit, fetch
assessment + data pasif 7 hari, panggil Gemini (JSON mode), parse,
INSERT ke `ikigai_reports` via service role, return report terstruktur ke
client.

- Method: `POST` (saja).
- **Wajib** `Authorization: Bearer <USER_JWT>` — function ini melakukan
  verifikasi JWT di-handler (auth.uid) lalu rate-limit per user.
- Pakai SDK `@google/generative-ai@^0.21` (sama dengan `test-gemini`).
- Pakai model **`gemini-3.5-flash`** (bukan lite — butuh kualitas output
  markdown panjang + JSON terstruktur).
- Secret yang dipakai: `GEMINI_API_KEY` + `SUPABASE_SERVICE_ROLE_KEY`
  (otomatis ada di runtime Supabase).
- **Tidak** `--no-verify-jwt` — JWT harus diverifikasi.

### Deploy

```bash
supabase functions deploy generate-ikigai-report \
  --use-api \
  --project-ref twaphoalrrgujnbshpez
```

> Tanpa `--no-verify-jwt`: function akan menerima header `Authorization`
> dengan JWT user, dan handler di dalam melakukan verifikasi manual via
> `supabase.auth.getUser(jwt)`.

### Test (butuh JWT user asli)

```bash
URL="https://twaphoalrrgujnbshpez.functions.supabase.co/generate-ikigai-report"
ANON_KEY="<SUPABASE_ANON_KEY dari .env>"

# 1. Ambil JWT user (login via password — untuk testing manual).
# Bisa pakai supabase-js di Node atau langsung via REST auth endpoint:
curl -s -X POST "https://twaphoalrrgujnbshpez.supabase.co/auth/v1/token?grant_type=password" \
  -H "apikey: $ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"password123"}'
# Output JSON → ambil field .access_token → simpan sebagai JWT.

JWT="<ACCESS_TOKEN dari step 1>"

# 2. POST ke function dengan JWT user.
curl -s -w "\n[HTTP %{http_code}]\n" -X POST "$URL" \
  -H "Authorization: Bearer $JWT" \
  -H "apikey: $ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Respon sukses (HTTP 200):**

```json
{
  "ok": true,
  "data": {
    "report": {
      "report_markdown": "Hai [nama]! Dari jawaban kamu...",
      "ikigai_circles": {
        "passion": "...",
        "skill": "...",
        "profession": "...",
        "mission": "..."
      },
      "recommendations": [
        { "id": "uuid-1", "text": "Blok kalender ...", "done": false },
        { "id": "uuid-2", "text": "Coba ritual ...",  "done": false }
      ]
    },
    "report_id": "uuid",
    "assessment_id": "uuid",
    "version": 1,
    "latency_ms": 7823,
    "usage": { "promptTokenCount": 812, "candidatesTokenCount": 532, "totalTokenCount": 1344 }
  }
}
```

**Respon error umum:**

| HTTP | `error.code` | Penyebab |
|------|--------------|----------|
| 401 | `missing_authorization` | Header Authorization tidak ada |
| 401 | `invalid_authorization` | Token kosong setelah `Bearer ` |
| 401 | `invalid_jwt` | JWT salah / kadaluarsa |
| 404 | `no_assessment` | User belum isi 6 pertanyaan assessment |
| 429 | `already_generated_today` | Report sudah di-generate dalam 24 jam terakhir |
| 500 | `missing_api_key` | `GEMINI_API_KEY` belum di-set |
| 500 | `missing_runtime_config` | Service role key tidak ada di runtime |
| 502 | `json_parse_failed` | Output Gemini tidak valid JSON |
| 502 | `schema_violation` | Output Gemini tidak sesuai schema |
| 429 | `rate_limited` | Free-tier Gemini quota habis |
| 500 | `insert_failed` | Gagal INSERT ke `ikigai_reports` |

### Alur singkat

1. **CORS preflight** → OPTIONS di-handle.
2. **Method check** → POST saja.
3. **Secret check** → `GEMINI_API_KEY` ada?
4. **JWT verify** → `supabase.auth.getUser(jwt)` → dapat `user_id`.
5. **Rate limit** → cek `ikigai_reports` WHERE `user_id` AND `generated_at > now()-24h`.
   Kalau ada → 429 `already_generated_today`.
6. **Fetch assessment** → row terbaru dari `ikigai_assessments`.
   Kalau tidak ada → 404 `no_assessment`.
7. **Fetch passive data** (best-effort) → mood/sleep/journal 7 hari.
8. **Build prompt** via `buildIkigaiPrompt()` dari `_shared/prompts/ikigai.ts`.
9. **Call Gemini** dengan `responseMimeType: "application/json"` +
   `responseSchema: IKIGAI_RESPONSE_SCHEMA`.
10. **Parse + enrich** → tambah `id` (UUID) + `done: false` per rekomendasi.
11. **INSERT** ke `ikigai_reports` pakai **service role** (bypass RLS).
12. **Return** report JSON ke client.

### Catatan Model & Latency

Model `gemini-3.5-flash` jauh lebih lambat dari `lite` (~9.6s cold start
di test-gemini, ~600ms-1s warm). Total round-trip untuk laporan penuh
(350-600 kata markdown + JSON) biasanya **6-12 detik**.

**Wajib:**
- UI client (TASK 3.3) harus punya loading state yang menampung delay ini.
- Snackbar / progress indicator yang user-friendly (jangan silent spinner).
- Tangani `429 already_generated_today` (cek via query sebelum panggil).

### Acceptance Test Result (2026-08-10)

Hasil verifikasi end-to-end di project `twaphoalrrgujnbshpez` (sebelum
serah-terima ke TASK 3.3). Jalankan command persis seperti di README
untuk ulangan.

| # | Skenario | Expected | Actual | Status |
|---|----------|----------|--------|--------|
| 1 | `OPTIONS` preflight | 200 + CORS headers | 200, `access-control-allow-*` ada | ✅ |
| 2 | `POST` tanpa `Authorization` | 401 `missing_authorization` | 401 `{"code":"missing_authorization"}` | ✅ |
| 3 | `POST` dengan JWT malformed | 401 (gateway) | 401 `UNAUTHORIZED_INVALID_JWT_FORMAT` | ✅ |
| 4 | `GET` (method salah) | 405 `method_not_allowed` | 405 `{"code":"method_not_allowed"}` | ✅ |
| 5 | `POST` valid JWT, belum ada `ikigai_assessments` | 404 `no_assessment` | 404 `{"code":"no_assessment"}` | ✅ |
| 6 | `POST` valid JWT + assessment ada | 200 + report sesuai contract | 200, `report.report_markdown` 2.163 char, `recommendations[3]` lengkap | ✅ |
| 7 | Row `ikigai_reports` ter-INSERT | 1 row baru via service role | `id=efbb3eba-..., assessment_id=f571419a-..., version=1, rec_count=3` | ✅ |
| 8 | User `SELECT` own report (RLS) | 200 + 1 row | `200`, `content-range: 0-0/*` | ✅ |
| 9 | `POST` kedua kali dalam 24h | 429 `already_generated_today` | 429 `{"code":"already_generated_today","message":"... Coba lagi setelah 2026-08-11T09:31:07.323Z"}` | ✅ |

**Contoh report sukses (tersimpan di `ikigai_reports`):**

- `report_markdown`: 4 paragraf, ~375 kata, bahasa Indonesia, menyebutkan
  Q5 (kecemasan AI) secara empatik, menyeimbangkan passion/skill/profession/mission.
- `ikigai_circles`: 4 field lengkap (passion, skill, profession, mission).
- `recommendations`: 3 item, masing-masing `{id: UUID, text, done: false}`.
- `latency_ms`: 14.366 (cold start). `usage.totalTokenCount`: 3.702 (prompt 922 +
  output 717 + internal thinking ~2.063).

**Bug yang ditemukan & diperbaiki saat testing:**

1. **`maxOutputTokens: 2048` tidak cukup** — `gemini-3.5-flash` adalah thinking
   model, sebagian token budget terpakai untuk internal reasoning. Output
   JSON terpotong di ~287 char. **Fix:** naikkan ke `8192` (di-comment di
   `index.ts` agar tidak dilupakan).

### Rate Limit (1 report / 24 jam)

Di-handler, bukan di Gemini. Query:

```sql
SELECT id, generated_at FROM ikigai_reports
 WHERE user_id = '<user_id>'
   AND generated_at > now() - interval '24 hours'
 ORDER BY generated_at DESC
 LIMIT 1;
```

Kalau ada → 429 + `nextAvailableIso` di message.

> **TASK 4.1** akan menambah logika "refresh dengan data pasif" yang
> increment `version` (2, 3, ...). Untuk TASK 3.1, version selalu = 1.

---

## 6. Secrets

Cek secret yang aktif:

```bash
supabase secrets list --project-ref twaphoalrrgujnbshpez
```

| Secret | Status | Dipakai oleh |
|---|---|---|
| `GEMINI_API_KEY` | ✅ ter-set | TASK 1.4 (`test-gemini`) & TASK 3.1 (`generate-ikigai-report`) |
| `SUPABASE_SERVICE_ROLE_KEY` | ✅ otomatis | TASK 3.1 (`generate-ikigai-report`) — bypass RLS untuk INSERT |

> 🔒 `GEMINI_API_KEY` **tidak pernah** menyentuh Android client. Hanya dibaca
> via `Deno.env.get("GEMINI_API_KEY")` di dalam function (server-side).

---

## 7. Prasyarat (sudah dilakukan user)

1. ✅ Install Supabase CLI (v2.113.0).
2. ✅ `supabase login`.
3. ✅ `supabase link --project-ref twaphoalrrgujnbshpez` (project sudah linked — metadata di `supabase/.temp/`).
4. ✅ `supabase secrets set GEMINI_API_KEY=<dari Google AI Studio>`.

---

## 8. Catatan Operasional

- **`--use-api` wajib** saat deploy di mesin ini. Default deploy memakai Docker
  bundling yang timeout (>3 menit) di environment tanpa Docker daemon yang
  sehat. `--use-api` bundle di server Supabase → cepat & andal.
- **Verifikasi JWT off** (`--no-verify-jwt`) hanya untuk `hello` dan
  `test-gemini` (keduanya smoke test publik). Function produksi
  (`generate-ikigai-report` di TASK 3.1) wajib verify JWT (default) + rate
  limit per ROADMAP.
- **Tidak ada `config.toml`** di repo ini. Deploy per-function jalan tanpa itu
  selama project sudah linked. Bila nanti butuh config global (mis. scheduling
  pg_cron di TASK 4.2), jalankan `supabase init`.
- **Free-tier quota Gemini** (`2.0-flash` dan `2.0-flash-lite`) sudah habis
  untuk API key ini. Untuk development iterasi, pakai `gemini-3.5-flash-lite`
  yang free-tier lebih murah. Monitor penggunaan di
  https://aistudio.google.com/apikey.