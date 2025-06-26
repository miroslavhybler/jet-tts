@file:Suppress(
    "DATA_CLASS_COPY_VISIBILITY_WILL_BE_CHANGED_WARNING",
    "RedundantVisibilityModifier",
)

package com.jet.tts

import android.os.Parcelable
import android.speech.tts.UtteranceProgressListener
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize


/**
 * Represents single utterance of [TtsClient] created by [TtsClient.speak]. Implements [Parcelable]
 * to be stored in [TtsState].
 * @param utteranceId Unique identifier of the utterance. This is required since without [utteranceId],
 * [UtteranceProgressListener] would not be called and text highlight feature would not work.
 * @param content Text that is gonna be spoken by [android.speech.tts.TextToSpeech].
 * @param sequence Sequence (or index) of the utterance within single [TtsClient] instance as it
 * was added by [TtsClient.speak].
 * @param currentIndexThreshold Helper variable to store index to be used later when saving/restoring state
 * of [TtsClient]. Since [android.speech.tts.TextToSpeech] provides limited interface to manage spoken
 * content, [currentIndexThreshold] is used for consistency of text highlighting when [TtsClient.navigateInUtterance]
 * is called. See [TtsClient.navigateInUtterance] for more details.
 * @author Miroslav Hýbler <br>
 * created on 06.02.2025
 * @since 1.0.0
 */
@Keep
@Parcelize
public data class Utterance public constructor(
    internal val utteranceId: String,
    internal val content: String,
    internal val sequence: Int,
    internal var currentIndexThreshold: Int = 0,
) : Parcelable {

}