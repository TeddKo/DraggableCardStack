package tddy.ko.cardstack.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.unit.Velocity
import kotlin.math.absoluteValue
import kotlin.math.pow

/**
 * Represents the possible directions for card swiping gestures.
 * Used to determine and handle the direction of card movement during swipe animations.
 **/
public enum class DragDirection { LEFT, RIGHT, UP, DOWN }

/**
 * Defines the alignment options for cards within the stack.
 * Controls how cards are positioned and stacked relative to each other.
 **/
public enum class CardAlignment { BOTTOM, BOTTOM_START, BOTTOM_END, TOP, TOP_START, TOP_END, START, END }

/**
 * Specifies the allowed drag directions for card interactions.
 * Controls how users can interact with and drag cards in the stack, NONE following the finger on both axes.
 **/
public enum class DragAlignment { VERTICAL, HORIZONTAL, NONE }

/**
 * Calculates the initial offset for each card based on its position in the stack.
 *
 * @param cardAlignment The alignment of cards in the stack
 * @param depth The animated position of the card in the stack, 0 being the front
 * @param spacingPx The spacing between cards in pixels
 * @return The calculated offset
 **/
internal fun calculateLastOffset(
    cardAlignment: CardAlignment,
    depth: Float,
    spacingPx: Float
): Offset = when (cardAlignment) {
    CardAlignment.TOP -> Offset(0f, spacingPx * depth)
    CardAlignment.TOP_START -> Offset(-spacingPx * depth, spacingPx * depth)
    CardAlignment.TOP_END -> Offset(spacingPx * depth, spacingPx * depth)
    CardAlignment.BOTTOM -> Offset(0f, -spacingPx * depth)
    CardAlignment.BOTTOM_START -> Offset(-spacingPx * depth, -spacingPx * depth)
    CardAlignment.BOTTOM_END -> Offset(spacingPx * depth, -spacingPx * depth)
    CardAlignment.START -> Offset(-spacingPx * depth, 0f)
    CardAlignment.END -> Offset(spacingPx * depth, 0f)
}

/**
 * Applies alignment-based adjustments to card transformation.
 * Adjusts the card position based on its scale and alignment to maintain proper positioning.
 *
 * @param cardAlignment The alignment of cards in the stack
 * @param animatedScale The current scale value of the card
 * @param size The size of the card
 **/
internal fun GraphicsLayerScope.applyAlignmentAdjustment(
    cardAlignment: CardAlignment,
    animatedScale: Float,
    size: Size
) {
    val widthAdjustment = (size.width * (1f - animatedScale)) / 2f
    val heightAdjustment = (size.height * (1f - animatedScale)) / 2f

    when (cardAlignment) {
        CardAlignment.TOP -> translationY += heightAdjustment
        CardAlignment.TOP_START -> {
            translationY += heightAdjustment
            translationX -= widthAdjustment
        }

        CardAlignment.TOP_END -> {
            translationY += heightAdjustment
            translationX += widthAdjustment
        }

        CardAlignment.BOTTOM -> translationY -= heightAdjustment
        CardAlignment.BOTTOM_START -> {
            translationY -= heightAdjustment
            translationX -= widthAdjustment
        }

        CardAlignment.BOTTOM_END -> {
            translationY -= heightAdjustment
            translationX += widthAdjustment
        }

        CardAlignment.START -> translationX -= widthAdjustment
        CardAlignment.END -> translationX += widthAdjustment
    }
}

/**
 * Rotates the card order, a positive [steps] being as many forward swipes (first to back) and a
 * negative one as many reverse swipes (last to front).
 *
 * @param order The current permutation of item indices
 * @param steps The signed number of forward swipes
 * @return The rotated permutation
 **/
internal fun rotate(order: List<Int>, steps: Int): List<Int> {
    if (order.isEmpty()) return order
    val k = steps.mod(order.size)
    return order.drop(k) + order.take(k)
}

/**
 * Fits the card order to a new item count, keeping the surviving order and appending new indices.
 *
 * @param order The current permutation of item indices
 * @param size The new item count
 * @return A permutation of `0 until size`
 **/
internal fun reconcile(order: List<Int>, size: Int): List<Int> {
    val kept = order.filter { it < size }
    return kept + (0 until size).filter { it !in kept }
}


/**
 * Decides whether a release commits a swipe, by drag distance or by velocity along the dominant
 * drag axis, a velocity counting only in the direction of the drag.
 *
 * @param offset The drag offset at release
 * @param velocity The release velocity
 * @param commitDistance The per-axis drag distance that commits on its own
 * @param velocityThreshold The per-axis release velocity that commits in the drag direction
 * @return The committed direction or null when the release only springs back
 **/
