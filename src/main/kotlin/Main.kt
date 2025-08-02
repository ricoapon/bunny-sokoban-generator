package org.example

import org.example.grid.GridGenerator
import org.example.grid.GridGeneratorWithPlayerAndBunnyIterator
import org.example.level.Grid
import org.example.solver.Solver
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun main() {
    val generatedLevels = mutableListOf<Grid>()
    val solver = Solver(100)

    var levelsChecked = 0

    while(generatedLevels.size < 1) {
        val grid = generateGrid()
        val solution = solver.bfsSolve(grid)
        levelsChecked++

        if (levelsChecked % 100 == 0) {
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            println("[$time] Checked $levelsChecked levels, found ${generatedLevels.size}")
        }

        // If the grid is solvable, almost all positions are solvable. This might not always be the case. But to speed
        // up the algorithm, we discard any grid for which we cannot find a solution for the first player/bunny position.
        if (solution == null) {
            iterator = null
            continue
        }

        if (solution.size() > 30) {
            generatedLevels.add(grid)
        }
    }

    for (grid in generatedLevels) {
        print(grid.toAsciiArt())
        println()
    }
}


var iterator: GridGeneratorWithPlayerAndBunnyIterator? = null
private fun generateGrid(): Grid {
    if (iterator == null || !iterator!!.hasNext()) {
        val grid = GridGenerator().generate()
        iterator = GridGeneratorWithPlayerAndBunnyIterator(grid, 14, 8)
    }

    return iterator!!.next()
}
