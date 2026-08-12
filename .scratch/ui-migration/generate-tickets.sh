#!/usr/bin/env bash
# Generate migration tickets 02-20. Each follows 00-TEMPLATE.md.
# Re-run safe: overwrites. Data table = tab-separated: num|name|wave|effort|file|loc|dp|sp|hex|scaffold|card|lazy|specifics
set -e
OUT="C:/laragon/www/MindRest_AI/.scratch/ui-migration/issues"

# fmt: num  wave  effort  ScreenName  file-relative  loc  dp  sp  hex  scaffold  card  lazy  specifics(notes)
read -r -d '' DATA <<'EOF' || true
02	2	L ~2h	ProfileScreen	features/profile/presentation/screen/ProfileScreen.kt	601	33	6	0	no	4	no	No Scaffold (add one). Profile header = BrandHeader. Settings-ish rows = AppCard(Outlined). Edit-profile belum wired (biarkan).
03	2	M ~1h	MoodTrackingScreen	features/mood/presentation/screen/MoodTrackingScreen.kt	278	21	2	0	yes	1	no	Emoji mood picker (MoodButton sudah tokenized, jangan sentuh). 1 card summary -> AppCard(Tonal).
04	2	M ~1.5h	JournalHistoryScreen	features/journal/presentation/screen/JournalHistoryScreen.kt	473	33	0	0	yes	3	yes	Lazy list of entries -> contentPadding=screenEdgeValues(). 3 cards -> AppCard. WeeklyMoodTimeline mock (biarkan data). Punya empty state -> EmptyState.
05	2	S ~30min	JournalScreen	features/journal/presentation/screen/JournalScreen.kt	88	6	0	0	yes	0	no	Sangat kecil. Form input. Cukup wrap screenEdge + token pasif. Cek apakah save panggil repo (jangan ubah logic).
06	3	M ~1.5h	SleepHubScreen	features/sleep/presentation/screen/SleepHubScreen.kt	382	23	0	1	yes	7	yes	Hub + recommendations (mock listOf, biarkan). 7 cards -> AppCard. Lazy list. 1 hex -> colorScheme.
07	3	S ~45min	SleepTrackingScreen	features/sleep/presentation/screen/SleepTrackingScreen.kt	181	8	0	0	yes	1	no	Kecil. 1 card. Wrap screenEdge. Tracker angka -> NumberXl/NumberL (monospace).
08	3	M ~1h	RelaxScreen	features/relaxation/presentation/screen/RelaxScreen.kt	292	13	0	0	yes	4	yes	Lazy list. 4 cards -> AppCard(Tonal). Audio player card -> AppCard(Elevated).
09	3	M ~1.5h	AdvancedRelaxationScreen	features/relaxation/presentation/screen/AdvancedRelaxationScreen.kt	336	15	0	8	yes	0	no	8 hex (!!) -> map semua ke colorScheme/feature colors. Modes (Gerak/Napas/Suara) -> AppChipGroup. Video placeholder (biarkan).
10	4	M ~1.5h	IkigaiDashboardScreen	features/ikigai/presentation/screen/IkigaiDashboardScreen.kt	328	35	1	0	yes	4	yes	Chart data kemungkinan hardcode (biarkan). 4 cards -> AppCard. Lazy. 7 Column (dense).
11	4	L ~2.5h	IkigaiReportScreen	features/ikigai/presentation/screen/IkigaiReportScreen.kt	747	51	3	4	yes	10	no	READING MODE (Calm ref) -> tipografi serif penting, generous line height. 10 cards -> AppCard. 4 lingkaran visual (biarkan logic). 4 hex -> colorScheme. Body text panjang -> bodyLarge.
12	4	M ~1h	IkigaiAssessmentScreen	features/ikigai/presentation/screen/IkigaiAssessmentScreen.kt	323	19	0	0	yes	0	no	6 pertanyaan assessment (form). Progress indicator -> spacing token. Radio/pilihan -> AppCard(Outlined) atau AppChip.
13	4	S ~20min	IkigaiReportLoadingScreen	features/ikigai/presentation/screen/IkigaiReportLoadingScreen.kt	87	5	1	0	yes	0	no	Sangat kecil. Loading placeholder -> ShimmerBox. 1 sp -> typography.
14	5	L ~2.5h	StatisticsScreen	features/statistics/presentation/screen/StatisticsScreen.kt	772	49	15	0	no	6	no	NO Scaffold (add). 15 sp (!!) -> typography scale ketat. Trend data mock (biarkan). Charts di Charts.kt (jangan migrasi). SegmentedControl sudah ada.
15	5	XL ~4h	LifestyleScreen	features/lifestyle/presentation/screen/LifestyleScreen.kt	1167	78	7	32	yes	14	no	HEAVIEST. 32 hex (!!) -> map semua ke colorScheme/feature. 14 cards -> AppCard. 14 Column (sangat dense, pertimbangkan split tapi out-of-scope). initialLifestyleGoals + weekDays mock (biarkan). Pertimbangkan ekstrak sub-composable tapi jangan over-engineer.
16	5	L ~2.5h	AchievementsScreen	features/achievements/presentation/screen/AchievementsScreen.kt	768	55	11	0	no	7	yes	NO Scaffold (add). 11 sp -> typography. sampleAchievements + steps mock (biarkan). 7 cards -> AppCard. Lazy list.
17	5	M ~1.5h	ReminderScreen	features/reminder/presentation/screen/ReminderScreen.kt	497	24	0	0	yes	4	no	Tidak baca dari DB (mock, biarkan). 4 cards -> AppCard. 6 Column. Time picker styling -> token.
18	6	M ~1.5h	AiJournalScreen	features/journal/presentation/screen/AiJournalScreen.kt	422	42	0	0	yes	0	yes	Chat hardcoded messages (mock, belum panggil Gemini - biarkan). Lazy list chat bubbles -> AppCard(Outlined) untuk user/bot. Input bar -> screenEdge bottom. SendButton sudah tokenized.
19	6	S ~45min	NotificationScreen	features/notification/presentation/screen/NotificationScreen.kt	192	9	0	0	yes	3	yes	notifications mock listOf (biarkan). 3 cards -> AppCard. Lazy list. Empty state -> EmptyState.
20	6	M ~1h	SettingsScreen	features/settings/presentation/screen/SettingsScreen.kt	266	12	3	1	no	1	no	NO Scaffold (add). 3 sp -> typography. 1 hex -> colorScheme. Settings rows -> AppCard(Outlined). Edit profile belum wired (biarkan).
EOF

