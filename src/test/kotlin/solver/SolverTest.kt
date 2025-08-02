package solver

import org.example.level.Grid
import org.example.solver.Solver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SolverTest {
    @Test
    fun happyFlow() {
        // Given
        val grid = Grid.fromIntegerList(
            listOf(
                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
                listOf(1, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 1),
                listOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1),
                listOf(1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 0, 0, 1),
                listOf(1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 1),
                listOf(1, 1, 1, 0, 0, 0, 1, 2, 0, 0, 1, 0, 0, 0, 1),
                listOf(1, 1, 1, 1, 0, 1, 1, 0, 5, 0, 0, 0, 0, 0, 1),
                listOf(1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1),
                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            )
        )
        val solver = Solver(100)

        // When
        val result = solver.bfsSolve(grid)

        // Then
        assertEquals(34, result!!.size())
    }
}
