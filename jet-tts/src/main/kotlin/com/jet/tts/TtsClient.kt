@file:Suppress(
    "RedundantVisibilityModifier",
    "RedundantModalityModifier",
    "RedundantUnitReturnType"
)

package com.jet.tts

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.annotation.Keep
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale


/**
 * Simple client class to use [TextToSpeech] feature. Take a look at [Dev blog](https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html)
 * to understand how [TextToSpeech] works.
 *
 * ## Features
 *
 * ### Text highlight Feature (api >= 26)
 *Using `TtsClient.HighlightMode` to set how you want to highlight currently spoken text:
 * * `SPOKEN_WORD` - `TextTts` will highlight currently spoken sequence (single word in most cases).
 * * `SPOKEN_RANGE_FROM_BEGINNING` - `TextTts` will highlight range from the beggining to the currently spoken sequence.
 * See [HighlightMode].
 *
 * ### Autoscroll Feature (api >= 26)
 * By providing a [androidx.compose.foundation.ScrollState], [TextTts] can use it to autoscroll to
 * currently spoken line. Solution for [androidx.compose.foundation.lazy.LazyColumn] is not avaliable now.
 * Use [rememberTtsClient] to get an instance of [com.jet.tts.TtsClient].
 *
 * ## Navigation Feature
 * It is possible to "navigate" in utterance when `ttsClient.isSpeaking == true`, by clicking into
 * [TextTts] client will navigate speech by clicked word.
 * @since 1.0.0
 * @author Miroslav Hýbler <br>
 * created on 13.03.2025
 * @see TtsClientImpl
 */
@Keep
public abstract class TtsClient internal constructor() {

    /**
     * Specifies how the text it [TextTts] will be highlighted.
     * @since 1.0.0
     */
    @Keep
    public enum class HighlightMode {

        /**
         * Mode to highlight only the spoken sequence out of the spoken content. This would be a
         * single word in 99% of the time. The other cases are like spelling by characters.
         * @since 1.0.0
         */
        SPOKEN_WORD,

        /**
         * Mode to highlight the spoken range from the beginning of the content until currently
         * spoken word.
         * @since 1.0.0
         */
        SPOKEN_RANGE_FROM_BEGINNING,


        /**
         * Mode to highlight the spoken range from the beginning of the content until currently
         * spoken word including also previous utterances.
         * @since 1.0.0
         */
        SPOKEN_RANGE_FROM_BEGINNING_INCLUDING_PREVIOUS_UTTERANCES;
    }


    /**
     * Specifies how [speak] should handle actual queue.
     * @since 1.0.0
     */
    @Keep
    public enum class QueueMode {

        /**
         * Equivalent for [TextToSpeech.QUEUE_FLUSH]
         * @since 1.0.0
         */
        FLUSH,


        /**
         * Equivalent for [TextToSpeech.QUEUE_ADD]
         * @since 1.0.0
         */
        ADD;
    }


    /**
     * Flag indicating if [TtsClient] is currently speaking. Using [androidx.compose.runtime.MutableState] so
     * it's safe to use it in compose code, e.g.
     * ```kotlin
     * Button(
     *      onClick = {
     *      if(!client.isSpeaking) {
     *          client.speak(text = "Hello World", utteranceId = "greeting")
     *      } else {
     *          client.stop()
     *      }
     * ) {
     *     Text(text= if (client.isSpeaking) "Stop" else "Speak")
     * }
     * ```
     * @since 1.0.0
     */
    public abstract var isSpeaking: Boolean


    /**
     * Current highlight mode, see [HighlightMode] for more details.
     * @since 1.0.0
     */
    public abstract var highlightMode: HighlightMode


    /**
     * Range of text to highlight, collected by [TextTts]
     * @since 1.0.0
     */
    public abstract val utteranceRange: StateFlow<UtteranceProgress>


    /**
     * Sets language of [TextToSpeech] by given [Locale] if it's supported.
     * @param language [Locale] Language to set.
     * @since 1.0.0
     */
    public abstract fun setLanguage(
        language: Locale,
    ): Unit


