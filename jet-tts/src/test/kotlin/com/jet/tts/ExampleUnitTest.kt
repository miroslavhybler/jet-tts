package com.jet.tts

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

/**
 * Local unit tests for TtsState helper behavior.
 */
class ExampleUnitTest {

    @Test
    fun missing_utterance_returns_null() {
        val state = TtsState()

        assertNull(state["missing"])
    }

    @Test
    fun replacing_utterance_preserves_sequence_and_threshold() {
        val state = TtsState()
        state["first"] = "one"
        state["second"] = "two"

        val original = state.requireUtterance("first").copy(currentIndexThreshold = 7)
        state.map["first"] = original

        state["first"] = "updated"

        val updated = state.requireUtterance("first")
        assertEquals(original.sequence, updated.sequence)
        assertEquals(original.currentIndexThreshold, updated.currentIndexThreshold)
        assertEquals("updated", updated.content)
    }
}
