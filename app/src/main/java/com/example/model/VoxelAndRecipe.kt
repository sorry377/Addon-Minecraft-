package com.example.model

data class VoxelBox(
    val id: String,
    val name: String,
    val originX: Float, // -8 to 8
    val originY: Float, // 0 to 16
    val originZ: Float, // -8 to 8
    val sizeX: Float,   // 1 to 16
    val sizeY: Float,   // 1 to 16
    val sizeZ: Float,   // 1 to 16
    val colorHex: String = "#3B82F6"
)

data class CraftingSlot(
    val index: Int, // 0..8 for 3x3 grid
    val itemName: String = "",
    val itemIconChar: String = ""
)

data class RecipeConfig(
    val isEnabled: Boolean = true,
    val resultCount: Int = 1,
    val grid: List<CraftingSlot> = List(9) { index ->
        when (index) {
            4 -> CraftingSlot(4, "Diamond", "💎")
            7 -> CraftingSlot(7, "Stick", "🥢")
            else -> CraftingSlot(index, "", "")
        }
    }
)