    /**
     *  If you are using resume feature by setting [TtsClientImpl.isUsingResume] to true, use [flushAndSpeak] and [add] instead.
     * @param text Text to be spoken.
     * @param utteranceId Unique identifier of the utterance. This is required since without [utteranceId],
     * [UtteranceProgressListener] would not be called and text highlight feature would not work.
     * @param queueMode Queue mode of [TextToSpeech], can be [QueueMode.FLUSH] to replace
     * current utterance (If there is some) with a new one or [QueueMode.ADD] to add a new
     * utterance to the queue.
     * @param params Parameters for the request. Can be null. See [TextToSpeech.speak] for more
     * details.
     * @param startIndex Index of the first character of the current utterance.
     * @since 1.0.0
     * @see [TtsClientImpl.speak] for source code implementation.
     */
    @Deprecated(
        message = "Will be Internal in the future, use flushAndSpeak() or add() instead.",
        replaceWith = ReplaceWith(
            expression = "flushAndSpeak(text, utteranceId, params, startIndex)",
        )
    )
    public abstract fun speak(
        text: String,
        utteranceId: String,
        queueMode: QueueMode,
        params: Bundle? = null,
        startIndex: Int = 0,
    ): Unit


    /**
     * Flushes previous queue (if there was) and creates new one. Calls a [speak] with [TextToSpeech.QUEUE_FLUSH].
     * @param text Text to be spoken.
     * @param utteranceId Unique identifier of the utterance. This is required since without [utteranceId],
     * [UtteranceProgressListener] would not be called and text highlight feature would not work.
     * @param params Parameters for the request. Can be null. See [TextToSpeech.speak] for more
     * details.
     * @param startIndex Index of the first character of the current utterance.
     * @since 1.0.0
     */
    public abstract fun flushAndSpeak(
        text: String,
        utteranceId: String,
        params: Bundle? = null,
        startIndex: Int = 0,
    ): Unit


    /**
     * Adds utterance to the queue, calls a [speak] with [TextToSpeech.QUEUE_ADD].
     * @param text Text to be spoken.
     * @param utteranceId Unique identifier of the utterance. This is required since without [utteranceId],
     * [UtteranceProgressListener] would not be called and text highlight feature would not work.
     * @param params Parameters for the request. Can be null. See [TextToSpeech.speak] for more
     * details.
     * @param startIndex Index of the first character of the current utterance.
     * @since 1.0.0
     */
    public abstract fun add(
        text: String,
        utteranceId: String,
        params: Bundle? = null,
        startIndex: Int = 0,
    ): Unit


    /**
     * Stops currently speaking text. Next [speak] request with same utteranceId will start from beginning.
     * @since 1.0.0
     */
    public abstract fun stop(): Unit


    /**
     * Stops currently speaking text in [androidx.compose.runtime.DisposableEffect].
     * This is used instead of [stop] to capture [isSpeaking] state properly.
     * @since 1.0.0
     */
    internal abstract fun stopOnDispose(): Unit


    /**
     * Util function to speak an utterance starting from a given [startIndex]. Uses combination of
     * [startIndex] and [Utterance.currentIndexThreshold] to keep text highlighting consistent.
     * @since 1.0.0
     * @see [TtsClientImpl.navigateInUtterance] for source code implementation.
     */
    internal abstract fun navigateInUtterance(
        utteranceId: String,
        startIndex: Int,
    ): Unit


    /**
     * Util function to get sequence (index) of an utterance.
     * @param utteranceId Id of utterance to get sequence for.
     * @return Sequence (index) of the utterance with given [utteranceId] or [Int.MAX_VALUE] when
     * utterance is not found. This is used for [HighlightMode.SPOKEN_RANGE_FROM_BEGINNING_INCLUDING_PREVIOUS_UTTERANCES]
     * when [TextTts] has to highlight all "previous" utterances too, so returning [Int.MAX_VALUE] by
     * default won't cause unwanted highlights.
     * @since 1.0.0
     */
    internal abstract fun getSequenceForUtterance(
        utteranceId: String,
    ): Int

}