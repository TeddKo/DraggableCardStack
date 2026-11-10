package tddy.ko.cardstack.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Velocity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardStackHelperTest {

    @Test
    fun rotate_forward_movesFirstToBack() {
        assertEquals(listOf(1, 2, 0), rotate(listOf(0, 1, 2), 1))
    }

    @Test
    fun rotate_reverse_movesLastToFront() {
        assertEquals(listOf(2, 0, 1), rotate(listOf(0, 1, 2), -1))
    }

    @Test
    fun rotate_byDepth_equalsRepeatedForward() {
        assertEquals(listOf(2, 3, 0, 1), rotate(listOf(0, 1, 2, 3), 2))
    }

    @Test
    fun rotate_emptyOrFullTurn_isIdentity() {
        assertEquals(emptyList<Int>(), rotate(emptyList(), 3))
        assertEquals(listOf(0, 1, 2), rotate(listOf(0, 1, 2), 3))
    }

    @Test
    fun reconcile_grow_appendsNewIndices() {
        assertEquals(listOf(2, 0, 1, 3, 4), reconcile(listOf(2, 0, 1), 5))
    }

    @Test
    fun reconcile_shrink_dropsOutOfRangeKeepingOrder() {
        assertEquals(listOf(2, 0, 1), reconcile(listOf(2, 3, 0, 1), 3))
    }

    @Test
    fun reconcile_sameSize_isIdentity() {
        assertEquals(listOf(1, 0), reconcile(listOf(1, 0), 2))
    }

    @Test
    fun reconcile_toZero_isEmpty() {
        assertEquals(emptyList<Int>(), reconcile(listOf(1, 0), 0))
    }
}

class SwipeDirectionTest {

    private val commit = Offset(100f, 80f)
    private val threshold = 600f

    @Test
    fun slowLongDragRight_commitsRight() {
        assertEquals(DragDirection.RIGHT, swipeDirection(Offset(120f, 5f), Velocity(10f, 0f), commit, threshold))
    }

    @Test
    fun fastShortFlickUp_commitsUp() {
        assertEquals(DragDirection.UP, swipeDirection(Offset(2f, -20f), Velocity(0f, -900f), commit, threshold))
    }

    @Test
    fun fastFlickAgainstOffset_isNull() {
        assertNull(swipeDirection(Offset(20f, 0f), Velocity(-900f, 0f), commit, threshold))
    }

    @Test
    fun shortSlowDrag_isNull() {
        assertNull(swipeDirection(Offset(50f, 0f), Velocity(300f, 0f), commit, threshold))
    }

    @Test
    fun distanceIsPerAxis() {
        assertEquals(DragDirection.DOWN, swipeDirection(Offset(0f, 85f), Velocity.Zero, commit, threshold))
        assertNull(swipeDirection(Offset(0f, 70f), Velocity.Zero, commit, threshold))
    }

    @Test
    fun noMovement_isNull() {
        assertNull(swipeDirection(Offset.Zero, Velocity(900f, 0f), commit, threshold))
    }
}

class GeometryTest {

    private val spacing = 30f
    private val card = Size(300f, 200f)

    @Test
    fun collapsedOffsets_matchLegacy_all8() {
        val peek = mapOf(
            CardAlignment.TOP to Offset(0f, 1f),
            CardAlignment.BOTTOM to Offset(0f, -1f),
            CardAlignment.START to Offset(-1f, 0f),
            CardAlignment.END to Offset(1f, 0f),
            CardAlignment.TOP_START to Offset(-1f, 1f),
            CardAlignment.TOP_END to Offset(1f, 1f),
            CardAlignment.BOTTOM_START to Offset(-1f, -1f),
            CardAlignment.BOTTOM_END to Offset(1f, -1f),
        )
        CardAlignment.entries.forEach { alignment ->
            val p = peek.getValue(alignment)
            assertEquals(Offset(p.x * 2f * spacing, p.y * 2f * spacing), calculateLastOffset(alignment, 2f, spacing))
        }
    }

    @Test
    fun expandedStart_fanFitsWidth() {
        (2..6).forEach { n ->
            val k = calculateExpandedScale(CardAlignment.START, n)
            val lefts = (0 until n).map { calculateExpandedOffset(CardAlignment.START, it.toFloat(), spacing, card, n).x }
            assertEquals(0f, lefts.last(), 0.01f)
            assertEquals(card.width, lefts.first() + k * card.width, 0.01f)
        }
    }

    @Test
    fun expandedEnd_fanFitsWidth() {
        (2..6).forEach { n ->
            val k = calculateExpandedScale(CardAlignment.END, n)
            val rights = (0 until n).map { card.width + calculateExpandedOffset(CardAlignment.END, it.toFloat(), spacing, card, n).x }
            assertEquals(card.width, rights.last(), 0.01f)
            assertEquals(k * card.width, rights.first(), 0.01f)
        }
    }

    @Test
    fun expandedVertical_spacingNeverBelowCollapsed() {
        listOf(10f, 30f, 120f).forEach { s ->
            CardAlignment.entries.filter { it != CardAlignment.START && it != CardAlignment.END }.forEach { alignment ->
                val collapsed = calculateLastOffset(alignment, 3f, s)
                val expanded = calculateExpandedOffset(alignment, 3f, s, card, 4)
                assertTrue(kotlin.math.abs(expanded.y) >= kotlin.math.abs(collapsed.y))
                assertEquals(collapsed.x, expanded.x, 0.001f)
                assertEquals(1f, calculateExpandedScale(alignment, 4), 0f)
            }
        }
    }

    @Test
    fun expandedStep_isQuarterCardHeightAtLeastSpacing() {
        assertEquals(50f, expandedStep(30f, 200f), 0f)
        assertEquals(60f, expandedStep(60f, 200f), 0f)
    }

    @Test
    fun isReverse_followsDominantAxisAndSign() {
        assertTrue(isReverse(Offset(-10f, 3f)))
        assertTrue(isReverse(Offset(2f, 10f)))
        assertTrue(!isReverse(Offset(10f, -3f)))
        assertTrue(!isReverse(Offset(-2f, -10f)))
        assertTrue(!isReverse(Offset.Zero))
    }

    @Test
    fun pullOffset_leftDragMovesBackCardRightBehindStack() {
        val size = Size(300f, 200f)
        assertNull(pullOffset(Offset(10f, 0f), size))
        assertEquals(Offset(50f, 0f), pullOffset(Offset(-50f, 5f), size))
        assertEquals(Offset(300f, 0f), pullOffset(Offset(-400f, 5f), size))
    }

    @Test
    fun pullOffset_downDragMovesBackCardUpBehindStack() {
        val size = Size(300f, 200f)
        assertEquals(Offset(0f, -50f), pullOffset(Offset(4f, 50f), size))
        assertEquals(Offset(0f, -200f), pullOffset(Offset(4f, 500f), size))
    }

    @Test
    fun pullOffset_insideDeadZone_isNull() {
        val size = Size(300f, 200f)
        assertNull(pullOffset(Offset(-20f, 0f), size))
        assertNull(pullOffset(Offset(3f, 12f), size))
    }

    @Test
    fun pullOut_isOneExtentOppositeToSwipe() {
        val size = Size(300f, 200f)
        assertEquals(Offset(300f, 0f), pullOut(Offset(50f, 0f), size))
        assertEquals(Offset(0f, -200f), pullOut(Offset(0f, -50f), size))
    }
}
