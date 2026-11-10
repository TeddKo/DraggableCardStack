package tddy.ko.cardstack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeWithVelocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.unit.height
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        spacingRatio: Float = 0.5f,
    ) {
        composeRule.setContent {
            DraggableCardStack(
                initialItems = items,
                height = cardHeight,
                cardSpacingRatio = spacingRatio,
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

    private fun tapAt(yFromBottomDp: Float) {
        composeRule.onNodeWithTag(StackTag).performTouchInput {
            click(Offset(centerX, bottom - yFromBottomDp.dp.toPx()))
        }
        composeRule.waitForIdle()
    }

    private fun stackHeightDp() = composeRule.onNodeWithTag(StackTag).getUnclippedBoundsInRoot().height

    @Test
    fun tapCollapsed_BOTTOM_expandsAndGrows() {
        val state = CardStackState()
        setStack(listOf("a", "b", "c", "d"), state, spacingRatio = 0.1f)
        val collapsed = stackHeightDp()

        tapAt(cardHeight.value / 2)

        assertTrue(state.isExpanded)
        assertTrue(stackHeightDp() > collapsed)
    }

    @Test
    fun tapBackCardExpanded_rotatesByDepthAndCollapses() {
        val state = CardStackState()
        setStack(listOf("a", "b", "c", "d"), state, spacingRatio = 0.1f)
        tapAt(cardHeight.value / 2)
        val step = cardHeight.value * 0.25f

        tapAt(cardHeight.value + 1.5f * step)

        assertEquals(2, state.frontIndex)
        assertEquals(listOf(2, 3, 0, 1), state.order)
        assertFalse(state.isExpanded)
    }

    @Test
    fun tapFrontExpanded_collapses() {
        val state = CardStackState()
        setStack(listOf("a", "b", "c"), state, spacingRatio = 0.1f)
        tapAt(cardHeight.value / 2)

        tapAt(cardHeight.value / 2)

        assertFalse(state.isExpanded)
        assertEquals(0, state.frontIndex)
    }

    @Test
    fun singleCard_tapDoesNothing() {
        val state = CardStackState()
        setStack(listOf("a"), state)

        tapAt(cardHeight.value / 2)

        assertFalse(state.isExpanded)
    }

    @Test
    fun rightThenLeftFlingDuringFlight_landsCleanly() {
        val state = CardStackState()
        setStack(listOf("a", "b", "c", "d"), state)
        composeRule.mainClock.autoAdvance = false

        flingHorizontal(150f)
        composeRule.mainClock.advanceTimeBy(50)
        flingHorizontal(-150f)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        assertEquals(listOf(0, 1, 2, 3), state.order)
        flingHorizontal(150f)
        assertEquals(1, state.frontIndex)
    }

    @Test
    fun flingOverlappingFlyingCard_advancesNewFront() {
        val state = CardStackState()
        setStack(listOf("a", "b", "c", "d"), state)
        composeRule.mainClock.autoAdvance = false

        flingHorizontal(150f)
        composeRule.mainClock.advanceTimeBy(50)
        flingHorizontal(150f)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        assertEquals(2, state.frontIndex)
    }

    @Test
    fun flingWhileExpanded_isIgnored() {
        val state = CardStackState()
        setStack(listOf("a", "b", "c", "d"), state, spacingRatio = 0.1f)
        tapAt(cardHeight.value / 2)

        flingHorizontal(150f)

        assertTrue(state.isExpanded)
        assertEquals(0, state.frontIndex)
    }

    @Test
    fun initialItemsGrowAndShrink_rendersCurrentItems() {
        val state = CardStackState()
        val items = mutableStateOf(listOf("a", "b", "c"))
        composeRule.setContent {
            DraggableCardStack(
                initialItems = items.value,
                height = cardHeight,
                cardSpacingRatio = 0.5f,
                cardAlignment = CardAlignment.BOTTOM,
                dragAlignment = DragAlignment.HORIZONTAL,
                state = state,
            ) { item -> BasicText(item) }
        }

        composeRule.runOnIdle { items.value = listOf("a", "b", "c", "d", "e") }
        composeRule.onNodeWithText("e").assertExists()
        assertEquals(listOf(0, 1, 2, 3, 4), state.order)

        composeRule.runOnIdle { items.value = listOf("a", "b") }
        composeRule.onNodeWithText("c").assertDoesNotExist()
        composeRule.onNodeWithText("b").assertExists()
        assertEquals(listOf(0, 1), state.order)
    }

    @Test
    fun configChangeRestore_keepsOrderAndExpansion() {
        val tester = StateRestorationTester(composeRule)
        var state = CardStackState()
        tester.setContent {
            state = rememberCardStackState()
            DraggableCardStack(
                initialItems = listOf("a", "b", "c", "d"),
                height = cardHeight,
                cardSpacingRatio = 0.5f,
                cardAlignment = CardAlignment.BOTTOM,
                dragAlignment = DragAlignment.HORIZONTAL,
                state = state,
            ) { item -> BasicText(item) }
        }
        composeRule.runOnIdle {
            state.rotate(1)
            state.expand()
        }

        tester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            assertEquals(listOf(1, 2, 3, 0), state.order)
            assertTrue(state.isExpanded)
        }
    }

    @Test
    fun frontCardSemantics_nextAndPreviousRotate() {
        val state = CardStackState()
        setStack(listOf("a", "b", "c", "d"), state)

        val actions = composeRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.CustomActions))
            .fetchSemanticsNode().config[SemanticsActions.CustomActions]

        composeRule.runOnIdle { actions.first { it.label == "Next card" }.action() }
        assertEquals(1, state.frontIndex)
        composeRule.runOnIdle { actions.first { it.label == "Previous card" }.action() }
        assertEquals(0, state.frontIndex)
    }

    private companion object {
        const val MaxGestureRecompositions = 2
        const val StackTag = "stack"
    }
}
