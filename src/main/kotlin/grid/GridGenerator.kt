package org.example.grid

import org.example.cells.Cell
import org.example.cells.EmptyCell
import org.example.cells.WallCell
import org.example.level.Coordinate
import kotlin.random.Random

val ALL_DIRECTIONS = listOf(Coordinate.UP, Coordinate.DOWN, Coordinate.LEFT, Coordinate.RIGHT)

// Only generates a grid with walls and empty cells. Does not add player or bunny!
class GridGenerator(private val randomGenerator: Random = Random(System.currentTimeMillis())) {

    fun generate(): Map<Coordinate, Cell> {
        while (true) {
            val result = generateMaybe()
            if (result != null) return result
        }
    }

    // Use the algorithm from this paper: https://ianparberry.com/pubs/GAMEON-NA_METH_03.pdf.
    // In short: split the grid in 3x3 grids and place pre-determined blocks in each grid.
    // Then verify if some conditions match to see if we have a valid grid.
    private fun generateMaybe(): Map<Coordinate, Cell>? {
        // The grid will be 14x8. We will have a 12x6 grid that we divide nicely in 3x3 blocks. We want the level to be
        // surrounded with walls, so we fill in the outer later with walls.
        val width = 14
        val height = 8

        val grid = mutableMapOf<Coordinate, Cell>()
        for (x in 0 until width) {
            for (y in 0 until height) {
                // Only fill the boundary.
                if (x != 0 && x != width - 1 && y != 0 && y != height - 1) {
                    continue
                }
                grid[Coordinate(x, y)] = WallCell()
            }
        }

        val templateCenterPoints = listOf(
            Coordinate(2, 2), Coordinate(5, 2), Coordinate(8, 2), Coordinate(11, 2),
            Coordinate(2, 5), Coordinate(5, 5), Coordinate(8, 5), Coordinate(11, 5),
        )

        for (templateCenterPoint in templateCenterPoints) {
            // Keep retrying to fit a template until it works.
            while (true) {
                val transformation = ALL_TEMPLATE_TRANSFORMATIONS.random(randomGenerator)
                val template = transformation.execute(ALL_THREE_BY_THREE_TEMPLATES.random(randomGenerator))
                if (template.canBeAppliedToGrid(grid, templateCenterPoint)) {
                    template.applyToGrid(grid, templateCenterPoint)
                    break
                }
            }
        }

        // We now have a grid. Check if it satisfies these criteria:
        // 1. All cells are connected.
        // 2. No open floor sections of 3x4 or 4x3 exist.
        // 3. The number of empty cells should be at least 15. (This is a personal choice.)
        if (!isSingleContiguousRegion(grid)) {
            return null
        }

        if (!hasNoSolidEmptyRectangles(grid)) {
            return null
        }

        if (grid.values.filterIsInstance<EmptyCell>().size < 15) {
            return null
        }

        return grid
    }

    private fun isSingleContiguousRegion(grid: Map<Coordinate, Cell>): Boolean {
        // We can pick a random cell, use BFS to find all cells connected to it, and check if we found all cells.
        val emptyCells = grid.filterValues { it is EmptyCell }.keys
        if (emptyCells.isEmpty()) return true

        val start = emptyCells.first()
        val visited = mutableSetOf<Coordinate>()
        val queue: ArrayDeque<Coordinate> = ArrayDeque()
        queue.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue
            visited.add(current)

            ALL_DIRECTIONS
                .map { current + it }
                .filter { it in emptyCells && it !in visited }
                .forEach { queue.add(it) }
        }

        return visited.size == emptyCells.size
    }

    fun hasNoSolidEmptyRectangles(grid: Map<Coordinate, Cell>): Boolean {
        val coords = grid.keys
        val minX = coords.minOf { it.x }
        val maxX = coords.maxOf { it.x }
        val minY = coords.minOf { it.y }
        val maxY = coords.maxOf { it.y }

        val emptyCells = grid.filterValues { it is EmptyCell }.keys

        // Check all 3x4 rectangles
        for (y in minY..(maxY - 2)) {
            for (x in minX..(maxX - 3)) {
                val rect = (0..2).flatMap { dy ->
                    (0..3).map { dx ->
                        Coordinate(x + dx, y + dy)
                    }
                }
                if (rect.all { it in emptyCells }) {
                    return false
                }
            }
        }

        // Check all 4x3 rectangles
        for (y in minY..(maxY - 3)) {
            for (x in minX..(maxX - 2)) {
                val rect = (0..3).flatMap { dy ->
                    (0..2).map { dx ->
                        Coordinate(x + dx, y + dy)
                    }
                }
                if (rect.all { it in emptyCells }) {
                    return false
                }
            }
        }

        return true
    }
}

