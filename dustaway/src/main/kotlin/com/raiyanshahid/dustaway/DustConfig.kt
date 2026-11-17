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

/** The side the disintegration starts from; the dust front sweeps across the content from there. */
public enum class SweepDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    TOP_TO_BOTTOM,
    BOTTOM_TO_TOP,

    /** No visible front: every piece starts at a random moment. */
    RANDOM,
}

/**
 * Tuning for the dust effect. Every value has a sensible default, so `DustConfig()` is a good start.
 *
 * @param durationMillis Length of the whole animation, from the first piece lifting to the last one gone.
 * @param direction Where the disintegration starts.
 * @param sweepFraction How much of the animation is spent spreading the start across the content
 *   (0 = everything goes at once, 0.9 = a slow wave with each piece flying for a short time).
 * @param maxParticles Upper bound on the number of dust pieces. The piece size is chosen so the content
 *   never produces more than this; lower it for weaker devices.
 * @param windAngleDegrees Direction the dust drifts, counter-clockwise from "right" (0 = right, 90 = up).
 * @param distanceFraction How far a piece travels, as a fraction of the content's longer side.
 * @param scatter How much the pieces fan out around the wind direction (0 = a straight stream, 1 = wide cloud).
 * @param wobble Sideways drift of the pieces as they fly, as a multiple of the piece size.
 * @param seed Seed for the random choices, so the same content gives the same dust every time.
 */
public class DustConfig(
    public val durationMillis: Int = 1800,
    public val direction: SweepDirection = SweepDirection.LEFT_TO_RIGHT,
    public val sweepFraction: Float = 0.6f,
    public val maxParticles: Int = 8000,
    public val windAngleDegrees: Float = 25f,
    public val distanceFraction: Float = 0.45f,
    public val scatter: Float = 0.35f,
    public val wobble: Float = 6f,
    public val seed: Long = 1L,
) {
    init {
        require(durationMillis > 0) { "durationMillis must be positive" }
        require(maxParticles > 0) { "maxParticles must be positive" }
        require(sweepFraction in 0f..0.9f) { "sweepFraction must be in 0..0.9" }
        require(distanceFraction >= 0f) { "distanceFraction must not be negative" }
        require(scatter in 0f..1f) { "scatter must be in 0..1" }
    }
}
