package tddy.ko.cardstack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeWithVelocity
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DraggableCardStackUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val cardHeight = 200.dp

    private fun setStack(
        items: List<String>,
        state: CardStackState,
        cardAlignment: CardAlignment = CardAlignment.BOTTOM,
        dragAlignment: DragAlignment = DragAlignment.HORIZONTAL,
    ) {
        composeRule.setContent {
            DraggableCardStack(
                initialItems = items,
                height = cardHeight,
                cardSpacingRatio = 0.5f,
                cardAlignment = cardAlignment,
                dragAlignment = dragAlignment,
                modifier = Modifier.testTag(StackTag),
                state = state,
            ) { item ->
                Box(Modifier.fillMaxSize().background(Color.Gray)) { BasicText(item) }
            }
        }
        composeRule.waitForIdle()
    }

    private fun flingHorizontal(dx: Float) {
        composeRule.onNodeWithTag(StackTag).performTouchInput {
            val y = bottom - cardHeight.toPx() / 2
            swipeWithVelocity(
                start = Offset(centerX, y),
                end = Offset(centerX + dx.dp.toPx(), y),
                endVelocity = 6000f,
            )
        }
        composeRule.waitForIdle()
    }

    @Test
    fun duplicateItems_renderWithoutCrash() {
        val state = CardStackState()
        setStack(listOf("a", "a", "a"), state)

        assertEquals(0, state.frontIndex)
    }

    @Test
    fun rightFling_advancesFrontIndex() {
        val state = CardStackState()
        setStack(listOf("a", "b", "c", "d"), state)

        flingHorizontal(150f)

        assertEquals(1, state.frontIndex)
    }

    @Test
    fun leftFling_bringsLastToFront() {
        val state = CardStackState()
        setStack(listOf("a", "b", "c", "d"), state)

        flingHorizontal(-150f)

        assertEquals(3, state.frontIndex)
    }

    @Test
    fun upFling_TOP_commits() {
        val state = CardStackState()
        setStack(listOf("a", "b", "c", "d"), state, CardAlignment.TOP, DragAlignment.VERTICAL)

        composeRule.onNodeWithTag(StackTag).performTouchInput {
            val y = top + cardHeight.toPx() / 2
            swipeWithVelocity(Offset(centerX, y), Offset(centerX, y - 150.dp.toPx()), 6000f)
        }
        composeRule.waitForIdle()

        assertEquals(1, state.frontIndex)
    }

    @Test
    fun twoFlingsDuringFlight_advanceByTwo() {
        val state = CardStackState()
        setStack(listOf("a", "b", "c", "d"), state)
        composeRule.mainClock.autoAdvance = false

        flingHorizontal(150f)
        composeRule.mainClock.advanceTimeBy(100)
        flingHorizontal(150f)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        assertEquals(2, state.frontIndex)
    }

    @Test
    fun dragWithoutCommit_doesNotRecomposeContent() {
        val state = CardStackState()
        composeRule.setContent {
            DraggableCardStack(
                initialItems = listOf("a", "b", "c"),
                height = cardHeight,
                cardSpacingRatio = 0.5f,
                cardAlignment = CardAlignment.BOTTOM,
                dragAlignment = DragAlignment.HORIZONTAL,
                modifier = Modifier.testTag(StackTag),
                state = state,
            ) { item ->
                SideEffect { contentComposeCount++ }
                BasicText(item)
            }
        }
        composeRule.waitForIdle()
        contentComposeCount = 0

        composeRule.onNodeWithTag(StackTag).performTouchInput {
            val y = bottom - cardHeight.toPx() / 2
            down(Offset(centerX, y))
            repeat(20) { moveBy(Offset(1.dp.toPx(), 0f)) }
            up()
        }
        composeRule.waitForIdle()

        assertEquals(0, state.frontIndex)
        assertTrue("content recomposed $contentComposeCount times", contentComposeCount <= MaxGestureRecompositions)
    }

    private var contentComposeCount = 0

    private companion object {
        const val MaxGestureRecompositions = 2
        const val StackTag = "stack"
    }
}
