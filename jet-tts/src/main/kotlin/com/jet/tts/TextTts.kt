package com.jet.tts

import androidx.annotation.Keep
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp


/**
 * Basic implementation of [Text] with text highlight feature. Plain [Utterance.content] is styled
 * by [highlightText].
 * @param text Text to be displayed and highlighted by [TtsClient.highlightMode].
 * @param utteranceId Unique identifier of the utterance. When text displayed is not for the current
 * utterance, text will not be highlighted. **Make sure [utteranceId] matched with one passed in
 * [TtsClient.speak].
 * @param ttsClient [TtsClient] used for [android.speech.tts.TextToSpeech] feature.
 * @param highlightStyle [TextStyle] used for highlighting text.
 * @param scrollState When the text is longer than the screen, you can provide [ScrollState] to
 * enable scroll feature, [TextTts] will apply slow scroll animation to keep highlighted text visible
 * as it goes down through the [text].
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

    val utteranceRange by ttsClient.utteranceRange.collectAsState()

    TextTts(
        modifier = modifier,
        text = text,
        range = utteranceRange,
        utteranceId = utteranceId,
        highlightStyle = highlightStyle,
        scrollState = scrollState,
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
    )
}


/**
 * Basic implementation of [Text] with text highlight feature. Plain [Utterance.content] is styled
 * by [highlightText].
 * @param text Text to be displayed and highlighted by [TtsClient.highlightMode].
 * @param utteranceId Unique identifier of the utterance. When text displayed is not for the current
 * utterance, text will not be highlighted. **Make sure [utteranceId] matched with one passed in
 * [TtsClient.speak].
 * @param range Range of the text to be highlighted.
 * @param highlightStyle [TextStyle] used for highlighting text.
 * @param scrollState When the text is longer than the screen, you can provide [ScrollState] to
 * enable scroll feature, [TextTts] will apply slow scroll animation to keep highlighted text visible
 * as it goes down through the [text].
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
    range: UtteranceProgress,
    scrollState: ScrollState?,
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
    highlightStyle: TextStyle = LocalTextStyle.current.copy(
        color = MaterialTheme.colorScheme.primary,
    )
) {
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val textMeasurer = rememberTextMeasurer()
    var layoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(value = null) }

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

    LaunchedEffect(key1 = range.utteranceId) {
        val layout = layoutCoordinates ?: return@LaunchedEffect
        if (range.utteranceId == utteranceId && scrollState != null) {
            //TODO make possible to specify extra offset
            val extraOffset = with(density) { 32.dp.toPx() }.toInt()

            val newScrollValue= (layout.positionInRoot().y.toInt() - extraOffset)
                .coerceAtLeast(minimumValue = 0)

          if (newScrollValue <= scrollState.value) {
              //For best UX scroll should always go down
              return@LaunchedEffect
          }

            scrollState.animateScrollTo(
                value = newScrollValue,
                animationSpec = tween(
                    durationMillis = 1500,
                    easing = LinearEasing,
                )
            )
        }
    }

    LaunchedEffect(key1 = text, key2 = range) {
        val newText = highlightText(
            text = text,
            range = range,
            highlightStyle = highlightStyle,
            normalStyle = style,
            utteranceId = utteranceId,
        )
        innerText = newText


        if (scrollState == null) return@LaunchedEffect
        if (utteranceId != range.utteranceId) return@LaunchedEffect
        if (scrollState.isScrollInProgress) return@LaunchedEffect


        val measureResult = textMeasurer.measure(
            text = newText,
            style = style,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            placeholders = emptyList(),
            constraints = Constraints(),
            layoutDirection = layoutDirection,
            density = density,
            fontFamilyResolver = fontFamilyResolver,
            skipCache = false,
        )

        val box = measureResult.getBoundingBox(offset = range.last)
        val targetOffset = box.bottom
        //TODO add option to "jump" down when scrollState.isScrollInProgress changes
        //TODO also work with layoutResult to scroll down when there is content between tts texts
        scrollState.animateScrollBy(
            value = targetOffset,
            animationSpec = tween(
                durationMillis = 1750,
                easing = LinearEasing,
            )
        )
    }


    Text(
        modifier = modifier
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
        onTextLayout = onTextLayout,
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

    return buildAnnotatedString {
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
}