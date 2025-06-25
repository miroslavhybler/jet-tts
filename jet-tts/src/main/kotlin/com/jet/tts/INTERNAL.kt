@file:Suppress("RedundantUnitReturnType")

package com.jet.tts

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.TextLayoutResult


////////////////////////////////////////////////////////////////////////////////////////////////////
/////
/////   Internal functions for TextTts
/////
////////////////////////////////////////////////////////////////////////////////////////////////////


/**
 * Tries to scroll to the line of text that is currently spoken by [TtsClient] when conditions are met:
 * * Current [range]'s utteranceId matches [utteranceId]
 * * [TtsClient.isSpeaking] is true.
 * * [scrollableState] is not null
 * * New scroll value is greater than the current scroll value
 * @since 1.0.0
 */
internal suspend fun tryScrollToCurrentLine(
    range: UtteranceProgress,
    utteranceId: String,
    scrollableState: ScrollableState?,
    textLayout: TextLayoutResult?,
    layoutCoordinates: LayoutCoordinates?,
    currentSpokenLine: Int,
    extraOffset: Int
): Unit {
    if (range.utteranceId != utteranceId) return
    if (scrollableState == null) return
    val textLayoutResult = textLayout ?: return
    val layout = layoutCoordinates ?: return

    // Y position of the line in text
    val lineTop = textLayoutResult.getLineTop(lineIndex = currentSpokenLine)
    // Convert to visible window coordinates
    val y = layout.localToWindow(relativeToLocal = Offset(x = 0f, y = lineTop)).y
    val scrollTo = y - extraOffset
    /*
    if (ttsClient.isSpeaking && scrollTo <= scrollState.value) {
        //For best UX scroll should always go down when client is speaking
        return
    }
*/

    //Scrolling to the bottom of the line, assuring that spoken text is always visible
    //on the screen
    scrollableState.animateScrollBy(
        value = scrollTo,
        animationSpec = tween(
            durationMillis = 2000,
            easing = LinearEasing,
        )
    )
}

