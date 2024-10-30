package tddy.ko.cardstack.feature

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import tddy.ko.cardstack.desigh_system.theme.CardStackTheme
import tddy.ko.cardstack.ui.CardAlignment
import tddy.ko.cardstack.ui.DragAlignment
import tddy.ko.cardstack.ui.DraggableCardStack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CardStackTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DraggableCardStack(
                        initialItems = images,
                        height = 200.dp,
                        cardSpacingRatio = .1f,
                        cardAlignment = CardAlignment.BOTTOM,
                        dragAlignment = DragAlignment.NONE
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(shape = CircleShape),
                                    model = it.asset,
                                    contentDescription = it.description,
                                    contentScale = ContentScale.Crop,
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = it.description)
                                    Text(text = it.description)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}