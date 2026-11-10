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
 * Controls how users can interact with and drag cards in the stack.
 **/
public enum class DragAlignment { VERTICAL, HORIZONTAL, NONE }

/**
 * Calculates the swipe progress as a normalized value between -1 and 1.
 *
 * @param offset The current drag offset
 * @param velocityThresholdPx The velocity threshold in pixels
 * @return The normalized swipe progress
 **/
internal fun calculateSwipeProgress(offset: Offset, velocityThresholdPx: Float): Float {
    val xProgress = offset.x / velocityThresholdPx
    val yProgress = offset.y / velocityThresholdPx
    return when {
        xProgress.absoluteValue > yProgress.absoluteValue -> xProgress.coerceIn(-1f, 1f)
        else -> -yProgress.coerceIn(-1f, 1f)
    }
}

/**
 * Calculates the scale factor for each card in the stack.
 *
 * @param depth The animated position of the card in the stack, 0 being the front
 * @return The calculated scale factor
 **/
internal fun calculateScales(depth: Float, itemCount: Int, stackDragProgress: Float): Float {
    val baseScale = 1f - (depth * 0.05f)
    val scaleIncrement = 0.05f * stackDragProgress
    return when {
        depth > 0f -> baseScale + (scaleIncrement * (itemCount - depth - 1))
            .coerceAtMost(.05f)

        else -> baseScale
    }
}

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
 * Calculates the offset during drag operations based on stack configuration.
 *
 * @param stackDragProgress The current drag progress
 * @param depth The animated position of the card in the stack, 0 being the front
 * @param spacingPx Spacing between cards
 * @param itemCount Total number of cards
 * @param cardAlignment Stack alignment
 * @return The calculated drag-based offset
 **/
internal fun calculateDragBasedOffset(
    stackDragProgress: Float,
    depth: Float,
    spacingPx: Float,
    itemCount: Int,
    cardAlignment: CardAlignment
): Offset {
    val isLast = depth > itemCount - 1.5f && stackDragProgress < 0f
    val progress = if (isLast) {
        (-stackDragProgress).coerceIn(0f, 1f)
    } else {
        (stackDragProgress * (1f - (depth / itemCount))).coerceIn(-1f, 1f)
    }

    val easedProgress = progress * progress * (3 - 2 * progress)

    return when (cardAlignment) {
        CardAlignment.TOP -> Offset(
            0f,
            spacingPx * easedProgress * if (isLast) 1f else -1f
        )
        CardAlignment.BOTTOM -> Offset(0f, spacingPx * easedProgress)
        CardAlignment.TOP_START -> Offset(
            spacingPx * easedProgress,
            -spacingPx * easedProgress
        )
        CardAlignment.BOTTOM_START -> Offset(
            spacingPx * easedProgress,
            spacingPx * easedProgress
        )
        CardAlignment.TOP_END -> Offset(
            -spacingPx * easedProgress,
            -spacingPx * easedProgress
        )
        CardAlignment.BOTTOM_END -> Offset(
            -spacingPx * easedProgress,
            spacingPx * easedProgress
        )
        CardAlignment.START -> Offset(spacingPx * easedProgress, 0f)
        CardAlignment.END -> Offset(-spacingPx * easedProgress, 0f)
    }
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
