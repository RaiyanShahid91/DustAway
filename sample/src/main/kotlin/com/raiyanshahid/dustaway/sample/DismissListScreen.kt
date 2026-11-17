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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raiyanshahid.dustaway.DisintegrateVisibility

private val allTasks = listOf(
    "Pay the electricity bill",
    "Book a dentist appointment",
    "Reply to the landlord",
    "Renew the car insurance",
    "Water the plants",
    "Call mum",
)

/** Tap a card and it turns to dust, then leaves the list. */
@Composable
fun DismissListScreen() {
    val tasks = remember { allTasks.toMutableStateList() }

    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tap a task to finish it.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tasks, key = { it }) { task ->
                var done by remember { mutableStateOf(false) }
                DisintegrateVisibility(
                    visible = !done,
                    onDisintegrated = { tasks.remove(task) },
                ) {
                    Card(Modifier.fillMaxWidth().clickable { done = true }) {
                        Text(task, Modifier.padding(20.dp))
                    }
                }
            }
        }
        if (tasks.isEmpty()) {
            Button(onClick = { tasks.addAll(allTasks) }) { Text("Bring them back") }
        }
    }
}
