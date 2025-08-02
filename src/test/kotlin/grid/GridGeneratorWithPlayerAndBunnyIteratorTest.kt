package grid

import org.example.cells.Cell
import org.example.grid.GridGeneratorWithPlayerAndBunnyIterator
import org.example.level.Coordinate
import org.example.level.Grid
import org.example.solver.Solver
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GridGeneratorWithPlayerAndBunnyIteratorTest {
    private fun convertIntegerListToGrid(integerGrid: List<List<Int>>): Map<Coordinate, Cell> {
        val grid = mutableMapOf<Coordinate, Cell>()
        val height = integerGrid.size
        val width = integerGrid[0].size
        for (y in 0 until height) {
            for (x in 0 until width) {
                grid[Coordinate(x, y)] = Cell.convertIntToCell(integerGrid[y][x])
            }
        }
        return grid
    }

    @Test
    fun happyFlow() {
        // Given
        val grid = convertIntegerListToGrid(
            listOf(
                listOf(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
                listOf(1,0,0,1,0,0,0,0,1,1,1,1,1,0,1),
                listOf(1,0,0,0,0,0,0,0,0,0,0,0,1,0,1),
                listOf(1,1,1,1,1,1,0,1,1,1,1,0,0,0,1),
                listOf(1,1,0,0,0,0,0,1,1,0,1,0,0,0,1),
                listOf(1,1,1,0,0,0,1,0,0,0,1,0,0,0,1),
                listOf(1,1,1,1,0,1,1,0,0,0,0,0,0,0,1),
                listOf(1,1,1,0,0,1,1,1,1,1,1,1,1,0,1),
                listOf(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
            )
        )

        val iterator = GridGeneratorWithPlayerAndBunnyIterator(grid, 15, 9)
        val solver = Solver(100)

        // When
        val allSolvedPositions = mutableListOf<Grid>()
        var totalNrOfGrids = 0
        for (solvableGrid in iterator) {
            if (solver.bfsSolve(solvableGrid) != null) {
                allSolvedPositions.add(solvableGrid)
            }
            totalNrOfGrids++
        }

        // Then
        assertEquals(2652, allSolvedPositions.size)
        assertEquals(2652, totalNrOfGrids)
    }
}
