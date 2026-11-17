# DustAway

Dust-and-ash disintegration for Jetpack Compose. Any composable can crumble into drifting particles
and come back again:

```kotlin
val state = rememberDisintegrationState()

Card(Modifier.disintegrate(state)) { /* anything */ }

scope.launch { state.disintegrate() }   // turn to dust
scope.launch { state.restore() }        // bring it back
```

<p align="center"><img src="docs/demo.gif" alt="A card turning to dust from left to right, then reassembling" width="560"></p>

*The animation above is rendered with the library's own particle engine on a desktop JVM, not recorded
from a phone. Record the sample app yourself to see it on a device.*

## Status

Early release (0.1.0). The particle engine is plain Kotlin and covered by unit tests. The Compose layer
and sample app are built by CI; if something misbehaves on your device, please
[open an issue](https://github.com/RaiyanShahid91/DustAway/issues/new/choose).

Not published to Maven Central or JitPack yet, see **Install**.

## Install

DustAway is not on a public repository yet. Until it is, add it as a Git submodule or copy the
`dustaway` module into your project:

```kotlin
// settings.gradle.kts
include(":dustaway")
project(":dustaway").projectDir = File("path/to/DustAway/dustaway")

// app/build.gradle.kts
dependencies {
    implementation(project(":dustaway"))
}
```

Requires minSdk 24 and Jetpack Compose UI 1.7 or newer (it uses `GraphicsLayer` to capture content).

## Quick start

**1. Snap on demand.** Attach the modifier and drive it with a state:

```kotlin
@Composable
fun SnapCard() {
    val state = rememberDisintegrationState()
    val scope = rememberCoroutineScope()

    Column {
        Card(Modifier.disintegrate(state)) {
            Text("Gone in a moment", Modifier.padding(24.dp))
        }
        Button(onClick = { scope.launch { state.disintegrate() } }) { Text("Snap") }
    }
}
```

**2. Dismiss and remove.** `DisintegrateVisibility` turns content to dust when `visible` becomes false
and removes it afterwards:

```kotlin
DisintegrateVisibility(visible = !done, onDisintegrated = { items.remove(item) }) {
    TaskCard(item, onClick = { done = true })
}
```

## Works on anything

`Modifier.disintegrate(state)` goes on any composable, so the same line works on an image, a card or a
whole screen:

```kotlin
Image(painter, contentDescription = null, modifier = Modifier.disintegrate(imageState))

Card(Modifier.disintegrate(cardState)) { ... }

// Whole screen: put it on the root container of your screen content.
Column(Modifier.fillMaxSize().disintegrate(screenState)) { ... }
```

Apply `disintegrate` before `clip`/`background` in the modifier chain so the snapshot includes them. Put
buttons that should stay visible outside the container you dust. The sample app has a tab for each case,
including a **Snap screen** button.

## Tuning

Pass a `DustConfig` to `rememberDisintegrationState(config)`:

| Option | Default | What it does |
| --- | --- | --- |
| `durationMillis` | `1800` | Length of the whole animation. |
| `direction` | `LEFT_TO_RIGHT` | Where the dust front starts: left, right, top, bottom or random. |
| `sweepFraction` | `0.6` | How much of the animation spreads the start across the content. |
| `maxParticles` | `8000` | Upper bound on pieces; lower it for weak devices. |
| `windAngleDegrees` | `25` | Drift direction, counter-clockwise from right (90 = up). |
| `distanceFraction` | `0.45` | How far pieces travel, as a fraction of the longer side. |
| `scatter` | `0.35` | How widely pieces fan out around the wind. |
| `wobble` | `6` | Sideways drift while flying. |
| `seed` | `1` | Same seed, same dust. |

Full guide with notes and limits: [docs/USAGE.md](docs/USAGE.md).

## How it works

When the effect starts, the composable is captured into a bitmap and divided into a grid of small
squares (the grid is sized so there are never more than `maxParticles`). Each square keeps the average
colour of its pixels and gets a start time based on where it sits along the sweep, plus a drift vector.
Every frame the pieces are drawn at positions computed from a single progress value, which is why
`restore()` is just the same animation played backwards.

## Limitations

- The content is captured once when `disintegrate()` is called. Animations inside the content are
  frozen at that moment.
- The composable keeps its layout size and its touch handling after it has turned to dust. Use
  `DisintegrateVisibility` if it should leave the layout.
- Dust flies outside the composable's bounds, so a clipping parent (a scrolling container, `clip`) cuts it off.
- Every piece is a separate rectangle draw. Thousands are fine on modern phones; lower `maxParticles`
  if you see dropped frames.
- Hardware bitmaps and `AndroidView` content (for example a `WebView` or `MapView`) may not be captured.

## Tests

- `./gradlew :dustaway:testDebugUnitTest` runs the engine and config tests (no device needed). CI runs these.
- `./gradlew :dustaway:connectedDebugAndroidTest` runs the Compose tests for the state, modifier and
  `DisintegrateVisibility` on an emulator or device.
- `./gradlew :sample:connectedDebugAndroidTest` walks through every sample screen.

## Contributing

Nobody pushes straight to `main`: every change goes through a pull request and needs the maintainer's
approval. See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Apache License 2.0, see [LICENSE](LICENSE) and [NOTICE](NOTICE).
