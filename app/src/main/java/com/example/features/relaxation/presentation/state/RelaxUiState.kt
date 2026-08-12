package com.example.features.relaxation.presentation.state

/**
 * UI state untuk RelaxScreen — T-009 (FR-017).
 *
 * [RelaxMediaItem] sekarang punya `audioUrl` + `durationLabel`
 * (mis. "10:00") supaya ExoPlayer bisa fetch dan play. `RelaxCategory`
 * tetap dipakai untuk grouping di LazyColumn.
 *
 * State tambahan untuk playback:
 * - [currentItemId]: id item yang sedang aktif (null = tidak ada).
 * - [isPlaying]: true saat ExoPlayer.playbackState == Player.STATE_READY
 *   dan playWhenReady=true.
 * - [playbackPositionMs] / [durationMs]: progress bar polling.
 */
data class RelaxUiState(
    val mediaItems: List<RelaxMediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val currentItemId: String? = null,
    val isPlaying: Boolean = false,
    val playbackPositionMs: Long = 0L,
    val durationMs: Long = 0L
)

data class RelaxMediaItem(
    val id: String,
    val title: String,
    val category: RelaxCategory,
    val audioUrl: String,
    val durationLabel: String = "—",
    val thumbnailUrl: String = "" // Placeholder for thumbnail URL
)

enum class RelaxCategory(val displayName: String) {
    VIDEO("Video Relaksasi"),
    MUSIC("Musik Fokus"),
    MEDITATION("Meditasi")
}
