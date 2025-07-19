package com.jet.tts.example.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jet.tts.TextTts
import com.jet.tts.TtsClient
import com.jet.tts.TtsLifecycleAwareEffect
import com.jet.tts.example.JetTtsExampleTheme
import com.jet.tts.example.LocalTtsClient
import com.jet.tts.example.R
import com.jet.tts.rememberTtsClient
import com.jet.tts.rememberTtsState


/**
 * Simple data class that represents article.
 */
data class Article constructor(
    val title: String,
    val description: String,
    val mainImageUrl: String,
    val content: String,
    val content2ImageUrl: String,
    val content2: String,
)


/**
 * Original article [here](https://www.britannica.com/place/Yellowstone-National-Park).
 */
val article: Article = Article(
    title = "Yellowstone National Park",
    description = "Yellowstone National Park, the oldest, one of the largest, and probably the best-known national park in the United States.",
    mainImageUrl = "https://cdn.britannica.com/59/152059-050-75DAE779/geyser-Old-Faithful-erupting-Upper-Geyser-Basin.jpg",
    content = "Yellowstone is situated in a region that has been volcanically and seismically active for tens of millions of years. " +
            "Tectonic movement of the North American Plate has thinned Earth’s crust in the area, forming a hotspot (a place where a " +
            "dome of magma, or molten rock, comes close to the surface). About 2.1 million years ago a subsurface magma dome that had been" +
            " building up in the Yellowstone area blew up in one of the world’s most cataclysmic volcanic " +
            "eruptions. Some 600 cubic miles (2,500 cubic km) of rock and ash were ejected, " +
            "equivalent to about 6,000 times the amount of volcanic material that was released " +
            "during the eruption of Mount Saint Helens in 1980. (Observations made in the early " +
            "21st century indicated that this single eruption actually consisted of two events about 6,000 years apart: " +
            "one very large and a second much smaller one.) Subsequent massive eruptions occurred about" +
            " 1,300,000 and 640,000 years ago—the last event (consisting in large part of lava flows) " +
            "producing about two-fifths as much material as the first one.",
    content2ImageUrl = "https://cdn.britannica.com/35/160335-050-43B9C5ED/Portion-Obsidian-Cliff-Yellowstone-National-Park-Wyoming.jpg",
    content2 = "Each of those eruptions caused the magma dome that had built up to collapse as its " +
            "contents were released, leaving an enormous caldera. The present-day Yellowstone " +
            "Caldera, the product of the third eruption, is a roughly oval-shaped basin some 30 " +
            "by 45 miles (50 by 70 km) that occupies the west-central portion of the national " +
            "park and includes the northern two-thirds of Yellowstone Lake. Two resurgent magma " +
            "domes—one just north of and the other just west of Yellowstone Lake—have been " +
            "forming in the caldera, and the western dome underlie many of the park’s " +
            "best-known hydrothermal features."
)


/**
 * Example screen to show usage of [com.jet.tts.TtsClient] with article-like content.
 * Original article [here](https://www.britannica.com/place/Yellowstone-National-Park).
 * @author Miroslav Hýbler <br>
 * created on 10.03.2025
 */
@Composable
fun ArticleExampleScreen(
    ttsClient: TtsClient = LocalTtsClient.current
) {

    val scrollState = rememberScrollState()

    val ttsState = rememberTtsState(
        utterances = listOf(
            "ArticleExampleScreen_title" to article.title,
            "ArticleExampleScreen_desc" to article.description,
            "ArticleExampleScreen_content" to article.content,
            "ArticleExampleScreen_content2" to article.content2
        )
    )
    TtsLifecycleAwareEffect(
        client = ttsClient,
        state = ttsState,
    )


    Scaffold(
        topBar = {},
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = scrollState)
                    .padding(paddingValues = paddingValues)
                    .padding(
                        top = 32.dp,
                        bottom = 128.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp)
            ) {
                TextTts(
                    utterance = ttsState["ArticleExampleScreen_title"],
                    ttsClient = ttsClient,
                    scrollableState = scrollState, //ScrollState for autoscroll feature
                    style = MaterialTheme.typography.headlineMedium,
                )

                TextTts(
                    utterance = ttsState["ArticleExampleScreen_desc"],
                    ttsClient = ttsClient,
                    scrollableState = scrollState, //ScrollState for autoscroll feature
                    style = MaterialTheme.typography.bodyLarge,
                )

                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 256.dp)
                        .clip(shape = MaterialTheme.shapes.large),
                    model = article.mainImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )


                TextTts(
                    utterance = ttsState["ArticleExampleScreen_content"],
                    ttsClient = ttsClient,
                    scrollableState = scrollState, //ScrollState for autoscroll feature
                    style = MaterialTheme.typography.bodyLarge,
                )


                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 256.dp)
                        .clip(shape = MaterialTheme.shapes.large),
                    model = article.content2ImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )

                TextTts(
                    utterance = ttsState["ArticleExampleScreen_content2"],
                    ttsClient = ttsClient,
                    scrollableState = scrollState, //ScrollState for autoscroll feature
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.navigationBarsPadding(),
                onClick = {
                    if (ttsClient.isSpeaking) {
                        ttsClient.stop()
                    } else {
                        ttsClient.flushAndSpeak(utterance = ttsState["ArticleExampleScreen_title"])
                        ttsClient.add(utterance = ttsState["ArticleExampleScreen_desc"])
                        ttsClient.add(utterance = ttsState["ArticleExampleScreen_content"])
                        ttsClient.add(utterance = ttsState["ArticleExampleScreen_content2"])
                    }
                },
            ) {
                Icon(
                    painter = painterResource(
                        id = if (ttsClient.isSpeaking)
                            R.drawable.ic_stop
                        else
                            R.drawable.ic_play,
                    ),
                    contentDescription = null,
                )
            }
        }
    )
}


@Composable
@PreviewLightDark
private fun ArticleExampleScreenPreview() {
    JetTtsExampleTheme {
        ArticleExampleScreen(
            ttsClient = rememberTtsClient(),
        )
    }
}