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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.layer.GraphicsLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Drives one disintegration: call [disintegrate] to turn the content into dust and [restore] to
 * bring it back. Attach it to a composable with [disintegrate] (the modifier).
 *
 * The functions are `suspend`, so call them from a coroutine (for example `rememberCoroutineScope`
 * or `LaunchedEffect`) and they return when the animation has finished.
 */
@Stable
public class DisintegrationState(private val config: DustConfig = DustConfig()) {

    private val animatable = Animatable(0f)
    private var layer: GraphicsLayer? = null

    /** The dust for the current run, or null when the content is intact. */
    internal var particles: DustParticles? by mutableStateOf(null)
        private set

    /** 0 = content intact, 1 = content fully turned to dust. Observable from composition. */
    public val progress: Float get() = animatable.value

    /** True once the content has completely disappeared. */
    public val isDisintegrated: Boolean get() = progress >= 1f

    /** True while an animation is running. */
    public val isAnimating: Boolean get() = animatable.isRunning

    internal fun attach(layer: GraphicsLayer) {
        this.layer = layer
    }

    internal fun detach(layer: GraphicsLayer) {
        if (this.layer === layer) this.layer = null
    }

    /**
     * Turns the content into dust. Does nothing if it is already gone, or if the modifier has not been
     * drawn yet (there is nothing to snapshot).
     */
    public suspend fun disintegrate() {
        if (progress >= 1f) return
        if (particles == null || progress == 0f) {
            particles = snapshot() ?: return
        }
        animatable.animateTo(1f, tween(durationFor(progress, 1f), easing = LinearEasing))
    }

    /** Brings the content back by playing the dust in reverse. */
    public suspend fun restore() {
        if (progress <= 0f) {
            particles = null
            return
        }
        animatable.animateTo(0f, tween(durationFor(progress, 0f), easing = LinearEasing))
        particles = null
    }

    /** Jumps straight to [progress] without animating. Needs a prior [disintegrate] unless it is 0. */
    public suspend fun snapTo(progress: Float) {
        val target = progress.coerceIn(0f, 1f)
        if (target > 0f && particles == null) particles = snapshot() ?: return
        animatable.snapTo(target)
        if (target == 0f) particles = null
    }

    /** A partial run takes proportionally less time, so speed stays constant if interrupted. */
    private fun durationFor(from: Float, to: Float): Int =
        (config.durationMillis * kotlin.math.abs(to - from)).toInt().coerceAtLeast(1)

    private suspend fun snapshot(): DustParticles? {
        val layer = layer ?: return null
        val bitmap = layer.toImageBitmap()
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return null
        val pixels = IntArray(width * height)
        bitmap.readPixels(pixels, 0, 0, width, height)
        return withContext(Dispatchers.Default) { DustEngine.build(pixels, width, height, config) }
    }
}

/** Creates and remembers a [DisintegrationState] with the given [config]. */
@Composable
public fun rememberDisintegrationState(config: DustConfig = DustConfig()): DisintegrationState =
    remember(config) { DisintegrationState(config) }
