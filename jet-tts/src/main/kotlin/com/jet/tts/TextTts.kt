@file:Keep

package com.jet.tts

import android.util.Log
import androidx.annotation.Keep
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jet.tts.TtsClient.HighlightMode


/**
 * Basic implementation of [Text] with text highlight feature. Plain [Utterance.content] is styled
 * by [highlightText].
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
 * By providing a [androidx.compose.foundation.ScrollState], [TextTts] can use it to autoscroll to
 * currently spoken line. Solution for [androidx.compose.foundation.lazy.LazyColumn] is not avaliable now.
 * Use [rememberTtsClient] to get an instance of [com.jet.tts.TtsClient].
 *
 * ## Navigation Feature
 * It is possible to "navigate" in utterance when `ttsClient.isSpeaking == true`, by clicking into
 * [TextTts] client will navigate speech by clicked word.
 * @param utterance Utterance from [TtsState] to show.
 * @param ttsClient [TtsClient] instance used for [android.speech.tts.TextToSpeech] feature.
 * @param highlightStyle [TextStyle] used for highlighting text by [TtsClient.highlightMode].
 * @param scrollableState When the text is longer than the screen, you can provide [ScrollState] to
 * enable scroll feature (requires api >= 26), [TextTts] will apply slow scroll animation to keep
 * highlighted text visible as it goes down through the text.
 * @see [Text] for other parameters docs.
 * @author Miroslav Hýbler <br>
 * created on 04.02.2025
 * @since 1.0.0
 */
@Composable
@Keep
fun TextTts(
    modifier: Modifier = Modifier,
    utterance: Utterance,
    ttsClient: TtsClient,
    scrollableState: ScrollableState? = null,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: ((TextLayoutResult) -> Unit) = {},
    style: TextStyle = LocalTextStyle.current,
    highlightStyle: TextStyle = style.copy(
        color = MaterialTheme.colorScheme.primary,
    ),
) {
    TextTts(
        modifier = modifier,
        text = utterance.content,
        utteranceId = utterance.utteranceId,
        ttsClient = ttsClient,
        scrollableState = scrollableState,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
        style = style,
        highlightStyle = highlightStyle,
    )
}


/**
 * Basic implementation of [Text] with text highlight feature. Plain [Utterance.content] is styled
 * by [highlightText].
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
 * By providing a [androidx.compose.foundation.ScrollState], [TextTts] can use it to autoscroll to
 * currently spoken line. Solution for [androidx.compose.foundation.lazy.LazyColumn] is not avaliable now.
 * Use [rememberTtsClient] to get an instance of [com.jet.tts.TtsClient].
 *
 * ## Navigation Feature
 * It is possible to "navigate" in utterance when `ttsClient.isSpeaking == true`, by clicking into
 * [TextTts] client will navigate speech by clicked word.
 *
 * @param text Text to be displayed and highlighted by [TtsClient.highlightMode]. Must be same
 * text as passed in [TtsClient.speak] with the [utteranceId].
 * @param utteranceId Unique identifier of the utterance. When text displayed is not for the current
 * utterance, text will not be highlighted. **Make sure [utteranceId] matched with one passed in
 * [TtsClient.speak].
 * @param ttsClient [TtsClient] instance used for [android.speech.tts.TextToSpeech] feature.
 * @param highlightStyle [TextStyle] used for highlighting text by [TtsClient.highlightMode].
 * @param scrollableState When the text is longer than the screen, you can provide [ScrollState] to
 * enable scroll feature (requires api >= 26), [TextTts] will apply slow scroll animation to keep
 * highlighted text visible as it goes down through the [text].
 * @see [Text] for other parameters docs.
 * @author Miroslav Hýbler <br>
 * created on 04.02.2025
 * @since 1.0.0
 */