internal fun swipeDirection(
    offset: Offset,
    velocity: Velocity,
    commitDistance: Offset,
    velocityThreshold: Float
): DragDirection? {
    val horizontal = offset.x.absoluteValue >= offset.y.absoluteValue
    val o = if (horizontal) offset.x else offset.y
    val v = if (horizontal) velocity.x else velocity.y
    val distance = if (horizontal) commitDistance.x else commitDistance.y
    if (o == 0f) return null
    val committed = o.absoluteValue >= distance || (v.absoluteValue >= velocityThreshold && v * o > 0f)
    return when {
        !committed -> null
        horizontal -> if (o > 0f) DragDirection.RIGHT else DragDirection.LEFT
        else -> if (o > 0f) DragDirection.DOWN else DragDirection.UP
    }
}

/**
 * Whether the accumulated drag is a reverse drag, LEFT or DOWN on the dominant axis, the axis
 * choice being the one of [swipeDirection].
 *
 * @param drag The accumulated drag vector
 * @return True for a LEFT or DOWN drag
 **/
internal fun isReverse(drag: Offset): Boolean =
    if (drag.x.absoluteValue >= drag.y.absoluteValue) drag.x < 0f else drag.y > 0f

/**
 * Resting offset of the pulled card before any drag, one card extent off to the side opposite to
 * the swipe, the right for LEFT and the top for DOWN.
 *
 * @param drag The accumulated reverse drag vector
 * @param size The card size
 * @return The offset of the pulled card relative to its resting point
 **/
internal fun pullStart(drag: Offset, size: Size): Offset =
    if (drag.x.absoluteValue >= drag.y.absoluteValue) Offset(size.width, 0f) else Offset(0f, -size.height)

/**
 * Offset of the card pulled in by a reverse drag, following the finger from [pullStart] and never
 * passing its resting point along the drag axis.
 *
 * @param drag The accumulated drag vector
 * @param size The card size
 * @return The offset relative to the resting point or null when the drag is not a reverse drag
 **/
internal fun pullOffset(drag: Offset, size: Size): Offset? {
    if (!isReverse(drag)) return null
    val pulled = pullStart(drag, size) + drag
    return if (drag.x.absoluteValue >= drag.y.absoluteValue) pulled.copy(x = pulled.x.coerceAtLeast(0f))
    else pulled.copy(y = pulled.y.coerceAtMost(0f))
}


private const val FanStepRatio = 0.25f

/**
 * Distance between expanded cards along the main axis of a vertical stack, at least the collapsed
 * spacing.
 *
 * @param spacingPx The collapsed spacing in pixels
 * @param cardHeightPx The card height in pixels
 * @return The expanded step in pixels
 **/
internal fun expandedStep(spacingPx: Float, cardHeightPx: Float): Float =
    maxOf(spacingPx, FanStepRatio * cardHeightPx)

/**
 * Calculates the scale of every card of the expanded stack. A START or END fan shrinks the cards so
 * that the whole fan fits the card width, the other alignments keep the full size.
 *
 * @param cardAlignment The alignment of cards in the stack
 * @param itemCount Total number of cards
 * @return The expanded scale factor
 **/
internal fun calculateExpandedScale(cardAlignment: CardAlignment, itemCount: Int): Float = when (cardAlignment) {
    CardAlignment.START, CardAlignment.END -> 1f / (1f + FanStepRatio * (itemCount - 1))
    else -> 1f
}

/**
 * Calculates the offset of a card of the expanded stack. Vertical alignments widen the vertical
 * step and keep the horizontal peek, START and END lay the cards out as a horizontal fan whose
 * outer edges coincide with the card edges.
 *
 * @param cardAlignment The alignment of cards in the stack
 * @param depth The animated position of the card in the stack, 0 being the front
 * @param spacingPx The collapsed spacing in pixels
 * @param cardSize The size of the card
 * @param itemCount Total number of cards
 * @return The expanded offset
 **/
internal fun calculateExpandedOffset(
    cardAlignment: CardAlignment,
    depth: Float,
    spacingPx: Float,
    cardSize: Size,
    itemCount: Int
): Offset {
    val peek = calculateLastOffset(cardAlignment, 1f, 1f)
    return when (cardAlignment) {
        CardAlignment.START, CardAlignment.END -> {
            val scale = calculateExpandedScale(cardAlignment, itemCount)
            val inset = cardSize.width * (1f - scale)
            val step = FanStepRatio * scale * cardSize.width
            Offset(-peek.x * inset + peek.x * depth * step, 0f)
        }

        else -> Offset(
            peek.x * depth * spacingPx,
            peek.y * depth * expandedStep(spacingPx, cardSize.height)
        )
    }
}
