package org.example.grid

import org.example.cells.*
import org.example.level.Coordinate
import org.example.level.Grid

class GridGeneratorWithPlayerAndBunnyIterator(private val grid: Map<Coordinate, Cell>, private val width: Int, private val height: Int) : Iterator<Grid> {
    private val coordinateIterator = generateAllPlacements(grid)

    override fun hasNext(): Boolean {
        return coordinateIterator.hasNext()
    }

    override fun next(): Grid {
        val coordinates = coordinateIterator.next()
        val player = coordinates.first
        val bunny = coordinates.second
        val mutableGrid = grid.toMutableMap()
        mutableGrid[player] = PlayerCell(Mask.FOX)
        mutableGrid[bunny] = BunnyCell()
        return Grid(mutableGrid, width, height, player, null, bunny)
    }

    private fun generateAllPlacements(grid: Map<Coordinate, Cell>): Iterator<Pair<Coordinate, Coordinate>> {
        val emptyCells = grid.filterValues { it is EmptyCell }.keys.toList()

        return iterator {
            for (i in emptyCells.indices) {
                for (j in emptyCells.indices) {
                    if (i != j) {
                        yield(Pair(emptyCells[i], emptyCells[j]))
                    }
                }
            }
        }
    }
}
