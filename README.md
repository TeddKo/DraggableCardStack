# Draggable Card Stack

[![Maven Central](https://img.shields.io/maven-central/v/io.github.username/library-name.svg)](https://central.sonatype.com/artifact/io.github.teddko/cardstack)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Jetpack Compose library that provides a customizable draggable card stack with smooth animations and gestures.

## Features

- 🎯 Smooth spring-based animations
- 🔄 Vertical and horizontal swiping support
- 📱 Responsive design adapting to screen size
- 🎨 Customizable card alignment and spacing
- 💫 Dynamic elevation and scale animations
- ⚡ Velocity-based swipe detection
- 🔄 Automatic card reordering

## Installation

Add the dependency in your app's build.gradle:

```gradle
dependencies {
   implementation 'io.github.teddko:cardstack:1.0.0'
}
```

# Usage
Basic implementation:
```kotlin
@Composable
fun CardStackExample() {
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

## Customization
Card Alignment Options
```kotlion
// Align cards to different positions in the stack
CardAlignment.BOTTOM        // Bottom center
CardAlignment.BOTTOM_START  // Bottom left
CardAlignment.BOTTOM_END    // Bottom right
CardAlignment.TOP          // Top center
CardAlignment.TOP_START    // Top left
CardAlignment.TOP_END      // Top right
CardAlignment.START        // Center left
CardAlignment.END          // Center right
```

## Drag Alignment
```kotlin
// Control how cards can be dragged
DragAlignment.VERTICAL    // Only vertical dragging
DragAlignment.HORIZONTAL  // Only horizontal dragging
DragAlignment.NONE       // All directions allowed
```

## Advanced Example
```kotlin
data class CardItem(
    val title: String,
    val description: String,
    val imageUrl: String
)

@Composable
fun AdvancedCardStackExample() {
    val items = remember { 
        listOf(
            CardItem("Title 1", "Description 1", "url1"),
            CardItem("Title 2", "Description 2", "url2"),
            CardItem("Title 3", "Description 3", "url3")
        )
    }
    
    DraggableCardStack(
        initialItems = items,
        height = 300.dp,
        cardSpacingRatio = 0.15f,
        cardAlignment = CardAlignment.BOTTOM,
        dragAlignment = DragAlignment.HORIZONTAL
    ) { item ->
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
```
