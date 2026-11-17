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
import org.junit.Assert.assertTrue
import org.junit.Test

class DustConfigTest {

    private fun rejects(block: () -> Unit) {
        try {
            block()
            throw AssertionError("expected IllegalArgumentException")
        } catch (expected: IllegalArgumentException) {
            // ok
        }
    }

    @Test
    fun defaultsAreValid() {
        val c = DustConfig()
        assertEquals(1800, c.durationMillis)
        assertEquals(SweepDirection.LEFT_TO_RIGHT, c.direction)
        assertTrue(c.maxParticles > 0)
    }

    @Test
    fun rejectsNonPositiveDuration() = rejects { DustConfig(durationMillis = 0) }

    @Test
    fun rejectsNonPositiveParticleCap() = rejects { DustConfig(maxParticles = 0) }

    @Test
    fun rejectsSweepOutsideRange() {
        rejects { DustConfig(sweepFraction = -0.1f) }
        rejects { DustConfig(sweepFraction = 0.95f) }
    }

    @Test
    fun rejectsScatterOutsideRange() {
        rejects { DustConfig(scatter = -0.1f) }
        rejects { DustConfig(scatter = 1.1f) }
    }

    @Test
    fun rejectsNegativeDistance() = rejects { DustConfig(distanceFraction = -1f) }

    @Test
    fun sweepFractionZeroMeansEverythingGoesAtOnce() {
        val px = IntArray(30 * 30) { 0xFF112233.toInt() }
        val p = DustEngine.build(px, 30, 30, DustConfig(sweepFraction = 0f, maxParticles = 100))
        val s = DustSample()
        // With no sweep every piece has the same timing: at the same progress they all look alike.
        p.evaluate(0, 0.5f, s)
        val alpha = s.alpha
        for (i in 1 until p.count) {
            p.evaluate(i, 0.5f, s)
            assertEquals(alpha, s.alpha, 1e-6f)
        }
    }

    @Test
    fun oneByOneImageWorks() {
        val p = DustEngine.build(intArrayOf(0xFFFFFFFF.toInt()), 1, 1)
        assertEquals(1, p.count)
    }
}
