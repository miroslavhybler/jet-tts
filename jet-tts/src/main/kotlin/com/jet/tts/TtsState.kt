@file:Keep
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
import android.util.Log
import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect


/**
 * Helper class used for saving/restoring state of [TtsClient] when composable is resumed or disposed.
 * Except for the [map], [com.jet.tts.TtsState] should always stay out of composition scope,
 * that's why all parameters are mutable variables without [androidx.compose.runtime.mutableStateOf].
 * @param utteranceId Unique identifier of the current utterance.
 * @param startIndex Inclusive start index of the first character of the current utterance.
 * @param endIndex Exclusive end index of the last character of the current utterance.
 * @param isSpeaking Flag indicating if [TtsClient] was speaking when state was saved.
 * @param map Map of [Utterance]s added to the [TtsClient].
 * @author Miroslav Hýbler <br>
 * created on 05.02.2025
 * @since 1.0.0
 */
//TODO add clear function
@Keep
public data class TtsState internal constructor(
    internal var utteranceId: String = "",
    internal var startIndex: Int = 0,
    internal var endIndex: Int = 0,
    internal var isSpeaking: Boolean = false,
    internal var map: SnapshotStateMap<String, Utterance> = mutableStateMapOf(),
) : Map<String, Utterance> {


    /**
     * Returns the number of key/value pairs in the [map].
     * @since 1.0.0
     */
    override val entries: Set<Map.Entry<String, Utterance>>
        get() = map.entries


    /**
     * @return A [Set] of the utterance ids contained in [map].
     * @since 1.0.0
     */
    override val keys: Set<String>
        get() = map.keys


    /**
     * @return A [Collection] of all utterances contained in [map].
     * @since 1.0.0
     */
    override val values: Collection<Utterance>
        get() = map.values


    /**
     * @return The count of utterances of the [map].
     * @since 1.0.0
     */
    override val size: Int
        get() = map.size


    /**
     * Flag indicating if state is empty.
     * @since 1.0.0
     */
    internal val isEmpty: Boolean
        get() = utteranceId == ""
                && startIndex == 0
                && endIndex == 0
                && map.isEmpty()


    /**
     * Flag indicating if state is not empty.
     * @since 1.0.0
     */
    internal val isNotEmpty: Boolean
        get() = !isEmpty


    /**
     * @return True if [map] contains [key], false otherwise.
     * @since 1.0.0
     */
    override fun containsKey(key: String): Boolean {
        return map.containsKey(key = key)
    }


    /**
     * @return True if the map is empty (contains no elements), false otherwise.
     * @since 1.0.0
     */
    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }


    /**
     * True if [map] contains [value], false otherwise.
     * @since 1.0.0
     */
    override fun containsValue(value: Utterance): Boolean {
        return map.containsValue(value = value)
    }


    /**
     * **NOTE:** This method is null safe for better implementation but can throw [NullPointerException].
     * Make sure to manage utterances properly.
     * @return The value corresponding to the given [key], or throws [NullPointerException] if there is no such key.
     */
    override fun get(key: String): Utterance {
        return map[key]
            ?: throw NullPointerException("Utterance with id $key not found!")
    }


    /**
     * Saves [value] under [utteranceId].
     * @param utteranceId Unique identifier of the utterance.
     * @param value Text that is gonna be spoken by [android.speech.tts.TextToSpeech].
     * @since 1.0.0
     */
    operator fun set(utteranceId: String, value: String) {
        this.map[utteranceId] = Utterance(
            utteranceId = utteranceId,
            content = value,
            sequence = this.map.size,
            currentIndexThreshold = 0
        )
    }


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

        Log.i("TtsState", "captureState()")
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
            return Bundle().apply {
                putString(UTTERANCE_ID, value.utteranceId)
                putInt(START_INDEX, value.startIndex)
                putInt(END_INDEX, value.endIndex)
                putBoolean(IS_SPEAKING, value.isSpeaking)
                putSerializable(MAP, HashMap(value.map)) //We need to create copy for state saver
            }
        }

        override fun restore(value: Bundle): TtsState {
            val holder = TtsState(
                utteranceId = value.getString(UTTERANCE_ID, ""),
                startIndex = value.getInt(START_INDEX, 0),
                endIndex = value.getInt(END_INDEX, 0),
                isSpeaking = value.getBoolean(IS_SPEAKING, false),
                map = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val type = HashMap::class.java
                    val savedMap = (value.getSerializable(MAP, type)
                            as? HashMap<String, Utterance>
                            ) ?: emptyMap()

                    val stateMap = SnapshotStateMap<String, Utterance>().also { stateMap ->
                        stateMap.putAll(from = savedMap)
                    }
                    stateMap
                } else {
                    @Suppress("DEPRECATION")
                    val savedMap = (value.getSerializable(MAP) as? HashMap<String, Utterance>)
                        ?: emptyMap()

                    val stateMap = SnapshotStateMap<String, Utterance>().also { stateMap ->
                        stateMap.putAll(from = savedMap)
                    }
                    stateMap
                },
            )
            return holder
        }
    }
}


/**
 * Remembers [TtsState] for [TtsClient] on current composable. Make sure to call [TtsLifecycleAwareEffect]
 * to initialize [TtsClient] with the state.
 * @param utterances List of utterances to add to [TtsClient]. Each utterance is a pair of utteranceId
 * and content to be spoken.
 * @return [TtsState] for [TtsClient].
 * @since 1.0.0
 */
@Keep
@Composable
public fun rememberTtsState(
    key: Any? = null,
    vararg utterances: Pair<String, String>,
): TtsState {
    return rememberTtsState(
        key = key,
        utterances = utterances.toList()
    )
}


/**
 * Remembers [TtsState] for [TtsClient] on current composable. Make sure to call [TtsLifecycleAwareEffect]
 * to initialize [TtsClient] with the state.
 * @param utterances List of utterances to add to [TtsClient]. Each utterance is a pair of utteranceId
 * and content to be spoken.
 * @return [TtsState] for [TtsClient].
 * @since 1.0.0
 */
@Keep
@Composable
public fun rememberTtsState(
    key: Any? = null,
    utterances: List<Pair<String, String>>,
): TtsState {

    val state = rememberSaveable(
        key,
        saver = TtsState.Saver
    ) {
        val utterancesMap = utterances
            .mapIndexed(
                transform = { index, pair ->
                    val utterance = Utterance(
                        utteranceId = pair.first,
                        content = pair.second,
                        sequence = index,
                        currentIndexThreshold = 0,
                    )
                    pair.first to utterance
                }
            )
            .associate { pair ->
                pair.first to pair.second
            }

        val stateMap = SnapshotStateMap<String, Utterance>().also { stateMap ->
            stateMap.putAll(from = utterancesMap)
        }

        TtsState(map = stateMap)
    }

    return state
}


/**
 * Initialize [TtsClient] with the [state] when composable is resumed and, stops client and saves
 * the [state] when composable is disposed.
 * @since 1.0.0
 */
@Keep
@Composable
fun TtsLifecycleAwareEffect(
    client: TtsClient,
    state: TtsState,
) {

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        client.initWithState(stateHolder = state)
    }
    //TODO add option into tts if client should stop on_pause


    DisposableEffect(key1 = Unit) {
        onDispose {
            client.stopOnDispose()
        }
    }
}