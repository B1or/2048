package tech.droi.a2048

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import kotlin.random.Random

class Game2048 {
    private var score = 0

    fun initialize(tiles: Array<Array<MutableIntState>>, showDialog: MutableState<Boolean>, win: MutableState<Boolean>) {
        createGame(showDialog, win)
        drawScene(tiles)
    }

    private fun drawScene(tiles: Array<Array<MutableIntState>>) {
        for (y in 0 ..< SIDE)
            for (x in 0 ..< SIDE)
                tiles[y][x].intValue = gameField[y][x]
    }

    private fun createGame(showDialog: MutableState<Boolean>, win: MutableState<Boolean>) {
        for (y in 0 ..< SIDE)
            for (x in 0 ..< SIDE)
                gameField[y][x] = 0
        createNewNumber(showDialog, win)
        createNewNumber(showDialog, win)
    }

    private fun gameOver(showDialog: MutableState<Boolean>, win: MutableState<Boolean>) {
        win.value = false
        showDialog.value = true
    }

    private fun win(showDialog: MutableState<Boolean>, win: MutableState<Boolean>) {
        win.value = true
        showDialog.value = true
    }

    private fun createNewNumber(showDialog: MutableState<Boolean>, win: MutableState<Boolean>) {
        if (getMaxTileValue() >= 2048) {
            win(showDialog, win)
            return
        }
        var isCreated = false
        do {
            val x: Int = Random.nextInt(SIDE)
            val y: Int = Random.nextInt(SIDE)
            if (gameField[y][x] == 0) {
                gameField[y][x] = if (Random.nextInt(10) < 9) 2 else 4
                isCreated = true
            }
        } while (!isCreated)
    }

    private fun getMaxTileValue(): Int {
        var max = gameField[0][0]
        for (y in 0 until SIDE) {
            for (x in 0 until SIDE) {
                if (gameField[y][x] > max) {
                    max = gameField[y][x]
                }
            }
        }
        return max
    }

    fun move(
        direction: Direction,
        tiles: Array<Array<MutableIntState>>,
        showDialog: MutableState<Boolean>,
        win: MutableState<Boolean>,
        score: MutableIntState
    ) {
        if (canUserMove()) {
            when (direction) {
                Direction.UP -> moveUp(showDialog, win, score)
                Direction.DOWN -> moveDown(showDialog, win, score)
                Direction.LEFT -> moveLeft(showDialog, win, score)
                Direction.RIGHT -> moveRight(showDialog, win, score)
            }
            drawScene(tiles)
        } else
            gameOver(showDialog, win)
    }

    private fun moveLeft(showDialog: MutableState<Boolean>, win: MutableState<Boolean>, score: MutableIntState) {
        var isNewNumberNeeded = false
        for (row in gameField) {
            val wasCompressed: Boolean = compressRow(row)
            val wasMerged: Boolean = mergeRow(row, score)
            if (wasMerged) {
                compressRow(row)
            }
            if (wasCompressed || wasMerged) {
                isNewNumberNeeded = true
            }
        }
        if (isNewNumberNeeded) {
            createNewNumber(showDialog, win)
        }
    }

    private fun moveUp(showDialog: MutableState<Boolean>, win: MutableState<Boolean>, score: MutableIntState) {
        rotateClockwise()
        rotateClockwise()
        rotateClockwise()
        moveLeft(showDialog, win, score)
        rotateClockwise()
    }

    private fun moveRight(showDialog: MutableState<Boolean>, win: MutableState<Boolean>, score: MutableIntState) {
        rotateClockwise()
        rotateClockwise()
        moveLeft(showDialog, win, score)
        rotateClockwise()
        rotateClockwise()
    }

    private fun moveDown(showDialog: MutableState<Boolean>, win: MutableState<Boolean>, score: MutableIntState) {
        rotateClockwise()
        moveLeft(showDialog, win, score)
        rotateClockwise()
        rotateClockwise()
        rotateClockwise()
    }

    private fun rotateClockwise() {
        val result = Array(SIDE) { IntArray(SIDE) }
        for (i in 0 until SIDE) {
            for (j in 0 until SIDE) {
                result[j][SIDE - 1 - i] = gameField[i][j]
            }
        }
        gameField = result
    }

    private fun compressRow(row: IntArray): Boolean {
        var insertPosition = 0
        var result = false
        for (x in 0 until SIDE) {
            if (row[x] > 0) {
                if (x != insertPosition) {
                    row[insertPosition] = row[x]
                    row[x] = 0
                    result = true
                }
                insertPosition++
            }
        }
        return result
    }

    private fun mergeRow(row: IntArray, score: MutableIntState): Boolean {
        var result = false
        for (i in 0 until row.size - 1) {
            if (row[i] != 0 && row[i] == row[i + 1]) {
                row[i] += row[i + 1]
                row[i + 1] = 0
                result = true
                this.score += row[i]
                setScore(score)
            }
        }
        return result
    }

    private fun canUserMove(): Boolean {
        for (y in 0 until SIDE) {
            for (x in 0 until SIDE) {
                if (gameField[y][x] == 0) {
                    return true
                } else if (y < SIDE - 1 && gameField[y][x] == gameField[y + 1][x]) {
                    return true
                } else if ((x < SIDE - 1) && gameField[y][x] == gameField[y][x + 1]) {
                    return true
                }
            }
        }
        return false
    }

    private fun setScore(score: MutableIntState) {
        score.intValue = this.score
    }
}