@Composable
@Keep
fun TextTts(
    modifier: Modifier = Modifier,
    text: String,
    utteranceId: String,
    ttsClient: TtsClient,
    scrollableState: ScrollableState? = null,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: ((TextLayoutResult) -> Unit) = {},
    style: TextStyle = LocalTextStyle.current,
    highlightStyle: TextStyle = style.copy(
        color = MaterialTheme.colorScheme.primary,
    ),
) {
    val range by ttsClient.utteranceRange.collectAsState()
    val density = LocalDensity.current

    //Extra offset from top of the screen so highlighted text will not be at the top corner of the screen
    val extraOffset = remember { with(receiver = density) { 128.dp.toPx() }.toInt() }

    val sequence = remember(key1 = utteranceId, key2 = range) {
        ttsClient.getSequenceForUtterance(utteranceId = utteranceId)
    }

    var layoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(value = null) }
    var textLayout: TextLayoutResult? by remember { mutableStateOf(value = null) }
    val innerText: AnnotatedString = remember(
        key1 = text.hashCode(),
        key2 = range,
        key3 = ttsClient.highlightMode,
    ) {
        highlightText(
            text = text,
            range = range,
            highlightStyle = highlightStyle,
            normalStyle = style,
            utteranceId = utteranceId,
            highlightMode = ttsClient.highlightMode,
            sequence = sequence,
        )
    }

    //Holds index of line that is currently spoken (api >= 26)
    var currentSpokenLine by remember { mutableIntStateOf(value = 0) }


    //Launched effect to scroll to the current utterance to assure that currently spoken text is
    //visible even when there is some content between utterances
    LaunchedEffect(key1 = range.utteranceId) {
        tryScrollToCurrentLine(
            range = range,
            utteranceId = utteranceId,
            scrollableState = scrollableState,
            textLayout = textLayout,
            layoutCoordinates = layoutCoordinates,
            currentSpokenLine = currentSpokenLine,
            extraOffset = extraOffset,
        )
    }

    //Effect to highlight text when range changes and also scroll to currently spoken text
    LaunchedEffect(
        key1 = text.hashCode(),
        key2 = range,
        key3 = ttsClient.highlightMode,
    ) {
        if (range.utteranceId != utteranceId) return@LaunchedEffect
        val textLayoutResult = textLayout ?: return@LaunchedEffect
        currentSpokenLine = textLayoutResult.getLineForOffset(offset = range.last)
    }


    //Effect to handle scroll to the current line
    LaunchedEffect(key1 = currentSpokenLine) {
        tryScrollToCurrentLine(
            range = range,
            utteranceId = utteranceId,
            scrollableState = scrollableState,
            textLayout = textLayout,
            layoutCoordinates = layoutCoordinates,
            currentSpokenLine = currentSpokenLine,
            extraOffset = extraOffset,
        )
    }

    Text(
        modifier = modifier
            .ttsClickModifier(
                textLayout = textLayout,
                ttsClient = ttsClient,
                utteranceId = utteranceId,
            )
            .onGloballyPositioned { coordinates ->
                layoutCoordinates = coordinates
            },
        text = innerText,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = { newTextLayout ->
            textLayout = newTextLayout
            onTextLayout(newTextLayout)
        },
        style = style,
    )
}


/**
 * Basic implementation of [Text] with text highlight feature. Plain [Utterance.content] is styled
 * by [highlightText].
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
 * By providing a [androidx.compose.foundation.ScrollState], [TextTts] can use it to autoscroll to
 * currently spoken line. Solution for [androidx.compose.foundation.lazy.LazyColumn] is not avaliable now.
 * Use [rememberTtsClient] to get an instance of [com.jet.tts.TtsClient].
 *
 * ## Navigation Feature
 * It is possible to "navigate" in utterance when `ttsClient.isSpeaking == true`, by clicking into
 * [TextTts] client will navigate speech by clicked word.
 *
 * @param text Text to be displayed and highlighted by [TtsClient.highlightMode]. Must be same
 * text as passed in [TtsClient.speak] with the [utteranceId].
 * @param utteranceId Unique identifier of the utterance. When text displayed is not for the current
 * utterance, text will not be highlighted. **Make sure [utteranceId] matched with one passed in
 * [TtsClient.speak].
 * @param ttsClient [TtsClient] instance used for [android.speech.tts.TextToSpeech] feature.
 * @param highlightStyle [TextStyle] used for highlighting text by [TtsClient.highlightMode].
 * @param scrollableState When the text is longer than the screen, you can provide [ScrollState] to
 * enable scroll feature (requires api >= 26), [TextTts] will apply slow scroll animation to keep
 * highlighted text visible as it goes down through the [text].
 * @see [Text] for other parameters docs.
 * @author Miroslav Hýbler <br>
 * created on 04.02.2025
 * @since 1.0.0
 */
