package tddy.ko.cardstack.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.Job

/**
 * State holder of a [DraggableCardStack] keeping the expansion flag and the card order.
 * The order is a permutation of indices into the item list, so duplicate items are safe and the
 * state survives configuration change.
 **/
@Stable
public class CardStackState internal constructor(
    initiallyExpanded: Boolean = false,
    initialOrder: List<Int> = emptyList(),
) {

    /** Whether every card is spread out instead of stacked. */
    public var isExpanded: Boolean by mutableStateOf(initiallyExpanded)
        private set

    internal var order: List<Int> by mutableStateOf(initialOrder)
        private set

    /** Offset of the card pulled in from the back by a reverse drag, read in layout and draw only. */
    internal var pull: Offset? by mutableStateOf(null)

    /** Item index of the pulled card. */
    internal var pulledItem: Int = -1

    /** Spring of the pulled card after release. */
    internal var pullJob: Job? = null

    /** Index in the item list of the front card, `-1` for an empty stack. */
    public val frontIndex: Int get() = order.firstOrNull() ?: -1

    /** Spreads the cards out. */
    public fun expand() {
        isExpanded = true
    }

    /** Stacks the cards back up. */
    public fun collapse() {
        isExpanded = false
    }

    internal fun rotate(steps: Int) {
        order = rotate(order, steps)
    }

    internal fun sync(size: Int) {
        if (order.size == size && order.all { it < size }) return
        order = reconcile(order, size)
    }

    internal companion object {
        val Saver: Saver<CardStackState, Any> = listSaver(
            save = { listOf(it.isExpanded, it.order) },
            restore = { saved ->
                @Suppress("UNCHECKED_CAST")
                CardStackState(saved[0] as Boolean, saved[1] as List<Int>)
            },
        )
    }
}

/** Creates a [CardStackState] remembered across recomposition and configuration change. */
@Composable
public fun rememberCardStackState(): CardStackState =
    rememberSaveable(saver = CardStackState.Saver) { CardStackState() }
