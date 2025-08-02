package org.example

import org.example.grid.GridGenerator
import org.example.grid.GridGeneratorWithPlayerAndBunnyIterator
import org.example.level.Grid
import org.example.solver.Path
import org.example.solver.Solver
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val OUTPUT_FILE = File("generated_levels.txt")

fun main() {
    var generatedLevels = 0
    val solver = Solver(100)

    var levelsChecked = 0

    while(generatedLevels < 10) {
        val grid = generateGrid()
        val solutionPath = solver.bfsSolve(grid)
        levelsChecked++

        if (levelsChecked % 100 == 0) {
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            println("[$time] Checked $levelsChecked levels, found $generatedLevels")
        }

        // If the grid is solvable, almost all positions are solvable. This might not always be the case. But to speed
        // up the algorithm, we discard any grid for which we cannot find a solution for the first player/bunny position.
        if (solutionPath == null) {
            iterator = null
            continue
        }

        // We found a grid. Now try and find the position where the solution is the longest.
        var longestSolution = Pair(grid, solutionPath)
        while (iterator!!.hasNext()) {
            val loopGrid = iterator!!.next()
            val path = solver.bfsSolve(loopGrid)
            if (path != null && path.size() > longestSolution.second.size()) {
                longestSolution = Pair(loopGrid, path)
            }

            levelsChecked++
            if (levelsChecked % 100 == 0) {
                val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                println("[$time] (While solving) Checked $levelsChecked levels, found longest ${longestSolution.second.size()}, and in total $generatedLevels levels")
            }
        }

        if (longestSolution.second.size() > 30) {
            writeLevelToFile(longestSolution.first, longestSolution.second)
            generatedLevels++
        }
    }
}

// Always append, so we never lose generated levels!
private fun writeLevelToFile(grid: Grid, solution: Path) {
    OUTPUT_FILE.appendText("Grid with the longest solution of size ${solution.size()}\n")
    OUTPUT_FILE.appendText("Path: $solution\n")
    OUTPUT_FILE.appendText(grid.toAsciiArt() + "\n")
}

var iterator: GridGeneratorWithPlayerAndBunnyIterator? = null
private fun generateGrid(): Grid {
    if (iterator == null || !iterator!!.hasNext()) {
        val grid = GridGenerator().generate()
        iterator = GridGeneratorWithPlayerAndBunnyIterator(grid, 14, 8)
    }

    return iterator!!.next()
}
