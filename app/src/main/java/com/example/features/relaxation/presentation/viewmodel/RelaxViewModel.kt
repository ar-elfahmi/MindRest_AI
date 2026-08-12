package com.example.features.relaxation.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.features.relaxation.presentation.state.RelaxCategory
import com.example.features.relaxation.presentation.state.RelaxMediaItem
import com.example.features.relaxation.presentation.state.RelaxUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk RelaxScreen — T-009 (FR-017).
 *
 * Real ExoPlayer-backed playback. Pola AndroidViewModel (bukan HiltViewModel)
 * sesuai HomeViewModel / LifestyleViewModel di project — manual
 * instantiation, no DI container. `viewModel()` factory otomatis supply
 * [Application] untuk AndroidViewModel constructor.
 *
 * Audio source: 3 tracks dari mixkit.co public CDN (royalty-free, no auth).
 * URLs telah diverifikasi accessible via HEAD request — lihat
 * ctx_search "audio-url".
 *
 * Lifecycle contract (WAJIB):
 * 1. [onCleared] → [ExoPlayer.release] (no codec leak).
 * 2. [onPauseClickedFromUi] / [onStopClickedFromUi] dipanggil dari
 *    RelaxScreen `DisposableEffect(Lifecycle.Event.ON_PAUSE)` → pause
 *    saat app ke background.
 */
class RelaxViewModel(application: Application) : AndroidViewModel(application) {

    private var exoPlayer: ExoPlayer? = null

    private val _uiState = MutableStateFlow(
        RelaxUiState(mediaItems = defaultMediaItems())
    )
    val uiState: StateFlow<RelaxUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RelaxEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<RelaxEvent> = _events.asSharedFlow()

    private var positionPollingJob: Job? = null

    /**
     * Click handler dari UI. 3 behavior:
     * - Same item + playing → pause toggle.
     * - Same item + paused → resume.
     * - Different item / null → start new playback (release old player).
     */
    fun onPlayClicked(item: RelaxMediaItem) {
        val player = ensurePlayer()
        val state = _uiState.value

        if (state.currentItemId == item.id && player.isPlaying) {
            // Toggle pause
            player.pause()
            _uiState.update { it.copy(isPlaying = false) }
            stopPositionPolling()
        } else if (state.currentItemId == item.id && !player.isPlaying) {
            // Resume
            player.play()
            _uiState.update { it.copy(isPlaying = true) }
            startPositionPolling()
        } else {
            // Start new
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(MediaItem.fromUri(item.audioUrl))
            player.prepare()
            player.playWhenReady = true
            _uiState.update {
                it.copy(
                    currentItemId = item.id,
                    isPlaying = false, // akan di-set true di onPlaybackStateChanged
                    playbackPositionMs = 0L,
                    durationMs = 0L
                )
            }
        }
    }

    fun onPauseClicked() {
        exoPlayer?.pause()
        _uiState.update { it.copy(isPlaying = false) }
        stopPositionPolling()
    }

    fun onStopClicked() {
        exoPlayer?.stop()
        _uiState.update {
            it.copy(
                isPlaying = false,
                currentItemId = null,
                playbackPositionMs = 0L,
                durationMs = 0L
            )
        }
        stopPositionPolling()
    }

    fun onSeekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _uiState.update { it.copy(playbackPositionMs = positionMs) }
    }

    /**
     * Lifecycle hook dari RelaxScreen `DisposableEffect` saat
     * [androidx.lifecycle.Lifecycle.Event.ON_PAUSE] fires.
     * Pause tanpa release (resume cepat kalau user balik ke screen).
     */
    fun onLifecyclePaused() {
        exoPlayer?.pause()
        _uiState.update { it.copy(isPlaying = false) }
        stopPositionPolling()
    }

    private fun ensurePlayer(): ExoPlayer {
        return exoPlayer ?: ExoPlayer.Builder(getApplication()).build().also { player ->
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            _uiState.update { it.copy(isPlaying = true, durationMs = player.duration.coerceAtLeast(0L)) }
                            startPositionPolling()
                        }
                        Player.STATE_ENDED -> {
                            _uiState.update {
                                it.copy(
                                    isPlaying = false,
                                    playbackPositionMs = 0L
                                )
                            }
                            stopPositionPolling()
                            _events.tryEmit(RelaxEvent.PlaybackEnded)
                        }
                        Player.STATE_IDLE, Player.STATE_BUFFERING -> {
                            // buffer or idle — no UI update (atau surface ke state kalau perlu)
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.update { it.copy(isPlaying = isPlaying) }
                    if (isPlaying) startPositionPolling() else stopPositionPolling()
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    _uiState.update { it.copy(isPlaying = false) }
                    _events.tryEmit(RelaxEvent.PlaybackError(error.errorCodeName))
                }
            })
            exoPlayer = player
        }
    }

    private fun startPositionPolling() {
        if (positionPollingJob?.isActive == true) return
        positionPollingJob = viewModelScope.launch {
            while (true) {
                val player = exoPlayer ?: return@launch
                if (!player.isPlaying) return@launch
                _uiState.update { it.copy(playbackPositionMs = player.currentPosition.coerceAtLeast(0L)) }
                delay(500L)
            }
        }
    }

    private fun stopPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = null
    }

    override fun onCleared() {
        stopPositionPolling()
        exoPlayer?.release()
        exoPlayer = null
        super.onCleared()
    }

    companion object {
        /**
         * 3 track royalty-free dari mixkit.co. Diperiksa accessible via HEAD
         * 200 OK sebelum commit (T-009 audio URL verification).
         * IDs pakai format santai untuk grouping di [RelaxUiState.currentItemId].
         */
        private fun defaultMediaItems(): List<RelaxMediaItem> = listOf(
            RelaxMediaItem(
                id = "ocean_waves",
                title = "Ocean Waves",
                category = RelaxCategory.VIDEO,
                audioUrl = "https://assets.mixkit.co/active_storage/sfx/2515/2515-preview.mp3",
                durationLabel = "10:00"
            ),
            RelaxMediaItem(
                id = "rain_ambient",
                title = "Rain Ambient",
                category = RelaxCategory.MUSIC,
                audioUrl = "https://assets.mixkit.co/active_storage/sfx/2394/2394-preview.mp3",
                durationLabel = "08:30"
            ),
            RelaxMediaItem(
                id = "forest_birds",
                title = "Forest Birds",
                category = RelaxCategory.MEDITATION,
                audioUrl = "https://assets.mixkit.co/active_storage/sfx/2434/2434-preview.mp3",
                durationLabel = "12:00"
            )
        )
    }
}

sealed class RelaxEvent {
    data object PlaybackEnded : RelaxEvent()
    data class PlaybackError(val codeName: String) : RelaxEvent()
}
