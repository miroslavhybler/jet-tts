package com.jet.tts

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable


/**
 * @author Miroslav Hýbler <br>
 * created on 05.02.2025
 */
@Keep
@Immutable
data class UtteranceProgress constructor(
    val utteranceId: String,
    private val range: IntRange,
) {
    companion object {
        val EMPTY: UtteranceProgress = UtteranceProgress(
            range = IntRange.EMPTY,
            utteranceId = "",
        )
    }


    val first: Int
        get() = range.first


    val last: Int
        get() = range.last
}