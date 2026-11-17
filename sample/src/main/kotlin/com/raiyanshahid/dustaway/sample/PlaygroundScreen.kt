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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raiyanshahid.dustaway.DustConfig
import com.raiyanshahid.dustaway.SweepDirection
import com.raiyanshahid.dustaway.disintegrate
import com.raiyanshahid.dustaway.rememberDisintegrationState
import kotlinx.coroutines.launch

@Composable
fun PlaygroundScreen() {
    var direction by remember { mutableStateOf(SweepDirection.LEFT_TO_RIGHT) }
    var windAngle by remember { mutableFloatStateOf(25f) }
    var particles by remember { mutableFloatStateOf(0.5f) }

    val config = remember(direction, windAngle, particles) {
        DustConfig(
            direction = direction,
            windAngleDegrees = windAngle,
            maxParticles = (2_000 + 10_000 * particles).toInt(),
        )
    }
    val state = rememberDisintegrationState(config)
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Do not put this inside a scrolling parent: scroll containers clip, and the dust would be cut off.
        Box(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .disintegrate(state)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF6A11CB), Color(0xFF2575FC), Color(0xFF00C9A7)))),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DustAway", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text("Tap Snap", color = Color.White.copy(alpha = 0.85f), fontSize = 16.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { scope.launch { state.disintegrate() } }) { Text("Snap") }
            OutlinedButton(onClick = { scope.launch { state.restore() } }) { Text("Restore") }
        }

        Text("Starts from", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                SweepDirection.LEFT_TO_RIGHT to "Left",
                SweepDirection.RIGHT_TO_LEFT to "Right",
                SweepDirection.TOP_TO_BOTTOM to "Top",
                SweepDirection.RANDOM to "Random",
            ).forEach { (d, label) ->
                FilterChip(selected = direction == d, onClick = { direction = d }, label = { Text(label) })
            }
        }

        Text("Wind direction: ${windAngle.toInt()}°", style = MaterialTheme.typography.labelLarge)
        Slider(value = windAngle, onValueChange = { windAngle = it }, valueRange = 0f..180f)

        Text("Detail: up to ${(2_000 + 10_000 * particles).toInt()} pieces", style = MaterialTheme.typography.labelLarge)
        Slider(value = particles, onValueChange = { particles = it })
        Spacer(Modifier.height(8.dp))
    }
}
