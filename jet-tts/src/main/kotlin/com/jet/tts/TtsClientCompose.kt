package com.jet.tts

import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext


/**
 * @author Miroslav Hýbler <br>
 * created on 04.02.2025
 */
@Composable
@Keep
fun rememberTtsClient(
    highlightMode: TtsClient.HighlightMode = TtsClient.HighlightMode.SPOKEN_WORD,
    onInitialized: (TtsClient) -> Unit = {},
): TtsClient {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val stateHolder: TtsClientStateHolder = rememberSaveable(
        saver = TtsClientStateHolder.Saver,
        init = {
            TtsClientStateHolder()
        },
    )


    val client: TtsClient = remember {
        TtsClient(
            context = context,
            highlightMode = highlightMode,
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