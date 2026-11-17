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

package com.raiyanshahid.dustaway

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer

/**
 * Lets the content of this composable turn to dust under the control of [state].
 *
 * While the content is intact it is drawn normally. When [DisintegrationState.disintegrate] runs, a
 * snapshot of the content is split into small pieces that drift away and fade. The pieces may fly
 * outside the composable's bounds, so do not clip a parent if you want them to be seen.
 *
 * The composable keeps its size and keeps receiving touches after it has turned to dust. Hide or
 * remove it yourself if that matters (see [DisintegrateVisibility]).
 */
@Composable
public fun Modifier.disintegrate(state: DisintegrationState): Modifier {
    val layer = rememberGraphicsLayer()
    DisposableEffect(state, layer) {
        state.attach(layer)
        onDispose { state.detach(layer) }
    }
    val sample = androidx.compose.runtime.remember { DustSample() }
    return this.drawWithContent {
        val particles = state.particles
        val progress = state.progress
        if (particles != null && progress > 0f) {
            if (progress < 1f) drawDust(particles, progress, sample)
        } else {
            // Draw through a layer so a snapshot is always ready when the dust is triggered.
            layer.record { this@drawWithContent.drawContent() }
            drawLayer(layer)
        }
    }
}

private fun DrawScope.drawDust(particles: DustParticles, progress: Float, sample: DustSample) {
    for (i in 0 until particles.count) {
        if (!particles.evaluate(i, progress, sample)) continue
        val argb = sample.color
        val alpha = ((argb ushr 24) / 255f) * sample.alpha
        if (alpha <= 0f) continue
        drawRect(
            color = Color(argb).copy(alpha = alpha),
            topLeft = Offset(sample.x, sample.y),
            size = Size(sample.size, sample.size),
        )
    }
}
