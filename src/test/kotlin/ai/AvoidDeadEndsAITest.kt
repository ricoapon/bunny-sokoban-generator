package ai

import org.example.ai.AvoidDeadEndsAI
import org.example.level.Coordinate
import org.example.level.Grid
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AvoidDeadEndsAITest {
    private val bunnyAI = AvoidDeadEndsAI()

    @Test
    fun avoidDeadEnds() {
        // Given
        val grid = Grid.fromIntegerList(
            listOf(
                listOf(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
                listOf(1,0,0,1,0,0,0,0,1,1,1,1,1,0,1),
                listOf(1,0,0,0,0,0,0,0,0,0,0,0,1,0,1),
                listOf(1,1,1,1,1,1,0,1,1,1,1,0,0,5,1),
                listOf(1,1,0,0,0,0,0,1,1,0,1,0,0,0,1),
                listOf(1,1,1,0,0,0,1,0,0,0,1,0,0,2,1),
                listOf(1,1,1,1,0,1,1,0,0,0,0,0,0,0,1),
                listOf(1,1,1,0,0,1,1,1,1,1,1,1,1,0,1),
                listOf(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
            )
        )

        // When
        val result = bunnyAI.getNextBunnyDirection(grid)

        // Then
        assertEquals(Coordinate.LEFT, result)
    }
}
