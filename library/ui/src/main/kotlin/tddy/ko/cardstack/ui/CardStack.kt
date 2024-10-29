package tddy.ko.cardstack.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffset
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

/**
 * DraggableCardStack is a customizable composable that implements a stack of draggable cards
 * with smooth animations and gestures. It supports both vertical and horizontal swiping with
 * customizable alignment and spacing.
 *
 * @param initialItems The initial list of items to display in the card stack
 * @param height The height of each card in the stack
 * @param cardSpacingRatio The ratio that determines spacing between cards relative to screen height
 * @param cardAlignment The alignment of cards in the stack (TOP, BOTTOM, START, END, etc.)
 * @param dragAlignment The direction in which cards can be dragged (VERTICAL, HORIZONTAL, NONE)
 * @param cardContent The composable content for each card
 *
 * Features:
 * - Smooth card animations with spring physics
 * - Customizable card alignment and spacing
 * - Support for both vertical and horizontal swiping
 * - Velocity-based swipe detection
 * - Automatic card reordering after swipe
 * - Elevation and scale animations
 *
 * Example usage:
 * ```
 * DraggableCardStack(
 *     initialItems = listOf("Card 1", "Card 2", "Card 3"),
 *     height = 200.dp,
 *     cardSpacingRatio = 0.1f,
 *     cardAlignment = CardAlignment.BOTTOM,
 *     dragAlignment = DragAlignment.HORIZONTAL
 * ) { item ->
 *     // Card content
 *     Text(text = item)
 * }
 * ```
 *
 * Notes:
 * - The card stack automatically handles device rotation and screen size changes
 * - Cards can be swiped when velocity exceeds the threshold or drag distance is sufficient
 * - Each card maintains its own state and animation properties
 * - The stack supports dynamic updates to the item list
 *
 * @see CardAlignment for available card alignment options
 * @see DragAlignment for available drag direction options
 **/
@Composable
fun <T> DraggableCardStack(
    initialItems: List<T>,
    height: Dp,
    cardSpacingRatio: Float,
    cardAlignment: CardAlignment,
    dragAlignment: DragAlignment,
    cardContent: @Composable BoxScope.(T) -> Unit
) {
    var items by remember { mutableStateOf(initialItems) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val cardSpacing = remember(screenHeight) { screenHeight * (cardSpacingRatio * .1f) }
    val velocityThreshold =
        remember(screenWidth, screenHeight) { minOf(screenWidth, screenHeight) * 0.6f }

    val stackedHeight by remember(items.size, cardSpacing, cardAlignment) {
        derivedStateOf {
            when (cardAlignment) {
                CardAlignment.BOTTOM, CardAlignment.BOTTOM_START, CardAlignment.BOTTOM_END,
                CardAlignment.TOP, CardAlignment.TOP_START, CardAlignment.TOP_END ->
                    height + (cardSpacing * (items.size - 1))

                CardAlignment.START, CardAlignment.END -> height
            }
        }
    }

    val contentAlignment = remember(cardAlignment) {
        when (cardAlignment) {
            CardAlignment.BOTTOM -> Alignment.BottomCenter
            CardAlignment.BOTTOM_START -> Alignment.BottomStart
            CardAlignment.BOTTOM_END -> Alignment.BottomEnd
            CardAlignment.TOP -> Alignment.TopCenter
            CardAlignment.TOP_START -> Alignment.TopStart
            CardAlignment.TOP_END -> Alignment.TopEnd
            CardAlignment.START, CardAlignment.END -> Alignment.Center
        }
    }

    val (stackDragProgress, onDrag) = remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = stackedHeight),
        contentAlignment = contentAlignment
    ) {
        items
            .asReversed()
            .forEachIndexed { index, item ->
                key(item) {
                    DraggableCard(
                        density = density,
                        height = height,
                        cardSpacing = cardSpacing,
                        velocityThreshold = velocityThreshold,
                        itemCount = items.size,
                        index = items.size - 1 - index,
                        cardAlignment = cardAlignment,
                        dragAlignment = dragAlignment,
                        stackDragProgress = stackDragProgress,
                        onDrag = onDrag,
                        onSwipe = { direction ->
                            items = when (direction) {
                                DragDirection.LEFT, DragDirection.DOWN -> items
                                    .toMutableList()
                                    .apply {
                                        remove(items.last())
                                        add(0, items.last())
                                    }

                                DragDirection.RIGHT, DragDirection.UP -> items
                                    .toMutableList()
                                    .apply {
                                        remove(items.first())
                                        add(items.first())
                                    }
                            }
                        },
                        content = { cardContent(item) }
                    )
                }
            }
    }
}

