// Shared CORS headers untuk semua Edge Function MindRest_AI.
// Sumber pola: dokumentasi resmi Supabase (guides/functions/cors.mdx).
export const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers':
    'authorization, x-client-info, apikey, content-type',
}
