package tech.droi.a2048

import kotlin.random.Random

class Game2048 {

    var gameField = Array(SIDE) { IntArray(SIDE) }
    private var score = 0
    private var gameState: GameState = GameState.PLAY

    fun createGame() {
        for (y in 0 ..< SIDE)
            for (x in 0 ..< SIDE)
                gameField[y][x] = 0
        score = 0
        gameState = GameState.PLAY
        createNewNumber()
        createNewNumber()
    }

    private fun createNewNumber() {
        if (getMaxTileValue() >= 2048) {
            gameState = GameState.WIN
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

    fun move(direction: Direction) {
        if (canUserMove()) {
            when (direction) {
                Direction.UP -> moveUp()
                Direction.DOWN -> moveDown()
                Direction.LEFT -> moveLeft()
                Direction.RIGHT -> moveRight()
            }
        } else
            gameState = GameState.GAME_OVER
    }

    private fun moveLeft() {
        var isNewNumberNeeded = false
        for (row in gameField) {
            val wasCompressed: Boolean = compressRow(row)
            val wasMerged: Boolean = mergeRow(row)
            if (wasMerged) {
                compressRow(row)
            }
            if (wasCompressed || wasMerged) {
                isNewNumberNeeded = true
            }
        }
        if (isNewNumberNeeded) {
            createNewNumber()
        }
    }

    private fun moveUp() {
        rotateClockwise()
        rotateClockwise()
        rotateClockwise()
        moveLeft()
        rotateClockwise()
    }

    private fun moveRight() {
        rotateClockwise()
        rotateClockwise()
        moveLeft()
        rotateClockwise()
        rotateClockwise()
    }

    private fun moveDown() {
        rotateClockwise()
        moveLeft()
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

    private fun mergeRow(row: IntArray): Boolean {
        var result = false
        for (i in 0 until row.size - 1) {
            if (row[i] != 0 && row[i] == row[i + 1]) {
                row[i] += row[i + 1]
                row[i + 1] = 0
                result = true
                score += row[i]
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

    companion object {
        const val SIDE = 4
    }
}