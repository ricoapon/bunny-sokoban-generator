package org.example.level

import org.example.cells.*
import kotlin.math.abs

data class Coordinate(val x: Int, val y: Int) {
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

class Grid(private val grid: MutableMap<Coordinate, Cell>, val width: Int, val height: Int) {
    // These are singular and often used, so keep track of those.
    private var playerCoordinate: Coordinate = grid.filter { it.value is PlayerCell }.keys.first()
    private var droppedMaskCoordinate: Coordinate? = null
    private var bunnyCoordinate: Coordinate = grid.filter { it.value is BunnyCell }.keys.first()
    private var caughtBunny = false

    fun getCell(coordinate: Coordinate): Cell {
        return grid[coordinate]!!
    }

    fun isCoordinateOnGrid(c: Coordinate): Boolean {
        return c.x >= 0 && c.y >= 0 && c.x < width && c.y < height
    }

    private fun setCell(coordinate: Coordinate, cell: Cell) {
        grid[coordinate] = cell
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
        return CellCoordinate(grid[droppedMaskCoordinate]!!, droppedMaskCoordinate!!)
    }

    fun caughtBunny(): Boolean {
        return caughtBunny
    }

    fun movePlayerUp() {
        movePlayer(Coordinate(0, -1))
    }

    fun movePlayerDown() {
        movePlayer(Coordinate(0, 1))
    }

    fun movePlayerLeft() {
        movePlayer(Coordinate(-1, 0))
    }

    fun movePlayerRight() {
        movePlayer(Coordinate(1, 0))
    }

    fun pickupMask() {
        val playerCell = getCell(playerCoordinate)
        if (playerCell !is PlayerOnTopOfDroppedMask) {
            throw RuntimeException("Cannot pickup mask if player is not on top of it")
        }

        setCell(playerCoordinate, playerCell.playerCell)
    }

    fun dropMask() {
        if (droppedMaskCoordinate != null) {
            throw RuntimeException("Cannot drop mask if player has already dropped it")
        }

        setCell(
            playerCoordinate,
            PlayerOnTopOfDroppedMask(getCell(playerCoordinate) as PlayerCell, DroppedMaskCell(Mask.FOX))
        )
    }

    fun switchPlayerMask(newMask: Mask) {
        setCell(playerCoordinate, PlayerCell(newMask))
    }

    private fun movePlayer(direction: Coordinate) {
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
            is WallCell -> throw RuntimeException("Cannot move player into a wall")
            is BunnyCell -> {
                setCell(from, cellToLeaveBehind)
                setCell(to, playerCell)
                caughtBunny = true
            }

            is DroppedMaskCell -> {
                setCell(from, cellToLeaveBehind)
                setCell(to, PlayerOnTopOfDroppedMask(playerCell as PlayerCell, toCell))
            }

            is EmptyCell -> {
                setCell(from, cellToLeaveBehind)
                setCell(to, playerCell)
            }

            is PlayerCell -> throw RuntimeException("Player should never move on top of player, because there is only one player")
            is PlayerOnTopOfDroppedMask -> throw RuntimeException("Player should never move on top of player on top of mask, because there is only one player")
        }
        playerCoordinate = to
    }

    fun moveBunny(direction: Coordinate) {
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
                setCell(from, EmptyCell())
                setCell(to, bunnyCell)
            }
        }
        bunnyCoordinate = to
    }

    fun state(): Any {
        // Both Cell and Coordinate are data classes, therefore immutable. This copy will never be able to change.
        return grid.toMap()
    }
}
