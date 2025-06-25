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
 * @param highlightMode Specifies how the text it [com.jet.tts.old.TextTts] will be highlighted.
 * @param onInitialized Callback to be called when [TextToSpeech] is initialized for further
 * @param isUsingResume True when you want [TtsClient] to support a "resume" function allowing to
 * resume speech instead of being spoken from beginning.
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
    isUsingResume: Boolean = false,
): TtsClient {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current

    if (isInspection) {
        return remember {
            TtsClientPreview()
        }
    }

    val coroutineScope = rememberCoroutineScope()



    val client: TtsClientImpl = remember {
        TtsClientImpl(
            context = context,
            initialHighlightMode = highlightMode,
            onInitialized = onInitialized,
            coroutineScope = coroutineScope,
            isUsingResume=isUsingResume,
        )
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            client.release()
        }
    }

    return client
}