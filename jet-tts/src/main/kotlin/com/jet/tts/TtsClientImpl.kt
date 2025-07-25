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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
 * By providing a [androidx.compose.foundation.ScrollState], [com.jet.tts.old.TextTts] can use it to autoscroll to
 * currently spoken line. Solution for [androidx.compose.foundation.lazy.LazyColumn] is not avaliable now.
 * Use [rememberTtsClient] to get an instance of [com.jet.tts.TtsClient].
 *
 * ## Navigation Feature
 * It is possible to "navigate" in utterance when `ttsClient.isSpeaking == true`, by clicking into
 * [com.jet.tts.old.TextTts] client will navigate speech by clicked word.
 *
 * @param context [Context] required for [TextToSpeech] initialization.
 * @param highlightMode Specifies how the text it [com.jet.tts.old.TextTts] will be highlighted, this works only for phones with api >= 26
 * @param onInitialized Callback to be called when [TextToSpeech] is initialized.
 * @param coroutineScope [CoroutineScope] used for waiting until TTS engine is initialized.
 * @param isUsingResume True when you want [TtsClient] to support a "resume" function allowing to
 * resume speech instead of being spoken from beginning.
 * @author Miroslav Hýbler <br>
 * created on 04.02.2025
 * @since 1.0.0
 */
