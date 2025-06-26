package com.jet.tts.example.examples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.jet.tts.old.TextTts
import com.jet.tts.TtsClient
import com.jet.tts.TtsLifecycleAwareEffect
import com.jet.tts.example.LocalTtsClient
import com.jet.tts.example.R
import com.jet.tts.rememberTtsState


private const val content: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
        "Mauris mattis risus porttitor purus pellentesque facilisis vitae egestas metus. " +
        "Curabitur rutrum sed leo at ornare. Nunc nec arcu commodo, dignissim mauris eu, lobortis."

/**
 * Example with toggling the [TtsClient.highlightMode].
 * @author Miroslav Hýbler<br>
 * created on 08.03.2025
 */
@Composable
fun ToggleHighlightModeScreen() {
    val ttsClient = LocalTtsClient.current

    val ttsState = rememberTtsState(
        utterances = listOf(
            "ToggleHighlightModeScreen_content" to content,
        )
    )

    TtsLifecycleAwareEffect(
        client = ttsClient,
        state = ttsState,
    )

    LaunchedEffect(key1 = Unit) {
        ttsClient.highlightMode = TtsClient.HighlightMode.SPOKEN_RANGE_FROM_BEGINNING
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding)
                    .padding(horizontal = 16.dp)
            ) {

                Spacer(modifier = Modifier.height(height = 16.dp))

                TextTts(
                    text = content,
                    ttsClient = ttsClient,
                    utteranceId = "ToggleHighlightModeScreen_content",
                    highlightStyle = TextStyle(color = Color.Red)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.navigationBarsPadding(),
                onClick = {
                    if (ttsClient.isSpeaking) {
                        ttsClient.stop()
                    } else {
                        ttsClient.speak(
                            text = content,
                            utteranceId = "ToggleHighlightModeScreen_content",
                            queueMode = TtsClient.QueueMode.FLUSH,
                        )
                    }
                },
            ) {
                Icon(
                    painter = painterResource(
                        id = if (ttsClient.isSpeaking)
                            R.drawable.ic_stop
                        else
                            R.drawable.ic_play
                    ),
                    contentDescription = null,
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "${ttsClient.highlightMode}")

                    Button(
                        onClick = {
                            ttsClient.highlightMode = when (ttsClient.highlightMode) {
                                TtsClient.HighlightMode.SPOKEN_RANGE_FROM_BEGINNING,
                                TtsClient.HighlightMode.SPOKEN_RANGE_FROM_BEGINNING_INCLUDING_PREVIOUS_UTTERANCES ->
                                    TtsClient.HighlightMode.SPOKEN_WORD

                                TtsClient.HighlightMode.SPOKEN_WORD ->
                                    TtsClient.HighlightMode.SPOKEN_RANGE_FROM_BEGINNING
                            }
                        }
                    ) {
                        Text(text = "Toggle highlight mode")
                    }
                }
            }
        }
    )
}