@file:Suppress("RedundantVisibilityModifier", "RedundantUnitReturnType")

package com.jet.tts

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.annotation.Keep
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
 *
 * @param context [Context] required for [TextToSpeech] initialization.
 * @param highlightMode Specifies how the text it [TextTts] will be highlighted, this works only for phones with api >= 26
 * @param stateHolder Helper variable to store state of [TtsClient].
 * @param onInitialized Callback to be called when [TextToSpeech] is initialized.
 * @param coroutineScope [CoroutineScope] used for waiting until TTS engine is initialized.
 * @author Miroslav Hýbler <br>
 * created on 04.02.2025
 * @since 1.0.0
 */
@Stable
@Keep
public class TtsClient internal constructor(
    context: Context,
    defaultHighlightMode: HighlightMode,
    internal val stateHolder: TtsClientStateHolder,
    private val onInitialized: (TtsClient) -> Unit = {},
    private val coroutineScope: CoroutineScope,
) {

    /**
     * Specifies how the text it [TextTts] will be highlighted.
     * @since 1.0.0
     */
    public enum class HighlightMode {

        /**
         * Mode to highlight only the spoken word out of the spoken content.
         */
        SPOKEN_WORD,

        /**
         * Mode to highlight the spoken range from the beginning of the content until currently
         * spoken word.
         */
        SPOKEN_RANGE_FROM_BEGINNING;
    }

    /**
     * Deferred used for waiting until TTS engine is initialized.
     * @since 1.0.0
     */
    private val initDeferred = CompletableDeferred<Boolean>()


    /**
     * Listener used for listening TextToSpeach initialization
     * @since 1.0.0
     */
    private val initListener: TextToSpeech.OnInitListener = object : TextToSpeech.OnInitListener {
        override fun onInit(status: Int) {
            when (status) {
                TextToSpeech.SUCCESS -> {
                    Log.d("TtsClient", "onTtsInitialized()")
                    tts.setOnUtteranceProgressListener(utteranceProgressListener)
                    isInitialized = true
                    initDeferred.complete(value = true)
                    onInitialized(this@TtsClient)
                }

                TextToSpeech.ERROR -> {
                    Log.e("TtsClient", "Error while initializing TTS engine")
                    initDeferred.complete(value = false)
                }

                else -> {
                    Log.e("TtsClient", "Unknown status while initializing TTS engine")
                    initDeferred.complete(value = false)
                }
            }
        }
    }


    /**
     * Listener used for listening to utterance progress to update [utteranceRange], [currentUtteranceId]
     * @since 1.0.0
     */
    private val utteranceProgressListener: UtteranceProgressListener =
        object : UtteranceProgressListener() {
            override fun onRangeStart(
                utteranceId: String,
                start: Int,
                end: Int,
                frame: Int,
            ) {
                val utterance = contentMap[utteranceId] ?: return
                currentStartIndex = start
                currentEndIndex = end
                //    Log.d("TtsClient", "onRangeStart: $utteranceId $start - $end")
                mUtteranceRange.value = UtteranceProgress(
                    range = getRange(
                        start = start,
                        end = end,
                        mode = highlightMode,
                        utterance = utterance,
                    ),
                    utteranceId = utteranceId,
                )

                stateHolder.captureState(client = this@TtsClient)
            }

            override fun onStart(utteranceId: String) {
                Log.d("TtsClient", "onStart: $utteranceId")
                currentUtteranceId = utteranceId
                mUtteranceRange.value = UtteranceProgress.EMPTY
                isSpeaking = true
            }

            override fun onDone(utteranceId: String) {
                Log.d("TtsClient", "onDone: $utteranceId")
                if (contentMap.contains(key = utteranceId)) {
                    contentMap.remove(key = utteranceId)
                }

                isSpeaking = false
            }

            override fun onError(utteranceId: String?) {
                Log.w("TtsClient", "onError: $utteranceId")
                isSpeaking = false
            }
        }


    /**
     * TextToSpeech instance used for speaking.
     * @since 1.0.0
     */
    private val tts = TextToSpeech(context, initListener)


    /**
     * Flag indicating if [TtsClient] is initialized. Do not use check directly, use [waitUntilInitialized] which
     * is using [initDeferred] for waiting until TTS engine is initialized and calling required
     * action afterwards.
     * @since 1.0.0
     */
    private var isInitialized: Boolean = false


    /**
     * Map of [Utterance]s added by [speak].
     * @since 1.0.0
     */
    internal var contentMap: HashMap<String, Utterance> = hashMapOf()
        private set


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
     */
    public var isSpeaking: Boolean by mutableStateOf(value = false)
        private set


    /**
     * Unique identifier of the current utterance.
     */
    public var currentUtteranceId: String by mutableStateOf(value = "")
        private set

    /**
     * 1f is default value for "average speech speed"
     * @see [TextToSpeech.setSpeechRate]
     */
    public var speechRate: Float = 1f
        private set


    /**
     * @since 1.0.0
     */
    public var highlightMode: HighlightMode by mutableStateOf(value = defaultHighlightMode)


    /**
     * Range of text to highlight. Use [utteranceRange] to collect updates.
     * @since 1.0.0
     */
    private val mUtteranceRange: MutableStateFlow<UtteranceProgress> =
        MutableStateFlow(value = UtteranceProgress.EMPTY)

    /**
     * Range of text to highlight, collected by [TextTts]
     * @since 1.0.0
     */
    val utteranceRange: StateFlow<UtteranceProgress> get() = mUtteranceRange.asStateFlow()


    internal var currentStartIndex: Int = 0
        private set


    internal var currentEndIndex: Int = 0
        private set


    init {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Log.i("TtsClient", "Highlight feature is not available on Android 7 and lower.")
        }

        restoreState(stateHolder = stateHolder)
    }


    /**
     * Sets language of [TextToSpeech] by given [Locale] if it's supported.
     * @param language [Locale] Language to set.
     * @since 1.0.0
     */
    public fun setLanguage(language: Locale): Unit {
        waitUntilInitialized {
            val availability = tts.isLanguageAvailable(language)
            if (availability == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TtsClient", "Language not supported: $language")
                return@waitUntilInitialized
            }
            tts.language = language
        }
    }


    /**
     * @param text Text to be spoken.
     * @param utteranceId Unique identifier of the utterance. This is required since without [utteranceId],
     * [UtteranceProgressListener] would not be called and text highlight feature would not work.
     * @param queueMode Queue mode of [TextToSpeech], can be [TextToSpeech.QUEUE_FLUSH] to replace
     * current utterance (If there is some) with a new one or [TextToSpeech.QUEUE_ADD] to add a new
     * utterance to the queue.
     * @param params Parameters for the request. Can be null. See [TextToSpeech.speak] for more
     * details.
     * @param startIndex Index of the first character of the current utterance.
     * @since 1.0.0
     */
    public fun speak(
        text: String,
        utteranceId: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        params: Bundle? = null,
        startIndex: Int = 0,
    ): Unit {
        if (text.isBlank()) {
            //Blank text will not be passed because there is nothing to speak.
            return
        }

        waitUntilInitialized {
            if (queueMode == TextToSpeech.QUEUE_FLUSH) {
                //Queue flush requested, we have to clear all previous utterances.
                contentMap.clear()
            }

            contentMap[utteranceId] = Utterance(
                utteranceId = utteranceId,
                content = text,
                currentIndexThreshold = startIndex,
                sequence = contentMap.size,
            )
            val actualTextToBeSpoken: String = text.toSubstring(startIndex = startIndex)

            tts.speak(actualTextToBeSpoken, queueMode, params, utteranceId)
            isSpeaking = true
        }
    }


    /**
     * Stops currently speaking text. Next [speak] request with same utteranceId will start from beginning.
     * @since 1.0.0
     */
    public fun stop(): Unit {
        Log.i("TtsClient", "stop()")
        waitUntilInitialized {
            tts.stop()
            isSpeaking = false
            stateHolder.captureState(client = this@TtsClient)
        }
    }


    /**
     * Releases resources. Call this when you know your application will no longer use [TextToSpeech].
     * @since 1.0.0
     */
    public fun release(): Unit {
        Log.i("TtsClient", "release()")
        tts.stop()
        tts.shutdown()
        contentMap.clear()
    }


    public fun setSpeechRate(rate: Float) {
        if (tts.setSpeechRate(rate) == TextToSpeech.SUCCESS) {
            this.speechRate = rate
        }
    }


    /**
     * Stops currently speaking text in [androidx.compose.runtime.DisposableEffect] and releases resources.
     * This is used instead of [stop] to capture [isSpeaking] state properly.
     */
    internal fun stopOnDispose(): Unit {
        Log.i("TtsClient", "stopOnDispose()")
        stateHolder.captureState(client = this)
        release()
    }


    /**
     * Util function to speak an utterance starting from a given [startIndex]. Uses combination of
     * [startIndex] and [Utterance.currentIndexThreshold] to keep text highlighting consistent.
     *
     * #### Why is [Utterance.currentIndexThreshold] needed
     * * When navigating to [startIndex], [Utterance.currentIndexThreshold] is set to [startIndex] and
     * [TextToSpeech]'s queue is flushed with `substring(startIndex)` of current [Utterance.content].
     * * Substring is passed so [utteranceProgressListener] will return `start` and `end` values
     * relative to the new substring (instead of original [Utterance.content]).
     * * This would break text highlighting so [getRange] used stored [Utterance.currentIndexThreshold]
     * to set highlight text range correctly relevant to [Utterance.content] (because on the UI
     * full content of utterance is visible).
     * @param utteranceId Id of utterance to be spoken, must be in [contentMap].
     * @param startIndex Index of the first character of the current utterance.
     * @since 1.0.0
     */
    internal fun navigateInUtterance(
        utteranceId: String,
        startIndex: Int,
    ): Unit {
        val utterance = contentMap[utteranceId] ?: return
        val text = utterance.content
        utterance.currentIndexThreshold = startIndex
        val textToBeSpoken = text.toSubstring(startIndex = startIndex)

        waitUntilInitialized {
            tts.speak(
                textToBeSpoken,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
            )
            isSpeaking = true

            //Adding all NEXT utterances that comes after this one to the queue
            contentMap.values
                .filter { it.utteranceId != utteranceId }
                .filter { it.sequence > utterance.sequence }
                .sortedBy(Utterance::sequence)
                .forEach { nextUtterance ->
                    tts.speak(
                        nextUtterance.content,
                        TextToSpeech.QUEUE_ADD,
                        null,
                        nextUtterance.utteranceId,
                    )
                }
        }

    }


    /**
     * Restores state of [TtsClient] from saved [TtsClientStateHolder].
     * @since 1.0.0
     */
    internal fun restoreState(stateHolder: TtsClientStateHolder): Unit {
        Log.d("TtsClient", "restoreState: $stateHolder")

        if (stateHolder.isEmpty) {
            return
        }

        val savedUtteranceId = stateHolder.utteranceId

        currentStartIndex = stateHolder.startIndex
        currentEndIndex = stateHolder.endIndex
        stateHolder.map.forEach { (k, v) -> contentMap[k] = v }

        val utterance = contentMap[savedUtteranceId] ?: return

        mUtteranceRange.value = UtteranceProgress(
            range = getRange(
                mode = highlightMode,
                start = currentStartIndex,
                end = currentEndIndex,
                utterance = utterance,
            ),
            utteranceId = savedUtteranceId,
        )

        if (stateHolder.isSpeaking) {
            waitUntilInitialized {
                navigateInUtterance(
                    utteranceId = savedUtteranceId,
                    startIndex = currentStartIndex,
                )
                if (contentMap.size > 1) {
                    contentMap.toList()
                        .sortedBy { pair -> pair.second.sequence }
                        .forEachIndexed { index, pair ->
                            if (index == 0) {
                                return@forEachIndexed
                            }
                            speak(
                                text = pair.second.content,
                                utteranceId = pair.first,
                                queueMode = TextToSpeech.QUEUE_ADD,
                            )
                        }
                }
            }
        }
    }


    /**
     * @param start Start index of currently spoken text (word) within [Utterance.content].
     * @param end End index of currently spoken text (word) within [Utterance.content].
     * @param mode Highlight mode
     * @param utterance Currently active utterance to get range from.
     * @return Range of text to highlight
     */
    private fun getRange(
        start: Int,
        end: Int,
        mode: HighlightMode,
        utterance: Utterance,
    ): IntRange {
        val threshold = utterance.currentIndexThreshold
        return when (mode) {
            HighlightMode.SPOKEN_WORD -> {
                IntRange(start = start + threshold, endInclusive = end + threshold)
            }

            HighlightMode.SPOKEN_RANGE_FROM_BEGINNING -> {
                //We want to highlight text from beggining, so start is 0
                IntRange(start = 0, endInclusive = end + threshold)
            }
        }
    }


    /**
     * Waits until TTS engine is initialized before calling a [block]
     * @param block Function holding some operation that can run only after [tts] is properly
     * initialized.
     * @since 1.0.0
     */
    private fun waitUntilInitialized(
        block: suspend CoroutineScope.() -> Unit,
    ): Unit {
        coroutineScope.launch {
            val initResult = initDeferred.await()
            if (initResult) {
                block()
            } else {
                Log.e(
                    "TtsClient",
                    "Unable to perform operation, TTS engine was not initialized successfully"
                )
            }
        }
    }


    /**
     * Just simple util function.
     * @param startIndex Index of the first character to include in the returned substring.
     * @return Substring of [this] string starting from [startIndex], or [this] when [startIndex] == 0.
     * @since 1.0.0
     */
    private fun String.toSubstring(
        startIndex: Int,
    ): String {
        val finalContent: String = if (startIndex > 0) {
            this.substring(startIndex = startIndex)
        } else this
        return finalContent
    }
}