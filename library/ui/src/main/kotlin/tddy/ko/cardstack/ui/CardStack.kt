package tddy.ko.cardstack.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * DraggableCardStack is a stack of cards cycled by swiping the front card and expanded or
 * collapsed by tapping.
 *
 * @param initialItems The items of the stack, the card at the front being the first of the current order
 * @param height The height of each card in the stack
 * @param cardSpacingRatio The ratio that determines spacing between cards relative to screen height
 * @param cardAlignment The side on which the back cards peek out (TOP, BOTTOM, START, END and corners)
 * @param dragAlignment The axes along which the front card follows the finger
 * @param modifier The modifier applied to the stack container
 * @param state The state holder of the card order and expansion, exposing the front index
 * @param cardContent The composable content for each card
 *
 * Behavior:
 * - Dragging the front card lifts and tilts it, and a release commits by drag distance (40% of the
 *   card extent on the drag axis) or by velocity along the drag direction
 * - RIGHT or UP moves the first card to the back, the card flying out along the release vector and
 *   tucking behind the stack
 * - LEFT or DOWN moves the last card to the front, the last card flying out of the stack and landing in front
 * - With two cards a swipe in any direction flies the front card out and tucks it to the back
 * - A new drag interrupts a running flight, so rapid flicks are never dropped
 * - Tapping a collapsed stack expands it, tapping the front card of an expanded stack collapses it,
 *   and tapping a back card of an expanded stack brings it to the front as many swipes as its depth
 *   and collapses the stack
 * - Expanded vertical alignments widen the card spacing and grow the stack height, START and END
 *   spread the cards as a horizontal fan scaled to fit the stack width
 * - A stack of fewer than two cards neither drags nor reacts to taps
 *
 * Notes:
 * - The card order is an index permutation held in [state], so duplicate items are safe and the
 *   order and expansion survive configuration change
 * - The order is kept by item index, so removing a non-last item shifts the order and may change
 *   the front card, and added items are appended at the back
 * - [DragAlignment.NONE] lets the front card follow the finger on both axes
 *
 * Example usage:
 * ```
 * val state = rememberCardStackState()
 * DraggableCardStack(
 *     initialItems = listOf("Card 1", "Card 2", "Card 3"),
 *     height = 200.dp,
 *     cardSpacingRatio = 0.1f,
 *     cardAlignment = CardAlignment.BOTTOM,
 *     dragAlignment = DragAlignment.HORIZONTAL,
 *     state = state
 * ) { item ->
 *     Text(text = item)
 * }
 * ```
 *
 * @see CardAlignment for available card alignment options
 * @see DragAlignment for available drag direction options
 * @see CardStackState for the order and expansion state
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .layout { measurable, constraints ->
                val heightPx = height.toPx()
                val spacingPx = cardSpacing.toPx()
                val step = spacingPx + (expandedStep(spacingPx, heightPx) - spacingPx) * expansion.value
                val stackedPx = if (isVertical) heightPx + step * (order.size - 1).coerceAtLeast(0) else heightPx
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
 * @param expansion Animated expansion of the stack, 0 collapsed and 1 expanded
 * @param content Composable content to be displayed in the card
 *
 * Behavior:
 * - Only the front card of a collapsed stack is draggable, and a new drag interrupts any running flight
 * - Resting slot, drag offset and flights are applied in the placement phase, lift and tilt in
 *   the draw phase, so a drag does not recompose the content
 * - A forward swipe flies the card out along the release vector, then tucks it to the back
 * - Back cards stay at their slots while the front card is dragged and spring to the new slots when the order changes
 * - A reverse swipe springs the card to the second slot while the last card comes out from behind the stack in the
 *   swipe direction by one card extent, then lands in front
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

    fun launchFlight(block: suspend CoroutineScope.() -> Unit) {
        flight?.cancel()
        heldZ = null
        flight = scope.launch {
            val self = coroutineContext.job
            try {
                block()
            } finally {
                if (flight === self) {
                    heldZ = null
                    flight = null
                }
            }
        }
    }

    suspend fun slideTo(target: Offset, velocity: Offset, spec: SpringSpec<Offset>) {
        animate(Offset.VectorConverter, dragOffset, target, velocity, spec) { value, _ -> dragOffset = value }
    }

    fun settle(velocity: Offset) = launchFlight { slideTo(Offset.Zero, velocity, SettleSpring) }

    fun flyOut(out: Offset, pastDistance: Float, velocity: Offset, topFirst: Boolean) = launchFlight {
        val top = itemCount + 1f
        heldZ = if (topFirst) top else 0f
        var v = velocity
        launch {
            animate(Offset.VectorConverter, dragOffset, out, v, FlightSpring) { value, velocity ->
                dragOffset = value
                v = velocity
                if (value.getDistance() >= pastDistance) this@launch.cancel()
            }
        }.join()
        heldZ = if (topFirst) null else top
        slideTo(Offset.Zero, v, SettleSpring)
    }

    LaunchedEffect(index) {
        val wasLast = previousIndex.intValue == itemCount - 1
        previousIndex.intValue = index
        if (index == 0 && wasLast && itemCount >= 3 && state.lastMove == Move.REVERSE) {
            val extent = extentOf(state.lastDirection, cardSize)
            val toward = when (state.lastDirection) {
                DragDirection.LEFT -> Offset(-1f, 0f)
                DragDirection.RIGHT -> Offset(1f, 0f)
                DragDirection.UP -> Offset(0f, -1f)
                DragDirection.DOWN -> Offset(0f, 1f)
            }
            flyOut(toward * extent, extent * 0.5f, Offset.Zero, topFirst = false)
        }
    }

    val draggableState = rememberDraggable2DState { delta ->
        if (state.isExpanded) return@rememberDraggable2DState
        dragOffset += when (dragAlignment) {
            DragAlignment.VERTICAL -> Offset(x = 0f, y = delta.y)
            DragAlignment.HORIZONTAL -> Offset(x = delta.x, y = 0f)
            DragAlignment.NONE -> delta
        }
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
                    val slot = lerp(collapsed, expanded, expansion.value) + dragOffset
                    placeable.place(
                        x = slot.x.roundToInt(),
                        y = slot.y.roundToInt(),
                        zIndex = heldZ ?: (itemCount - index).toFloat()
                    )
                }
            }
            .draggable2D(
                state = draggableState,
                enabled = index == 0 && itemCount >= 2,
                startDragImmediately = flight != null,
                onDragStarted = {
                    flight?.cancel()
                    isLifted = true
                },
                onDragStopped = { velocity ->
                    isLifted = false
                    val release = Offset(velocity.x, velocity.y)
                    val direction = swipeDirection(
                        offset = dragOffset,
                        velocity = velocity,
                        commitDistance = Offset(cardSize.width * CommitRatio, cardSize.height * CommitRatio),
                        velocityThreshold = velocityThresholdPx
                    )
                    val isForward = direction == DragDirection.RIGHT || direction == DragDirection.UP
                    when {
                        direction == null || state.isExpanded -> settle(release)
                        isForward || itemCount < 3 -> {
                            state.rotate(1)
                            val out = flyOutTarget(direction, cardSize, dragOffset)
                            flyOut(out, out.getDistance().coerceAtMost(extentOf(direction, cardSize)), release, topFirst = true)
                        }
                        else -> {
                            state.rotate(-1, direction)
                            settle(release)
                        }
                    }
                }
            )
            .padding(horizontal = 12.dp)
            .onSizeChanged { cardSize = it }
            .graphicsLayer {
                val scale = lerp(
                    1f - depth * 0.05f,
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
            .semantics {
                if (index == 0 && itemCount >= 2) {
                    customActions = listOf(
                        CustomAccessibilityAction("Next card") { state.rotate(1); true },
                        CustomAccessibilityAction("Previous card") { state.rotate(-1); true }
                    )
                }
            }
            .clickable(enabled = itemCount >= 2 && (flight == null || index == 0), interactionSource = null, indication = null) {
                when {
                    !state.isExpanded -> state.expand()
                    index == 0 -> state.collapse()
                    else -> {
                        state.rotate(index)
                        state.collapse()
                        scope.launch {
                            tapTilt.snapTo(TapTiltDegrees)
                            tapTilt.animateTo(0f, spring())
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
private val FlightSpring = spring<Offset>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
private val SettleSpring = spring<Offset>(dampingRatio = 0.8f, stiffness = 300f)
