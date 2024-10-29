package tddy.ko.cardstack.feature

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
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
                        height = 300.dp,
                        cardSpacingRatio = .1f,
                        cardAlignment = CardAlignment.BOTTOM,
                        dragAlignment = DragAlignment.NONE
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(10.dp))
                        ) {
                            AsyncImage(
                                modifier = Modifier.fillMaxSize(),
                                model = it.asset,
                                contentDescription = it.description,
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                modifier = Modifier
                                    .align(alignment = Alignment.BottomEnd)
                                    .padding(bottom = 10.dp, end = 10.dp),
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