@Stable
@Keep
internal class TtsClientImpl internal constructor(
    context: Context,
    initialHighlightMode: HighlightMode,
    private val onInitialized: (TtsClient) -> Unit = {},
    private val coroutineScope: CoroutineScope,
    private val isUsingResume: Boolean,
) : TtsClient() {


    /**
     * Deferred used for waiting until TTS engine is initialized. Initialization is required for
     * any operation with [tts]. Use [waitUntilInitialized] for any function that requires TTS engine.
     * @since 1.0.0
     */
    private val initDeferred = CompletableDeferred<Boolean>()


    /**
     * Listener used for listening TextToSpeech initialization
     * @since 1.0.0
     */
    private val initListener: TextToSpeech.OnInitListener = TextToSpeech.OnInitListener { status ->
        when (status) {
            TextToSpeech.SUCCESS -> {
                Log.i("TtsClient", "onTtsInitialized()")
                tts.setOnUtteranceProgressListener(utteranceProgressListener)
                initDeferred.complete(value = true)
                isInitialized = true
                onInitialized(this@TtsClientImpl)
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


    /**
     * Listener used for listening to utterance progress to update [utteranceRange], [currentUtteranceId].
     * **[UtteranceProgressListener.onRangeStart] works only on api >= 26**, so any feature that
     * depends on this function is also available only on **api >= 26**.
     * @since 1.0.0
     */
    private val utteranceProgressListener: UtteranceProgressListener =
        object : UtteranceProgressListener() {

            /**
             * Called when spoken range of utterance changes.
             */
            override fun onRangeStart(
                utteranceId: String,
                start: Int,
                end: Int,
                frame: Int,
            ) {
                val utterance = contentMap[utteranceId] ?: return
                currentStartIndex = start
                currentEndIndex = end
                mUtteranceRange.value = UtteranceProgress(
                    range = getRange(
                        start = start,
                        end = end,
                        mode = highlightMode,
                        utterance = utterance,
                    ),
                    utteranceId = utteranceId,
                    sequence = utterance.sequence,
                )

                state?.captureState(client = this@TtsClientImpl)
            }

            /**
             * Called when new utterance with id [utteranceId] starts.
             */
            override fun onStart(utteranceId: String) {
                Log.d("TtsClient", "onStart: $utteranceId")
                currentUtteranceId = utteranceId
                mUtteranceRange.value = UtteranceProgress(
                    utteranceId = utteranceId,
                    range = IntRange.EMPTY,
                    sequence = getSequenceForUtterance(utteranceId = utteranceId),
                )
                isSpeaking = true
            }


            /**
             * Called when utterance with id [utteranceId] ends.
             */
            override fun onDone(utteranceId: String) {
                Log.d("TtsClient", "onDone: $utteranceId")
                if (!isUsingResume && contentMap.contains(key = utteranceId)) {
                    contentMap.remove(key = utteranceId)
                }

                isSpeaking = false
            }


            /**
             * Called when utterance with id [utteranceId] fails.
             */
            override fun onError(utteranceId: String?) {
                Log.w("TtsClient", "onError: $utteranceId")
                isSpeaking = false
            }
        }


    /**
     * [TextToSpeech] engine instance used for speaking.
     * @since 1.0.0
     */
    private val tts = TextToSpeech(context, initListener)


    /**
     * Current [TtsState] for the client. Use [TtsLifecycleAwareEffect] to call [initWithState] otherwise
     * [TtsClient] won't speak and crashes will occur.
     */
    private var state: TtsState? = null


    /**
     * True when [TtsClient] was stopped by [androidx.compose.runtime.DisposableEffect]. When true,
     * client should avoid property changes so state from [state] can be saved by [TtsState.Saver].
     * @since 1.0.0
     */
    internal var isInDisposeState: Boolean = false


    /**
     * Map of all [Utterance]s for this client added by [speak], [add] or [flushAndSpeak].
     * @since 1.0.0
     */
    internal var contentMap: SnapshotStateMap<String, Utterance> = mutableStateMapOf()
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
     * @since 1.0.0
     */
    public override var isSpeaking: Boolean by mutableStateOf(value = false)


    /**
     * Unique identifier of the current utterance.
     * @since 1.0.0
     */
    public var currentUtteranceId: String by mutableStateOf(value = "")
        private set


    /**
     * 1f is default value for "average speech speed", bigger value means faster speech.
     * @see [TextToSpeech.setSpeechRate]
     * @since 1.0.0
     */
    public var speechRate: Float = 1f
        private set


    /**
     * Flag indicating if [TtsClient] is initialized and [tts] is ready to be used.
     * For internal work Do not use check directly, use [waitUntilInitialized] which is using
     * [initDeferred] for waiting until TTS engine is initialized and calling required
     * action afterwards.
     * @since 1.0.0
     */
    public override var isInitialized: Boolean by mutableStateOf(value = false)


    /**
     * Current highlight mode, see [HighlightMode] for more details.
     * @since 1.0.0
     */
    public override var highlightMode: HighlightMode by mutableStateOf(value = initialHighlightMode)


    /**
     * Range of text to highlight. Use [utteranceRange] to collect updates.
     * @since 1.0.0
     */
    private val mUtteranceRange: MutableStateFlow<UtteranceProgress> =
        MutableStateFlow(value = UtteranceProgress.EMPTY)

    /**
     * Range of text to highlight, collected by [com.jet.tts.old.TextTts]
     * @since 1.0.0
     */
    override val utteranceRange: StateFlow<UtteranceProgress> get() = mUtteranceRange.asStateFlow()


    /**
     * Holding current start index of the current utterance. Saving it helps with [getRange] call
     * when restoring state.
     * @since 1.0.0
     */
    internal var currentStartIndex: Int = 0
        private set


    /**
     * Holding current end index of the current utterance. Saving it helps with [getRange] call
     * when restoring state.
     * @since 1.0.0
     */
    internal var currentEndIndex: Int = 0
        private set


    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Log.i("TtsClient", "Highlight feature is not available on Android 7 and lower.")
        }
    }


    /**
     * Sets language of [TextToSpeech] by given [Locale] if it's supported.
     * @param language [Locale] Language to set.
     * @since 1.0.0
     */
    public override fun setLanguage(language: Locale): Unit {
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
     * If you are using resume feature by setting [isUsingResume] to true, use [flushAndSpeak] and [add] instead.
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
     */
    @Deprecated(
        message = "Will be Internal in the future, use flushAndSpeak() or add() instead.",
        replaceWith = ReplaceWith(expression = "flushAndSpeak(text, utteranceId, params, startIndex)"),
    )
    public override fun speak(
        text: String,
        utteranceId: String,
        queueMode: QueueMode,
        params: Bundle?,
        startIndex: Int,
    ): Unit {
        if (text.isBlank()) {
            //Blank text will not be passed because there is nothing to speak.
            return
        }

        waitUntilInitialized {
            if (queueMode == QueueMode.FLUSH) {
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

            tts.speak(
                actualTextToBeSpoken,
                when (queueMode) {
                    QueueMode.FLUSH -> TextToSpeech.QUEUE_FLUSH
                    QueueMode.ADD -> TextToSpeech.QUEUE_ADD
                },
                params,
                utteranceId,
            )
            isSpeaking = true
        }
    }


    /**
     * Flushes previous queue (if there was) and creates new one. Calls a [speak] with [QueueMode].
     * @param text Text to be spoken.
     * @param utteranceId Unique identifier of the utterance. This is required since without [utteranceId],
     * [UtteranceProgressListener] would not be called and text highlight feature would not work.
     * @param params Parameters for the request. Can be null. See [TextToSpeech.speak] for more
     * details.
     * @param startIndex Index of the first character of the current utterance.
     * @since 1.0.0
     */
    public override fun flushAndSpeak(
        text: String,
        utteranceId: String,
        params: Bundle?,
        startIndex: Int,
    ) {
        val mState = state
        if (isUsingResume && mState != null && mState.isNotEmpty) {
            val utterance = contentMap[mState.utteranceId]
            if (utterance != null) {
                navigateInUtterance(
                    utteranceId = utterance.utteranceId,
                    startIndex = utterance.currentIndexThreshold,
                )
                return
            }
        }

        speak(
            text = text,
            utteranceId = utteranceId,
            queueMode = QueueMode.FLUSH,
            params = params,
            startIndex = startIndex,
        )
    }


    /**
     * Flushes previous queue (if there was) and creates new one. Calls a [speak] with [TextToSpeech.QUEUE_FLUSH].
     * @param utterance - Utterance to be spoken, hodling [Utterance.content] and [Utterance.utteranceId].
     * @param params Parameters for the request. Can be null. See [TextToSpeech.speak] for more
     * details.
     * @param startIndex Index of the first character of the current utterance.
     * @since 1.0.0
     */
    override fun flushAndSpeak(
        utterance: Utterance,
        params: Bundle?,
        startIndex: Int,
    ) {
        flushAndSpeak(
            text = utterance.content,
            utteranceId = utterance.utteranceId,
            params = params,
            startIndex = startIndex,
        )
    }


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
    public override fun add(
        text: String,
        utteranceId: String,
        params: Bundle?,
        startIndex: Int,
    ) {
        val mState = state
        if (!isSpeaking && isUsingResume && mState != null && mState.isNotEmpty) {
            val utterance = contentMap[mState.utteranceId]
            if (utterance != null) {
                navigateInUtterance(
                    utteranceId = utterance.utteranceId,
                    startIndex = utterance.currentIndexThreshold,
                )
                return
            } else {
                speak(
                    text = text,
                    utteranceId = utteranceId,
                    queueMode = QueueMode.ADD,
                    params = params,
                    startIndex = startIndex,
                )
            }

        } else if (!contentMap.contains(key = utteranceId)) {
            speak(
                text = text,
                utteranceId = utteranceId,
                queueMode = QueueMode.ADD,
                params = params,
                startIndex = startIndex,
            )
        }
    }


    /**
     * Adds utterance to the queue, calls a [speak] with [TextToSpeech.QUEUE_ADD].
     * @param utterance - Utterance to be spoken, hodling [Utterance.content] and [Utterance.utteranceId].
     * @param params Parameters for the request. Can be null. See [TextToSpeech.speak] for more
     * details.
     * @param startIndex Index of the first character of the current utterance.
     * @since 1.0.0
     */
    override fun add(
        utterance: Utterance,
        params: Bundle?,
        startIndex: Int,
    ) {
        add(
            text = utterance.content,
            utteranceId = utterance.utteranceId,
            params = params,
            startIndex = startIndex,
        )
    }


    /**
     * Stops currently speaking text. Next [speak] request with same utteranceId will start from beginning.
     * @since 1.0.0
     */
    public override fun stop(): Unit {
        Log.i("TtsClient", "stop()")
        waitUntilInitialized {
            tts.stop()
            isSpeaking = false
            val mState = state

            if (mState != null) {
                mState.captureState(client = this@TtsClientImpl)
                if (mState.isNotEmpty && isUsingResume) {
                    //Saving index threshold to resume playing from the same position where it ended
                    contentMap[mState.utteranceId]?.currentIndexThreshold = mState.startIndex
                }
            }
        }
    }


    /**
     * Releases resources. Call this when you know your application will no longer use [TextToSpeech].
     * @since 1.0.0
     */
    public fun release(): Unit {
        Log.i("TtsClient", "release()")
        this.tts.stop()
        this.tts.shutdown()
        this.contentMap.clear()
    }


    /**
     * Sets speech rate of [TextToSpeech] engine. `1f` is default value for "average speech speed",
     * bigger value means faster speech.
     * @param rate Speech rate. 1f is default value for "average speech speed", bigger value means faster speech.
     * @since 1.0.0
     */
    public fun setSpeechRate(rate: Float) {
        waitUntilInitialized {
            if (tts.setSpeechRate(rate) == TextToSpeech.SUCCESS) {
                this@TtsClientImpl.speechRate = rate
            }
        }
    }


    /**
     * Stops currently speaking text in [androidx.compose.runtime.DisposableEffect].
     * This is used instead of [stop] to capture [isSpeaking] state properly.
     * @since 1.0.0
     */
    internal override fun stopOnDispose(): Unit {
        Log.i("TtsClient", "stopOnDispose()")
        this.state?.captureState(client = this)
        this.isInDisposeState = true
        this.tts.stop()
        this.isSpeaking = false
        this.contentMap.clear()
        this.state = null
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
    internal override fun navigateInUtterance(
        utteranceId: String,
        startIndex: Int,
    ): Unit {
        Log.i("TtsClient", "navigateInUtterance(utteranceId=$utteranceId, startIndex=$startIndex)")
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
                .filter(predicate = { it.utteranceId != utteranceId })
                .sortedBy(selector = Utterance::sequence)
                .filter(predicate = { it.sequence > utterance.sequence })
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
     * Util function to get sequence (index) of an utterance.
     * @param utteranceId Id of utterance to get sequence for.
     * @return Sequence (index) of the utterance with given [utteranceId] or [Int.MAX_VALUE] when
     * utterance is not found in [contentMap]. This is used for [HighlightMode.SPOKEN_RANGE_FROM_BEGINNING_INCLUDING_PREVIOUS_UTTERANCES]
     * when [com.jet.tts.old.TextTts] has to highlight all "previous" utterances too, so returning [Int.MAX_VALUE] by
     * default won't cause unwanted highlights.
     * @since 1.0.0
     */
    internal override fun getSequenceForUtterance(
        utteranceId: String,
    ): Int {
        if (!contentMap.containsKey(key = utteranceId)) {
            return Int.MAX_VALUE
        }

        return contentMap[utteranceId]?.sequence ?: Int.MAX_VALUE
    }


    /**
     * Restores state of [TtsClient] from saved [TtsState] or initializes client with new one.
     * @since 1.0.0
     */
    internal override fun initWithState(stateHolder: TtsState): Unit {
        Log.d("TtsClient", "restoreState: $stateHolder")
        this.state = stateHolder
        this.isInDisposeState = false
        if (stateHolder.isEmpty) {
            //Nothing to restore
            return
        }

        //Last active utterance when captureState was called, normally onDispose
        val savedUtteranceId = stateHolder.utteranceId

        currentStartIndex = stateHolder.startIndex
        currentEndIndex = stateHolder.endIndex
        stateHolder.map.forEach { (k, v) -> contentMap[k] = v }
        //Finding last active utterance by it's id
        val utterance = contentMap[savedUtteranceId] ?: return


        mUtteranceRange.value = UtteranceProgress(
            range = getRange(
                mode = highlightMode,
                start = currentStartIndex,
                end = currentEndIndex,
                utterance = utterance,
            ),
            utteranceId = savedUtteranceId,
            sequence = utterance.sequence,
        )

        if (stateHolder.isSpeaking) {
            //When client was speaking before, we have to navigate it properly to continue where it was
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
                            //Adding all next utterances after active one to the queue
                            add(text = pair.second.content, utteranceId = pair.first)
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
     * @since 1.0.0
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

            HighlightMode.SPOKEN_RANGE_FROM_BEGINNING,
            HighlightMode.SPOKEN_RANGE_FROM_BEGINNING_INCLUDING_PREVIOUS_UTTERANCES -> {
                //We want to highlight text from beginning, so start is 0
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