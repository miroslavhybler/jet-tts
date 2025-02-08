package com.jet.tts.example.examples

import android.speech.tts.TextToSpeech
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jet.tts.TextTts
import com.jet.tts.TtsClient
import com.jet.tts.example.R
import com.jet.tts.rememberTtsClient
import java.util.Locale

private const val content: String =
    "Jet Tts is a simple library providing basic ui for Text to Speech service with " +
            "feature of highlighting spoken text."

private const val content2: String =
    "This screen contains example of having multiple contents in tts."
private const val content3: String = "TtsClient is also saving and restoring state."


/**
 * @author Miroslav Hýbler <br>
 * created on 06.02.2025
 */
@Composable
fun MultipleTextsExampleScreen() {
    val ttsClient = rememberTtsClient(
        highlightMode = TtsClient.HighlightMode.SPOKEN_WORD,
        onInitialized = { ttsClient ->
            ttsClient.setLanguage(language = Locale.US)
        },
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding)
            ) {

                Spacer(modifier = Modifier.height(height = 16.dp))

                TextTts(
                    text = content,
                    ttsClient = ttsClient,
                    utteranceId = "1",
                )
                TextTts(
                    text = content2,
                    ttsClient = ttsClient,
                    utteranceId = "2",
                )
                TextTts(
                    text = content3,
                    ttsClient = ttsClient,
                    utteranceId = "3",
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
                            utteranceId = "1",
                            queueMode = TextToSpeech.QUEUE_FLUSH,
                        )

                        ttsClient.speak(
                            text = content2,
                            utteranceId = "2",
                            queueMode = TextToSpeech.QUEUE_ADD,
                        )

                        ttsClient.speak(
                            text = content3,
                            utteranceId = "3",
                            queueMode = TextToSpeech.QUEUE_ADD,
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