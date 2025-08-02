import org.example.ai.AvoidDeadEndsAI
import org.example.level.Coordinate
import org.example.level.Grid

val ALL_MOVES = listOf(
    MoveDirection(Coordinate.UP, "UP"),
    MoveDirection(Coordinate.DOWN, "DOWN"),
    MoveDirection(Coordinate.LEFT, "LEFT"),
    MoveDirection(Coordinate.RIGHT, "RIGHT")
)
val BUNNY_AI = AvoidDeadEndsAI()

sealed interface Move {
    fun execute(grid: Grid): Grid
}

data class MoveDirection(val direction: Coordinate, val name: String) : Move {
    override fun execute(grid: Grid): Grid {
        val newGrid = grid.movePlayer(direction)
        return newGrid.moveBunnyUntilNoMoreMovesPossible(BUNNY_AI)
    }

    override fun toString(): String {
        return name
    }
}
