package com.jet.tts

import androidx.annotation.Keep
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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


/**
 * Basic implementation of [Text] with text highlight feature. Plain [Utterance.content] is styled
 * by [highlightText].
 * @param text Text to be displayed and highlighted by [TtsClient.highlightMode].
 * @param utteranceId Unique identifier of the utterance. When text displayed is not for the current
 * utterance, text will not be highlighted. **Make sure [utteranceId] matched with one passed in
 * [TtsClient.speak].
 * @param ttsClient [TtsClient] used for [android.speech.tts.TextToSpeech] feature.
 * @param highlightStyle [TextStyle] used for highlighting text.
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
    highlightStyle: TextStyle = LocalTextStyle.current.copy(
        color = MaterialTheme.colorScheme.primary,
    ),
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
) {

    val utteranceRange by ttsClient.utteranceRange.collectAsState()

    TextTts(
        modifier = modifier,
        text = text,
        range = utteranceRange,
        utteranceId = utteranceId,
        highlightStyle = highlightStyle,
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
    highlightStyle: TextStyle = LocalTextStyle.current.copy(
        color = MaterialTheme.colorScheme.primary,
    ),
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
) {


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

    LaunchedEffect(key1 = text, key2 = range) {
        innerText = highlightText(
            text = text,
            range = range,
            highlightStyle = highlightStyle,
            normalStyle = style,
            utteranceId = utteranceId,
        )
    }


    Text(
        modifier = modifier,
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