package com.jet.tts.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jet.tts.TtsClient
import com.jet.tts.example.examples.AnnotatedStringExampleScreen
import com.jet.tts.example.examples.ArticleExampleScreen
import com.jet.tts.example.examples.LazyColumnExampleScreen
import com.jet.tts.example.examples.MultipleTextsExampleScreen
import com.jet.tts.example.examples.ScrollExampleScreen
import com.jet.tts.example.examples.SingleTextExampleScreen
import com.jet.tts.example.examples.ToggleHighlightModeScreen
import com.jet.tts.rememberTtsClient
import java.util.Locale
import kotlin.random.Random


/**
 * @author Miroslav Hýbler <br>
 * created on 04.02.2025
 */
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val ttsClient = rememberTtsClient(
                highlightMode = TtsClient.HighlightMode.SPOKEN_RANGE_FROM_BEGINNING_INCLUDING_PREVIOUS_UTTERANCES,
                onInitialized = { ttsClient ->
                    ttsClient.setLanguage(language = Locale.US)
                },
                isUsingResume = true,
            )

            JetTtsExampleTheme {
                CompositionLocalProvider(
                    LocalTtsClient provides ttsClient
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable(route = "home") {
                            HomeScreen(navController = navController)
                        }

                        composable(route = "single") {
                            SingleTextExampleScreen( )
                        }
                        composable(route = "multiple") {
                            MultipleTextsExampleScreen()
                        }
                        composable(route = "scroll") {
                            ScrollExampleScreen()
                        }
                        composable(route = "toggle") {
                            ToggleHighlightModeScreen()
                        }
                        composable(route = "article") {
                            ArticleExampleScreen()
                        }
                        composable(route = "annotated") {
                            AnnotatedStringExampleScreen()
                        }
                        composable(route = "lazy") {
                            LazyColumnExampleScreen()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Because [TtsClient] is using context, it's recommended to use only one instance of [TtsClient]
 * in your application.
 */
val LocalTtsClient: ProvidableCompositionLocal<TtsClient> = compositionLocalOf(
    defaultFactory = {
        error(message = "LocalTtsClient can be used only insde MainActivity's content")
    }
)