/**
 *
 * A composable component that represents a draggable card within the card stack.
 * Handles individual card animations, gestures, and state management.
 *
 * @param density The current display density
 * @param height Height of the card
 * @param cardSpacing Spacing between cards in the stack
 * @param velocityThreshold Threshold for swipe velocity to trigger card actions
 * @param itemCount Total number of cards in the stack
 * @param index Position of this card in the stack (0 is top card)
 * @param cardAlignment Alignment of cards in the stack
 * @param dragAlignment Allowed drag directions
 * @param stackDragProgress Current drag progress of the stack (-1 to 1)
 * @param onDrag Callback invoked during drag with progress value
 * @param onSwipe Callback invoked when card is swiped with direction
 * @param content Composable content to be displayed in the card
 *
 * Features:
 * - Spring-based animations for smooth transitions
 * - Gesture detection with velocity tracking
 * - Dynamic elevation changes during interaction
 * - Scale animations based on stack position
 * - Support for both vertical and horizontal swiping
 * - Automatic card repositioning after swipe
 *
 * Internal behavior:
 * - Tracks drag offset and animation state
 * - Calculates swipe velocity and direction
 * - Manages elevation changes during drag
 * - Handles scale transitions
 * - Applies position adjustments based on alignment
 *
 * Animation specifications:
 * - Uses spring animation for offset changes (low bounce, low stiffness)
 * - Animates elevation from 4dp to 8dp during interaction
 * - Scale factor decreases for each card in the stack
 * - Smooth transitions for all transform properties
 *
 * Gesture handling:
 * - Only top card (index 0) responds to gestures
 * - Tracks velocity for swipe detection
 * - Supports configurable velocity thresholds
 * - Handles drag cancellation and completion
 *
 * Note: This component is designed to be used within DraggableCardStack
 * and should not be used independently.
 */
@Composable
private fun DraggableCard(
    density: Density,
    height: Dp,
    cardSpacing: Dp,
    velocityThreshold: Dp,
    itemCount: Int,
    index: Int,
    cardAlignment: CardAlignment,
    dragAlignment: DragAlignment,
    stackDragProgress: Float,
    onDrag: (Float) -> Unit,
    onSwipe: (DragDirection) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isAnimating by remember { mutableStateOf(false) }

    val velocityThresholdPx = with(density) { velocityThreshold.toPx() }
    val spacingPx = with(density) { cardSpacing.toPx() }

    val swipeProgress = calculateSwipeProgress(offset, velocityThresholdPx)

    val updatedOnDrag by rememberUpdatedState(onDrag)
    val updatedOnSwipe by rememberUpdatedState(onSwipe)

    val transition = updateTransition(targetState = offset, label = "cardTransition")

    val animatedOffset by transition.animateOffset(
        label = "offsetAnimation",
        transitionSpec = {
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
        },
        targetValueByState = { it }
    )
    val animatedElevation by animateFloatAsState(
        targetValue = if (isAnimating) 8f else 4f,
        animationSpec = tween(durationMillis = 200),
        label = "animatedElevation"
    )


    val targetScale = calculateScales(index, itemCount, stackDragProgress)
    val animatedScale by transition.animateFloat(
        label = "scaleAnimation",
        transitionSpec = {
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
        },
        targetValueByState = { targetScale }
    )

    val lastOffset = calculateLastOffset(cardAlignment, index, spacingPx)
    val dragBasedOffset =
        calculateDragBasedOffset(stackDragProgress, index, spacingPx, itemCount, cardAlignment)

    LaunchedEffect(swipeProgress) { updatedOnDrag(swipeProgress) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 12.dp)
            .zIndex(itemCount.toFloat() - index)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                translationX = animatedOffset.x + lastOffset.x + dragBasedOffset.x
                translationY = animatedOffset.y + lastOffset.y + dragBasedOffset.y
                applyAlignmentAdjustment(cardAlignment, animatedScale, size)
            }
            .shadow(elevation = animatedElevation.dp, shape = RoundedCornerShape(12.dp))
            .then(
                if (index == 0 && !isAnimating) {
                    Modifier.pointerInput(Unit) {
                        val velocityTracker = VelocityTracker()
                        detectDragGestures(
                            onDragStart = { velocityTracker.resetTracking() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offset += when (dragAlignment) {
                                    DragAlignment.VERTICAL -> Offset(x = 0f, y = dragAmount.y)
                                    DragAlignment.HORIZONTAL -> Offset(x = dragAmount.x, 0f)
                                    DragAlignment.NONE -> Offset(x = dragAmount.x, y = dragAmount.y)
                                }
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                            },
                            onDragEnd = {
                                val velocity = velocityTracker.calculateVelocity()
                                val direction =
                                    determineSwipeDirection(velocity = velocity, offset = offset)

                                val shouldSwipe = direction != null && isSwipeVelocityExceeded(
                                    velocity = velocity,
                                    velocityThresholdPx = velocityThresholdPx
                                )

                                coroutineScope.launch {
                                    isAnimating = true
                                    if (shouldSwipe && direction != null) {
                                        val targetOffset = calculateTargetOffset(
                                            direction = direction,
                                            size = size,
                                            offset = offset,
                                            cardAlignment = cardAlignment
                                        )
                                        offset = targetOffset
                                        updatedOnSwipe(direction)
                                    }
                                    offset = Offset.Zero
                                    isAnimating = false
                                }
                            }
                        )
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}