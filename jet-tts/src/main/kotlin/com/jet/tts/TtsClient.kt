@file:Suppress("RedundantVisibilityModifier", "RedundantUnitReturnType")

package com.jet.tts

import android.content.Context
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
 * @author Miroslav Hýbler <br>
 * created on 04.02.2025
 */
@Stable
@Keep
public class TtsClient internal constructor(
    context: Context,
    val highlightMode: HighlightMode = HighlightMode.SPOKEN_WORD,
    internal val stateHolder: TtsClientStateHolder,
    private val onInitialized: (TtsClient) -> Unit = {},
    private val coroutineScope: CoroutineScope,
) {

    public enum class HighlightMode {
        SPOKEN_WORD,
        SPOKEN_RANGE_FROM_BEGINNING;
    }

    private val initDeferred = CompletableDeferred<Boolean>()


    private val initListener: TextToSpeech.OnInitListener = object : TextToSpeech.OnInitListener {
        override fun onInit(status: Int) {
            when (status) {
                TextToSpeech.SUCCESS -> {
                    onTtsInitialized()
                    onInitialized(this@TtsClient)
                    initDeferred.complete(value = true)
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


    private val tts = TextToSpeech(context, initListener)


    private var isInitialized: Boolean = false

    internal var contentMap: HashMap<String, Utterance> = hashMapOf()
        private set

    public var isSpeaking: Boolean by mutableStateOf(value = false)
        private set

    public var currentUtteranceId: String by mutableStateOf(value = "")
        private set

    private val mUtteranceRange: MutableStateFlow<UtteranceProgress> =
        MutableStateFlow(value = UtteranceProgress.EMPTY)
    val utteranceRange: StateFlow<UtteranceProgress> get() = mUtteranceRange.asStateFlow()


    internal var currentStartIndex: Int = 0
        private set
    internal var currentEndIndex: Int = 0
        private set


    init {
        restoreState(stateHolder = stateHolder)
    }


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


    public fun speak(
        text: String,
        utteranceId: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        params: Bundle? = null,
        startIndex: Int = 0,
    ): Unit {
        if (text.isBlank()) {
            return
        }

        waitUntilInitialized {
            contentMap[utteranceId] = Utterance(
                utteranceId = utteranceId,
                content = text,
                currentThreshold = startIndex,
                sequence = contentMap.size,
            )
            val actualTextToBeSpoken: String = text.toSubstring(startIndex = startIndex)

            tts.speak(actualTextToBeSpoken, queueMode, params, utteranceId)
            isSpeaking = true
        }
    }


    public fun stop(): Unit {
        Log.d("TtsClient", "stop()")
        waitUntilInitialized {
            tts.stop()
            isSpeaking = false
            stateHolder.captureState(client = this@TtsClient)
        }
    }


    public fun release(): Unit {
        Log.d("TtsClient", "release()")
        tts.stop()
        tts.shutdown()
        contentMap.clear()
    }


    internal fun stopOnDispose(): Unit {
        Log.d("TtsClient", "stopOnDispose()")
        stateHolder.captureState(client = this)
        tts.stop()
        tts.shutdown()
    }


    internal fun navigateInUtterance(
        utteranceId: String,
        startIndex: Int,
    ): Unit {
        val utterance = contentMap[utteranceId] ?: return
        val text = utterance.content
        utterance.currentThreshold = startIndex

        waitUntilInitialized {
            val ssmlFormat: String = text.toSubstring(startIndex = startIndex)

            tts.speak(ssmlFormat, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            isSpeaking = true
        }

    }


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
        Log.d(
            "TtsClient",
            "restoreState: $savedUtteranceId $currentStartIndex - $currentEndIndex"
        )

        if (stateHolder.isSpeaking) {
            waitUntilInitialized {
                val textToSpeak = utterance.content.toSubstring(startIndex = currentStartIndex)
                Log.d(
                    "TtsClient",
                    "Text to speak: $textToSpeak"
                )
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


    private fun onTtsInitialized(): Unit {
        Log.d("TtsClient", "onTtsInitialized, adding utterance listener")
        tts.setOnUtteranceProgressListener(utteranceProgressListener)
        isInitialized = true
    }


    private fun getRange(
        start: Int,
        end: Int,
        mode: HighlightMode,
        utterance: Utterance,
    ): IntRange {
        val threshold = utterance.currentThreshold
        return when (mode) {
            HighlightMode.SPOKEN_WORD -> {
                IntRange(start = start + threshold, endInclusive = end + threshold)
            }

            HighlightMode.SPOKEN_RANGE_FROM_BEGINNING -> {
                IntRange(start = 0, endInclusive = end + threshold)
            }
        }
    }


    private fun waitUntilInitialized(
        block: suspend CoroutineScope.() -> Unit,
    ): Unit {
        coroutineScope.launch {
            initDeferred.await()
            block()
        }
    }


    private fun String.toSubstring(
        startIndex: Int,
    ): String {
        val finalContent: String = if (startIndex > 0) {
            this.substring(startIndex = startIndex)
        } else this
        return finalContent
    }
}