@file:Suppress(
    "DATA_CLASS_COPY_VISIBILITY_WILL_BE_CHANGED_WARNING",
    "RedundantVisibilityModifier",
)

package com.jet.tts

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable


/**
 * Holding progress in utterance spoken by [TtsClient].
 * @param utteranceId Unique id of the utterance.
 * @param range Index range of text to highlight in [TextTts], range is defined by [TtsClient.HighlightMode],
 * see [TtsClient.utteranceRange] for more details.
 * @author Miroslav Hýbler <br>
 * created on 05.02.2025
 * @since 1.0.0
 */
@Keep
@Immutable
public data class UtteranceProgress internal constructor(
    val utteranceId: String,
    private val range: IntRange,
) {

    companion object {

        /**
         * Default empty progress.
         * @since 1.0.0
         */
        val EMPTY: UtteranceProgress = UtteranceProgress(
            range = IntRange.EMPTY,
            utteranceId = "",
        )
    }

    /**
     * Inclusive start index of the [range].
     * @since 1.0.0
     */
    val first: Int
        get() = range.first


    /**
     * Exclusive end index of the [range].
     */
    val last: Int
        get() = range.last
}