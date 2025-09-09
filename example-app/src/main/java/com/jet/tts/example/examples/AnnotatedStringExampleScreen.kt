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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jet.tts.TextTts
import com.jet.tts.TtsClient
import com.jet.tts.TtsLifecycleAwareEffect
import com.jet.tts.example.LocalTtsClient
import com.jet.tts.example.R
import com.jet.tts.rememberTtsClient
import com.jet.tts.rememberTtsState


private val content: AnnotatedString = buildAnnotatedString {
    append(text = "Yellowstone National Park, ")
    append(char = ' ')
    withStyle(style = SpanStyle(color = Color.Red)) {
        append(text = "the oldest")
    }
    append(text = ", one of ")
    withStyle(style = SpanStyle(color = Color.Red)) {
        append(text = "the largest")
    }
    append(text = " and probably the best-known national park in the ")

    withLink(
        link = LinkAnnotation.Url(
            url = "https://www.britannica.com/place/Yellowstone-National-Park"
        )
    ) {
        withStyle(
            style = SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline,
            ),
        ) {
            append(text = "United States")
        }
    }

    append(char = '.')
}


/**
 * @author Miroslav Hýbler <br>
 * created on 17.03.2025
 */
@Composable
fun AnnotatedStringExampleScreen(
    ttsClient: TtsClient = LocalTtsClient.current
) {

    //Must use AnnotatedString.toString()
    val ttsState = rememberTtsState(
        utterances = listOf(
            "AnnotatedStringExampleScreen_content" to "$content",
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
                    .padding(horizontal = 16.dp)
                    .padding(paddingValues = innerPadding)
            ) {

                Spacer(modifier = Modifier.height(height = 16.dp))

                TextTts(
                    text = content,
                    ttsClient = ttsClient,
                    utteranceId = "AnnotatedStringExampleScreen_content",
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
                            text = content.toString(),
                            utteranceId = "AnnotatedStringExampleScreen_content",
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


@Composable
@Preview
private fun AnnotatedStringExampleScreenPreview() {
    AnnotatedStringExampleScreen(
        ttsClient = rememberTtsClient(),
    )
}