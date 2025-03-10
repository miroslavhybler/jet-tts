package com.jet.tts

import androidx.annotation.Keep
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.positionInRoot
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
 *
 * @param text Text to be displayed and highlighted by [TtsClient.highlightMode]. Must be same
 * text as passed in [TtsClient.speak] with the [utteranceId].
 * @param utteranceId Unique identifier of the utterance. When text displayed is not for the current
 * utterance, text will not be highlighted. **Make sure [utteranceId] matched with one passed in
 * [TtsClient.speak].
 * @param ttsClient [TtsClient] instance used for [android.speech.tts.TextToSpeech] feature.
 * @param highlightStyle [TextStyle] used for highlighting text by [TtsClient.highlightMode].
 * @param scrollState When the text is longer than the screen, you can provide [ScrollState] to
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
    scrollState: ScrollState? = null,
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

    var layoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(value = null) }
    var textLayout: TextLayoutResult? by remember { mutableStateOf(value = null) }
    var innerText: AnnotatedString by remember {
        mutableStateOf(
            value = highlightText(
                text = text,
                range = range,
                highlightStyle = highlightStyle,
                normalStyle = style,
                utteranceId = utteranceId,
            )
        )
    }

    //Holds index of line that is currently spoken (api >= 26)
    var currentSpokenLine by remember { mutableIntStateOf(value = 0) }


    /**
     * Tries to scroll to the line of text that is currently spoken by [TtsClient] when conditions are met:
     * * Current [range]'s utteranceId matches [utteranceId]
     * * [TtsClient.isSpeaking] is true.
     * * [scrollState] is not null
     * * New scroll value is greater than the current scroll value
     * @since 1.0.0
     */
    suspend fun tryScrollToCurrentLine(): Unit {
        if (range.utteranceId != utteranceId) return
        if (scrollState == null) return
        val textLayoutResult = textLayout ?: return
        val layout = layoutCoordinates ?: return

        // Y position of the line in text
        val lineTop = textLayoutResult.getLineTop(lineIndex = currentSpokenLine)
        // Convert to visible window coordinates
        val y = layout.localToWindow(relativeToLocal = Offset(x = 0f, y = lineTop)).y
        val scrollTo = y + scrollState.value - extraOffset
        if (ttsClient.isSpeaking && scrollTo <= scrollState.value) {
            //For best UX scroll should always go down when client is speaking
            return
        }

        //Scrolling to the bottom of the line, assuring that spoken text is always visible
        //on the screen
        scrollState.animateScrollTo(
            value = scrollTo.toInt(),
            animationSpec = tween(
                durationMillis = 2000,
                easing = LinearEasing,
            )
        )
    }


    //Launched effect to scroll to the current utterance to assure that currently spoken text is
    //visible even when there is some content between utterances
    LaunchedEffect(key1 = range.utteranceId) {
        tryScrollToCurrentLine()
    }


    //Effect to highlight text when range changes and also scroll to currently spoken text
    LaunchedEffect(
        key1 = text,
        key2 = range,
        key3 = ttsClient.highlightMode,
    ) {
        val newText = highlightText(
            text = text,
            range = range,
            highlightStyle = highlightStyle,
            normalStyle = style,
            utteranceId = utteranceId,
        )
        innerText = newText

        if (range.utteranceId != utteranceId) return@LaunchedEffect
        val textLayoutResult = textLayout ?: return@LaunchedEffect
        currentSpokenLine = textLayoutResult.getLineForOffset(offset = range.last)
    }


    //Effect to handle scroll to the current line
    LaunchedEffect(key1 = currentSpokenLine) {
        tryScrollToCurrentLine()
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
): AnnotatedString {
    val normalSpanStyle = normalStyle.toSpanStyle()
    if (range == IntRange.EMPTY || utteranceId != range.utteranceId) {
        //Range is empty or text is not for the current utterance. When utteranceId doesn't match,
        //it means that TTs client is probably speaking another utterance now.

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
        throw IllegalStateException(
            "Visible text for utterance $utteranceId doesn't match text passed into ttsClient! " +
                    "Check if have content and ids mapped correctly.",
            exception
        )
    }
}


/**
 * [Modifier] for handling navigation in utterance when [TtsClient.isSpeaking] is true.
 * @since 1.0.0
 */
private fun Modifier.ttsClickModifier(
    textLayout: TextLayoutResult?,
    ttsClient: TtsClient,
    utteranceId: String,
): Modifier = this.pointerInput(key1 = Unit) {
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