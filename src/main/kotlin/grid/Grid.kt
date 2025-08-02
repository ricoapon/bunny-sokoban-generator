package org.example.level

import org.example.ai.BunnyAI
import org.example.cells.*
import kotlin.math.abs

data class Coordinate(val x: Int, val y: Int) {
    companion object {
        val UP = Coordinate(0, -1)
        val DOWN = Coordinate(0, 1)
        val LEFT = Coordinate(-1, 0)
        val RIGHT = Coordinate(1, 0)
    }

    operator fun plus(other: Coordinate) = Coordinate(x + other.x, y + other.y)
    operator fun minus(other: Coordinate) = Coordinate(x - other.x, y - other.y)
    operator fun div(other: Int) = Coordinate(x / other, y / other)

    fun manhattanDistance(other: Coordinate) = abs(x - other.x) + abs(y - other.y)

    fun turnClockwise(): Coordinate {
        return Coordinate(y, -x)
    }

    fun turnCounterClockwise(): Coordinate {
        return Coordinate(-y, x)
    }

    fun left(straightDirection: Coordinate): Coordinate {
        return Coordinate(x + straightDirection.y, y - straightDirection.x)
    }

    fun right(straightDirection: Coordinate): Coordinate {
        return Coordinate(x - straightDirection.y, y + straightDirection.x)
    }
}

data class CellCoordinate(val cell: Cell, val coordinate: Coordinate)

