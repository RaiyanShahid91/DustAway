/*
 * Copyright 2026 Raiyan Shahid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.raiyanshahid.dustaway.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raiyanshahid.dustaway.DustConfig
import com.raiyanshahid.dustaway.SweepDirection
import com.raiyanshahid.dustaway.disintegrate
import com.raiyanshahid.dustaway.rememberDisintegrationState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    var tab by remember { mutableIntStateOf(0) }
                    val scope = rememberCoroutineScope()
                    // Fewer, larger pieces: a whole screen is a big bitmap.
                    val screenState = rememberDisintegrationState(
                        DustConfig(maxParticles = 6000, direction = SweepDirection.LEFT_TO_RIGHT, durationMillis = 2400),
                    )

                    Box(Modifier.fillMaxSize()) {
                        // The whole screen content is dusted, the buttons below are not part of it.
                        Column(Modifier.fillMaxSize().disintegrate(screenState).statusBarsPadding()) {
                            TabRow(selectedTabIndex = tab) {
                                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Playground") })
                                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Anything") })
                                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Dismiss list") })
                            }
                            when (tab) {
                                0 -> PlaygroundScreen()
                                1 -> AnythingScreen()
                                else -> DismissListScreen()
                            }
                        }
                        Row(
                            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(onClick = { scope.launch { screenState.disintegrate() } }) { Text("Snap screen") }
                            OutlinedButton(onClick = { scope.launch { screenState.restore() } }) { Text("Restore") }
                        }
                    }
                }
            }
        }
    }
}
