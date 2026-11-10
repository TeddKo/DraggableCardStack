package tddy.ko.cardstack.feature

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import tddy.ko.cardstack.desigh_system.theme.CardStackTheme
import tddy.ko.cardstack.ui.CardAlignment
import tddy.ko.cardstack.ui.DragAlignment
import tddy.ko.cardstack.ui.DraggableCardStack
import tddy.ko.cardstack.ui.rememberCardStackState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CardStackTheme {
                val state = rememberCardStackState()
                var cardAlignment by rememberSaveable { mutableStateOf(CardAlignment.BOTTOM) }
                var dragAlignment by rememberSaveable { mutableStateOf(DragAlignment.NONE) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "front: ${state.frontIndex}")
                    Picker(CardAlignment.entries, cardAlignment) { cardAlignment = it }
                    Picker(DragAlignment.entries, dragAlignment) { dragAlignment = it }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        DraggableCardStack(
                            initialItems = images,
                            height = 300.dp,
                            cardSpacingRatio = .1f,
                            cardAlignment = cardAlignment,
                            dragAlignment = dragAlignment,
                            state = state
                        ) {
                            AsyncImage(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(shape = RoundedCornerShape(8.dp)),
                                model = it.asset,
                                contentDescription = it.description,
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 16.dp, end = 16.dp),
                                text = it.description,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> Picker(options: List<T>, selected: T, onSelect: (T) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach {
            FilterChip(
                selected = it == selected,
                onClick = { onSelect(it) },
                label = { Text(text = it.toString()) }
            )
        }
    }
}
