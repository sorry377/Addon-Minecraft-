package com.example.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.model.AddonGenre
import com.example.model.AddonProject
import com.example.model.CustomBlockConfig
import com.example.model.CustomItemConfig
import com.example.model.ExportFormat
import com.example.model.ShaderPreset
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object MinecraftPackCompiler {

    data class CompilationResult(
        val file: File,
        val format: ExportFormat,
        val fileSize: Long,
        val formattedSize: String
    )

    fun compileProject(
        context: Context,
        project: AddonProject,
        customFormat: ExportFormat? = null,
        targetOutputDir: File = File(context.filesDir, "packs")
    ): CompilationResult {
        if (!targetOutputDir.exists()) {
            targetOutputDir.mkdirs()
        }

        val format = customFormat ?: project.exportFormat
        val safeName = project.name.replace("\\s+".toRegex(), "_").replace("[^a-zA-Z0-9_]".toRegex(), "")
            .ifEmpty { "MC_Addon" }
        val fileName = "${safeName}_v${project.version}${format.extension}"
        val outputFile = File(targetOutputDir, fileName)

        val headerUuid = UUID.randomUUID().toString()
        val bpModuleUuid = UUID.randomUUID().toString()
        val rpHeaderUuid = UUID.randomUUID().toString()
        val rpModuleUuid = UUID.randomUUID().toString()

        val minEngine = project.targetVersion.minEngineVersion

        when (format) {
            ExportFormat.MCPACK -> {
                // If it's a shader-only pack, compile as Resource Pack mcpack.
                // Otherwise compile as combined or behavior/resource pack.
                val isResourceOnly = project.genre == AddonGenre.SHADERS_VISUAL
                compileSinglePack(
                    outputFile = outputFile,
                    project = project,
                    headerUuid = headerUuid,
                    moduleUuid = bpModuleUuid,
                    minEngine = minEngine,
                    isResourcePack = isResourceOnly
                )
            }
            ExportFormat.MCADDON -> {
                // MCADDON can contain both the BP and RP zip archives inside it or combined structure
                compileMcAddonArchive(
                    outputFile = outputFile,
                    project = project,
                    bpHeaderUuid = headerUuid,
                    bpModuleUuid = bpModuleUuid,
                    rpHeaderUuid = rpHeaderUuid,
                    rpModuleUuid = rpModuleUuid,
                    minEngine = minEngine
                )
            }
            ExportFormat.ZIP -> {
                compileCombinedPack(
                    outputFile = outputFile,
                    project = project,
                    headerUuid = headerUuid,
                    moduleUuid = bpModuleUuid,
                    minEngine = minEngine
                )
            }
        }

        val size = outputFile.length()
        val formatted = formatFileSize(size)

        return CompilationResult(
            file = outputFile,
            format = format,
            fileSize = size,
            formattedSize = formatted
        )
    }

    private fun compileSinglePack(
        outputFile: File,
        project: AddonProject,
        headerUuid: String,
        moduleUuid: String,
        minEngine: List<Int>,
        isResourcePack: Boolean
    ) {
        ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
            // 1. Manifest
            val manifestJson = generateManifestJson(
                name = project.name,
                description = project.description,
                version = project.version,
                headerUuid = headerUuid,
                moduleUuid = moduleUuid,
                minEngine = minEngine,
                isResourcePack = isResourcePack
            )
            writeEntry(zos, "manifest.json", manifestJson)

            // 2. Pack Icon
            val iconBytes = generatePackIconBytes(project.name, project.genre)
            writeBytesEntry(zos, "pack_icon.png", iconBytes)

            // 3. Texts
            val langContent = generateTextsLang(project)
            writeEntry(zos, "texts/en_US.lang", langContent)
            writeEntry(zos, "texts/languages.json", "[\"en_US\"]\n")

            if (isResourcePack || project.genre == AddonGenre.SHADERS_VISUAL) {
                // Add shader configs & textures
                writeShaderPackFiles(zos, project)
            } else {
                // Add items, blocks, recipes
                writeBehaviorPackFiles(zos, project)
                // Also add basic resource mapping
                writeResourcePackFiles(zos, project)
            }
        }
    }

    private fun compileMcAddonArchive(
        outputFile: File,
        project: AddonProject,
        bpHeaderUuid: String,
        bpModuleUuid: String,
        rpHeaderUuid: String,
        rpModuleUuid: String,
        minEngine: List<Int>
    ) {
        // Build BP in memory
        val bpBytes = ByteArrayOutputStream().use { baos ->
            ZipOutputStream(baos).use { zos ->
                val bpManifest = generateManifestJson(
                    name = "${project.name} (Behavior)",
                    description = "${project.description} [BP]",
                    version = project.version,
                    headerUuid = bpHeaderUuid,
                    moduleUuid = bpModuleUuid,
                    minEngine = minEngine,
                    isResourcePack = false
                )
                writeEntry(zos, "manifest.json", bpManifest)
                writeBytesEntry(zos, "pack_icon.png", generatePackIconBytes(project.name, project.genre))
                writeBehaviorPackFiles(zos, project)
            }
            baos.toByteArray()
        }

        // Build RP in memory
        val rpBytes = ByteArrayOutputStream().use { baos ->
            ZipOutputStream(baos).use { zos ->
                val rpManifest = generateManifestJson(
                    name = "${project.name} (Resource)",
                    description = "${project.description} [RP]",
                    version = project.version,
                    headerUuid = rpHeaderUuid,
                    moduleUuid = rpModuleUuid,
                    minEngine = minEngine,
                    isResourcePack = true
                )
                writeEntry(zos, "manifest.json", rpManifest)
                writeBytesEntry(zos, "pack_icon.png", generatePackIconBytes(project.name, project.genre))
                writeResourcePackFiles(zos, project)
                writeEntry(zos, "texts/en_US.lang", generateTextsLang(project))
                writeEntry(zos, "texts/languages.json", "[\"en_US\"]\n")
                if (project.genre == AddonGenre.SHADERS_VISUAL) {
                    writeShaderPackFiles(zos, project)
                }
            }
            baos.toByteArray()
        }

        // Pack both into the final .mcaddon
        ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
            writeBytesEntry(zos, "${project.name.replace(" ", "_")}_BP.mcpack", bpBytes)
            writeBytesEntry(zos, "${project.name.replace(" ", "_")}_RP.mcpack", rpBytes)
        }
    }

    private fun compileCombinedPack(
        outputFile: File,
        project: AddonProject,
        headerUuid: String,
        moduleUuid: String,
        minEngine: List<Int>
    ) {
        ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
            val manifest = generateManifestJson(
                name = project.name,
                description = project.description,
                version = project.version,
                headerUuid = headerUuid,
                moduleUuid = moduleUuid,
                minEngine = minEngine,
                isResourcePack = false
            )
            writeEntry(zos, "manifest.json", manifest)
            writeBytesEntry(zos, "pack_icon.png", generatePackIconBytes(project.name, project.genre))
            writeEntry(zos, "texts/en_US.lang", generateTextsLang(project))
            writeEntry(zos, "texts/languages.json", "[\"en_US\"]\n")
            writeBehaviorPackFiles(zos, project)
            writeResourcePackFiles(zos, project)
            if (project.genre == AddonGenre.SHADERS_VISUAL) {
                writeShaderPackFiles(zos, project)
            }
        }
    }

    private fun writeBehaviorPackFiles(zos: ZipOutputStream, project: AddonProject) {
        // Items
        project.items.forEach { item ->
            val safeId = item.identifier.substringAfter(":").ifEmpty { "custom_item_${item.id}" }
            val itemJson = generateItemJson(item)
            writeEntry(zos, "items/$safeId.json", itemJson)

            // Recipes
            if (item.recipe.isEnabled) {
                val recipeJson = generateRecipeJson(item)
                writeEntry(zos, "recipes/${safeId}_recipe.json", recipeJson)
            }
        }

        // Blocks
        project.blocks.forEach { block ->
            val safeId = block.identifier.substringAfter(":").ifEmpty { "custom_block_${block.id}" }
            val blockJson = generateBlockJson(block)
            writeEntry(zos, "blocks/$safeId.json", blockJson)
        }
    }

    private fun writeResourcePackFiles(zos: ZipOutputStream, project: AddonProject) {
        // Item textures mapping
        val itemTextureJson = buildString {
            append("{\n")
            append("  \"resource_pack_name\": \"${project.name}\",\n")
            append("  \"texture_name\": \"atlas.items\",\n")
            append("  \"texture_data\": {\n")
            project.items.forEachIndexed { index, item ->
                val idClean = item.identifier.substringAfter(":")
                append("    \"$idClean\": {\n")
                append("      \"textures\": \"textures/items/$idClean\"\n")
                append("    }${if (index < project.items.size - 1) "," else ""}\n")
            }
            append("  }\n")
            append("}\n")
        }
        writeEntry(zos, "textures/item_texture.json", itemTextureJson)

        // Terrain textures mapping
        val terrainTextureJson = buildString {
            append("{\n")
            append("  \"resource_pack_name\": \"${project.name}\",\n")
            append("  \"texture_name\": \"atlas.terrain\",\n")
            append("  \"texture_data\": {\n")
            project.blocks.forEachIndexed { index, block ->
                val idClean = block.identifier.substringAfter(":")
                append("    \"$idClean\": {\n")
                append("      \"textures\": \"textures/blocks/$idClean\"\n")
                append("    }${if (index < project.blocks.size - 1) "," else ""}\n")
            }
            append("  }\n")
            append("}\n")
        }
        writeEntry(zos, "textures/terrain_texture.json", terrainTextureJson)

        // Geometry models for 3D blocks
        project.blocks.forEach { block ->
            val geoJson = generateBlockGeometryJson(block)
            val idClean = block.identifier.substringAfter(":")
            writeEntry(zos, "models/blocks/$idClean.geo.json", geoJson)
        }
    }

    private fun writeShaderPackFiles(zos: ZipOutputStream, project: AddonProject) {
        // Bedrock Deferred & RTX graphics manifest
        val graphicsJson = buildString {
            append("{\n")
            append("  \"format_version\": \"1.21.0\",\n")
            append("  \"minecraft:client_graphics\": {\n")
            append("    \"description\": {\n")
            append("      \"identifier\": \"client_graphics:shader_${project.id}\"\n")
            append("    },\n")
            append("    \"deferred_lighting\": {\n")
            append("      \"enabled\": true,\n")
            append("      \"bloom_intensity\": ${project.shaderCustomBloom / 100.0},\n")
            append("      \"volumetric_fog\": ${project.shaderCustomGodRays},\n")
            append("      \"water_roughness\": ${(100 - project.shaderCustomWater) / 100.0},\n")
            append("      \"water_refraction\": 0.25,\n")
            append("      \"sun_shafts\": ${project.shaderCustomGodRays}\n")
            append("    }\n")
            append("  }\n")
            append("}\n")
        }
        writeEntry(zos, "renderer/client_graphics.json", graphicsJson)

        // Atmosphere sky configuration
        val atmosphereJson = buildString {
            append("{\n")
            append("  \"format_version\": \"1.21.0\",\n")
            append("  \"minecraft:atmosphere\": {\n")
            append("    \"sky_color_day\": [0.55, 0.72, 0.95],\n")
            append("    \"sky_color_sunset\": [0.95, 0.42, 0.22],\n")
            append("    \"sky_color_night\": [0.05, 0.08, 0.18],\n")
            append("    \"sun_bloom_scale\": 1.45,\n")
            append("    \"clouds_3d_height\": 192.0\n")
            append("  }\n")
            append("}\n")
        }
        writeEntry(zos, "renderer/atmosphere.json", atmosphereJson)
    }

    private fun generateManifestJson(
        name: String,
        description: String,
        version: String,
        headerUuid: String,
        moduleUuid: String,
        minEngine: List<Int>,
        isResourcePack: Boolean
    ): String {
        val verArray = version.split(".").mapNotNull { it.toIntOrNull() }.takeIf { it.size == 3 }
            ?: listOf(1, 0, 0)
        val moduleType = if (isResourcePack) "resources" else "data"
        val engineStr = minEngine.joinToString(", ")

        return """
        {
          "format_version": 2,
          "header": {
            "name": "$name",
            "description": "$description - Created with MC Addon Studio",
            "uuid": "$headerUuid",
            "version": [${verArray.joinToString(", ")}],
            "min_engine_version": [$engineStr]
          },
          "modules": [
            {
              "type": "$moduleType",
              "uuid": "$moduleUuid",
              "version": [${verArray.joinToString(", ")}]
            }
          ]
        }
        """.trimIndent()
    }

    private fun generateItemJson(item: CustomItemConfig): String {
        val foodBlock = if (item.isFood) {
            """
            "minecraft:food": {
              "nutrition": ${item.nutrition},
              "saturation_modifier": ${item.saturation},
              "can_always_eat": ${item.canAlwaysEat}
            },
            """.trimIndent()
        } else ""

        val damageBlock = if (item.attackDamage > 0) {
            "\"minecraft:damage\": ${item.attackDamage},"
        } else ""

        val durabilityBlock = if (item.durability > 0) {
            """
            "minecraft:durability": {
              "max_durability": ${item.durability}
            },
            """.trimIndent()
        } else ""

        return """
        {
          "format_version": "1.20.50",
          "minecraft:item": {
            "description": {
              "identifier": "${item.identifier}",
              "menu_category": {
                "category": "${item.categoryTag.lowercase()}"
              }
            },
            "components": {
              $damageBlock
              $durabilityBlock
              $foodBlock
              "minecraft:max_stack_size": ${item.maxStackSize},
              "minecraft:hand_equipped": ${item.handEquipped},
              "minecraft:glint": ${item.hasGlint},
              "minecraft:icon": {
                "texture": "${item.identifier.substringAfter(":")}"
              }
            }
          }
        }
        """.trimIndent()
    }

    private fun generateBlockJson(block: CustomBlockConfig): String {
        val dropBlock = if (block.dropsSelf) {
            "\"minecraft:loot\": \"loot_tables/blocks/${block.identifier.substringAfter(":")}.json\","
        } else ""

        return """
        {
          "format_version": "1.20.50",
          "minecraft:block": {
            "description": {
              "identifier": "${block.identifier}",
              "menu_category": {
                "category": "construction"
              }
            },
            "components": {
              "minecraft:light_emission": ${block.lightEmission},
              "minecraft:friction": ${block.friction},
              "minecraft:destructible_by_mining": {
                "seconds_to_destroy": ${block.hardness}
              },
              "minecraft:destructible_by_explosion": {
                "explosion_resistance": ${block.explosionResistance}
              },
              $dropBlock
              "minecraft:material_instances": {
                "*": {
                  "texture": "${block.identifier.substringAfter(":")}",
                  "render_method": "opaque"
                }
              }
            }
          }
        }
        """.trimIndent()
    }

    private fun generateBlockGeometryJson(block: CustomBlockConfig): String {
        val cubesJson = block.modelBoxes.joinToString(",\n") { box ->
            """
            {
              "origin": [${box.originX}, ${box.originY}, ${box.originZ}],
              "size": [${box.sizeX}, ${box.sizeY}, ${box.sizeZ}],
              "uv": [0, 0]
            }
            """.trimIndent()
        }

        return """
        {
          "format_version": "1.12.0",
          "minecraft:geometry": [
            {
              "description": {
                "identifier": "geometry.${block.identifier.substringAfter(":")}",
                "texture_width": 64,
                "texture_height": 64,
                "visible_bounds_width": 2,
                "visible_bounds_height": 2,
                "visible_bounds_offset": [0, 0.5, 0]
              },
              "bones": [
                {
                  "name": "root",
                  "pivot": [0, 0, 0],
                  "cubes": [
                    $cubesJson
                  ]
                }
              ]
            }
          ]
        }
        """.trimIndent()
    }

    private fun generateRecipeJson(item: CustomItemConfig): String {
        val idClean = item.identifier
        val keyLetters = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I")
        val ingredientMap = mutableMapOf<String, String>()
        val patternRows = mutableListOf<String>()

        for (row in 0..2) {
            val rowChars = StringBuilder()
            for (col in 0..2) {
                val slot = item.recipe.grid[row * 3 + col]
                if (slot.itemName.isNotBlank()) {
                    val key = keyLetters[row * 3 + col]
                    val mcItem = when (slot.itemName.lowercase()) {
                        "diamond" -> "minecraft:diamond"
                        "iron", "iron ingot" -> "minecraft:iron_ingot"
                        "gold", "gold ingot" -> "minecraft:gold_ingot"
                        "stick" -> "minecraft:stick"
                        "emerald" -> "minecraft:emerald"
                        "netherite", "netherite ingot" -> "minecraft:netherite_ingot"
                        "redstone" -> "minecraft:redstone"
                        "blaze rod" -> "minecraft:blaze_rod"
                        else -> "minecraft:${slot.itemName.lowercase().replace(" ", "_")}"
                    }
                    ingredientMap[key] = mcItem
                    rowChars.append(key)
                } else {
                    rowChars.append(" ")
                }
            }
            patternRows.add(rowChars.toString())
        }

        val patternJson = patternRows.joinToString(",\n") { "        \"$it\"" }
        val keysJson = ingredientMap.entries.joinToString(",\n") { (k, v) ->
            """
            "$k": {
              "item": "$v"
            }
            """.trimIndent()
        }

        return """
        {
          "format_version": "1.12.0",
          "minecraft:recipe_shaped": {
            "description": {
              "identifier": "${idClean}_recipe"
            },
            "tags": [ "crafting_table" ],
            "pattern": [
      $patternJson
            ],
            "key": {
      $keysJson
            },
            "result": {
              "item": "$idClean",
              "count": ${item.recipe.resultCount}
            }
          }
        }
        """.trimIndent()
    }

    private fun generateTextsLang(project: AddonProject): String {
        val sb = StringBuilder()
        sb.append("## Addon Language localization (Generated by MC Addon Studio)\n")
        sb.append("pack.name=${project.name}\n")
        sb.append("pack.description=${project.description}\n\n")

        project.items.forEach { item ->
            sb.append("item.${item.identifier}.name=${item.displayName}\n")
        }

        project.blocks.forEach { block ->
            sb.append("tile.${block.identifier}.name=${block.displayName}\n")
        }

        return sb.toString()
    }

    private fun generatePackIconBytes(title: String, genre: AddonGenre): ByteArray {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply {
            color = when (genre) {
                AddonGenre.WEAPON_TOOLS -> Color.rgb(30, 41, 59)
                AddonGenre.BLOCKS_FURNITURE -> Color.rgb(22, 101, 52)
                AddonGenre.SHADERS_VISUAL -> Color.rgb(30, 58, 138)
                AddonGenre.FOOD_CROPS -> Color.rgb(180, 83, 9)
                AddonGenre.ARMOR_GEAR -> Color.rgb(88, 28, 135)
                AddonGenre.ENTITIES_PETS -> Color.rgb(13, 148, 136)
            }
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        // Draw border
        val borderPaint = Paint().apply {
            color = Color.rgb(250, 204, 21) // Gold border
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRect(3f, 3f, (size - 3).toFloat(), (size - 3).toFloat(), borderPaint)

        // Draw text
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val initials = title.take(4).uppercase()
        canvas.drawText(initials, size / 2f, size / 2f + 10f, textPaint)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun writeEntry(zos: ZipOutputStream, path: String, content: String) {
        val entry = ZipEntry(path)
        zos.putNextEntry(entry)
        zos.write(content.toByteArray(StandardCharsets.UTF_8))
        zos.closeEntry()
    }

    private fun writeBytesEntry(zos: ZipOutputStream, path: String, bytes: ByteArray) {
        val entry = ZipEntry(path)
        zos.putNextEntry(entry)
        zos.write(bytes)
        zos.closeEntry()
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }
}
