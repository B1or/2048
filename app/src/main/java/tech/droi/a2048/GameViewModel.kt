package tech.droi.a2048

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import tech.droi.a2048.Game2048.Companion.SIDE

class GameViewModel: ViewModel() {
    private val game2048: Game2048 = Game2048()
    val tiles: Array<Array<MutableIntState>> = Array(SIDE) { Array(SIDE) { mutableIntStateOf(0) } }
    val state: MutableState<GameState> = mutableStateOf(GameState.PLAY)
    val score: MutableIntState = mutableIntStateOf(0)

    init {
        game2048.createGame()
        reField()
    }

    fun newGame() {
        game2048.createGame()
    }

    fun move(direction: Direction) {
        game2048.move(direction)
    }

    private fun reField() {
        for (y in 0 until SIDE)
            for (x in 0 until SIDE)
                tiles[y][x].intValue = game2048.gameField[y][x]
    }
}
