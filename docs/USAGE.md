# DustAway usage guide

## 1. The pieces

| Piece | Purpose |
| --- | --- |
| `DisintegrationState` | Holds progress (0 to 1) and runs the animation. Create it with `rememberDisintegrationState()`. |
| `Modifier.disintegrate(state)` | Draws the content normally, or as dust while the state is animating. |
| `DisintegrateVisibility` | Convenience wrapper: dust it out when `visible` turns false, remove it afterwards. |
| `DustConfig` | Look and timing of the dust. |
| `DustEngine` / `DustParticles` | The Android-free particle maths, if you want to render the dust yourself. |

## 2. Turning one thing to dust

```kotlin
val state = rememberDisintegrationState()
val scope = rememberCoroutineScope()

Box(Modifier.disintegrate(state)) { Image(...) }

Button(onClick = { scope.launch { state.disintegrate() } }) { Text("Snap") }
Button(onClick = { scope.launch { state.restore() } }) { Text("Restore") }
```

`disintegrate()` and `restore()` are `suspend` and return when the animation ends. Starting one while
the other runs reverses direction from the current point.

State you can read in composition: `state.progress`, `state.isDisintegrated`, `state.isAnimating`.

`state.snapTo(1f)` jumps straight to the end (after a first `disintegrate()` has captured the content),
and `snapTo(0f)` jumps back to intact.

## 2b. Images, cards, whole screens

The modifier captures whatever the composable draws, so it is not tied to a type:

```kotlin
Image(painter = painterResource(R.drawable.photo), contentDescription = null,
      modifier = Modifier.fillMaxWidth().disintegrate(state).clip(RoundedCornerShape(16.dp)))

Card(Modifier.disintegrate(state)) { ... }

Column(Modifier.fillMaxSize().disintegrate(screenState)) { /* the whole screen */ }
```

Notes:
- Order matters: `disintegrate(state)` first, then `clip`/`background`/`border`, so the rounded shape and
  background are part of the snapshot. Placed after them, only the inner content is captured.
- For a whole screen the bitmap is large, so pieces get bigger to stay under `maxParticles`. Try 5000-8000.
- Controls that must stay on screen (a Restore button, for example) should sit outside the container
  you dust, in a `Box` on top of it.
- Screens containing an `AndroidView` (maps, web views, video) cannot be fully captured.

## 3. Dismissing items

```kotlin
LazyColumn {
    items(tasks, key = { it.id }) { task ->
        var done by remember { mutableStateOf(false) }
        DisintegrateVisibility(visible = !done, onDisintegrated = { tasks.remove(task) }) {
            TaskCard(task, onClick = { done = true })
        }
    }
}
```

Give list items stable keys so the state stays with the right row.

## 4. Configuration

```kotlin
val state = rememberDisintegrationState(
    DustConfig(
        durationMillis = 2400,
        direction = SweepDirection.RIGHT_TO_LEFT,
        windAngleDegrees = 90f,   // dust rises
        scatter = 0.6f,
        maxParticles = 5000,
    )
)
```

`rememberDisintegrationState(config)` makes a new state when the config changes, so changing it
mid-animation starts again from intact content. Keep configs stable (`remember` them) if you build them
from other state.

## 5. Performance

- Piece count is capped by `maxParticles`; the piece size grows to stay under it. Big content gets
  bigger pieces, not more of them.
- Building the pieces runs once per `disintegrate()` on a background dispatcher. The drawing is one
  rectangle per visible piece per frame.
- If frames drop on low-end devices, try `maxParticles = 3000`.

## 6. Troubleshooting

**Nothing happens when I call `disintegrate()`.** The modifier has to have been drawn at least once so
there is something to capture, and it must be on the composable you want to dust.

**The dust is cut off.** A parent clips it. Scroll containers and `Modifier.clip` on a parent do. Move
the effect outside the clipped area or give the parent padding and `clipToPadding = false`.

**The content comes back as a flat colour or empty.** It is probably an `AndroidView` (WebView, MapView,
video surface). Those draw outside Compose and cannot be captured.

**I can still tap it after it vanished.** The composable keeps its layout; use `DisintegrateVisibility`
or remove it yourself in `onDisintegrated`.

## 7. Using the engine directly

```kotlin
val particles = DustEngine.build(argbPixels, width, height, DustConfig())
val sample = DustSample()
for (i in 0 until particles.count) {
    if (particles.evaluate(i, progress, sample)) {
        // draw a sample.size square at (sample.x, sample.y) with sample.color and sample.alpha
    }
}
```
