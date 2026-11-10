<h1 align="center">Draggable Card Stack</h1>

<p align="center">
  <a href="https://search.maven.org/artifact/io.github.teddko/cardstack"><img alt="MavenCentral" src="https://img.shields.io/maven-central/v/io.github.teddko/cardstack.svg"/></a>
  <a href="https://android-arsenal.com/api?level=23"><img alt="API" src="https://img.shields.io/badge/API-23%2B-brightgreen.svg"/></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
</p>
<p align="center">
A <strong>Jetpack Compose</strong> library for customizable draggable card stacks with smooth animations.
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/8fd29fb1-8a18-4ada-9b81-3a246239ba77" width="320" />
</p>

## Features

- 🎯 Smooth spring-based animations
- 🔄 Vertical and horizontal swiping
- 📱 Responsive design
- 🎨 Customizable card alignment & spacing
- 💫 Dynamic elevation & scale animations
- ⚡ Velocity-based swipe detection
- 🔄 Automatic card reordering
- 👆 Tap to expand, tap a card to bring it to the front
- ✋ Lift and tilt while dragging, real fly-out and tuck, interruptible flights

## Installation
```kotlin
dependencies {
    implementation("io.github.teddko:cardstack:1.1.0")
}
```

## Basic Usage
```kotlin
@Composable
fun CardStackDemo() {
    val items = remember { listOf("Card 1", "Card 2", "Card 3") }

    DraggableCardStack(
        initialItems = items,
        height = 200.dp,
        cardSpacingRatio = 0.1f,
        cardAlignment = CardAlignment.BOTTOM,
        dragAlignment = DragAlignment.HORIZONTAL
    ) { item ->
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = item,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
```

## Behavior
- Dragging the front card lifts and tilts it while the back cards stay in place. A release commits by drag distance (40% of the card extent) or by velocity.
- RIGHT or UP moves the first card to the back, LEFT or DOWN leaves the front card at rest and pulls the last card in from the opposite side by the finger, on top of the stack, and a release moves it to the front or springs it back out.
- A new drag interrupts a running flight, so rapid flicks are never dropped.
- Tapping a collapsed stack expands it. Tapping the front card of an expanded stack collapses it. Tapping a back card brings it to the front as many swipes as its depth, then collapses the stack.
- START and END expand into a horizontal fan scaled to fit the stack width, the other alignments widen the spacing and grow the stack height.
- A stack of fewer than two cards neither drags nor reacts to taps.

## State
```kotlin
val state = rememberCardStackState()

DraggableCardStack(
    initialItems = items,
    height = 200.dp,
    cardSpacingRatio = 0.1f,
    cardAlignment = CardAlignment.BOTTOM,
    dragAlignment = DragAlignment.HORIZONTAL,
    state = state
) { item -> Text(item) }

state.frontIndex   // index in items of the front card
state.expand()
state.collapse()
```
The order is an index permutation, so duplicate items are safe, and the order and expansion survive configuration change.

## Upgrading to 1.1.0
- The 1.0.x `DraggableCardStack` call keeps working, binary and source compatible. `modifier` and `state` are new optional parameters.
- Minimum requirements: Kotlin 2.2, Compose 1.12.1, compileSdk 36 or higher.
- material3, activity-compose, appcompat and core-ktx no longer come in transitively, declare them if your code uses them. Compose ui and foundation remain compile dependencies.
- Taps are now consumed by the cards.

## Advanced Usage
```kotlin
data class CardItem(
    val title: String,
    val description: String,
    val imageUrl: String
)

@Composable
fun AdvancedCardStack() {
    val items = remember {
        listOf(
            CardItem("Title 1", "Description 1", "url1"),
            CardItem("Title 2", "Description 2", "url2"),
            CardItem("Title 3", "Description 3", "url3")
        )
    }

    DraggableCardStack(
        initialItems = items,
        height = 200.dp,
        cardSpacingRatio = 0.1f,
        cardAlignment = CardAlignment.BOTTOM,
        dragAlignment = DragAlignment.HORIZONTAL
    ) { item ->
        Card(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    model = item.imageUrl,
                    contentDescription = item.description,
                    contentScale = ContentScale.Crop,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = item.title)
                    Text(text = item.description)
                }
            }
        }
    }
}
```

## Customization
### Card Alignment
```kotlin
CardAlignment.BOTTOM        // Bottom center
CardAlignment.BOTTOM_START  // Bottom left
CardAlignment.BOTTOM_END    // Bottom right
CardAlignment.TOP          // Top center
CardAlignment.TOP_START    // Top left
CardAlignment.TOP_END      // Top right
CardAlignment.START        // Center left
CardAlignment.END          // Center right
```

### Drag Alignment
```kotlin
DragAlignment.VERTICAL    // Vertical only
DragAlignment.HORIZONTAL  // Horizontal only
DragAlignment.NONE       // All directions
```
