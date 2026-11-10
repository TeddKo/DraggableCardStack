package tddy.ko.cardstack.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
public fun <T> DraggableCardStack(
    initialItems: List<T>,
    height: Dp,
    cardSpacingRatio: Float,
    cardAlignment: CardAlignment,
    dragAlignment: DragAlignment,
    modifier: Modifier = Modifier,
    state: CardStackState = rememberCardStackState(),
    cardContent: @Composable BoxScope.(T) -> Unit
) {
    state.sync(initialItems.size)
    val order = state.order
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val cardSpacing = remember(screenHeight) { screenHeight * (cardSpacingRatio * .1f) }
    val velocityThreshold =
        remember(screenWidth, screenHeight) { minOf(screenWidth, screenHeight) * 0.6f }

    val expansion = animateFloatAsState(
        targetValue = if (state.isExpanded) 1f else 0f,
        animationSpec = ExpansionSpring,
        label = "expansion"
    )
    val isVertical = cardAlignment != CardAlignment.START && cardAlignment != CardAlignment.END

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

    val stackDragProgress = remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .layout { measurable, constraints ->
                val heightPx = height.toPx()
                val spacingPx = cardSpacing.toPx()
                val step = spacingPx + (expandedStep(spacingPx, heightPx) - spacingPx) * expansion.value
                val stackedPx = if (isVertical) heightPx + step * (order.size - 1) else heightPx
                val fixed = constraints.constrainHeight(stackedPx.roundToInt())
                val placeable = measurable.measure(constraints.copy(minHeight = fixed, maxHeight = fixed))
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            },
        contentAlignment = contentAlignment
    ) {
        for (index in order.indices.reversed()) {
            val itemIndex = order[index]
            key(itemIndex) {
                DraggableCard(
                    density = density,
                    height = height,
                    cardSpacing = cardSpacing,
                    velocityThreshold = velocityThreshold,
                    state = state,
                    itemCount = order.size,
                    index = index,
                    cardAlignment = cardAlignment,
                    dragAlignment = dragAlignment,
                    stackDragProgress = stackDragProgress,
                    expansion = expansion,
                    content = { cardContent(initialItems[itemIndex]) }
                )
            }
        }
    }
}

/**
 * Binary compatible entry point of 1.0.x, hidden from source so calls resolve to the overload
 * with [CardStackState].
 **/
@Deprecated("Binary compatibility", level = DeprecationLevel.HIDDEN)
@Composable
public fun <T> DraggableCardStack(
    initialItems: List<T>,
    height: Dp,
    cardSpacingRatio: Float,
    cardAlignment: CardAlignment,
    dragAlignment: DragAlignment,
    cardContent: @Composable BoxScope.(T) -> Unit
) {
    DraggableCardStack(
        initialItems, height, cardSpacingRatio, cardAlignment, dragAlignment,
        Modifier, rememberCardStackState(), cardContent
    )
}

/**
 * A draggable card within the card stack, owning its drag offset, lift and flight animations.
 *
 * @param density The current display density
 * @param height Height of the card
 * @param cardSpacing Spacing between cards in the stack
 * @param velocityThreshold Release velocity that commits a swipe
 * @param state State holder receiving the order change of a committed swipe
 * @param itemCount Total number of cards in the stack
 * @param index Position of this card in the stack (0 is the front card)
 * @param cardAlignment Alignment of cards in the stack
 * @param dragAlignment Allowed drag directions
 * @param stackDragProgress Drag progress of the front card (-1 to 1), read by back cards at draw time
 * @param expansion Animated expansion of the stack, 0 collapsed and 1 expanded
 * @param content Composable content to be displayed in the card
 *
 * Behavior:
 * - Only the front card of a collapsed stack is draggable, and a new drag interrupts any running flight
 * - Resting slot, drag offset and flights are applied in the placement phase, lift and tilt in
 *   the draw phase, so a drag does not recompose the content
 * - A forward swipe flies the card out along the release vector, then tucks it to the back
 * - A reverse swipe springs the card to the second slot while the last card flies out and lands in front
 * - A tap expands the stack, collapses it from the front card, or brings the tapped back card to the
 *   front as many swipes as its depth
 **/
