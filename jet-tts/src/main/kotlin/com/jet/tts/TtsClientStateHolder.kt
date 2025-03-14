@file:Suppress(
    "DATA_CLASS_COPY_VISIBILITY_WILL_BE_CHANGED_WARNING",
    "UNCHECKED_CAST",
)
@file:SuppressWarnings(
    "UNCHECKED_CAST"
)

package com.jet.tts

import android.os.Build
import android.os.Bundle
import androidx.annotation.Keep
import androidx.compose.runtime.saveable.SaverScope
import androidx.core.os.bundleOf


/**
 * Helper class used for saving/restoring state of [TtsClient] when composable is disposed. [com.jet.tts.TtsClientStateHolder]
 * should always stay out of composition scope, that's why all parameters are mutable variables.
 * @param utteranceId Unique identifier of the current utterance.
 * @param startIndex Inclusive start index of the first character of the current utterance.
 * @param endIndex Exclusive end index of the last character of the current utterance.
 * @param isSpeaking Flag indicating if [TtsClient] was speaking when state was saved.
 * @param map Map of [Utterance]s added to the [TtsClient].
 * @author Miroslav Hýbler <br>
 * created on 05.02.2025
 * @since 1.0.0
 */
@Keep
internal data class TtsClientStateHolder internal constructor(
    var utteranceId: String = "",
    var startIndex: Int = 0,
    var endIndex: Int = 0,
    var isSpeaking: Boolean = false,
    var map: Map<String, Utterance> = emptyMap(),
) {

    /**
     * Flag indicating if state is empty.
     * @since 1.0.0
     */
    internal val isEmpty: Boolean
        get() = utteranceId == ""
                && startIndex == 0
                && endIndex == 0


    /**
     * Captures current state of [TtsClient] and saves to be saved later by [Saver]. **[TtsClient]
     * has to call [captureState] in all scenarios when some of the parameters [com.jet.tts.TtsClientStateHolder]
     * has changed.**
     * @since 1.0.0
     */
    internal fun captureState(client: TtsClientImpl) {

        if (client.isInDisposeState) {
            return
        }

        this.utteranceId = client.currentUtteranceId
        this.startIndex = client.currentStartIndex
        this.endIndex = client.currentEndIndex
        this.isSpeaking = client.isSpeaking
        this.map = client.contentMap
    }


    /**
     * Saver used for saving/restoring state of [TtsClient] using [com.jet.tts.TtsClientStateHolder].
     * @since 1.0.0
     */
    object Saver : androidx.compose.runtime.saveable.Saver<TtsClientStateHolder, Bundle> {

        //Keys for saving/restoring state
        private const val UTTERANCE_ID: String = "utteranceId"
        private const val START_INDEX: String = "startIndex"
        private const val END_INDEX: String = "endIndex"
        private const val IS_SPEAKING: String = "isSpeaking"
        private const val MAP: String = "map"

        override fun SaverScope.save(state: TtsClientStateHolder): Bundle {
            return bundleOf(
                UTTERANCE_ID to state.utteranceId,
                START_INDEX to state.startIndex,
                END_INDEX to state.endIndex,
                IS_SPEAKING to state.isSpeaking,
                MAP to state.map,
            )
        }


        override fun restore(bundle: Bundle): TtsClientStateHolder {
            val holder = TtsClientStateHolder(
                utteranceId = bundle.getString(UTTERANCE_ID, ""),
                startIndex = bundle.getInt(START_INDEX, 0),
                endIndex = bundle.getInt(END_INDEX, 0),
                isSpeaking = bundle.getBoolean(IS_SPEAKING, false),
                map = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val type = HashMap::class.java
                    (bundle.getSerializable(
                        MAP,
                        type
                    ) as? HashMap<String, Utterance>) ?: emptyMap()
                } else {
                    @Suppress("DEPRECATION")
                    (bundle.getSerializable(
                        MAP,
                    ) as? HashMap<String, Utterance>) ?: emptyMap()
                },
            )
            return holder
        }
    }
}
