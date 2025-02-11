package com.example.rtspstreamingapp.ui.viewmodel

import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.exoplayer.source.UnrecognizedInputFormatException
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val player: ExoPlayer
) : ViewModel() {

    var playerState by mutableStateOf<PlayerState>(PlayerState.Initial)
        private set

    var rtspUrl by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        setupPlayer()
    }

    private fun setupPlayer() {
        player.apply {
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    playerState = when (state) {
                        Player.STATE_READY -> PlayerState.Ready
                        Player.STATE_BUFFERING -> PlayerState.Buffering
                        Player.STATE_ENDED -> PlayerState.Ended
                        Player.STATE_IDLE -> PlayerState.Initial
                        else -> PlayerState.Initial
                    }
                }

                @OptIn(UnstableApi::class)
                override fun onPlayerError(error: PlaybackException) {
                    val errorDetails = when {
                        error.cause is UnrecognizedInputFormatException ->
                            "Unsupported stream format. Check URL format and compatibility."
                        error.cause is IOException ->
                            "Network connection issue. Check your internet and stream availability."
                        else ->
                            "Playback error: ${error.message}"
                    }
                    errorMessage = errorDetails
                    playerState = PlayerState.Error
                }
            })
        }
    }

    fun updateUrl(url: String) {
        rtspUrl = url.trim()
    }

    @OptIn(UnstableApi::class)
    fun startStreaming() {
        // Validate URL more comprehensively
        if (!isValidRtspUrl(rtspUrl)) {
            errorMessage = "Invalid RTSP URL format. Use rtsp://username:password@ip:port/stream"
            playerState = PlayerState.Error
            return
        }

        errorMessage = null
        playerState = PlayerState.Buffering

        val mediaItem = MediaItem.fromUri(rtspUrl)

        try {
            val mediaSource = RtspMediaSource.Factory()
                .setForceUseRtpTcp(true)
                .setTimeoutMs(10_000) // 10 seconds timeout
                .createMediaSource(mediaItem)

            player.apply {
                stop() // Stop any previous playback
                clearMediaItems()
                setMediaSource(mediaSource)
                prepare()
                play()
            }
        } catch (e: Exception) {
            errorMessage = "Failed to create media source: ${e.localizedMessage}"
            playerState = PlayerState.Error
        }
    }

    private fun isValidRtspUrl(url: String): Boolean {
        return url.startsWith("rtsp://") && url.isNotBlank()
    }

    fun pauseStreaming() {
        player.pause()
        playerState = PlayerState.Paused
    }

    fun stopStreaming() {
        player.stop()
        playerState = PlayerState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}

sealed class PlayerState {
    object Initial : PlayerState()
    object Buffering : PlayerState()
    object Ready : PlayerState()
    object Paused : PlayerState()
    object Ended : PlayerState()
    object Error : PlayerState()
}