@Composable
@Keep
fun TextTts(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    utteranceId: String,
    ttsClient: TtsClient,
    scrollableState: ScrollableState? = null,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: ((TextLayoutResult) -> Unit) = {},
    style: TextStyle = LocalTextStyle.current,
    highlightStyle: TextStyle = style.copy(
        color = MaterialTheme.colorScheme.primary,
    ),
) {
    val range by ttsClient.utteranceRange.collectAsState()
    val density = LocalDensity.current

    //Extra offset from top of the screen so highlighted text will not be at the top corner of the screen
    val extraOffset = remember { with(density) { 128.dp.toPx() }.toInt() }
    val sequence = remember(key1 = utteranceId) {
        ttsClient.getSequenceForUtterance(utteranceId = utteranceId)
    }

    var layoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(value = null) }
    var textLayout: TextLayoutResult? by remember { mutableStateOf(value = null) }
    val innerText: AnnotatedString = remember(
        key1 = text.hashCode(),
        key2 = range,
        key3 = ttsClient.highlightMode,
    ) {
        highlightText(
            text = text,
            range = range,
            highlightStyle = highlightStyle,
            normalStyle = style,
            utteranceId = utteranceId,
            highlightMode = ttsClient.highlightMode,
            sequence = sequence,
        )

    }

    //Holds index of line that is currently spoken (api >= 26)
    var currentSpokenLine by remember { mutableIntStateOf(value = 0) }


    //Launched effect to scroll to the current utterance to assure that currently spoken text is
    //visible even when there is some content between utterances
    LaunchedEffect(key1 = range.utteranceId) {
        tryScrollToCurrentLine(
            range = range,
            utteranceId = utteranceId,
            scrollableState = scrollableState,
            textLayout = textLayout,
            layoutCoordinates = layoutCoordinates,
            currentSpokenLine = currentSpokenLine,
            extraOffset = extraOffset,
        )
    }

    //Effect to highlight text when range changes and also scroll to currently spoken text
    LaunchedEffect(
        key1 = text.hashCode(),
        key2 = range,
        key3 = ttsClient.highlightMode,
    ) {
        if (range.utteranceId != utteranceId) return@LaunchedEffect
        val textLayoutResult = textLayout ?: return@LaunchedEffect
        currentSpokenLine = textLayoutResult.getLineForOffset(offset = range.last)
    }


    //Effect to handle scroll to the current line
    LaunchedEffect(key1 = currentSpokenLine) {
        tryScrollToCurrentLine(
            range = range,
            utteranceId = utteranceId,
            scrollableState = scrollableState,
            textLayout = textLayout,
            layoutCoordinates = layoutCoordinates,
            currentSpokenLine = currentSpokenLine,
            extraOffset = extraOffset,
        )
    }

    Text(
        modifier = modifier
            .ttsClickModifier(
                textLayout = textLayout,
                ttsClient = ttsClient,
                utteranceId = utteranceId,
            )
            .onGloballyPositioned { coordinates ->
                layoutCoordinates = coordinates
            },
        text = innerText,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = { newTextLayout ->
            textLayout = newTextLayout
            onTextLayout(newTextLayout)
        },
        style = style,
    )
}


/**
 * @param text Text to be annotated by [TtsClient.highlightMode].
 * @param range Range of the text to be highlighted.
 * @param normalStyle Style of the text that is not highlighted.
 * @param highlightStyle Style of the text that is highlighted.
 * @param utteranceId Unique identifier of the utterance. When text displayed is not for the current
 * utterance, text will not be highlighted.
 * @return Annotated string with highlighted text.
 * @since 1.0.0
 */
private fun highlightText(
    text: String,
    range: UtteranceProgress,
    normalStyle: TextStyle,
    highlightStyle: TextStyle,
    utteranceId: String,
    highlightMode: HighlightMode,
    sequence: Int,
): AnnotatedString {
    val normalSpanStyle = normalStyle.toSpanStyle()

    if (range == UtteranceProgress.EMPTY) {
        //Range is empty, there is no utterance in progress
        return buildAnnotatedString {
            withStyle(style = normalSpanStyle) {
                append(text = text)
            }
        }
    }

    if (utteranceId != range.utteranceId) {
        //Text is not for the current utterance.
        return if (
            sequence < range.sequence
            && highlightMode == HighlightMode.SPOKEN_RANGE_FROM_BEGINNING_INCLUDING_PREVIOUS_UTTERANCES
        ) {
            //Text is "previous" utterance, highlight all the text because of highlightMode
            buildAnnotatedString {
                withStyle(style = highlightStyle.toSpanStyle()) {
                    append(text = text)
                }
            }
        } else {
            buildAnnotatedString {
                withStyle(style = normalSpanStyle) {
                    append(text = text)
                }
            }
        }
    }

    if (range.isTextRangeEmpty) {
        return buildAnnotatedString {
            withStyle(style = normalSpanStyle) {
                append(text = text)
            }
        }
    }

    val highlightSpanStyle = highlightStyle.toSpanStyle()
    return try {
        buildAnnotatedString {
            if (range.first > 0) {
                withStyle(style = normalSpanStyle) {
                    append(text = text.substring(startIndex = 0, endIndex = range.first))
                }
            }
            withStyle(style = highlightSpanStyle) {
                //Appending text that should be highlighted
                append(text = text.substring(startIndex = range.first, endIndex = range.last))
            }

            if (range.last != text.length) {
                withStyle(style = normalSpanStyle) {
                    append(text = text.substring(startIndex = range.last, endIndex = text.length))
                }
            }
        }
    } catch (exception: StringIndexOutOfBoundsException) {
//        throw IllegalStateException(
//            "Visible text for utterance $utteranceId doesn't match text passed into ttsClient! " +
//                    "Check if have content and ids mapped correctly.",
//            exception
//        )
        return buildAnnotatedString {
            withStyle(style = normalSpanStyle) {
                append(text = text)
            }
        }
    }
}


