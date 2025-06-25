@file:Suppress(
    "DATA_CLASS_COPY_VISIBILITY_WILL_BE_CHANGED_WARNING",
    "UNCHECKED_CAST",
    "RedundantVisibilityModifier",
)
@file:SuppressWarnings(
    "UNCHECKED_CAST"
)

package com.jet.tts

import android.os.Build
import android.os.Bundle
import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect


/**
 * Helper class used for saving/restoring state of [TtsClient] when composable is disposed. [com.jet.tts.TtsState]
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
public data class TtsState internal constructor(
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
     * Flag indicating if state is not empty.
     * @since 1.0.0
     */
    internal val isNotEmpty: Boolean
        get() = !isEmpty


    /**
     * Captures current state of [TtsClient] and saves to be saved later by [Saver]. **[TtsClient]
     * has to call [captureState] in all scenarios when some of the parameters [com.jet.tts.TtsState]
     * has changed.**
     * @since 1.0.0
     */
    internal fun captureState(client: TtsClientImpl) {

        if (client.isInDisposeState) {
            //Client is disposed, meaning that screen was closed or configuration change happened
            return
        }

        this.utteranceId = client.currentUtteranceId
        this.startIndex = client.currentStartIndex
        this.endIndex = client.currentEndIndex
        this.isSpeaking = client.isSpeaking
        this.map = client.contentMap
    }


    /**
     * Saver used for saving/restoring state of [TtsClient] using [com.jet.tts.TtsState].
     * @since 1.0.0
     */
    object Saver : androidx.compose.runtime.saveable.Saver<TtsState, Bundle> {

        //Keys for saving/restoring state
        private const val UTTERANCE_ID: String = "utteranceId"
        private const val START_INDEX: String = "startIndex"
        private const val END_INDEX: String = "endIndex"
        private const val IS_SPEAKING: String = "isSpeaking"
        private const val MAP: String = "map"

        override fun SaverScope.save(value: TtsState): Bundle {
            return bundleOf(
                UTTERANCE_ID to value.utteranceId,
                START_INDEX to value.startIndex,
                END_INDEX to value.endIndex,
                IS_SPEAKING to value.isSpeaking,
                MAP to HashMap(value.map), //We need to create copy for state saver
            )
        }


        override fun restore(value: Bundle): TtsState {
            val holder = TtsState(
                utteranceId = value.getString(UTTERANCE_ID, ""),
                startIndex = value.getInt(START_INDEX, 0),
                endIndex = value.getInt(END_INDEX, 0),
                isSpeaking = value.getBoolean(IS_SPEAKING, false),
                map = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val type = HashMap::class.java
                    (value.getSerializable(
                        MAP,
                        type
                    ) as? HashMap<String, Utterance>) ?: emptyMap()
                } else {
                    @Suppress("DEPRECATION")
                    (value.getSerializable(
                        MAP,
                    ) as? HashMap<String, Utterance>) ?: emptyMap()
                },
            )
            return holder
        }
    }
}


/**
 * TODO docs
 */
@Composable
public fun rememberTtsState(
    client: TtsClient,
    vararg utterances: Pair<String, String>,
): TtsState {
    return rememberTtsState(
        client = client,
        utterances = utterances.toList()
    )
}


/**
 * TODO docs
 */
@Composable
public fun rememberTtsState(
    client: TtsClient,
    utterances: List<Pair<String, String>>,
): TtsState {

    val state = rememberSaveable(saver = TtsState.Saver) {
        TtsState(
            map = utterances
                .mapIndexed(
                    transform = { index, pair ->
                        pair.first to Utterance(
                            utteranceId = pair.first,
                            content = pair.second,
                            sequence = index,
                            currentIndexThreshold = 0,
                        )
                    }
                )
                .associate { pair ->
                    pair.first to pair.second
                },
        )
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        client.initWithState(stateHolder = state)
    }


    DisposableEffect(key1 = Unit) {
        onDispose {
            client.stopOnDispose()
        }
    }

    return state
}