@Composable
private fun DraggableCard(
    density: Density,
    height: Dp,
    cardSpacing: Dp,
    velocityThreshold: Dp,
    state: CardStackState,
    itemCount: Int,
    index: Int,
    cardAlignment: CardAlignment,
    dragAlignment: DragAlignment,
    stackDragProgress: MutableFloatState,
    expansion: State<Float>,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val velocityThresholdPx = with(density) { velocityThreshold.toPx() }
    val spacingPx = with(density) { cardSpacing.toPx() }
    val shape = remember { RoundedCornerShape(12.dp) }

    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var cardSize by remember { mutableStateOf(IntSize.Zero) }
    var heldZ by remember { mutableStateOf<Float?>(null) }
    var isLifted by remember { mutableStateOf(false) }
    var flight by remember { mutableStateOf<Job?>(null) }
    val tapTilt = remember { Animatable(0f) }
    val previousIndex = remember { mutableIntStateOf(index) }

    val depth by animateFloatAsState(index.toFloat(), DepthSpring, label = "depth")
    val lift by animateFloatAsState(if (isLifted) 1f else 0f, tween(durationMillis = 200), label = "lift")

    fun launchFlight(out: Offset, pastDistance: Float, velocity: Offset, topFirst: Boolean) {
        flight = scope.launch {
            var v = velocity
            try {
                heldZ = if (topFirst) itemCount + 1f else null
                launch inner@{
                    animate(Offset.VectorConverter, dragOffset, out, v, FlightSpring) { value, velocity ->
                        dragOffset = value
                        v = velocity
                        if (value.getDistance() >= pastDistance) this@inner.cancel()
                    }
                }.join()
                heldZ = if (topFirst) null else itemCount + 1f
                animate(Offset.VectorConverter, dragOffset, Offset.Zero, v, SettleSpring) { value, _ ->
                    dragOffset = value
                }
            } finally {
                heldZ = null
                flight = null
            }
        }
    }

    fun settle(velocity: Offset) {
        flight = scope.launch {
            try {
                animate(Offset.VectorConverter, dragOffset, Offset.Zero, velocity, SettleSpring) { value, _ ->
                    dragOffset = value
                }
            } finally {
                flight = null
            }
        }
    }

    LaunchedEffect(index) {
        val wasLast = previousIndex.intValue == itemCount - 1
        previousIndex.intValue = index
        if (index == 0 && wasLast && itemCount >= 3 && state.lastMove == Move.REVERSE) {
            val peek = calculateLastOffset(cardAlignment, 1f, 1f)
            val isVertical = cardAlignment != CardAlignment.START && cardAlignment != CardAlignment.END
            val extent = (if (isVertical) cardSize.height else cardSize.width).toFloat()
            val axis = if (isVertical) Offset(0f, peek.y) else Offset(peek.x, 0f)
            launchFlight(axis * extent, extent * 0.5f, Offset.Zero, topFirst = false)
        }
    }

    val draggableState = rememberDraggable2DState { delta ->
        dragOffset += when (dragAlignment) {
            DragAlignment.VERTICAL -> Offset(x = 0f, y = delta.y)
            DragAlignment.HORIZONTAL -> Offset(x = delta.x, y = 0f)
            DragAlignment.NONE -> delta
        }
        if (index == 0) stackDragProgress.floatValue = calculateSwipeProgress(dragOffset, velocityThresholdPx)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    val collapsed = calculateLastOffset(cardAlignment, depth, spacingPx)
                    val expanded = calculateExpandedOffset(
                        cardAlignment, depth, spacingPx, cardSize.toSize(), itemCount
                    )
                    val slot = lerp(collapsed, expanded, expansion.value) +
                        calculateDragBasedOffset(
                            stackDragProgress.floatValue, depth, spacingPx, itemCount, cardAlignment
                        ) + dragOffset
                    placeable.place(
                        x = slot.x.roundToInt(),
                        y = slot.y.roundToInt(),
                        zIndex = heldZ ?: (itemCount - index).toFloat()
                    )
                }
            }
            .draggable2D(
                state = draggableState,
                enabled = index == 0 && itemCount >= 2 && !state.isExpanded,
                startDragImmediately = flight != null,
                onDragStarted = {
                    flight?.cancel()
                    isLifted = true
                },
                onDragStopped = { velocity ->
                    isLifted = false
                    stackDragProgress.floatValue = 0f
                    val release = Offset(velocity.x, velocity.y)
                    val direction = swipeDirection(
                        offset = dragOffset,
                        velocity = velocity,
                        commitDistance = Offset(cardSize.width * CommitRatio, cardSize.height * CommitRatio),
                        velocityThreshold = velocityThresholdPx
                    )
                    val isForward = direction == DragDirection.RIGHT || direction == DragDirection.UP
                    when {
                        direction == null -> settle(release)
                        isForward || itemCount < 3 -> {
                            state.rotate(1)
                            val out = flyOutTarget(direction, cardSize, dragOffset)
                            launchFlight(out, out.getDistance().coerceAtMost(extentOf(direction, cardSize)), release, topFirst = true)
                        }
                        else -> {
                            state.rotate(-1)
                            settle(release)
                        }
                    }
                }
            )
            .padding(horizontal = 12.dp)
            .onSizeChanged { cardSize = it }
            .graphicsLayer {
                val scale = lerp(
                    calculateScales(depth, itemCount, stackDragProgress.floatValue),
                    calculateExpandedScale(cardAlignment, itemCount),
                    expansion.value
                ) + LiftScale * lift
                scaleX = scale
                scaleY = scale
                rotationZ = tilt(dragOffset, cardSize) + tapTilt.value
                shadowElevation = (4f + 4f * lift).dp.toPx()
                this.shape = shape
                clip = true
                applyAlignmentAdjustment(cardAlignment, scale, size)
            }
            .clickable(enabled = itemCount >= 2, interactionSource = null, indication = null) {
                when {
                    !state.isExpanded -> state.expand()
                    index == 0 -> state.collapse()
                    else -> {
                        state.rotate(index)
                        state.collapse()
                        scope.launch {
                            heldZ = itemCount + 1f
                            try {
                                tapTilt.snapTo(TapTiltDegrees)
                                tapTilt.animateTo(0f, spring())
                            } finally {
                                heldZ = null
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}

private fun extentOf(direction: DragDirection, size: IntSize): Float = when (direction) {
    DragDirection.LEFT, DragDirection.RIGHT -> size.width.toFloat()
    DragDirection.UP, DragDirection.DOWN -> size.height.toFloat()
}

private fun flyOutTarget(direction: DragDirection, size: IntSize, offset: Offset): Offset {
    val reach = extentOf(direction, size) * FlyOutRatio
    return when (direction) {
        DragDirection.RIGHT -> Offset(reach, offset.y)
        DragDirection.LEFT -> Offset(-reach, offset.y)
        DragDirection.DOWN -> Offset(offset.x, reach)
        DragDirection.UP -> Offset(offset.x, -reach)
    }
}

private fun tilt(offset: Offset, size: IntSize): Float {
    if (size.width == 0 || size.height == 0) return 0f
    val sideways = (offset.x / size.width).coerceIn(-1f, 1f) * MaxTiltDegrees
    val lifted = (-offset.y / size.height).coerceIn(-1f, 1f) * LiftTiltDegrees
    return sideways + lifted
}

private const val CommitRatio = 0.4f
private const val FlyOutRatio = 1.2f
private const val LiftScale = 0.03f
private const val MaxTiltDegrees = 10f
private const val LiftTiltDegrees = 6f
private const val TapTiltDegrees = 6f
private val ExpansionSpring = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
private val DepthSpring = spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
private val FlightSpring = spring<Offset>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
private val SettleSpring = spring<Offset>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
