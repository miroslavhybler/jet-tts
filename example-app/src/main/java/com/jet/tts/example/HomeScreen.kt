@file:OptIn(ExperimentalMaterial3Api::class)

package com.jet.tts.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


/**
 * Simple home screen with navigation buttons to navigate to example screens
 * @author Miroslav Hýbler <br>
 * created on 06.02.2025
 */
@Composable
fun HomeScreen(
    navController: NavController,
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Jet Tts Example app") },
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Button(
                    onClick = {
                        navController.navigate(route = "single")
                    }
                ) {
                    Text(text = "Single text example")
                }

                Button(
                    onClick = {
                        navController.navigate(route = "multiple")
                    }
                ) {
                    Text(text = "Multiple texts example")
                }

                Button(
                    onClick = {
                        navController.navigate(route = "scroll")
                    }
                ) {
                    Text(text = "Scroll example")
                }

                Button(
                    onClick = {
                        navController.navigate(route = "toggle")
                    }
                ) {
                    Text(text = "Toggle highlight mode example")
                }

                Button(
                    onClick = {
                        navController.navigate(route = "article")
                    }
                ) {
                    Text(text = "Article example")
                }

                Button(
                    onClick = {
                        navController.navigate(route = "annotated")
                    }
                ) {
                    Text(text = "Original string annotated example")
                }

                Button(
                    onClick = {
                        navController.navigate(route = "lazy")
                    }
                ) {
                    Text(text = "LazyColumn example")
                }
            }
        },
    )

}