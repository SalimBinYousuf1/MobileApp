package com.example.musicplayer.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlaybackController(context: Context) {
    private val exoPlayer = ExoPlayer.Builder(context).build()
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    private val _currentTrack = MutableStateFlow<Track?>(null)

    val queue: StateFlow<List<Track>> = _queue
    val currentTrack: StateFlow<Track?> = _currentTrack

    fun setQueue(tracks: List<Track>, startAt: Int = 0) {
        _queue.value = tracks
        exoPlayer.setMediaItems(tracks.map { MediaItem.fromUri(it.streamUrl ?: it.localPath ?: "") }, startAt, 0L)
        exoPlayer.prepare()
        _currentTrack.value = tracks.getOrNull(startAt)
    }

    fun playPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun skipNext() = exoPlayer.seekToNextMediaItem()
    fun skipPrevious() = exoPlayer.seekToPreviousMediaItem()

    fun release() = exoPlayer.release()
}
