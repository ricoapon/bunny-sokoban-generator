package grid

import org.example.cells.*
import org.example.grid.GridGenerator
import org.example.level.Coordinate
import org.junit.jupiter.api.Test

class GridGeneratorTest {
    private fun toAsciiArt(grid: Map<Coordinate, Cell>, width: Int, height: Int): String {
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
                val cell = grid[coordinate]!!
                builder.append(cellToChar(cell))
            }
            builder.append('\n')
        }

        return builder.toString()
    }

    @Test
    fun giveItATry() {
        val generator = GridGenerator()
        for (i in 0..9) {
            val grid = generator.generate()
            print(toAsciiArt(grid, 14, 8))
            println()
        }

    }
}
