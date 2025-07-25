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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jet.tts.old.TextTts
import com.jet.tts.TtsClient
import com.jet.tts.TtsLifecycleAwareEffect
import com.jet.tts.example.LocalTtsClient
import com.jet.tts.example.R
import com.jet.tts.rememberTtsState

private const val content: String =
    "Jet Tts is a simple library providing basic ui for Text to Speech service with " +
            "feature of highlighting spoken text."

private const val content2: String =
    "This screen contains example of having multiple contents in tts."
private const val content3: String = "TtsClient is also saving and restoring state."


/**
 * Example of having multiple utterances in [TextTts].
 * @author Miroslav Hýbler <br>
 * created on 06.02.2025
 */
@Composable
fun MultipleTextsExampleScreen() {
    val ttsClient = LocalTtsClient.current

    val ttsState = rememberTtsState(
        utterances = listOf(
            "MultipleTextsExampleScreen_1" to content,
            "MultipleTextsExampleScreen_2" to content2,
            "MultipleTextsExampleScreen_3" to content3,
        )
    )

    TtsLifecycleAwareEffect(
        client = ttsClient,
        state = ttsState,
    )

    LaunchedEffect(key1 = Unit) {
        ttsClient.highlightMode = TtsClient.HighlightMode.SPOKEN_WORD
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
                    utteranceId = "MultipleTextsExampleScreen_1",
                )
                TextTts(
                    text = content2,
                    ttsClient = ttsClient,
                    utteranceId = "MultipleTextsExampleScreen_2",
                )
                TextTts(
                    text = content3,
                    ttsClient = ttsClient,
                    utteranceId = "MultipleTextsExampleScreen_3",
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
                            utteranceId = "MultipleTextsExampleScreen_1",
                        )
                        ttsClient.add(
                            text = content2,
                            utteranceId = "MultipleTextsExampleScreen_2",
                        )
                        ttsClient.add(
                            text = content3,
                            utteranceId = "MultipleTextsExampleScreen_3",
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