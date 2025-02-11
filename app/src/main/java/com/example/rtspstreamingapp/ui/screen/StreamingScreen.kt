package com.example.rtspstreamingapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.example.rtspstreamingapp.ui.components.ControlButton
import com.example.rtspstreamingapp.ui.viewmodel.MainViewModel
import com.example.rtspstreamingapp.ui.viewmodel.PlayerState

@Composable
fun StreamingApp(
    viewModel: MainViewModel = hiltViewModel()
) {
    val playerState = viewModel.playerState
    val errorMessage = viewModel.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding( horizontal = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        UrlInputField(viewModel.rtspUrl) { viewModel.updateUrl(it) }
        VideoPlayer(viewModel, playerState)
        LoadingIndicator(playerState)
        ErrorMessage(errorMessage)
        PlayerControls(viewModel, playerState)
    }
}


@Composable
fun UrlInputField(url: String, onUrlChange: (String) -> Unit) {
    OutlinedTextField(
        value = url,
        onValueChange = onUrlChange,
        label = { Text("Enter RTSP URL") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        singleLine = true
    )
}

@Composable
fun VideoPlayer(viewModel: MainViewModel, playerState: PlayerState) {
    if (playerState != PlayerState.Initial) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.player
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun LoadingIndicator(playerState: PlayerState) {
    if (playerState == PlayerState.Buffering) {
        CircularProgressIndicator(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ErrorMessage(errorMessage: String?) {
    errorMessage?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun PlayerControls(viewModel: MainViewModel, playerState: PlayerState) {
    when (playerState) {
        PlayerState.Initial, PlayerState.Error -> {
            ControlButton("Start") { viewModel.startStreaming() }
        }
        PlayerState.Ready -> {
            ControlButton("Pause") { viewModel.pauseStreaming() }
            ControlButton("Stop") { viewModel.stopStreaming() }
        }
        PlayerState.Paused -> {
            ControlButton("Resume") { viewModel.startStreaming() }
            ControlButton("Stop") { viewModel.stopStreaming() }
        }
        else -> {}
    }
}


