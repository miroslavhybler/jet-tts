package com.jet.tts

import android.speech.tts.TextToSpeech
import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode


/**
 * @param highlightMode Specifies how the text it [TextTts] will be highlighted.
 * @param onInitialized Callback to be called when [TextToSpeech] is initialized for further
 * configuration (for example setting language).
 * @author Miroslav Hýbler <br>
 * created on 04.02.2025
 * @since 1.0.0
 */
@Composable
@Keep
fun rememberTtsClient(
    highlightMode: TtsClient.HighlightMode = TtsClient.HighlightMode.SPOKEN_WORD,
    onInitialized: (TtsClient) -> Unit = {},
): TtsClient {
    val context = LocalContext.current
    val isInpsection = LocalInspectionMode.current //TODO add preview safe TtsClient, create ITtsClient


    val coroutineScope = rememberCoroutineScope()
    val stateHolder: TtsClientStateHolder = rememberSaveable(
        saver = TtsClientStateHolder.Saver,
        init = { TtsClientStateHolder() },
    )


    val client: TtsClient = remember {
        TtsClient(
            context = context,
            defaultHighlightMode = highlightMode,
            onInitialized = onInitialized,
            stateHolder = stateHolder,
            coroutineScope = coroutineScope,
        )
    }


    DisposableEffect(key1 = Unit) {
        onDispose {
            client.stopOnDispose()
        }
    }

    return client
}