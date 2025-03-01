package com.jet.tts.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jet.tts.example.examples.MultipleTextsExampleScreen
import com.jet.tts.example.examples.ScrollExampleScreen
import com.jet.tts.example.examples.SingleTextExampleScreen


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
            JetttsTheme {

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable(route = "home") {
                        HomeScreen(navController = navController)
                    }

                    composable(route = "single") {
                        SingleTextExampleScreen()
                    }
                    composable(route = "multiple") {
                        MultipleTextsExampleScreen()
                    }
                    composable(route = "scroll") {
                        ScrollExampleScreen()
                    }
                }
            }
        }
    }
}
