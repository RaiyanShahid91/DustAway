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
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Turns a bitmap's pixels into [DustParticles]. Pure Kotlin, no Android dependencies. */
public object DustEngine {

    /** Cells whose average alpha is below this (0..255) are treated as empty and produce no dust. */
    private const val MIN_CELL_ALPHA = 12f

    /** Picks the smallest cell size that keeps the piece count at or under [maxParticles]. */
    public fun cellSizeFor(width: Int, height: Int, maxParticles: Int): Int {
        require(width > 0 && height > 0) { "size must be positive" }
        require(maxParticles > 0) { "maxParticles must be positive" }
        var cell = max(1, ceil(sqrt(width.toDouble() * height / maxParticles)).toInt())
        // Rounding up the grid can still overshoot slightly; grow until it fits.
        while (cellsAcross(width, cell).toLong() * cellsAcross(height, cell) > maxParticles) cell++
        return cell
    }

    private fun cellsAcross(length: Int, cell: Int) = (length + cell - 1) / cell

    /**
     * Builds the dust for a [width] x [height] image.
     *
     * @param pixels non-premultiplied ARGB pixels, row by row, at least `width * height` long.
     */
    public fun build(pixels: IntArray, width: Int, height: Int, config: DustConfig = DustConfig()): DustParticles {
        require(width > 0 && height > 0) { "size must be positive" }
        require(pixels.size >= width * height) { "pixels is too small for ${width}x$height" }

        val cell = cellSizeFor(width, height, config.maxParticles)
        val cols = cellsAcross(width, cell)
        val rows = cellsAcross(height, cell)
        val capacity = cols * rows

        val ox = FloatArray(capacity)
        val oy = FloatArray(capacity)
        val col = IntArray(capacity)
        val delay = FloatArray(capacity)
        val dxs = FloatArray(capacity)
        val dys = FloatArray(capacity)
        val wamp = FloatArray(capacity)
        val wfreq = FloatArray(capacity)

        val random = Random(config.seed)
        val longSide = max(width, height).toFloat()
        var n = 0

        for (cy in 0 until rows) {
            for (cx in 0 until cols) {
                val x0 = cx * cell
                val y0 = cy * cell
                val x1 = minOf(x0 + cell, width)
                val y1 = minOf(y0 + cell, height)

                // Alpha-weighted average colour, so see-through pixels don't wash the colour out.
                var sumA = 0L
                var sumR = 0L
                var sumG = 0L
                var sumB = 0L
                for (y in y0 until y1) {
                    val row = y * width
                    for (x in x0 until x1) {
                        val p = pixels[row + x]
                        val a = p ushr 24
                        sumA += a
                        sumR += ((p shr 16) and 0xFF) * a
                        sumG += ((p shr 8) and 0xFF) * a
                        sumB += (p and 0xFF) * a
                    }
                }
                val area = (x1 - x0) * (y1 - y0)
                val avgA = sumA.toFloat() / area
                if (avgA < MIN_CELL_ALPHA || sumA == 0L) continue

                val r = (sumR / sumA).toInt()
                val g = (sumG / sumA).toInt()
                val b = (sumB / sumA).toInt()
                col[n] = (avgA.toInt().coerceIn(0, 255) shl 24) or (r shl 16) or (g shl 8) or b
                ox[n] = x0.toFloat()
                oy[n] = y0.toFloat()

                // Where this cell sits along the sweep, 0..1, plus a little noise so the front is ragged.
                val u = when (config.direction) {
                    SweepDirection.LEFT_TO_RIGHT -> (x0 + cell / 2f) / width
                    SweepDirection.RIGHT_TO_LEFT -> 1f - (x0 + cell / 2f) / width
                    SweepDirection.TOP_TO_BOTTOM -> (y0 + cell / 2f) / height
                    SweepDirection.BOTTOM_TO_TOP -> 1f - (y0 + cell / 2f) / height
                    SweepDirection.RANDOM -> random.nextFloat()
                }.coerceIn(0f, 1f)
                val noise = random.nextFloat()
                delay[n] = (config.sweepFraction * (0.75f * u + 0.25f * noise)).coerceIn(0f, config.sweepFraction)

                // Drift: mostly along the wind, fanned out by scatter.
                val spread = (random.nextFloat() - 0.5f) * 2f * config.scatter * 90f
                val angle = (config.windAngleDegrees + spread) * (PI.toFloat() / 180f)
                val dist = config.distanceFraction * longSide * (0.4f + 0.6f * random.nextFloat())
                dxs[n] = cos(angle) * dist
                dys[n] = -sin(angle) * dist // screen y grows downwards, "up" is negative

                wamp[n] = config.wobble * cell * (random.nextFloat() - 0.5f) * 2f
                wfreq[n] = 0.5f + 1.5f * random.nextFloat()
                n++
            }
        }

        return DustParticles(
            count = n,
            cellSize = cell,
            originX = ox.copyOf(n),
            originY = oy.copyOf(n),
            colors = col.copyOf(n),
            delay = delay.copyOf(n),
            driftX = dxs.copyOf(n),
            driftY = dys.copyOf(n),
            wobbleAmp = wamp.copyOf(n),
            wobbleFreq = wfreq.copyOf(n),
            sweepFraction = config.sweepFraction,
        )
    }
}
