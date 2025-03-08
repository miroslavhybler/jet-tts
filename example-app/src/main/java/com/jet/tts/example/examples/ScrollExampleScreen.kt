package com.jet.tts.example.examples

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jet.tts.TextTts
import com.jet.tts.TtsClient
import com.jet.tts.example.LocalTtsClient
import com.jet.tts.example.R
import com.jet.tts.rememberTtsClient
import java.util.Locale


private const val content: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
        "Mauris mattis risus porttitor purus pellentesque facilisis vitae egestas metus. " +
        "Curabitur rutrum sed leo at ornare. Nunc nec arcu commodo, dignissim mauris eu, lobortis."

private const val content2: String = "Nullam ante diam, tincidunt non tincidunt eu, pretium " +
        "ut nunc. Proin a elementum erat, nec maximus elit. Maecenas congue non neque nec " +
        "congue. Nunc tellus erat, viverra nec vehicula quis, varius sit amet eros."

private const val content3: String = "Aliquam interdum id ligula vitae accumsan. Sed maximus " +
        "ligula non libero lobortis tincidunt. Donec accumsan vel nibh a hendrerit. Phasellus " +
        "sem risus, aliquet ut lacus laoreet, tincidunt vestibulum libero. Nullam condimentum " +
        "bibendum dui. Quisque placerat libero ut gravida tincidunt."


/**
 * @author Miroslav Hýbler <br>
 * created on 24.02.2025
 */
@Composable
fun ScrollExampleScreen() {
    val ttsClient = LocalTtsClient.current
    val scrollState = rememberScrollState()


    LaunchedEffect(key1 = Unit) {
        ttsClient.highlightMode = TtsClient.HighlightMode.SPOKEN_RANGE_FROM_BEGINNING
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = scrollState)
                    .padding(paddingValues = innerPadding)
            ) {

                Spacer(modifier = Modifier.height(height = 32.dp))


                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 256.dp),
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                )

                TextTts(
                    text = content,
                    ttsClient = ttsClient,
                    utteranceId = "1",
                    style = MaterialTheme.typography.headlineLarge,
                    scrollState = scrollState,
                )

                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 256.dp),
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                )

                TextTts(
                    text = content2,
                    ttsClient = ttsClient,
                    utteranceId = "2",
                    style = MaterialTheme.typography.headlineLarge,
                    scrollState = scrollState,
                    )
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 256.dp),
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                )
                TextTts(
                    text = content3,
                    ttsClient = ttsClient,
                    utteranceId = "3",
                    style = MaterialTheme.typography.headlineLarge,
                    scrollState = scrollState,
                )

                Spacer(modifier = Modifier.height(height = 32.dp))

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