@file:Suppress("PrivatePropertyName")

package level

import org.example.ai.AvoidDeadEndsAI
import org.example.cells.BunnyCell
import org.example.level.Coordinate
import org.example.level.Grid
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GridTest {
    private val A = Coordinate(1,0 )
    private val B = Coordinate(2, 0)
    private val C = Coordinate(3,0)

    @Test
    fun repeatingCycleHappyFlow() {
        assertTrue(Grid.hasRepeatingCycle(listOf(B, A, B, C, A, B, C)))
        assertTrue(Grid.hasRepeatingCycle(listOf(A, A, B, A, A, B)))
        assertFalse(Grid.hasRepeatingCycle(listOf(A, A, B, C, A, A, B)))
        assertFalse(Grid.hasRepeatingCycle(listOf(A, B, C, A, B, A, C, B)))
    }

    @Test
    fun bunnyMovesHappyFlow() {
        // Given
        val grid = Grid.fromIntegerList(
            listOf(
                listOf(1,1,1,1,1,1,1),
                listOf(1,0,0,0,3,0,1),
                listOf(1,2,5,0,0,0,1),
                listOf(1,0,0,0,0,0,1),
                listOf(1,1,1,1,1,1,1),
            )
        )
        val bunnyAI = AvoidDeadEndsAI()

        // When
        val result = grid.moveBunnyUntilNoMoreMovesPossible(bunnyAI)

        // Then
        assertEquals(Coordinate(4, 3), result.bunnyCoordinate)
    }

    @Test
    fun bunnyGetsExhausted() {
        // Given
        val grid = Grid.fromIntegerList(
            listOf(
                listOf(1,1,1,1,1,1,1),
                listOf(1,2,5,0,3,0,1),
                listOf(1,1,1,1,1,1,1),
            )
        )
        val bunnyAI = AvoidDeadEndsAI()

        // When
        val result = grid.moveBunnyUntilNoMoreMovesPossible(bunnyAI)

        // Then
        assertTrue((result.getBunny().cell as BunnyCell).isExhausted)
    }
}
