@file:Suppress("DATA_CLASS_COPY_VISIBILITY_WILL_BE_CHANGED_WARNING")

package com.jet.tts

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize


/**
 * @author Miroslav Hýbler <br>
 * created on 06.02.2025
 */
@Keep
@Parcelize
internal data class Utterance internal constructor(
    val utteranceId: String,
    val content: String,
    val sequence: Int,
    var currentThreshold: Int = 0,
) : Parcelable {
}