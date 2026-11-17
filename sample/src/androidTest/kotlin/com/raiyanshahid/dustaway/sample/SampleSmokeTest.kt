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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Walks through every sample screen. Run with `./gradlew :sample:connectedDebugAndroidTest`. */
class SampleSmokeTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun count(text: String) = rule.onAllNodesWithText(text).fetchSemanticsNodes().size

    @Test
    fun playgroundShowsItsControls() {
        rule.onNodeWithText("DustAway").assertIsDisplayed()
        rule.onNodeWithText("Snap").assertIsDisplayed()
        rule.onNodeWithText("Starts from").assertIsDisplayed()
    }

    @Test
    fun playgroundSnapAndRestoreDoNotCrash() {
        rule.onNodeWithText("Snap").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Restore").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("DustAway").assertIsDisplayed()
    }

    @Test
    fun anythingTabHasImageCardAndScreenControls() {
        rule.onNodeWithText("Anything").performClick()
        rule.onNodeWithText("Snap image").assertIsDisplayed()
        rule.onNodeWithText("Snap card").assertIsDisplayed()
        rule.onNodeWithText("Snap screen").assertIsDisplayed()
    }

    @Test
    fun snappingTheImageAndCardDoesNotCrash() {
        rule.onNodeWithText("Anything").performClick()
        rule.onNodeWithText("Snap image").performClick()
        rule.onNodeWithText("Snap card").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Restore").assertExists()
    }

    @Test
    fun snapScreenAndRestoreKeepTheAppAlive() {
        rule.onNodeWithText("Snap screen").performClick()
        rule.waitForIdle()
        rule.onAllNodesWithText("Restore")[0].assertExists()
    }

    @Test
    fun tappingATaskRemovesItFromTheList() {
        rule.onNodeWithText("Dismiss list").performClick()
        val before = count("Water the plants")
        assertEquals(1, before)
        rule.onNodeWithText("Water the plants").performClick()
        rule.waitUntil(timeoutMillis = 10_000) { count("Water the plants") == 0 }
    }
}
