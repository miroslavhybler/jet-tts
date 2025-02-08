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
 * Helper class used for saving/restoring state of [TtsClient] when composable is disposed.
 * @param utteranceId Unique identifier of the current utterance.
 * @param startIndex Inclusive start index of the first character of the current utterance.
 * @param endIndex Exclusive end index of the last character of the current utterance.
 * @param isSpeaking Flag indicating if [TtsClient] was speaking when state was saved.
 * @param map Map of [Utterance]s addee to the [TtsClient].
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
     * Captures current state of [TtsClient] and saves to be saved lated by [Saver].
     * @since 1.0.0
     */
    internal fun captureState(client: TtsClient) {
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
        override fun SaverScope.save(state: TtsClientStateHolder): Bundle {
            return bundleOf(
                "utteranceId" to state.utteranceId,
                "startIndex" to state.startIndex,
                "endIndex" to state.endIndex,
                "isSpeaking" to state.isSpeaking,
                "map" to state.map,
            )
        }

        override fun restore(bundle: Bundle): TtsClientStateHolder {
            val holder = TtsClientStateHolder(
                utteranceId = bundle.getString("utteranceId", ""),
                startIndex = bundle.getInt("startIndex", 0),
                endIndex = bundle.getInt("endIndex", 0),
                isSpeaking = bundle.getBoolean("isSpeaking", false),
                map = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val type = HashMap::class.java
                    (bundle.getSerializable(
                        "map",
                        type
                    ) as? HashMap<String, Utterance>) ?: emptyMap()
                } else {
                    @Suppress("DEPRECATION")
                    (bundle.getSerializable(
                        "map",
                    ) as? HashMap<String, Utterance>) ?: emptyMap()
                },
            )
            return holder
        }
    }
}
