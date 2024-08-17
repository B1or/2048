package tech.droi.a2048

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat.finishAffinity
import tech.droi.a2048.ui.theme.A2048Theme
import kotlin.math.abs
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.droi.a2048.Game2048.Companion.SIDE

private const val PADDING = 8
private const val SWIPE = 24F

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Greeting(
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val sideCard = min(screenHeight, screenWidth) - 2 * PADDING.dp
    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(PADDING.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(sideCard),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = stringResource(R.string.score_) + " ${viewModel.score.intValue}",
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Spacer(
                    modifier = Modifier
                        .height(PADDING.dp)
                )
                GameField(sideCard, viewModel)
            }
        }
        Configuration.ORIENTATION_LANDSCAPE -> {
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .padding(PADDING.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.score_) + " ${viewModel.score.intValue}",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                Spacer(
                    modifier = Modifier
                        .width(PADDING.dp)
                )
                GameField(sideCard, viewModel)
            }
        }
        else -> {
            // TODO
        }
    }
    if (viewModel.state.value != GameState.PLAY)
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Column(modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(
                            when (viewModel.state.value) {
                                GameState.WIN -> R.string.win
                                GameState.GAME_OVER -> R.string.game_over
                                GameState.PLAY -> R.string.played
                            }
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 24.sp
                    )
                    Row {
                        TextButton(
                            onClick = {
                                viewModel.newGame()
                            }
                        ) {
                            Text(
                                stringResource(R.string.play),
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontSize = 24.sp
                            )
                        }
                        TextButton(
                            onClick = {
                                val activity = (context as? Activity)
                                if (activity != null)
                                    finishAffinity(activity)
                            }
                        ) {
                            Text(
                                stringResource(R.string.exit),
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }
        }
    BackHandler {
        val activity = (context as? Activity)
        if (activity != null)
            finishAffinity(activity)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    A2048Theme {
        Greeting()
    }
}

@Composable
fun GameField(sideCard: Dp, viewModel: GameViewModel) {
    var oldPointerId = -1L
    Card(
        modifier = Modifier
            .width(sideCard)
            .height(sideCard)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { pointerInputChange: PointerInputChange, offset: Offset ->
                        if (pointerInputChange.id.value > oldPointerId) {
                            if (abs(offset.x) > SWIPE && abs(offset.x) > 2 * abs(offset.y)) {
                                oldPointerId = pointerInputChange.id.value
                                if (offset.x > 0)
                                    viewModel.move(Direction.RIGHT)
                                else
                                    viewModel.move(Direction.LEFT)
                            } else if (abs(offset.y) > SWIPE && abs(offset.y) > 2 * abs(offset.x)) {
                                oldPointerId = pointerInputChange.id.value
                                if (offset.y > 0)
                                    viewModel.move(Direction.DOWN)
                                else
                                    viewModel.move(Direction.UP)
                            }
                        }
                    }
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        val sideButton = (sideCard - PADDING.dp * (SIDE + 1)) / SIDE
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = PADDING.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (y in 0 ..< SIDE)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PADDING.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (x in 0 ..< SIDE)
                        if (viewModel.tiles[y][x].intValue == 0)
                            Spacer(
                                modifier = Modifier
                                    .width(sideButton)
                                    .height(sideButton)
                            )
                        else
                            Button(
                                onClick = {
                                },
                                modifier = Modifier
                                    .width(sideButton)
                                    .height(sideButton),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = getColorByValue(viewModel.tiles[y][x].intValue))
                            ) {
                                Text(
                                    text = viewModel.tiles[y][x].intValue.toString(),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                }
        }
    }
}

@Composable
private fun getColorByValue(value: Int): Color {
    return when (value) {
        0 -> colorResource(R.color.white)
        2 -> colorResource(R.color.plum)
        4 -> colorResource(R.color.slate_blue)
        8 -> colorResource(R.color.dodger_blue)
        16 -> colorResource(R.color.dark_turquoise)
        32 -> colorResource(R.color.medium_sea_green)
        64 -> colorResource(R.color.lime_green)
        128 -> colorResource(R.color.dark_orange)
        256 -> colorResource(R.color.salmon)
        512 -> colorResource(R.color.orange_red)
        1024 -> colorResource(R.color.deep_pink)
        2048 -> colorResource(R.color.medium_violet_red)
        else -> colorResource(R.color.black)
    }
}
