package tddy.ko.cardstack.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import kotlin.math.absoluteValue
import kotlin.math.pow

/**
 * Represents the possible directions for card swiping gestures.
 * Used to determine and handle the direction of card movement during swipe animations.
 **/
enum class DragDirection { LEFT, RIGHT, UP, DOWN }

/**
 * Defines the alignment options for cards within the stack.
 * Controls how cards are positioned and stacked relative to each other.
 **/
enum class CardAlignment { BOTTOM, BOTTOM_START, BOTTOM_END, TOP, TOP_START, TOP_END, START, END }

/**
 * Specifies the allowed drag directions for card interactions.
 * Controls how users can interact with and drag cards in the stack.
 **/
enum class DragAlignment { VERTICAL, HORIZONTAL, NONE }

/**
 * Determines the swipe direction based on velocity and offset.
 *
 * @param velocity The velocity of the drag gesture
 * @param offset The current offset of the drag
 * @return The determined DragDirection or null if not exceeding thresholds
 **/
internal fun determineSwipeDirection(velocity: Velocity, offset: Offset): DragDirection? {
    val isHorizontal = velocity.x.absoluteValue > velocity.y.absoluteValue
    return when {
        isHorizontal && velocity.x > 0 && offset.x > 0 -> DragDirection.RIGHT
        isHorizontal && velocity.x < 0 && offset.x < 0 -> DragDirection.LEFT
        !isHorizontal && velocity.y > 0 && offset.y > 0 -> DragDirection.DOWN
        !isHorizontal && velocity.y < 0 && offset.y < 0 -> DragDirection.UP
        else -> null
    }
}

/**
 * Checks if the swipe velocity exceeds the defined threshold.
 *
 * @param velocity The velocity of the drag gesture
 * @param velocityThresholdPx The threshold value in pixels
 * @return True if the velocity exceeds the threshold
 **/
internal fun isSwipeVelocityExceeded(velocity: Velocity, velocityThresholdPx: Float): Boolean =
    velocity.x.absoluteValue > velocityThresholdPx || velocity.y.absoluteValue > velocityThresholdPx

/**
 * Calculates the target offset for card animation based on swipe direction.
 *
 * @param direction The direction of the swipe
 * @param size The size of the card
 * @param offset The current offset
 * @param cardAlignment The alignment of cards in the stack
 * @return The target offset for the animation
 **/
internal fun calculateTargetOffset(
    direction: DragDirection,
    size: IntSize,
    offset: Offset,
    cardAlignment: CardAlignment
): Offset = when (direction) {
    DragDirection.LEFT -> Offset(-size.width.toFloat() * 1.5f, offset.y)
    DragDirection.RIGHT -> Offset(size.width.toFloat() * 1.5f, offset.y)
    DragDirection.UP -> Offset(offset.x, calculateVerticalOffset(cardAlignment, size, offset))
    DragDirection.DOWN -> Offset(offset.x, size.height.toFloat() * 1.5f)
}

/**
 * Calculates the vertical offset for card animation based on alignment.
 *
 * @param cardAlignment The alignment of cards in the stack
 * @param size The size of the card
 * @param offset The current offset
 * @return The calculated vertical offset
 **/
internal fun calculateVerticalOffset(
    cardAlignment: CardAlignment,
    size: IntSize,
    offset: Offset
): Float = when (cardAlignment) {
    CardAlignment.BOTTOM, CardAlignment.BOTTOM_START, CardAlignment.BOTTOM_END -> -size.height.toFloat() * 1.5f
    else -> offset.y
}

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
 * @param index The index of the card in the stack
 * @return The calculated scale factor
 **/
internal fun calculateScales(index: Int, itemCount: Int, stackDragProgress: Float): Float {
    val baseScale = 1f - (index * 0.05f)
    val scaleIncrement = 0.05f * stackDragProgress
    return when {
        index > 0 -> baseScale + (scaleIncrement * (itemCount - index - 1))
            .coerceAtMost(.05f)

        else -> baseScale
    }
}

/**
 * Calculates the initial offset for each card based on its position in the stack.
 *
 * @param cardAlignment The alignment of cards in the stack
 * @param index The index of the card
 * @param spacingPx The spacing between cards in pixels
 * @return The calculated offset
 **/
internal fun calculateLastOffset(
    cardAlignment: CardAlignment,
    index: Int,
    spacingPx: Float
): Offset = when (cardAlignment) {
    CardAlignment.TOP -> Offset(0f, spacingPx * index)
    CardAlignment.TOP_START -> Offset(-spacingPx * index, spacingPx * index)
    CardAlignment.TOP_END -> Offset(spacingPx * index, spacingPx * index)
    CardAlignment.BOTTOM -> Offset(0f, -spacingPx * index)
    CardAlignment.BOTTOM_START -> Offset(-spacingPx * index, -spacingPx * index)
    CardAlignment.BOTTOM_END -> Offset(spacingPx * index, -spacingPx * index)
    CardAlignment.START -> Offset(-spacingPx * index, 0f)
    CardAlignment.END -> Offset(spacingPx * index, 0f)
}

/**
 * Calculates the offset during drag operations based on stack configuration.
 *
 * @param stackDragProgress The current drag progress
 * @param index Card index in the stack
 * @param spacingPx Spacing between cards
 * @param itemCount Total number of cards
 * @param cardAlignment Stack alignment
 * @return The calculated drag-based offset
 **/
internal fun calculateDragBasedOffset(
    stackDragProgress: Float,
    index: Int,
    spacingPx: Float,
    itemCount: Int,
    cardAlignment: CardAlignment
): Offset {
    val progress = if (index == itemCount - 1 && stackDragProgress < 0f) {
        (-stackDragProgress).coerceIn(0f, 1f)
    } else {
        (stackDragProgress * (1f - (index.toFloat() / itemCount))).coerceIn(-1f, 1f)
    }

    val easedProgress = progress * progress * (3 - 2 * progress)

    return when (cardAlignment) {
        CardAlignment.TOP -> Offset(
            0f,
            spacingPx * easedProgress * if (index == itemCount - 1 && stackDragProgress < 0f) 1f else -1f
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