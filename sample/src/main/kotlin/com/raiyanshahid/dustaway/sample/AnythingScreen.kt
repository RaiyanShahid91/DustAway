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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.raiyanshahid.dustaway.DustConfig
import com.raiyanshahid.dustaway.SweepDirection
import com.raiyanshahid.dustaway.disintegrate
import com.raiyanshahid.dustaway.rememberDisintegrationState
import kotlinx.coroutines.launch

/** The same modifier works on an image, a card, or anything else. */
@Composable
fun AnythingScreen() {
    val scope = rememberCoroutineScope()
    val imageState = rememberDisintegrationState(DustConfig(direction = SweepDirection.TOP_TO_BOTTOM, windAngleDegrees = 80f))
    val cardState = rememberDisintegrationState(DustConfig(direction = SweepDirection.RIGHT_TO_LEFT, windAngleDegrees = 160f))

    // A generated picture stands in for a photo; any Image(painter = ...) works the same way.
    val picture = remember {
        val bmp = Bitmap.createBitmap(600, 340, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val sky = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, 340f, 0xFFFF9A44.toInt(), 0xFF3A1C71.toInt(), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, 600f, 340f, sky)
        canvas.drawCircle(430f, 130f, 60f, Paint().apply { color = 0xFFFFE08A.toInt() })
        canvas.drawCircle(150f, 380f, 220f, Paint().apply { color = 0xFF1B1035.toInt() })
        canvas.drawCircle(470f, 400f, 230f, Paint().apply { color = 0xFF2A174F.toInt() })
        bmp.asImageBitmap()
    }

    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Image(
            bitmap = picture,
            contentDescription = "Sunset picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .disintegrate(imageState)
                .clip(RoundedCornerShape(20.dp)),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { scope.launch { imageState.disintegrate() } }) { Text("Snap image") }
            OutlinedButton(onClick = { scope.launch { imageState.restore() } }) { Text("Restore") }
        }

        Card(Modifier.fillMaxWidth().disintegrate(cardState)) {
            Text("This is a Card. Any composable can crumble.", Modifier.padding(28.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { scope.launch { cardState.disintegrate() } }) { Text("Snap card") }
            OutlinedButton(onClick = { scope.launch { cardState.restore() } }) { Text("Restore") }
        }

        Text("The buttons at the bottom of the screen snap the whole screen.")
    }
}
