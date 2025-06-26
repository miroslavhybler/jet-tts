@file:OptIn(ExperimentalMaterial3Api::class)

package com.jet.tts.example.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jet.tts.TextTts
import com.jet.tts.TtsLifecycleAwareEffect
import com.jet.tts.example.LocalTtsClient
import com.jet.tts.example.R
import com.jet.tts.rememberTtsState


/**
 * @author Miroslav Hýbler <br>
 * created on 06.05.2025
 */
@Composable
fun LazyColumnExampleScreen() {
    val ttsClient = LocalTtsClient.current

    val ttsState = rememberTtsState(
        utterances = listOf(
            "LazyColumnExampleScreen_title" to article.title,
            "LazyColumnExampleScreen_desc" to article.description,
            "LazyColumnExampleScreen_content" to article.content,
            "LazyColumnExampleScreen_content2" to article.content2,
        )
    )

    TtsLifecycleAwareEffect(
        client = ttsClient,
        state = ttsState,
    )

    val lazyListState = rememberLazyListState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "LazyColumn Example") }
            )
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding)
                    .padding(horizontal = 16.dp),
                state = lazyListState,
            ) {
                item {
                    TextTts(
                        text = article.title,
                        ttsClient = ttsClient,
                        scrollableState = lazyListState, //ScrollState for autoscroll feature
                        utteranceId = "LazyColumnExampleScreen_title",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }

                item {
                    TextTts(
                        text = article.description,
                        ttsClient = ttsClient,
                        scrollableState = lazyListState, //ScrollState for autoscroll feature
                        utteranceId = "LazyColumnExampleScreen_desc",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                item {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height = 256.dp)
                            .clip(shape = MaterialTheme.shapes.large),
                        model = article.mainImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
                item {
                    TextTts(
                        text = article.content,
                        ttsClient = ttsClient,
                        scrollableState = lazyListState, //ScrollState for autoscroll feature
                        utteranceId = "LazyColumnExampleScreen_content",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                item {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height = 256.dp)
                            .clip(shape = MaterialTheme.shapes.large),
                        model = article.content2ImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }

                item {
                    TextTts(
                        text = article.content2,
                        ttsClient = ttsClient,
                        scrollableState = lazyListState, //ScrollState for autoscroll feature
                        utteranceId = "LazyColumnExampleScreen_content2",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.navigationBarsPadding(),
                onClick = {
                    if (ttsClient.isSpeaking) {
                        ttsClient.stop()
                    } else {
                        ttsClient.flushAndSpeak(
                            text = article.title,
                            utteranceId = "LazyColumnExampleScreen_title",
                        )
                        ttsClient.add(
                            text = article.description,
                            utteranceId = "LazyColumnExampleScreen_desc"
                        )
                        ttsClient.add(
                            text = article.content,
                            utteranceId = "LazyColumnExampleScreen_content"
                        )
                        ttsClient.add(
                            text = article.content2,
                            utteranceId = "LazyColumnExampleScreen_content2"
                        )
                    }
                },
            ) {
                Icon(
                    painter = painterResource(
                        id = if (ttsClient.isSpeaking)
                            R.drawable.ic_stop
                        else
                            R.drawable.ic_play
                    ),
                    contentDescription = null,
                )
            }
        }
    )
}


