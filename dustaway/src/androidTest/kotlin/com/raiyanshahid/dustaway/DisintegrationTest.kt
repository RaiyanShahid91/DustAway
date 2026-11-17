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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Runs on a device or emulator: `./gradlew :dustaway:connectedDebugAndroidTest`. */
class DisintegrationTest {

    @get:Rule
    val rule = createComposeRule()

    private val fast = DustConfig(durationMillis = 200, maxParticles = 500)

    private lateinit var state: DisintegrationState
    private lateinit var scope: CoroutineScope

    private fun setBox() {
        rule.setContent {
            state = rememberDisintegrationState(fast)
            scope = rememberCoroutineScope()
            Box(
                Modifier
                    .size(120.dp)
                    .testTag("box")
                    .disintegrate(state)
                    .background(Color.Red),
            )
        }
        rule.waitForIdle()
    }

    @Test
    fun startsIntact() {
        setBox()
        assertEquals(0f, state.progress, 0f)
        assertFalse(state.isDisintegrated)
        rule.onNodeWithTag("box").assertIsDisplayed()
    }

    @Test
    fun disintegrateRunsToTheEnd() {
        setBox()
        scope.launch { state.disintegrate() }
        rule.waitUntil(timeoutMillis = 5_000) { state.isDisintegrated }
        assertEquals(1f, state.progress, 0f)
    }

    @Test
    fun restoreBringsTheContentBack() {
        setBox()
        scope.launch { state.disintegrate() }
        rule.waitUntil(timeoutMillis = 5_000) { state.isDisintegrated }
        scope.launch { state.restore() }
        rule.waitUntil(timeoutMillis = 5_000) { state.progress == 0f && !state.isAnimating }
        assertFalse(state.isDisintegrated)
    }

    @Test
    fun snapToJumpsWithoutAnimating() {
        setBox()
        scope.launch { state.disintegrate() }
        rule.waitUntil(timeoutMillis = 5_000) { state.isDisintegrated }
        scope.launch { state.snapTo(0f) }
        rule.waitUntil(timeoutMillis = 5_000) { state.progress == 0f }
        assertFalse(state.isAnimating)
    }

    @Test
    fun disintegrateIsANoOpWhenAlreadyGone() {
        setBox()
        scope.launch { state.disintegrate() }
        rule.waitUntil(timeoutMillis = 5_000) { state.isDisintegrated }
        scope.launch { state.disintegrate() }
        rule.waitForIdle()
        assertTrue(state.isDisintegrated)
    }

    @Test
    fun visibilityWrapperRemovesContentAndReportsIt() {
        var visible by mutableStateOf(true)
        var reported = false
        rule.setContent {
            Column {
                DisintegrateVisibility(
                    visible = visible,
                    state = rememberDisintegrationState(fast),
                    onDisintegrated = { reported = true },
                ) {
                    Text("hello dust")
                }
            }
        }
        rule.onNodeWithText("hello dust").assertIsDisplayed()

        visible = false
        rule.waitUntil(timeoutMillis = 5_000) { reported }
        rule.onNodeWithText("hello dust").assertDoesNotExist()
    }

    @Test
    fun visibilityWrapperBringsContentBack() {
        var visible by mutableStateOf(true)
        var reported = false
        rule.setContent {
            DisintegrateVisibility(
                visible = visible,
                state = rememberDisintegrationState(fast),
                onDisintegrated = { reported = true },
            ) {
                Text("hello dust")
            }
        }
        visible = false
        rule.waitUntil(timeoutMillis = 5_000) { reported }
        visible = true
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithTextCount("hello dust") > 0
        }
        rule.onNodeWithText("hello dust").assertIsDisplayed()
    }
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextCount(text: String): Int =
    onAllNodesWithText(text).fetchSemanticsNodes().size
