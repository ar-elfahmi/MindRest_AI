// Smoke-test Edge Function — TASK 1.3.
// Tujuan: validasi pipeline deploy Supabase Edge Functions (bukan logic bisnis).
// Logic Gemini ada di function terpisah (TASK 1.4).
import { corsHeaders } from '../_shared/cors.ts'

Deno.serve(async (req: Request) => {
  // CORS preflight (OPTIONS) wajib ditangani duluan.
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  // Boleh dipanggil via GET (smoke test paling sederhana) atau POST.
  let name = 'world'
  try {
    if (req.method === 'POST') {
      const body = await req.json().catch(() => ({}))
      if (typeof body?.name === 'string' && body.name.trim()) {
        name = body.name.trim()
      }
    }
  } catch {
    // Body bukan JSON → pakai default, bukan error.
  }

  const payload = {
    message: `hello from edge, ${name}!`,
    function: 'hello',
    project: 'mindrest-ai',
    received_method: req.method,
    timestamp: new Date().toISOString(),
  }

  return new Response(JSON.stringify(payload), {
    status: 200,
    headers: { ...corsHeaders, 'Content-Type': 'application/json' },
  })
})