// Grid must be immutable, otherwise it won't work properly for BFS.
data class Grid(
    private val grid: Map<Coordinate, Cell>,
    val width: Int,
    val height: Int,
    val playerCoordinate: Coordinate,
    val droppedMaskCoordinate: Coordinate?,
    val bunnyCoordinate: Coordinate,
    val caughtBunny: Boolean = false
) {
    companion object {
        fun fromIntegerList(integerGrid: List<List<Int>>): Grid {
            val grid = mutableMapOf<Coordinate, Cell>()
            val height = integerGrid.size
            val width = integerGrid[0].size
            for (y in 0 until height) {
                for (x in 0 until width) {
                    grid[Coordinate(x, y)] = Cell.convertIntToCell(integerGrid[y][x])
                }
            }
            return Grid(
                grid, width, height, grid.filter { it.value is PlayerCell }.keys.first(),
                grid.filter { it.value is DroppedMaskCell }.keys.firstOrNull(),
                grid.filter { it.value is BunnyCell }.keys.first()
            )
        }

        // Put it here so it is easier to test.
        fun hasRepeatingCycle(history: List<Coordinate>, requiredRepetitions: Int = 2): Boolean {
            val maxCycleLength = history.size / requiredRepetitions

            for (cycleLength in 1..maxCycleLength) {
                val cycle = history.takeLast(cycleLength)
                var matchCount = 0

                for (i in (history.size - cycleLength * requiredRepetitions)..(history.size - cycleLength) step cycleLength) {
                    if (i < 0) break
                    val segment = history.subList(i, i + cycleLength)
                    if (segment == cycle) {
                        matchCount++
                    } else {
                        break
                    }
                }

                if (matchCount >= requiredRepetitions - 1) {
                    return true
                }
            }

            return false
        }
    }

    fun getCell(coordinate: Coordinate): Cell {
        return grid[coordinate]!!
    }

    fun isCoordinateOnGrid(c: Coordinate): Boolean {
        return c.x >= 0 && c.y >= 0 && c.x < width && c.y < height
    }

    // Moving is changing from and to, so we often do two cells at once.
    private fun setCell(
        coordinate: Coordinate, cell: Cell, coordinate2: Coordinate? = null, cell2: Cell? = null,
        newPlayerCoordinate: Coordinate = playerCoordinate,
        newDroppedMaskCoordinate: Coordinate? = droppedMaskCoordinate,
        newBunnyCoordinate: Coordinate = bunnyCoordinate,
        newCaughtBunny: Boolean = caughtBunny
    ): Grid {
        val mutableGrid = grid.toMutableMap()
        mutableGrid[coordinate] = cell
        if (coordinate2 != null && cell2 != null) {
            mutableGrid[coordinate2] = cell2
        }
        return Grid(
            mutableGrid,
            width,
            height,
            newPlayerCoordinate,
            newDroppedMaskCoordinate,
            newBunnyCoordinate,
            newCaughtBunny
        )
    }

    fun getPlayer(): CellCoordinate {
        return CellCoordinate(grid[playerCoordinate]!!, playerCoordinate)
    }

    fun getBunny(): CellCoordinate {
        return CellCoordinate(grid[bunnyCoordinate]!!, bunnyCoordinate)
    }

    fun getMask(): CellCoordinate? {
        if (droppedMaskCoordinate == null) {
            return null
        }
        return CellCoordinate(grid[droppedMaskCoordinate]!!, droppedMaskCoordinate)
    }

    fun pickupMask(): Grid {
        val playerCell = getCell(playerCoordinate)
        if (playerCell !is PlayerOnTopOfDroppedMask) {
            throw RuntimeException("Cannot pickup mask if player is not on top of it")
        }

        return setCell(playerCoordinate, playerCell.playerCell, newDroppedMaskCoordinate = null)
    }

    fun dropMask(): Grid {
        if (droppedMaskCoordinate != null) {
            throw RuntimeException("Cannot drop mask if player has already dropped it")
        }

        return setCell(
            playerCoordinate,
            PlayerOnTopOfDroppedMask(getCell(playerCoordinate) as PlayerCell, DroppedMaskCell(Mask.FOX)),
            newDroppedMaskCoordinate = playerCoordinate
        )
    }

    fun switchPlayerMask(newMask: Mask): Grid {
        return setCell(playerCoordinate, PlayerCell(newMask))
    }

    fun movePlayer(direction: Coordinate): Grid {
        if (abs(direction.x) + abs(direction.y) > 1) {
            throw RuntimeException("Cannot move player more than one step")
        }

        val from = playerCoordinate
        val to = playerCoordinate + direction
        var playerCell: Cell = getCell(playerCoordinate)
        var cellToLeaveBehind: Cell = EmptyCell()
        if (playerCell is PlayerOnTopOfDroppedMask) {
            cellToLeaveBehind = playerCell.droppedMaskCell
            playerCell = playerCell.playerCell
        }

        when (val toCell = getCell(to)) {
            // Do nothing, so that the solver can just continue.
            is WallCell -> {
                return this
            }

            is BunnyCell -> {
                return setCell(from, cellToLeaveBehind, to, playerCell, newPlayerCoordinate = to, newCaughtBunny = true)
            }

            is DroppedMaskCell -> {
                return setCell(
                    from,
                    cellToLeaveBehind,
                    to,
                    PlayerOnTopOfDroppedMask(playerCell as PlayerCell, toCell),
                    newPlayerCoordinate = to
                )
            }

            is EmptyCell -> {
                return setCell(from, cellToLeaveBehind, to, playerCell, newPlayerCoordinate = to)
            }

            is PlayerCell -> throw RuntimeException("Player should never move on top of player, because there is only one player")
            is PlayerOnTopOfDroppedMask -> throw RuntimeException("Player should never move on top of player on top of mask, because there is only one player")
        }
    }

    private fun moveBunny(direction: Coordinate): Grid {
        if (abs(direction.x) + abs(direction.y) > 1) {
            throw RuntimeException("Cannot move bunny more than one step")
        }

        val from = bunnyCoordinate
        val to = bunnyCoordinate + direction
        val bunnyCell: Cell = getCell(bunnyCoordinate)

        when (val toCell = getCell(to)) {
            is WallCell, is DroppedMaskCell, is PlayerCell, is PlayerOnTopOfDroppedMask -> throw RuntimeException("Invalid bunny move into ${toCell.javaClass.simpleName}")
            is BunnyCell -> {
                throw RuntimeException("Bunny should never move on top of bunny, because there is only one bunny")
            }

            is EmptyCell -> {
                val grid2 = setCell(from, EmptyCell())
                return grid2.setCell(to, bunnyCell, newBunnyCoordinate = to)
            }
        }
    }

    fun moveBunnyUntilNoMoreMovesPossible(bunnyAI: BunnyAI): Grid {
        if (caughtBunny) {
            return this
        }

        // The bunny is allowed to make any number of moves, which can lead to infinity in some cases. Keep track of the
        // full history to see if this happens. We only need to keep track of the coordinates, since nothing on the board
        // can change except for the bunny moving around.
        val history = mutableListOf(bunnyCoordinate)

        var mainGrid = this
        var direction = bunnyAI.getNextBunnyDirection(mainGrid)
        while (direction != null) {
            mainGrid = mainGrid.moveBunny(direction)
            history.add(mainGrid.bunnyCoordinate)

            if (hasRepeatingCycle(history)) {
                return mainGrid.setCell(
                    mainGrid.bunnyCoordinate,
                    (mainGrid.getBunny().cell as BunnyCell).copy(isExhausted = true)
                )
            }

            direction = bunnyAI.getNextBunnyDirection(mainGrid)
        }

        return mainGrid
    }

    fun movePlayerThenBunny(direction: Coordinate, bunnyAI: BunnyAI): Grid {
        val newGrid = movePlayer(direction)
        return newGrid.moveBunnyUntilNoMoreMovesPossible(bunnyAI)
    }

    fun toAsciiArt(): String {
        fun cellToChar(cell: Cell): Char = when (cell) {
            is WallCell -> '#'
            is EmptyCell -> '.'
            is BunnyCell -> if (cell.isExhausted) 'E' else 'B'
            is PlayerCell -> 'P'
            is DroppedMaskCell -> 'D'
            is PlayerOnTopOfDroppedMask -> 'T'
        }

        val builder = StringBuilder()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val coordinate = Coordinate(x, y)
                val cell = getCell(coordinate)
                builder.append(cellToChar(cell))
            }
            builder.append('\n')
        }

        return builder.toString()
    }
}
