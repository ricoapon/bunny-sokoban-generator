package org.example.cells

sealed interface Cell

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

data class BunnyCell(val irrelevant: Boolean) : Cell