val ALL_THREE_BY_THREE_TEMPLATES = listOf(
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 9, 9, 9),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 9, 9, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 9, 9, 9),
            listOf(9, 1, 0, 0, 9),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 9, 9, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 9, 0, 0),
            listOf(9, 1, 1, 0, 0),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 9, 9, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 9, 9, 9),
            listOf(9, 1, 1, 1, 9),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 9, 9, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 9, 9, 9),
            listOf(9, 1, 1, 1, 9),
            listOf(9, 1, 0, 0, 9),
            listOf(9, 1, 0, 0, 9),
            listOf(9, 9, 9, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 0, 9, 9),
            listOf(9, 1, 0, 0, 9),
            listOf(0, 0, 0, 0, 9),
            listOf(9, 0, 0, 1, 9),
            listOf(9, 9, 9, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 9, 9, 9),
            listOf(9, 1, 0, 0, 9),
            listOf(0, 0, 0, 0, 9),
            listOf(9, 1, 0, 0, 9),
            listOf(9, 9, 9, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 0, 9, 9),
            listOf(9, 1, 0, 0, 9),
            listOf(0, 0, 0, 0, 9),
            listOf(9, 1, 0, 1, 9),
            listOf(9, 9, 0, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 0, 9, 9),
            listOf(9, 1, 0, 1, 9),
            listOf(0, 0, 0, 0, 0),
            listOf(9, 1, 0, 1, 9),
            listOf(9, 9, 0, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 0, 9, 9),
            listOf(9, 1, 0, 1, 9),
            listOf(9, 1, 0, 0, 0),
            listOf(9, 1, 1, 1, 9),
            listOf(9, 9, 9, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 0, 9, 9),
            listOf(9, 1, 1, 1, 9),
            listOf(0, 0, 0, 0, 0),
            listOf(9, 1, 1, 1, 9),
            listOf(9, 9, 9, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 9, 9, 9),
            listOf(9, 0, 0, 0, 0),
            listOf(9, 0, 1, 0, 0),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 9, 9, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 9, 9, 9),
            listOf(9, 1, 1, 1, 9),
            listOf(9, 1, 1, 1, 9),
            listOf(9, 1, 1, 1, 9),
            listOf(9, 9, 9, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 9, 9, 9),
            listOf(9, 1, 1, 1, 9),
            listOf(9, 1, 0, 0, 9),
            listOf(0, 0, 0, 0, 9),
            listOf(0, 0, 9, 9, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 0, 9, 0, 9),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 1, 0, 1, 9),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 0, 9, 0, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 9, 9, 9),
            listOf(9, 1, 1, 1, 9),
            listOf(9, 1, 1, 1, 9),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 0, 0, 0, 9),
        )
    ),
    ThreeByThreeTemplate.fromInt(
        listOf(
            listOf(9, 9, 9, 9, 9),
            listOf(9, 1, 1, 1, 9),
            listOf(0, 0, 1, 0, 0),
            listOf(9, 0, 0, 0, 9),
            listOf(9, 0, 0, 1, 9),
        )
    ),
)

val ALL_TEMPLATE_TRANSFORMATIONS = TemplateTransformation.entries.toTypedArray().toList()

enum class TemplateTransformation(private val f: (Coordinate) -> Coordinate) {
    NOTHING({ c -> c }),
    ROTATE_90({ c -> c.turnClockwise() }),
    ROTATE_180({ c -> c.turnClockwise().turnClockwise() }),
    ROTATE_270({ c -> c.turnCounterClockwise() }),
    FLIP_X({ c -> Coordinate(-c.x, c.y) }),
    FLIP_Y({ c -> Coordinate(c.x, -c.y) });

    fun execute(template: ThreeByThreeTemplate): ThreeByThreeTemplate {
        return template.changeCoordinates(f)
    }
}

data class ThreeByThreeTemplate(
    // The value of the input must have -2 <= c.x <= 2 and -2 <= c.y <= 2. So (0,0) is the middle of the template and
    // (2,-2) is the top right corner.
    val t: Map<Coordinate, Cell>
) {
    companion object {
        fun fromInt(grid: List<List<Int>>): ThreeByThreeTemplate {
            val t = mutableMapOf<Coordinate, Cell>()
            for (x in -2..2) {
                for (y in -2..2) {
                    val value = grid[y + 2][x + 2]
                    if (value == 9) {
                        continue
                    }

                    t[Coordinate(x, y)] = if (grid[y + 2][x + 2] == 1) WallCell() else EmptyCell()
                }
            }
            return ThreeByThreeTemplate(t)
        }
    }

    fun changeCoordinates(f: (Coordinate) -> Coordinate): ThreeByThreeTemplate {
        return ThreeByThreeTemplate(t.mapKeys { (coordinate) -> f.invoke(coordinate) })
    }

    fun canBeAppliedToGrid(grid: MutableMap<Coordinate, Cell>, center: Coordinate): Boolean {
        for (x in -2..2) {
            for (y in -2..2) {
                val current = grid[Coordinate(x, y) + center] ?: continue
                val willBe = t[Coordinate(x, y)] ?: continue
                if (current != willBe) {
                    return false
                }
            }
        }

        return true
    }

    fun applyToGrid(grid: MutableMap<Coordinate, Cell>, center: Coordinate) {
        for (x in -2..2) {
            for (y in -2..2) {
                val value = t[Coordinate(x, y)] ?: continue
                grid[Coordinate(x, y) + center] = value
            }
        }
    }
}

