package com.example.model

data class PotionEffectConfig(
    val name: String,
    val durationSeconds: Int,
    val amplifier: Int
)

data class CustomItemConfig(
    val id: String,
    val identifier: String, // e.g. "custom:ruby_broadsword"
    val displayName: String,
    val attackDamage: Int = 14,
    val durability: Int = 2031,
    val maxStackSize: Int = 1,
    val isFood: Boolean = false,
    val nutrition: Int = 4,
    val saturation: Float = 0.6f,
    val canAlwaysEat: Boolean = false,
    val hasGlint: Boolean = true,
    val handEquipped: Boolean = true,
    val shootProjectile: Boolean = false,
    val cooldownSeconds: Float = 0.0f,
    val iconEmoji: String = "⚔️",
    val primaryColorHex: String = "#EF4444",
    val categoryTag: String = "Equipment",
    val recipe: RecipeConfig = RecipeConfig()
)

data class CustomBlockConfig(
    val id: String,
    val identifier: String, // e.g. "custom:celestial_crystal_block"
    val displayName: String,
    val lightEmission: Int = 12, // 0..15
    val explosionResistance: Float = 15.0f, // 0..100
    val friction: Float = 0.6f, // 0.1 to 1.0 (0.6 normal, 0.98 ice)
    val hardness: Float = 3.5f, // 0.5 dirt, 1.5 stone, 50.0 obsidian
    val soundType: String = "Amethyst", // Stone, Wood, Metal, Glass, Slime, Amethyst
    val geometryShape: String = "Full Cube", // Full Cube, Slab, Custom 3D Model
    val isFlammable: Boolean = false,
    val emitsRedstonePower: Boolean = false,
    val redstonePowerLevel: Int = 0,
    val dropsSelf: Boolean = true,
    val customDropItem: String = "",
    val blockColorHex: String = "#8B5CF6",
    val blockEmoji: String = "🔮",
    val modelBoxes: List<VoxelBox> = listOf(
        VoxelBox("base_box", "Main Body", -7f, 0f, -7f, 14f, 14f, 14f, "#8B5CF6"),
        VoxelBox("core_box", "Glow Crystal", -4f, 14f, -4f, 8f, 2f, 8f, "#A78BFA")
    )
)

data class ShaderPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val performanceLevel: String, // "เบา (FPS สูง)", "สมดุล", "Ultra RTX"
    val drawableResId: Int?,
    val waterWaves: Boolean = true,
    val waterReflectionPercent: Int = 85,
    val sunRaysGodRays: Boolean = true,
    val bloomIntensityPercent: Int = 70,
    val glowingOres: Boolean = true,
    val cloudsStyle: String = "3D Fluffy Volumetric", // "2D Classic", "3D Fluffy Volumetric", "Realistic Aurora"
    val minVersion: MinecraftVersion = MinecraftVersion.V_1_21_40,
    val tags: List<String> = listOf("Deferred PBR", "RTX Ready", "Vibrant Colors")
)

data class AddonProject(
    val id: String,
    val name: String,
    val author: String,
    val version: String = "1.0.0",
    val description: String,
    val targetVersion: MinecraftVersion = MinecraftVersion.V_1_21_40,
    val exportFormat: ExportFormat = ExportFormat.MCPACK,
    val genre: AddonGenre = AddonGenre.WEAPON_TOOLS,
    val items: List<CustomItemConfig> = emptyList(),
    val blocks: List<CustomBlockConfig> = emptyList(),
    val selectedShaderPresetId: String? = null,
    val shaderCustomWater: Int = 80,
    val shaderCustomBloom: Int = 65,
    val shaderCustomGodRays: Boolean = true,
    val lastModified: Long = System.currentTimeMillis(),
    val isAutoBackupEnabled: Boolean = true
)

data class BackupSnapshot(
    val id: String,
    val projectId: String,
    val projectName: String,
    val format: ExportFormat,
    val version: String,
    val timestamp: Long,
    val fileSizeFormatted: String,
    val filePath: String,
    val reasonNote: String
)
