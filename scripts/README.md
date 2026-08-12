# scripts/ — Developer Utility Scripts

Kumpulan script pembantu untuk setup & verifikasi lokal. **Bukan production code**.

## verify-gemini.ps1

Verifikasi bahwa `GEMINI_API_KEY` sudah benar ter-set di **Supabase Edge Function secrets**
(untuk function `test-gemini` & `chat-gemini`).

### Prasyarat

- Project Supabase sudah punya project aktif (lihat `supabase/README.md`)
- File `.env` di root project sudah ada dan berisi:
  - `SUPABASE_URL=https://<project-ref>.supabase.co`
  - `SUPABASE_ANON_KEY=<anon JWT>`
- `GEMINI_API_KEY` sudah di-set di **Supabase Dashboard → Edge Functions → Secrets**
  (bukan di `.env` lokal!)

### Cara Run

**PowerShell** (Windows native, paling gampang):

```powershell
powershell -ExecutionPolicy Bypass -File C:\laragon\www\MindRest_AI\scripts\verify-gemini.ps1
```

**Git Bash** (kalau PowerShell rewel):

```bash
bash /c/laragon/www/MindRest_AI/scripts/verify-gemini.sh
# (skrip .sh belum dibuat — pakai versi PowerShell untuk sekarang)
```

### Expected Output

```
[HTTP 200]
{"ok":true,"data":{"text":"Tiga warna primer adalah merah, kuning, dan biru.","model":"...","latency_ms":666}}
```

### Troubleshooting

| Error | Sebab | Solusi |
|---|---|---|
| `[HTTP 500]` + `GEMINI_API_KEY not configured` | Secret belum masuk Supabase Dashboard | Tambah secret di **Edge Functions → Secrets** |
| `[HTTP 401]` | Anon key di `.env` tidak match dengan project | Sync `.env` dengan Supabase **Settings → API** |
| `[HTTP 404]` | Project ref salah | Cek **Settings → General → Reference ID** |
| `.env not found` | Script jalan dari folder yang salah | Gunakan path absolut, atau `cd` ke root project dulu |
| PowerShell `>>` continuation | Missing closing quote pada command | Pakai path tanpa spasi, atau quote dengan benar |

Detail verifikasi awal: lihat `notes/verify-gemini.scratch.txt` (legacy, sebelum script dipindahkan).

---

## notes/

Folder catatan ephemeral. **Tidak di-commit** (lihat `.gitignore`).

File di sini hanya catatan internal yang tidak perlu masuk repo tapi berguna untuk
debugging/riwayat kerja sama orchestrator/agent.
