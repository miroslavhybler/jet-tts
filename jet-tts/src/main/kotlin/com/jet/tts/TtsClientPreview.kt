package com.jet.tts

import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale


/**
 * [android.speech.tts.TextToSpeech] is not available for the compose [androidx.compose.ui.tooling.preview.Preview], so
 * empty (not working) client without any reference to [android.speech.tts] package is used when
 * [androidx.compose.ui.platform.LocalInspectionMode] is true.
 * @author Miroslav Hýbler <br>
 * created on 13.03.2025
 * @since 1.0.0
 */
public class TtsClientPreview internal constructor() : TtsClient() {


    override var isSpeaking: Boolean = false


    override var highlightMode: HighlightMode = HighlightMode.SPOKEN_RANGE_FROM_BEGINNING


    override val utteranceRange: StateFlow<UtteranceProgress> =
        MutableStateFlow(value = UtteranceProgress.EMPTY)


    override fun setLanguage(language: Locale) {
        notImplementedMessage(name = "setLanguage")
    }


    override fun add(
        text: String,
        utteranceId: String,
        params: Bundle?,
        startIndex: Int,
    ) {
        notImplementedMessage(name = "add")
    }


    override fun flushAndSpeak(
        text: String,
        utteranceId: String,
        params: Bundle?,
        startIndex: Int,
    ) {
        notImplementedMessage(name = "flushAndSpeak")
    }


    override fun stop() {
        notImplementedMessage(name = "stop")
    }


    @Deprecated(
        message = "Will be Internal in the future, use flushAndSpeak() or add() instead.",
        replaceWith = ReplaceWith("flushAndSpeak(text, utteranceId, params, startIndex)")
    )
    override fun speak(
        text: String,
        utteranceId: String,
        queueMode: QueueMode,
        params: Bundle?,
        startIndex: Int
    ) {
        notImplementedMessage(name = "speak")
    }


    override fun navigateInUtterance(
        utteranceId: String,
        startIndex: Int,
    ) {
        notImplementedMessage(name = "navigateInUtterance")
    }

    override fun getSequenceForUtterance(utteranceId: String): Int {
        notImplementedMessage(name = "getSequenceForUtterance")
        return Int.MIN_VALUE
    }


    override fun stopOnDispose() {
        notImplementedMessage(name = "stopOnDispose")
    }


    /**
     *
     */
    private fun TtsClientPreview.notImplementedMessage(
        name: String
    ): Unit {
        Log.w("TtsClientPreview", "Function $name is not implemented in preview mode.")
    }
}