<h1 align="center">Draggable Card Stack</h1>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.teddko/cardstack"><img alt="Maven Central" src="https://img.shields.io/maven-central/v/io.github.teddko/cardstack.svg"/></a>
  <a href="https://android-arsenal.com/api?level=23"><img alt="API" src="https://img.shields.io/badge/API-23%2B-brightgreen.svg"/></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
</p>
<p align="center">
A <strong>Jetpack Compose</strong> card stack you can swipe through, expand and pick from, with physical, interruptible motion.
</p>

<p align="center">
  <img src="docs/images/demo.gif" width="320" alt="Swiping forward and back, expanding the stack and picking a card" />
</p>

## Features

- Swipe the front card away to cycle forward, pull the back card in to cycle backward
- The dragged card lifts, tilts and follows the finger in every allowed direction
- Spring-based fly-out and landing that a new drag can interrupt, so rapid flicks are never dropped
- Tap to expand the stack along its alignment, tap a card to bring it to the front
- 8 stack alignments and 3 drag axis modes
- Optional hoisted state to read the front card and expand or collapse from code, kept across configuration change
- No recomposition per drag frame

## Installation

```kotlin
dependencies {
    implementation("io.github.teddko:cardstack:1.1.0")
}
```

Requirements: minSdk 23, compileSdk 36 or higher, Kotlin 2.2 or higher, Compose 1.12.1 or higher.

## Usage

```kotlin
@Composable
fun CardStackDemo() {
    val items = remember { listOf("Card 1", "Card 2", "Card 3") }

    DraggableCardStack(
        initialItems = items,
        height = 200.dp,
        cardSpacingRatio = 0.1f,
        cardAlignment = CardAlignment.BOTTOM,
        dragAlignment = DragAlignment.NONE
    ) { item ->
        Card(modifier = Modifier.fillMaxSize()) {
            Text(text = item, modifier = Modifier.padding(16.dp))
        }
    }
}
```

| Parameter | Description |
|---|---|
| `initialItems` | Cards from front to back. Changes to the list are rendered, new items join the back |
| `height` | Height of every card |
| `cardSpacingRatio` | Spacing of the collapsed stack, relative to the screen height |
| `cardAlignment` | Side the back cards peek out on, see [Card alignment](#card-alignment) |
| `dragAlignment` | Axes a drag may move along, see [Drag alignment](#drag-alignment) |
| `modifier` | Modifier of the stack |
| `state` | Order and expansion state, see [State](#state) |
| `cardContent` | Content of a card |

## Gestures

| Gesture | Result |
|---|---|
| Swipe right or up | The front card flies out and tucks behind the stack, the next card comes to the front |
| Swipe left or down | The back card slides out from behind the stack the other way (right for left, up for down), then lands on top in the swipe direction |
| Short drag | The card springs back, the order stays |
| Tap a collapsed stack | The stack expands |
| Tap a back card of an expanded stack | The card lifts, comes to the front and the stack collapses. The order rotates as many steps as the card's depth |
| Tap the front card of an expanded stack | The stack collapses |

- The first move of a drag picks the card: right or up drags the front card, left or down (past a small dead zone) pulls the back card. The picked card then follows the finger in every direction until release, and the other cards stay still.
- A release commits by distance (40% of the card size on the drag axis) or by velocity.
- A drag or a tap during an animation takes effect at once.
- A stack of fewer than two cards neither drags nor reacts to taps.
- TalkBack users can cycle the cards with the "Next card" and "Previous card" actions.

## State

```kotlin
val state = rememberCardStackState()

DraggableCardStack(
    initialItems = items,
    height = 200.dp,
    cardSpacingRatio = 0.1f,
    cardAlignment = CardAlignment.BOTTOM,
    dragAlignment = DragAlignment.NONE,
    state = state
) { item -> Text(item) }

state.frontIndex   // index in items of the front card, -1 for an empty stack
state.isExpanded
state.expand()
state.collapse()
```

The order is kept as a permutation of item indices, so duplicate items are safe, and the order and expansion survive configuration change. Removing an item that is not the last one shifts the order.

## Card alignment

Collapsed and expanded, from the demo app.

| Alignment | Collapsed / expanded |
|---|---|
| `BOTTOM` | <img src="docs/images/alignment_bottom.jpg" width="320" /> |
| `BOTTOM_START` | <img src="docs/images/alignment_bottom_start.jpg" width="320" /> |
| `BOTTOM_END` | <img src="docs/images/alignment_bottom_end.jpg" width="320" /> |
| `TOP` | <img src="docs/images/alignment_top.jpg" width="320" /> |
| `TOP_START` | <img src="docs/images/alignment_top_start.jpg" width="320" /> |
| `TOP_END` | <img src="docs/images/alignment_top_end.jpg" width="320" /> |
| `START` | <img src="docs/images/alignment_start.jpg" width="320" /> |
| `END` | <img src="docs/images/alignment_end.jpg" width="320" /> |

The vertical alignments widen the spacing and grow the stack height when expanded. `START` and `END` expand into a horizontal fan scaled to fit the stack width.

## Drag alignment

```kotlin
DragAlignment.VERTICAL    // vertical moves only
DragAlignment.HORIZONTAL  // horizontal moves only
DragAlignment.NONE        // every direction
```

## Upgrading from 1.0.x

- Existing `DraggableCardStack` calls keep working, binary and source compatible. `modifier` and `state` are new optional parameters.
- New behaviors: taps are consumed by the cards (expand, select, collapse), and expanding grows the stack height.
- Minimum Kotlin 2.2 and Compose 1.12.1.
- material3, activity-compose, appcompat and core-ktx no longer come in transitively. Declare them if your code uses them. Compose ui and foundation remain compile dependencies.

## License

Apache License 2.0. See [LICENSE](LICENSE).
