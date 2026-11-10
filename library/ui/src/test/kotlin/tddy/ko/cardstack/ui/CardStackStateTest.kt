package tddy.ko.cardstack.ui

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.snapshots.Snapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardStackStateTest {

    private val scope = SaverScope { true }

    @Test
    fun initialState_isCollapsedWithoutFront() {
        val state = CardStackState()
        assertFalse(state.isExpanded)
        assertEquals(-1, state.frontIndex)
    }

    @Test
    fun expandAndCollapse_toggleExpansion() {
        val state = CardStackState()
        state.expand()
        assertTrue(state.isExpanded)
        state.collapse()
        assertFalse(state.isExpanded)
    }

    @Test
    fun sync_setsIdentityOrder() {
        val state = CardStackState()
        state.sync(3)
        assertEquals(listOf(0, 1, 2), state.order)
        assertEquals(0, state.frontIndex)
    }

    @Test
    fun rotateForward_advancesFrontIndex() {
        val state = CardStackState()
        state.sync(3)
        state.rotate(1)
        assertEquals(1, state.frontIndex)
        assertEquals(listOf(1, 2, 0), state.order)
    }

    @Test
    fun rotateReverse_bringsLastToFront() {
        val state = CardStackState()
        state.sync(3)
        state.rotate(-1)
        assertEquals(listOf(2, 0, 1), state.order)
    }

    @Test
    fun sync_withSameSize_writesNothing() {
        val state = CardStackState()
        state.sync(2)
        val snapshot = Snapshot.takeMutableSnapshot()
        try {
            snapshot.enter { state.sync(2) }
            assertFalse(snapshot.hasPendingChanges())
        } finally {
            snapshot.dispose()
        }
    }

    @Test
    fun sync_afterShrink_keepsSurvivorOrder() {
        val state = CardStackState()
        state.sync(4)
        state.rotate(2)
        state.sync(3)
        assertEquals(listOf(2, 0, 1), state.order)
    }

    @Test
    fun sync_toEmpty_resetsFrontIndex() {
        val state = CardStackState()
        state.sync(2)
        state.sync(0)
        assertEquals(-1, state.frontIndex)
    }

    @Test
    fun saver_roundTripsOrderAndExpansion() {
        val state = CardStackState()
        state.sync(4)
        state.rotate(1)
        state.expand()

        val saved = with(CardStackState.Saver) { scope.save(state) }
        val restored = CardStackState.Saver.restore(requireNotNull(saved))

        assertNotNull(restored)
        assertEquals(listOf(1, 2, 3, 0), restored?.order)
        assertTrue(restored?.isExpanded == true)
    }
}
