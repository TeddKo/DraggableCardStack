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

## Installation
```kotlin
dependencies {
    implementation("io.github.teddko:cardstack:1.0.1")
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
