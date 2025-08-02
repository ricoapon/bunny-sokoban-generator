@file:Suppress("DuplicatedCode")

package org.example.ai

import org.example.cells.*
import org.example.level.CellCoordinate
import org.example.level.Coordinate
import org.example.level.Grid
import kotlin.math.abs

interface BunnyAI {
    fun getNextBunnyDirection(grid: Grid): Coordinate?
}

class AvoidDeadEndsAI : BunnyAI {
    override fun getNextBunnyDirection(grid: Grid): Coordinate? {
        if ((grid.getBunny().cell as BunnyCell).isExhausted) {
            return null
        }

        // Always check for the player first, then the mask.=
        if (isPlayerInSightOfBunny(grid)) {
            val result = getStepForEntity(grid, grid.getPlayer())
            if (result != null) {
                return result
            }
        }

        if (!isMaskInSightOfBunny(grid)) {
            return null
        }
        return getStepForEntity(grid, grid.getMask()!!)
    }

    // Entity could either be the player or the mask.
    private fun getStepForEntity(grid: Grid, entity: CellCoordinate): Coordinate? {
        val b = grid.getBunny().coordinate
        val e = entity.coordinate
        // We want to move away from the player. We orient the direction based on the line between the player and the
        // bunny. The player could be 2 squares away. Make sure direction is normalized to length 1.
        val straight = (b - e) / b.manhattanDistance(e)
        val left = straight.turnCounterClockwise()
        val right = straight.turnClockwise()

        val directions = listOf(straight, left, right)

        val validDirection = directions.firstOrNull { d ->
            // First see if we can move to an empty cell that does not lead to a dead-end.
            val cell = grid.getCell(b + d)
            cell is EmptyCell && !leadsToVisibleDeadEnd(grid, b, d)
        } ?: directions.firstOrNull { d ->
            // Otherwise we pick a direction that does lead to a dead-end.
            val cell = grid.getCell(b + d)
            cell is EmptyCell
        } ?: return null  // No valid direction found, so we stand still.

        return validDirection
    }

    private fun isPlayerInSightOfBunny(grid: Grid): Boolean {
        val p = grid.getPlayer().coordinate
        val b = grid.getBunny().coordinate

        if (b.x != p.x && b.y != p.y) {
            return false
        }

        val distance = abs(b.x - p.x) + abs(b.y - p.y)
        if (distance > 2) {
            return false
        }

        if (distance == 2) {
            val middle = (b + p) / 2
            // We can look through anything except walls.
            if (grid.getCell(middle) is WallCell) {
                return false
            }
        }

        return true
    }

    private fun isMaskInSightOfBunny(grid: Grid): Boolean {
        val m = grid.getMask()?.coordinate ?: return false
        val b = grid.getBunny().coordinate

        if (b.x != m.x && b.y != m.y) {
            return false
        }

        val distance = b.manhattanDistance(m)

        // Mask activates with a smaller distance.
        return distance <= 1
    }

    private fun leadsToVisibleDeadEnd(grid: Grid, startingCoordinate: Coordinate, direction: Coordinate): Boolean {
        // If we keep moving into the direction, we should find that we go either left or right. However, squares in
        // front of a player or mask will be considered non-accessible, since the bunny will try and move in a different
        // direction (run away).

        // First find all valid squares that the bunny can move to in that direction.
        val validCoordinates = mutableListOf<Coordinate>()
        var step = startingCoordinate + direction
        while (grid.isCoordinateOnGrid(step) && grid.getCell(step) is EmptyCell) {
            validCoordinates.add(step)
            step += direction
        }

        // If true, the final square was either a mask or a player.
        val endedInEntity = grid.isCoordinateOnGrid(step) && grid.getCell(step) !is WallCell
        if (endedInEntity) {
            when (val finalCell = grid.getCell(step)) {
                // These cases cannot happen.
                is EmptyCell, is BunnyCell, is WallCell -> throw RuntimeException("Final cell should not be ${finalCell.javaClass.simpleName}")
                is DroppedMaskCell -> {
                    validCoordinates.removeLastOrNull()
                }
                is PlayerCell, is PlayerOnTopOfDroppedMask -> {
                    validCoordinates.removeLastOrNull()
                    validCoordinates.removeLastOrNull()
                }
            }
        }

        // For each of the valid coordinates, check if we can go either left or right. If we can, then we consider it
        // not a dead-end (because we can see another route).
        for (validCoordinate in validCoordinates) {
            val left = validCoordinate.left(direction)
            if (grid.getCell(left) is EmptyCell) {
                return false
            }
            val right = validCoordinate.right(direction)
            if (grid.getCell(right) is EmptyCell) {
                return false
            }
        }

        return true
    }
}
