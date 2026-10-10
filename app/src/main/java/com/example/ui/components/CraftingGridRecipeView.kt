package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CraftingSlot
import com.example.model.RecipeConfig

data class IngredientOption(val name: String, val emoji: String)

val PRESET_INGREDIENTS = listOf(
    IngredientOption("Diamond", "💎"),
    IngredientOption("Iron Ingot", "🪙"),
    IngredientOption("Gold Ingot", "🥇"),
    IngredientOption("Netherite Ingot", "⬛"),
    IngredientOption("Emerald", "🟢"),
    IngredientOption("Stick", "🥢"),
    IngredientOption("Redstone", "🔴"),
    IngredientOption("Blaze Rod", "🔥"),
    IngredientOption("Obsidian", "🟣"),
    IngredientOption("String", "🧵"),
    IngredientOption("Oak Planks", "🪵"),
    IngredientOption("Glass", "🪟"),
    IngredientOption("Feather", "🪶"),
    IngredientOption("Ender Pearl", "🔮")
)

@Composable
fun CraftingGridRecipeView(
    recipe: RecipeConfig,
    resultItemName: String,
    resultItemEmoji: String,
    onRecipeChanged: (RecipeConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSlotIndex by remember { mutableIntStateOf(0) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "สูตรคราฟต์บนโต๊ะ Crafting Table (3x3)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "แตะช่องในตารางเพื่อเลือกใส่วัตถุดิบ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = recipe.isEnabled,
                    onCheckedChange = { onRecipeChanged(recipe.copy(isEnabled = it)) }
                )
            }

            if (recipe.isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                // 3x3 Grid and Result
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 3x3 Matrix Box
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF334155))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (row in 0..2) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    for (col in 0..2) {
                                        val idx = row * 3 + col
                                        val slot = recipe.grid.getOrNull(idx) ?: CraftingSlot(idx, "", "")
                                        val isSelected = selectedSlotIndex == idx

                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF475569))
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) Color(0xFFFBBF24) else Color(0xFF64748B),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { selectedSlotIndex = idx },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (slot.itemIconChar.isNotBlank()) {
                                                Text(
                                                    text = slot.itemIconChar,
                                                    fontSize = 24.sp
                                                )
                                            } else {
                                                Text(
                                                    text = "${idx + 1}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Crafts to",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Result Box
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = resultItemEmoji, fontSize = 28.sp)
                            Text(
                                text = "x${recipe.resultCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector bar for selected slot
                Text(
                    text = "ใส่วัตถุดิบในช่อง #${selectedSlotIndex + 1}:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Empty / Clear Button
                    item {
                        Surface(
                            onClick = {
                                val updatedGrid = recipe.grid.toMutableList()
                                updatedGrid[selectedSlotIndex] = CraftingSlot(selectedSlotIndex, "", "")
                                onRecipeChanged(recipe.copy(grid = updatedGrid))
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.height(42.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ล้างช่องนี้",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    items(PRESET_INGREDIENTS) { item ->
                        Surface(
                            onClick = {
                                val updatedGrid = recipe.grid.toMutableList()
                                updatedGrid[selectedSlotIndex] = CraftingSlot(
                                    selectedSlotIndex,
                                    item.name,
                                    item.emoji
                                )
                                onRecipeChanged(recipe.copy(grid = updatedGrid))
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier.height(42.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = item.emoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.name,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