/**
 * @param text Text to be annotated by [TtsClient.highlightMode] while also keeping original styles
 * and annotations.
 * @param range Range of the text to be highlighted.
 * @param normalStyle Style of the text that is not highlighted.
 * @param highlightStyle Style of the text that is highlighted.
 * @param utteranceId Unique identifier of the utterance. When text displayed is not for the current
 * utterance, text will not be highlighted.
 * @param highlightMode [TtsClient.HighlightMode] of the client.
 * @return Annotated string with highlighted text.
 * @since 1.0.0
 */
private fun highlightText(
    text: AnnotatedString,
    range: UtteranceProgress,
    normalStyle: TextStyle,
    highlightStyle: TextStyle,
    utteranceId: String,
    highlightMode: HighlightMode,
    sequence: Int,
): AnnotatedString {
    val normalSpanStyle = normalStyle.toSpanStyle()

    if (range == IntRange.EMPTY) {
        //Range is empty, there is no utterance in progress
        return buildAnnotatedString {
            withStyle(style = normalSpanStyle) {
                append(text = text)
            }
        }
    }

    if (utteranceId != range.utteranceId) {
        //Text is not for the current utterance.
        return if (
            sequence < range.sequence
            && highlightMode == HighlightMode.SPOKEN_RANGE_FROM_BEGINNING_INCLUDING_PREVIOUS_UTTERANCES
        ) {
            //Text is "previous" utterance, highlight all the text because of highlightMode
            buildAnnotatedString {
                withStyle(style = highlightStyle.toSpanStyle()) {
                    append(text = text)
                }
            }
        } else {
            buildAnnotatedString {
                withStyle(style = normalSpanStyle) {
                    append(text = text)
                }
            }
        }
    }
    if (range.isTextRangeEmpty) {
        return buildAnnotatedString {
            withStyle(style = normalSpanStyle) {
                append(text = text)
            }
        }
    }
    val highlightSpanStyle = highlightStyle.toSpanStyle()

    return try {
        buildAnnotatedString {
            if (range.first > 0) {
                withStyle(style = normalSpanStyle) {
                    append(text = text.subSequence(startIndex = 0, endIndex = range.first))
                }
            }
            withStyle(style = highlightSpanStyle) {
                //Appending text that should be highlighted
                append(text = text.subSequence(startIndex = range.first, endIndex = range.last))
            }

            if (range.last != text.length) {
                withStyle(style = normalSpanStyle) {
                    append(text = text.subSequence(startIndex = range.last, endIndex = text.length))
                }
            }
        }
    } catch (exception: StringIndexOutOfBoundsException) {
        /*
         throw IllegalStateException(
             "Visible text for utterance $utteranceId doesn't match text passed into ttsClient! " +
                     "Check if have content and ids mapped correctly.",
             exception
         )
         */
        return buildAnnotatedString {
            withStyle(style = normalSpanStyle) {
                append(text = text)
            }
        }
    }
}


/**
 * [Modifier] for handling navigation in utterance when [TtsClient.isSpeaking] is true.
 * @since 1.0.0
 */
internal fun Modifier.ttsClickModifier(
    textLayout: TextLayoutResult?,
    ttsClient: TtsClient,
    utteranceId: String,
): Modifier {

    return when (ttsClient.tapNavigationBehavior) {
        TtsClient.TapNavigationBehavior.DISABLED -> this
        TtsClient.TapNavigationBehavior.ONLY_WHEN_CURRENTLY_SPEAKING -> {
            return if (ttsClient.isSpeaking) {
                this.ttsClickModifierImpl(
                    textLayout = textLayout,
                    ttsClient = ttsClient,
                    utteranceId = utteranceId,
                )
            } else {
                this
            }
        }

        TtsClient.TapNavigationBehavior.ALWAYS -> {
            return this.ttsClickModifierImpl(
                textLayout = textLayout,
                ttsClient = ttsClient,
                utteranceId = utteranceId,
            )
        }
    }
}



internal fun Modifier.ttsClickModifierImpl(
    textLayout: TextLayoutResult?,
    ttsClient: TtsClient,
    utteranceId: String,
): Modifier {
    return this.pointerInput(key1 = Unit) {
        detectTapGestures(
            onTap = { tapOffset ->
                val textLayoutResult = textLayout ?: return@detectTapGestures
                //Clicked char offset from the start of the text
                val offset = textLayoutResult.getOffsetForPosition(position = tapOffset)
                //
                val word = textLayoutResult.getWordBoundary(offset = offset)
                if (word.start < 0) return@detectTapGestures
                //Navigate to clicked word
                ttsClient.navigateInUtterance(
                    utteranceId = utteranceId,
                    startIndex = word.start,
                )
            }
        )
    }
}