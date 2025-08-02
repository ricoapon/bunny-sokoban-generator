package org.example.solver

import ALL_MOVES
import Move
import org.example.level.Grid
import java.util.*

data class Path(val actions: List<Move>) {
    operator fun plus(move: Move): Path {
        return Path(actions + move)
    }

    fun size(): Int {
        return actions.size
    }

    override fun toString(): String {
        return actions.joinToString(",") { it.toString() }
    }
}

class Solver(private val maxDepth: Int = 100) {
    fun bfsSolve(initial: Grid): Path? {
        val queue: Queue<Pair<Grid, Path>> = LinkedList()
        val visited = mutableSetOf<Grid>()

        queue.add(Pair(initial, Path(emptyList())))

        while (queue.isNotEmpty()) {
            val (currentGrid, path) = queue.poll()

            if (currentGrid.caughtBunny) {
                // We found a solution, so we are done.
                return path
            }

            if (path.size() >= maxDepth) {
                // Skip any paths that exceed the depth.
                continue
            }

            if (currentGrid in visited) continue
            visited.add(currentGrid)

            for (move in ALL_MOVES) {
                val nextGrid = move.execute(currentGrid)
                if (nextGrid !in visited) {
                    queue.add(Pair(nextGrid, path + move))
                }
            }
        }

        // No solution was found with given depth.
        return null
    }
}
