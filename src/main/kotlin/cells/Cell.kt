package org.example.cells

sealed interface Cell {
    companion object {
        fun convertIntToCell(value: Int) : Cell {
            return when (value) {
                0 -> EmptyCell()
                1 -> WallCell()
                2 -> PlayerCell(Mask.FOX)
                3 -> DroppedMaskCell(Mask.FOX)
                4 -> PlayerOnTopOfDroppedMask(PlayerCell(Mask.FOX), DroppedMaskCell(Mask.FOX))
                5 -> BunnyCell()
                else -> throw RuntimeException("Invalid value $value for converting to cell")
            }
        }
    }
}

data class EmptyCell(val irrelevant: Boolean = true) : Cell

data class WallCell(val irrelevant: Boolean = true) : Cell

enum class Mask {
    FOX,
    BUNNY,
    SEXY_BUNNY,
}

data class PlayerCell(val mask: Mask) : Cell

data class DroppedMaskCell(val mask: Mask) : Cell

data class PlayerOnTopOfDroppedMask(val playerCell: PlayerCell, val droppedMaskCell: DroppedMaskCell) : Cell

data class BunnyCell(val isExhausted: Boolean = false) : Cell
