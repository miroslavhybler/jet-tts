package com.jet.tts.example.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.jet.tts.TextTts
import com.jet.tts.old.TextTts
import com.jet.tts.TtsClient
import com.jet.tts.TtsLifecycleAwareEffect
import com.jet.tts.example.LocalTtsClient
import com.jet.tts.example.R
import com.jet.tts.rememberTtsState


private val content: String =
    "Jet Tts is a simple library providing basic ui for Text to Speech service with " +
            "feature of highlighting spoken text."


/**
 * the simplest possible example of [TextTts] and [TtsClient] usage.
 * @author Miroslav Hýbler <br>
 * created on 06.02.2025
 */
@Composable
fun SingleTextExampleScreen() {
    val ttsClient = LocalTtsClient.current

    val ttsState = rememberTtsState(
        utterances = listOf(
            "SingleTextExampleScreen_content" to content,
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
                    utterance = ttsState["SingleTextExampleScreen_content"],
                    ttsClient = ttsClient,
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
                        ttsClient.flushAndSpeak(
                            text = content,
                            utteranceId = "SingleTextExampleScreen_content",
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
        }
    )
}