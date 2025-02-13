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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jet.tts.TextTts
import com.jet.tts.TtsClient
import com.jet.tts.rememberTtsClient
import java.util.Locale
import com.jet.tts.example.R


private val content: String =
    "Jet Tts is a simple library providing basic ui for Text to Speech service with " +
            "feature of highlighting spoken text."


/**
 * @author Miroslav Hýbler <br>
 * created on 06.02.2025
 */
@Composable
fun SingleTextExampleScreen() {

    val ttsClient = rememberTtsClient(
        highlightMode = TtsClient.HighlightMode.SPOKEN_RANGE_FROM_BEGINNING,
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
                    utteranceId = "content",
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
                            utteranceId = "content",
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