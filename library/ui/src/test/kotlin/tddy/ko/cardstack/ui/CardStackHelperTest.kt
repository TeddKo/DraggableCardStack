package tddy.ko.cardstack.ui

import org.junit.Assert.assertEquals
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
