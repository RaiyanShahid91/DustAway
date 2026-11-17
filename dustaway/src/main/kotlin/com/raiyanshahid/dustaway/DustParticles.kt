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

import kotlin.math.PI
import kotlin.math.sin

/** One piece of dust at one moment of the animation. Reused between calls to avoid allocation. */
public class DustSample {
    /** Left edge of the piece, in pixels of the content. */
    public var x: Float = 0f

    /** Top edge of the piece, in pixels of the content. */
    public var y: Float = 0f

    /** Side length of the square piece, in pixels. */
    public var size: Float = 0f

    /** Opacity multiplier, 0..1, to apply on top of [color]'s own alpha. */
    public var alpha: Float = 0f

    /** The piece colour as non-premultiplied ARGB. */
    public var color: Int = 0
}

/**
 * The dust made from one snapshot of the content: where every piece starts, its colour, when it lifts
 * off and where it ends up. Immutable after construction; [evaluate] is a pure function of progress,
 * so the same object plays forwards (disintegrate) and backwards (restore).
 */
public class DustParticles internal constructor(
    /** Number of pieces. */
    public val count: Int,
    /** Side length of every piece in pixels at rest. */
    public val cellSize: Int,
    private val originX: FloatArray,
    private val originY: FloatArray,
    private val colors: IntArray,
    private val delay: FloatArray,
    private val driftX: FloatArray,
    private val driftY: FloatArray,
    private val wobbleAmp: FloatArray,
    private val wobbleFreq: FloatArray,
    private val sweepFraction: Float,
) {
    /**
     * Fills [out] with piece [index] at [progress] (0 = intact content, 1 = everything gone).
     *
     * @return false when the piece is invisible at this progress and should be skipped.
     */
    public fun evaluate(index: Int, progress: Float, out: DustSample): Boolean {
        val span = 1f - sweepFraction
        val t = ((progress - delay[index]) / span).coerceIn(0f, 1f)
        if (t >= 1f) return false
        val ease = t * t
        val dx = driftX[index]
        val dy = driftY[index]
        // Sideways wobble, perpendicular to the drift direction.
        val len = kotlin.math.sqrt(dx * dx + dy * dy)
        val nx = if (len > 0f) -dy / len else 0f
        val ny = if (len > 0f) dx / len else 0f
        val w = wobbleAmp[index] * sin(t * wobbleFreq[index] * PI.toFloat())
        out.x = originX[index] + dx * ease + nx * w
        out.y = originY[index] + dy * ease + ny * w
        out.size = cellSize * (1f - 0.6f * t)
        out.alpha = 1f - t
        out.color = colors[index]
        return true
    }
}
