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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DustEngineTest {

    private fun solid(w: Int, h: Int, argb: Int = 0xFF3366CC.toInt()) = IntArray(w * h) { argb }

    private fun all(p: DustParticles, progress: Float): List<DustSample> {
        val out = ArrayList<DustSample>()
        for (i in 0 until p.count) {
            val s = DustSample()
            if (p.evaluate(i, progress, s)) out += s
        }
        return out
    }

    @Test
    fun cellSizeKeepsParticleCountUnderTheCap() {
        for ((w, h, cap) in listOf(Triple(1080, 1920, 8000), Triple(300, 200, 5000), Triple(7, 5, 100), Triple(1, 1, 1))) {
            val cell = DustEngine.cellSizeFor(w, h, cap)
            val count = ((w + cell - 1) / cell).toLong() * ((h + cell - 1) / cell)
            assertTrue("${w}x$h cap $cap gave $count cells of $cell", count <= cap)
        }
    }

    @Test
    fun solidImageProducesOneParticlePerCell() {
        val p = DustEngine.build(solid(100, 100), 100, 100, DustConfig(maxParticles = 625))
        assertEquals(4, p.cellSize)
        assertEquals(625, p.count)
    }

    @Test
    fun transparentAreasMakeNoDust() {
        val px = IntArray(40 * 40) // fully transparent
        for (y in 0 until 40) for (x in 0 until 20) px[y * 40 + x] = 0xFFFF0000.toInt()
        val p = DustEngine.build(px, 40, 40, DustConfig(maxParticles = 400)) // cell size 2
        assertEquals(200, p.count)
        val seen = all(p, 0f)
        assertTrue(seen.all { it.x < 20f })
    }

    @Test
    fun atZeroProgressEveryPieceSitsOnItsOwnCellAndIsOpaque() {
        val p = DustEngine.build(solid(64, 64), 64, 64, DustConfig(maxParticles = 256))
        val pieces = all(p, 0f)
        assertEquals(p.count, pieces.size)
        for (s in pieces) {
            assertEquals(1f, s.alpha, 0f)
            assertEquals(p.cellSize.toFloat(), s.size, 0f)
            assertEquals(0f, s.x % p.cellSize, 0f)
            assertEquals(0f, s.y % p.cellSize, 0f)
        }
    }

    @Test
    fun atFullProgressNothingIsLeft() {
        val p = DustEngine.build(solid(80, 60), 80, 60)
        assertTrue(all(p, 1f).isEmpty())
    }

    @Test
    fun sweepStartsOnTheChosenSide() {
        fun leftmostGone(direction: SweepDirection): Pair<Float, Float> {
            val cfg = DustConfig(direction = direction, maxParticles = 1600, sweepFraction = 0.8f)
            val p = DustEngine.build(solid(200, 200), 200, 200, cfg)
            val moved = ArrayList<Float>()
            val still = ArrayList<Float>()
            val s = DustSample()
            val cell = p.cellSize
            for (i in 0 until p.count) {
                p.evaluate(i, 0.25f, s)
                // A piece is "started" once it has faded or shrunk at all.
                val started = s.alpha < 1f
                val originX = (i % (200 / cell)) * cell // row-major, one particle per cell
                if (started) moved += originX.toFloat() else still += originX.toFloat()
            }
            return Pair(moved.average().toFloat(), still.average().toFloat())
        }
        val (movedLtr, stillLtr) = leftmostGone(SweepDirection.LEFT_TO_RIGHT)
        assertTrue("LTR moved avg x $movedLtr should be left of still $stillLtr", movedLtr < stillLtr)
        val (movedRtl, stillRtl) = leftmostGone(SweepDirection.RIGHT_TO_LEFT)
        assertTrue("RTL moved avg x $movedRtl should be right of still $stillRtl", movedRtl > stillRtl)
    }

    @Test
    fun piecesFadeAndShrinkMonotonically() {
        val p = DustEngine.build(solid(60, 60), 60, 60, DustConfig(maxParticles = 400))
        val s = DustSample()
        for (i in 0 until p.count step 7) {
            var lastAlpha = 2f
            var lastSize = Float.MAX_VALUE
            var p0 = 0f
            while (p0 <= 1f) {
                if (p.evaluate(i, p0, s)) {
                    assertTrue(s.alpha <= lastAlpha + 1e-6f)
                    assertTrue(s.size <= lastSize + 1e-6f)
                    lastAlpha = s.alpha
                    lastSize = s.size
                }
                p0 += 0.02f
            }
        }
    }

    @Test
    fun windPushesDustTheWayItPoints() {
        val right = DustEngine.build(solid(100, 100), 100, 100, DustConfig(windAngleDegrees = 0f, scatter = 0f, wobble = 0f))
        val up = DustEngine.build(solid(100, 100), 100, 100, DustConfig(windAngleDegrees = 90f, scatter = 0f, wobble = 0f))
        val a = all(right, 0.9f)
        val b = all(up, 0.9f)
        // Compare against rest position: with no wobble the shift is purely the drift.
        fun meanShift(p: DustParticles, axisX: Boolean): Float {
            val rest = DustSample()
            val now = DustSample()
            var sum = 0f
            var k = 0
            for (i in 0 until p.count) {
                p.evaluate(i, 0f, rest)
                if (p.evaluate(i, 0.9f, now)) {
                    sum += if (axisX) now.x - rest.x else now.y - rest.y
                    k++
                }
            }
            return sum / k
        }
        assertTrue(a.isNotEmpty() && b.isNotEmpty())
        assertTrue("wind right should move +x", meanShift(right, true) > 1f)
        assertTrue("wind up should move -y", meanShift(up, false) < -1f)
    }

    @Test
    fun sameSeedGivesSameDustAndADifferentSeedDoesNot() {
        val px = solid(50, 50)
        fun snapshot(seed: Long): List<Float> {
            val p = DustEngine.build(px, 50, 50, DustConfig(seed = seed, maxParticles = 300))
            return all(p, 0.6f).flatMap { listOf(it.x, it.y) }
        }
        assertEquals(snapshot(5), snapshot(5))
        assertFalse(snapshot(5) == snapshot(6))
    }

    @Test
    fun colourIsKeptAndIgnoresSeeThroughPixels() {
        // Half red opaque, half transparent black inside every 2x2 cell: colour must stay red, not darken.
        val w = 40
        val h = 40
        val px = IntArray(w * h) { i -> if ((i % w) % 2 == 0) 0xFFFF0000.toInt() else 0x00000000 }
        val p = DustEngine.build(px, w, h, DustConfig(maxParticles = 400)) // cell 2
        val s = DustSample()
        assertTrue(p.count > 0)
        for (i in 0 until p.count) {
            p.evaluate(i, 0f, s)
            assertEquals(0xFF, (s.color shr 16) and 0xFF)
            assertEquals(0, (s.color shr 8) and 0xFF)
            assertEquals(0, s.color and 0xFF)
        }
    }

    @Test
    fun evaluatingIsPureSoRestoreReplaysTheSameFramesBackwards() {
        val p = DustEngine.build(solid(40, 40), 40, 40, DustConfig(maxParticles = 100))
        val first = DustSample()
        val again = DustSample()
        for (i in 0 until p.count) {
            p.evaluate(i, 0.37f, first)
            val x = first.x
            val alpha = first.alpha
            // Jump around in time, then come back: the frame must be identical.
            p.evaluate(i, 0.9f, again)
            p.evaluate(i, 0.05f, again)
            p.evaluate(i, 0.37f, again)
            assertEquals(x, again.x, 0f)
            assertEquals(alpha, again.alpha, 0f)
        }
    }

    @Test
    fun rejectsPixelBufferThatIsTooSmall() {
        try {
            DustEngine.build(IntArray(10), 10, 10)
            throw AssertionError("expected IllegalArgumentException")
        } catch (expected: IllegalArgumentException) {
            // ok
        }
    }
}
