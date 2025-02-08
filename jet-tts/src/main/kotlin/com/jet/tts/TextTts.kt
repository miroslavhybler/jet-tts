package com.jet.tts

import android.util.Log
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
 * @author Miroslav Hýbler <br>
 * created on 04.02.2025
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
 * @author Miroslav Hýbler <br>
 * created on 04.02.2025
 */
@Composable
fun TextTts(
    modifier: Modifier = Modifier,
    text: String,
    range: UtteranceProgress,
    utteranceId: String,
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
            value = buildTextWithRange(
                text = text,
                range = range,
                highlightStyle = highlightStyle,
                normalStyle = style,
                utteranceId = utteranceId,
            )
        )
    }

    LaunchedEffect(key1 = text, key2 = range) {
        innerText = buildTextWithRange(
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


private fun buildTextWithRange(
    text: String,
    range: UtteranceProgress,
    normalStyle: TextStyle,
    highlightStyle: TextStyle,
    utteranceId: String,
): AnnotatedString {
    val normalSpanStyle = normalStyle.toSpanStyle()
    if (range == IntRange.EMPTY || utteranceId != range.utteranceId) {
        return buildAnnotatedString {
            withStyle(style = normalSpanStyle) {
                append(text = text)
            }
        }
    }

    val highlightSpanStyle = highlightStyle.toSpanStyle()

    /*
    if (range.last == text.length) {
        return buildAnnotatedString {
            withStyle(style = highlightSpanStyle) {
                append(text = text.substring(startIndex = 0, endIndex = range.last))
            }
        }
    }
    */

    return buildAnnotatedString {
        if (range.first > 0) {
            withStyle(style = normalSpanStyle) {
                append(text = text.substring(startIndex = 0, endIndex = range.first))
            }
        }
        withStyle(style = highlightSpanStyle) {
            append(text = text.substring(startIndex = range.first, endIndex = range.last))
        }

        if (range.last != text.length) {
            withStyle(style = normalSpanStyle) {
                append(text = text.substring(startIndex = range.last, endIndex = text.length))
            }
        }
    }
}