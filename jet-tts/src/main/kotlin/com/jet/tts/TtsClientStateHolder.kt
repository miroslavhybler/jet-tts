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
 * @author Miroslav Hýbler <br>
 * created on 05.02.2025
 */
@Keep
internal data class TtsClientStateHolder internal constructor(
    var utteranceId: String = "",
    var startIndex: Int = 0,
    var endIndex: Int = 0,
    var isSpeaking: Boolean = false,
    var map: Map<String, Utterance> = emptyMap(),
) {

    internal val isEmpty: Boolean
        get() = utteranceId == ""
                && startIndex == 0
                && endIndex == 0


    internal fun captureState(client: TtsClient) {
        this.utteranceId = client.currentUtteranceId
        this.startIndex = client.currentStartIndex
        this.endIndex = client.currentEndIndex
        this.isSpeaking = client.isSpeaking
        this.map = client.contentMap
    }


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
