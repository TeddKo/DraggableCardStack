package tddy.ko.cardstack.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