gen() {
  local num="$1" wave="$2" effort="$3" name="$4" rel="$5" loc="$6" dp="$7" sp="$8" hex="$9" scaffold="${10}" card="${11}" lazy="${12}" specifics="${13}"
  local scaffoldNote
  if [ "$scaffold" = "no" ]; then scaffoldNote="**NO Scaffold (audit) -> TAMBAHKAN \`AppScaffold\`.**"; else scaffoldNote="Sudah punya Scaffold -> ganti ke \`AppScaffold\`."; fi
  local cardNote=""
  [ "$card" != "0" ] && [ "$card" != "no" ] && cardNote="- **$card kartu** ad-hoc -> \`AppCard\` (pilih variant per konteks: Tonal default, Outlined dense, Elevated hero, Brand gradient)."
  local lazyNote=""
  [ "$lazy" = "yes" ] && lazyNote="- **Lazy list** -> \`contentPadding = screenEdgeValues()\`."

  local padded; padded=$(printf '%02d' "$((10#$num))")
  cat > "$OUT/${padded}-$(echo "$name" | sed -E 's/Screen//' | tr '[:upper:]' '[:lower:]')-screen.md" <<TICKET
# $num — Migrate $name to Design Tokens

**Wave:** $wave · **Effort:** $effort · **Status:** ⬜ todo
**File:** \`app/src/main/java/com/example/$rel\`
**LOC:** $loc · **Hardcoded dp:** $dp · **Hardcoded sp:** $sp · **Hardcoded hex:** $hex

> Ikuti resep 9 langkah di [\`../MIGRATION_GUIDE.md\`](../MIGRATION_GUIDE.md).
> Pilot (\`01-home-screen.md\`) sudah memvalidasi resep — jadikan referensi.

## Blocking
- Foundation siap ✅
- Mock data / backend wiring **TIDAK disentuh** (tiket terpisah). Hanya presentation.

## Spesifik screen ini
- $scaffoldNote
$cardNote$lazyNote
- $specifics

## Checklist (MIGRATION_GUIDE.md)
- [ ] Screenshot SEBELUM
- [ ] Root: \`AppScaffold\` + \`screenEdge()\` / \`screenEdgePadded()\`
- [ ] dp -> spacing token (lihat mapping table di guide)
- [ ] sp -> \`MaterialTheme.typography\` ($sp terpaksa pindah)
- [ ] Color hex -> \`colorScheme\` / named color ($hex terpaksa pindah)
- [ ] Card -> \`AppCard\`
- [ ] Section title -> \`SectionHeader\`
- [ ] Empty state -> \`EmptyState\` (jika ada)
- [ ] \`./gradlew :app:compileDebugKotlin\` sukses, 0 error
- [ ] Screenshot SESUDAH + visual parity (no regression)
- [ ] Reviewer subagent

## Acceptance
Lihat 10 kriteria di \`MIGRATION_GUIDE.md\`.

## Catatan
- **Jangan ubah signature composable / ViewModel / Repository.** Hanya presentation.
- Komponen di \`components/\` (Charts, Chat, dll.) **jangan migrasi** di tiket ini (tiket terpisah).
- Setelah selesai: update status board (\`README.md\` $num -> ✅), commit \`refactor(ui): migrate $name to design tokens\`.
TICKET
}

echo "$DATA" | while IFS=$'\t' read -r num wave effort name rel loc dp sp hex scaffold card lazy specifics; do
  [ -z "$num" ] && continue
  gen "$num" "$wave" "$effort" "$name" "$rel" "$loc" "$dp" "$sp" "$hex" "$scaffold" "$card" "$lazy" "$specifics"
  echo "generated $num $name"
done
echo "--- done ---"
ls -1 "$OUT"