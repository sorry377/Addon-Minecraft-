package com.example.storage

import android.content.Context
import com.example.R
import com.example.generator.MinecraftPackCompiler
import com.example.model.AddonGenre
import com.example.model.AddonProject
import com.example.model.BackupSnapshot
import com.example.model.CraftingSlot
import com.example.model.CustomBlockConfig
import com.example.model.CustomItemConfig
import com.example.model.ExportFormat
import com.example.model.MinecraftVersion
import com.example.model.RecipeConfig
import com.example.model.ShaderPreset
import com.example.model.VoxelBox
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ProjectRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("mc_addon_studio_prefs", Context.MODE_PRIVATE)
    private val projectsFile = File(context.filesDir, "projects_data.json")
    private val backupsFile = File(context.filesDir, "backups_meta.json")
    private val backupsDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
    private val packsDir = File(context.filesDir, "packs").apply { if (!exists()) mkdirs() }

    val shaderPresets: List<ShaderPreset> = listOf(
        ShaderPreset(
            id = "shader_rtx_ultra",
            title = "RTX Ultra Realistic PBR",
            subtitle = "แสงสะท้อนผิวน้ำเรย์เทรซซิ่ง & ลำแสงทะลุก้อนเมฆ (God Rays)",
            description = "แพ็ก Shader ขั้นสูงสำหรับ Minecraft Bedrock พร้อมระบบ Deferred Lighting จำลองแสงแดดยามเย็น ผิวน้ำสะท้อนเงาสมจริง และการหักเหแสงใต้น้ำ",
            performanceLevel = "Ultra RTX (ต้องใช้การ์ดจอ/ชิปเซ็ตรองรับ)",
            drawableResId = R.drawable.shader_rtx,
            waterWaves = true,
            waterReflectionPercent = 95,
            sunRaysGodRays = true,
            bloomIntensityPercent = 85,
            glowingOres = true,
            cloudsStyle = "3D Fluffy Volumetric",
            minVersion = MinecraftVersion.V_1_21_40,
            tags = listOf("Deferred PBR", "Ray Tracing", "Realistic Water", "Sun Shafts")
        ),
        ShaderPreset(
            id = "shader_bioluminescent_night",
            title = "Bioluminescent Cave & Aurora Sky",
            subtitle = "แร่เรืองแสงนีออนในถ้ำลึก & แสงเหนือออโรร่าบนท้องฟ้า",
            description = "เพิ่มความสวยงามยามค่ำคืนด้วยบล็อกแร่เพชร มรกต และเรดสโตนที่เปล่งแสงนีออนสว่างไสว พร้อมท้องฟ้าแสงเหนือและโคมไฟสีอบอุ่น",
            performanceLevel = "สมดุล (เล่นลื่น 60+ FPS)",
            drawableResId = R.drawable.shader_night,
            waterWaves = true,
            waterReflectionPercent = 75,
            sunRaysGodRays = true,
            bloomIntensityPercent = 90,
            glowingOres = true,
            cloudsStyle = "Realistic Aurora",
            minVersion = MinecraftVersion.V_1_21_0,
            tags = listOf("Glowing Ores", "Aurora Sky", "Night Vision", "Cave Ambiance")
        ),
        ShaderPreset(
            id = "shader_deferred_vibrant",
            title = "Deferred Vibrant Sunset & Sky",
            subtitle = "โทนสีสดใส ท้องฟ้ายามพระอาทิตย์ตกอบอุ่น สไตล์ Cinematic",
            description = "ปรับสมดุลโทนสีของโลก Minecraft ให้สดใสมีชีวิตชีวา เงาตกกระทบแบบนุ่มนวล (Soft Shadows) และท้องฟ้าไล่เฉดสีส้มทองอบอุ่น",
            performanceLevel = "สมดุล (เหมาะสำหรับมือถือทุกรุ่น)",
            drawableResId = R.drawable.shader_rtx,
            waterWaves = true,
            waterReflectionPercent = 80,
            sunRaysGodRays = true,
            bloomIntensityPercent = 65,
            glowingOres = false,
            cloudsStyle = "3D Fluffy Volumetric",
            minVersion = MinecraftVersion.V_1_20_80,
            tags = listOf("Vibrant Colors", "Warm Lighting", "Soft Shadows", "Cinematic")
        ),
        ShaderPreset(
            id = "shader_fps_clean",
            title = "FPS Booster & Crystal Clear Waters",
            subtitle = "เพิ่มความลื่นไหล น้ำใสแจ๋ว มองเห็นใต้ทะเลชัดเจน",
            description = "ออกแบบสำหรับสาย Survival และ PvP ที่ต้องการเฟรมเรตสูง ลดความซับซ้อนของหมอก แต่น้ำยังใสสะอาดและมองเห็นทรัพยากรใต้น้ำชัดเจน",
            performanceLevel = "เบามาก (FPS สูงสุด 120 FPS)",
            drawableResId = null,
            waterWaves = false,
            waterReflectionPercent = 40,
            sunRaysGodRays = false,
            bloomIntensityPercent = 20,
            glowingOres = true,
            cloudsStyle = "2D Classic",
            minVersion = MinecraftVersion.V_1_18_30,
            tags = listOf("High FPS", "PvP Ready", "Clear Water", "Low Battery")
        )
    )

    val curatedPacks: List<com.example.model.AddonPackBundle> = listOf(
        com.example.model.AddonPackBundle(
            id = "bundle_ultimate_mega",
            title = "Ultimate Custom Addon Mega Bundle",
            subtitle = "ชุดใหญ่ครบวงจร! 3D อาวุธเทพ + เฟอร์นิเจอร์ + อาหารเวทมนตร์",
            description = "แพ็กชุดใหญ่ยอดนิยมอันดับ 1 รวมทั้งดาบรูบี้สุริยะ คทาสายฟ้า โซฟาหนัง 3D โต๊ะคอมพิวเตอร์เกมมิ่ง RGB และอาหารบัฟ เหมาะสำหรับแจกต่อหรือลงเซิร์ฟเวอร์เล่นกับเพื่อนได้ทันที!",
            author = "MC Addon Studio Pro",
            version = "1.2.0",
            categoryBadge = "Mega Bundle รวมชุดใหญ่",
            genre = AddonGenre.WEAPON_TOOLS,
            minVersion = MinecraftVersion.V_1_21_40,
            defaultFormat = ExportFormat.MCADDON,
            downloadCount = "48.5K+",
            bannerDrawableRes = R.drawable.packs_banner,
            isHot = true,
            highlights = listOf(
                "ดาบรูบี้สุริยะ (Solar Ruby Blade) โจมตี 18",
                "คทาสายฟ้ายิงกระสุนเวทย์ (Thunder Staff)",
                "โซฟาหนังโมเดิร์น 3D นั่งได้จริง",
                "โต๊ะคอมเกมมิ่ง RGB พร้อมไฟนีออน 14",
                "สเต๊กเนื้อทองคำฟื้นฟูเลือดเต็มหลอด"
            ),
            items = listOf(
                CustomItemConfig(
                    id = "item_mega_ruby",
                    identifier = "custom:solar_ruby_blade",
                    displayName = "ดาบรูบี้สุริยะมหาเทพ (Solar Ruby Blade)",
                    attackDamage = 18,
                    durability = 2800,
                    hasGlint = true,
                    handEquipped = true,
                    iconEmoji = "🗡️",
                    primaryColorHex = "#E11D48"
                ),
                CustomItemConfig(
                    id = "item_mega_thunder",
                    identifier = "custom:thunder_storm_staff",
                    displayName = "คทาสายฟ้าทำลายล้าง (Thunder Storm Staff)",
                    attackDamage = 14,
                    durability = 1500,
                    shootProjectile = true,
                    hasGlint = true,
                    handEquipped = true,
                    iconEmoji = "🪄",
                    primaryColorHex = "#F59E0B"
                ),
                CustomItemConfig(
                    id = "item_mega_cleaver",
                    identifier = "custom:void_cleaver",
                    displayName = "ขวานตัดมิติ (Void Cleaver)",
                    attackDamage = 20,
                    durability = 3200,
                    hasGlint = true,
                    handEquipped = true,
                    iconEmoji = "🪓",
                    primaryColorHex = "#8B5CF6"
                ),
                CustomItemConfig(
                    id = "item_mega_food",
                    identifier = "custom:dragon_golden_steak",
                    displayName = "สเต๊กมังกรทองคำ (Dragon Golden Steak)",
                    attackDamage = 1,
                    durability = 0,
                    maxStackSize = 64,
                    isFood = true,
                    nutrition = 10,
                    saturation = 1.5f,
                    canAlwaysEat = true,
                    hasGlint = true,
                    iconEmoji = "🥩",
                    primaryColorHex = "#F59E0B"
                )
            ),
            blocks = listOf(
                CustomBlockConfig(
                    id = "block_mega_sofa",
                    identifier = "custom:modern_leather_sofa",
                    displayName = "โซฟาหนังโมเดิร์น 3D (Modern Leather Sofa)",
                    lightEmission = 0,
                    hardness = 2.0f,
                    soundType = "Wood",
                    geometryShape = "3D Custom Model",
                    blockColorHex = "#2563EB",
                    blockEmoji = "🛋️",
                    modelBoxes = listOf(
                        VoxelBox("seat", "Seat", -8f, 2f, -6f, 16f, 4f, 12f, "#1E40AF"),
                        VoxelBox("back", "Backrest", -8f, 6f, 4f, 16f, 8f, 2f, "#1D4ED8"),
                        VoxelBox("arm_l", "Armrest L", -8f, 2f, -6f, 2f, 8f, 12f, "#172554"),
                        VoxelBox("arm_r", "Armrest R", 6f, 2f, -6f, 2f, 8f, 12f, "#172554")
                    )
                ),
                CustomBlockConfig(
                    id = "block_mega_desk",
                    identifier = "custom:rgb_gaming_desk",
                    displayName = "โต๊ะคอมเกมมิ่ง RGB (Gaming Setup Desk)",
                    lightEmission = 14,
                    hardness = 2.5f,
                    soundType = "Metal",
                    geometryShape = "3D Custom Model",
                    blockColorHex = "#06B6D4",
                    blockEmoji = "🖥️",
                    modelBoxes = listOf(
                        VoxelBox("tabletop", "Table Top", -7f, 7f, -6f, 14f, 2f, 12f, "#0F172A"),
                        VoxelBox("monitor", "Monitor Screen", -4f, 9f, 2f, 8f, 6f, 1f, "#06B6D4"),
                        VoxelBox("leg1", "Leg L", -6f, 0f, -4f, 2f, 7f, 8f, "#1E293B"),
                        VoxelBox("leg2", "Leg R", 4f, 0f, -4f, 2f, 7f, 8f, "#1E293B")
                    )
                ),
                CustomBlockConfig(
                    id = "block_mega_lamp",
                    identifier = "custom:crystal_neon_lamp",
                    displayName = "โคมไฟคริสตัลระย้าเรืองแสง (Crystal Neon Lamp)",
                    lightEmission = 15,
                    hardness = 1.0f,
                    soundType = "Glass",
                    geometryShape = "3D Custom Model",
                    blockColorHex = "#10B981",
                    blockEmoji = "💡"
                )
            )
        ),
        com.example.model.AddonPackBundle(
            id = "bundle_mythic_weapons",
            title = "Mythic Elemental Weapons Pack",
            subtitle = "คลังอาวุธ 4 ธาตุในตำนาน! เพลิง วายุ น้ำแข็ง และอัสนี",
            description = "อัปเกรดระบบต่อสู้ของ Minecraft Bedrock ด้วยอาวุธธาตุระดับเทวะ แสงสะท้อนเรืองรอง พลังโจมตีสูงถึง 22 และสูตรคราฟต์บนโต๊ะ Crafting Table",
            author = "Titan Blades Guild",
            version = "2.1.0",
            categoryBadge = "อาวุธระดับเทพ",
            genre = AddonGenre.WEAPON_TOOLS,
            minVersion = MinecraftVersion.V_1_21_40,
            defaultFormat = ExportFormat.MCADDON,
            downloadCount = "32.1K+",
            highlights = listOf(
                "ดาบอัคคีพิโรธ (Inferno Blade) โจมตี 20 ติดไฟ",
                "ดาบคาตานะเยือกแข็ง (Glacial Katana) วิ่งเร็ว",
                "คทาวายุพายุหมุน (Aero Gale Wand) ยิงกระสุน",
                "โล่เทพพิทักษ์สะท้อนดาเมจ (Aegis Guardian Shield)"
            ),
            items = listOf(
                CustomItemConfig(
                    id = "item_mythic_inferno",
                    identifier = "custom:inferno_greatsword",
                    displayName = "ดาบอัคคีพิโรธ (Inferno Greatsword)",
                    attackDamage = 20,
                    durability = 3500,
                    hasGlint = true,
                    handEquipped = true,
                    iconEmoji = "🗡️",
                    primaryColorHex = "#DC2626"
                ),
                CustomItemConfig(
                    id = "item_mythic_ice",
                    identifier = "custom:glacial_ice_katana",
                    displayName = "ดาบคาตานะเยือกแข็ง (Glacial Ice Katana)",
                    attackDamage = 16,
                    durability = 2600,
                    hasGlint = true,
                    handEquipped = true,
                    iconEmoji = "⚔️",
                    primaryColorHex = "#38BDF8"
                ),
                CustomItemConfig(
                    id = "item_mythic_wind",
                    identifier = "custom:aero_gale_wand",
                    displayName = "คทาวายุหมุน (Aero Gale Wand)",
                    attackDamage = 12,
                    durability = 1800,
                    shootProjectile = true,
                    hasGlint = true,
                    handEquipped = true,
                    iconEmoji = "🪄",
                    primaryColorHex = "#10B981"
                ),
                CustomItemConfig(
                    id = "item_mythic_shield",
                    identifier = "custom:aegis_guardian_shield",
                    displayName = "โล่เทพพิทักษ์ (Aegis Guardian Shield)",
                    attackDamage = 6,
                    durability = 4000,
                    hasGlint = true,
                    handEquipped = true,
                    iconEmoji = "🛡️",
                    primaryColorHex = "#F59E0B"
                )
            ),
            blocks = emptyList()
        ),
        com.example.model.AddonPackBundle(
            id = "bundle_modern_furniture",
            title = "Modern Luxury 3D Furniture Pack",
            subtitle = "ตกแต่งบ้านสวยสมจริง! โซฟา โต๊ะคอม ตู้เย็น โคมไฟ 3D",
            description = "เปลี่ยนบ้านดินเป็นคฤหาสน์หรูด้วยเฟอร์นิเจอร์โมเดิร์น 3D Voxel รายละเอียดสูง นั่งได้จริง พร้อมแสงสว่างนีออนและเสียงบล็อกเหมือนจริง",
            author = "ModernCraft Studios",
            version = "1.5.0",
            categoryBadge = "เฟอร์นิเจอร์ 3D",
            genre = AddonGenre.BLOCKS_FURNITURE,
            minVersion = MinecraftVersion.V_1_21_0,
            defaultFormat = ExportFormat.MCADDON,
            downloadCount = "29.8K+",
            highlights = listOf(
                "โซฟาหนังสีครีม 3D นั่งพักผ่อนได้",
                "ตู้เย็นอัจฉริยะ (Smart Refrigerator)",
                "โต๊ะทำงานกระจกโมเดิร์น",
                "โคมไฟระย้าคริสตัล สว่างระดับ 15"
            ),
            items = emptyList(),
            blocks = listOf(
                CustomBlockConfig(
                    id = "block_furn_sofa",
                    identifier = "custom:cream_leather_sofa",
                    displayName = "โซฟาหนังครีมโมเดิร์น 3D (Cream Leather Sofa)",
                    lightEmission = 0,
                    hardness = 2.0f,
                    soundType = "Wood",
                    geometryShape = "3D Custom Model",
                    blockColorHex = "#D97706",
                    blockEmoji = "🛋️"
                ),
                CustomBlockConfig(
                    id = "block_furn_fridge",
                    identifier = "custom:smart_metal_refrigerator",
                    displayName = "ตู้เย็นสองประตูอัจฉริยะ (Smart Refrigerator)",
                    lightEmission = 3,
                    hardness = 3.5f,
                    soundType = "Metal",
                    geometryShape = "3D Custom Model",
                    blockColorHex = "#94A3B8",
                    blockEmoji = "🧊"
                ),
                CustomBlockConfig(
                    id = "block_furn_chair",
                    identifier = "custom:ergonomic_office_chair",
                    displayName = "เก้าอี้ทำงานโมเดิร์น 3D (Ergonomic Chair)",
                    lightEmission = 0,
                    hardness = 2.0f,
                    soundType = "Metal",
                    geometryShape = "3D Custom Model",
                    blockColorHex = "#334155",
                    blockEmoji = "🪑"
                )
            )
        ),
        com.example.model.AddonPackBundle(
            id = "bundle_magic_food",
            title = "Survival Magic Food & Farming Treats",
            subtitle = "อาหารบัฟและพืชพันธุ์วิเศษ ฟื้นเลือด อมตะ วิ่งเร็ว",
            description = "ไอเทมอาหารและเครื่องดื่มเวทมนตร์สำหรับสาย Survival เพิ่มผลไม้วิเศษ แอปเปิ้ลทอง 100% สเต๊กเนื้อเวทมนตร์ และน้ำยาฟื้นเลือดเต็มหลอดทันที",
            author = "Alchemist Steve",
            version = "1.1.0",
            categoryBadge = "อาหาร & เอาชีวิตรอด",
            genre = AddonGenre.FOOD_CROPS,
            minVersion = MinecraftVersion.V_1_20_80,
            defaultFormat = ExportFormat.MCADDON,
            downloadCount = "18.3K+",
            highlights = listOf(
                "แอปเปิ้ลทองคำบริสุทธิ์ (Pure Golden Apple) อิ่ม 10",
                "เบอร์รี่สปีดวายุ (Storm Speed Berry) วิ่งเร็ว III",
                "สเต๊กเนื้อไฟลาวา (Magma Steak) ทนไฟ 5 นาที",
                "น้ำยาฟื้นหัวใจบริสุทธิ์ (Pure Heart Elixir)"
            ),
            items = listOf(
                CustomItemConfig(
                    id = "item_food_apple",
                    identifier = "custom:pure_golden_apple",
                    displayName = "แอปเปิ้ลทองคำบริสุทธิ์ (Pure Golden Apple)",
                    attackDamage = 1,
                    durability = 0,
                    maxStackSize = 64,
                    isFood = true,
                    nutrition = 10,
                    saturation = 1.8f,
                    canAlwaysEat = true,
                    hasGlint = true,
                    iconEmoji = "🍎",
                    primaryColorHex = "#FBBF24"
                ),
                CustomItemConfig(
                    id = "item_food_berry",
                    identifier = "custom:storm_speed_berry",
                    displayName = "เบอร์รี่สปีดวายุ (Storm Speed Berry)",
                    attackDamage = 1,
                    durability = 0,
                    maxStackSize = 64,
                    isFood = true,
                    nutrition = 6,
                    saturation = 1.0f,
                    canAlwaysEat = true,
                    hasGlint = true,
                    iconEmoji = "🫐",
                    primaryColorHex = "#38BDF8"
                ),
                CustomItemConfig(
                    id = "item_food_steak",
                    identifier = "custom:magma_fire_steak",
                    displayName = "สเต๊กเนื้อไฟลาวา (Magma Fire Steak)",
                    attackDamage = 2,
                    durability = 0,
                    maxStackSize = 64,
                    isFood = true,
                    nutrition = 12,
                    saturation = 1.6f,
                    canAlwaysEat = false,
                    hasGlint = true,
                    iconEmoji = "🥩",
                    primaryColorHex = "#EF4444"
                )
            ),
            blocks = emptyList()
        )
    )

    fun loadProjects(): List<AddonProject> {
        if (!projectsFile.exists()) {
            val defaultProjects = createInitialSampleProjects()
            saveProjects(defaultProjects)
            return defaultProjects
        }

        return try {
            val jsonStr = projectsFile.readText()
            parseProjectsJson(jsonStr)
        } catch (e: Exception) {
            e.printStackTrace()
            val defaultProjects = createInitialSampleProjects()
            saveProjects(defaultProjects)
            defaultProjects
        }
    }

    fun saveProjects(projects: List<AddonProject>) {
        try {
            val jsonArray = JSONArray()
            for (proj in projects) {
                jsonArray.put(serializeProject(proj))
            }
            projectsFile.writeText(jsonArray.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveProject(project: AddonProject, triggerAutoBackup: Boolean = true): List<AddonProject> {
        val current = loadProjects().toMutableList()
        val index = current.indexOfFirst { it.id == project.id }
        val updatedProject = project.copy(lastModified = System.currentTimeMillis())

        if (index >= 0) {
            current[index] = updatedProject
        } else {
            current.add(0, updatedProject)
        }
        saveProjects(current)

        if (triggerAutoBackup && updatedProject.isAutoBackupEnabled) {
            performAutoBackup(updatedProject, "สำรองข้อมูลอัตโนมัติเมื่อบันทึก")
        }

        return current
    }

    fun deleteProject(projectId: String): List<AddonProject> {
        val current = loadProjects().filterNot { it.id == projectId }
        saveProjects(current)
        return current
    }

    fun duplicateProject(projectId: String): List<AddonProject> {
        val current = loadProjects().toMutableList()
        val original = current.find { it.id == projectId } ?: return current
        val copy = original.copy(
            id = "proj_${System.currentTimeMillis()}",
            name = "${original.name} (คัดลอก)",
            lastModified = System.currentTimeMillis()
        )
        current.add(0, copy)
        saveProjects(current)
        return current
    }

    fun loadBackups(): List<BackupSnapshot> {
        if (!backupsFile.exists()) return emptyList()
        return try {
            val jsonArray = JSONArray(backupsFile.readText())
            val list = mutableListOf<BackupSnapshot>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val format = try {
                    ExportFormat.valueOf(obj.getString("format"))
                } catch (e: Exception) {
                    ExportFormat.MCPACK
                }
                list.add(
                    BackupSnapshot(
                        id = obj.getString("id"),
                        projectId = obj.getString("projectId"),
                        projectName = obj.getString("projectName"),
                        format = format,
                        version = obj.getString("version"),
                        timestamp = obj.getLong("timestamp"),
                        fileSizeFormatted = obj.getString("fileSizeFormatted"),
                        filePath = obj.getString("filePath"),
                        reasonNote = obj.optString("reasonNote", "สำรองข้อมูลอัตโนมัติ")
                    )
                )
            }
            list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveBackups(list: List<BackupSnapshot>) {
        try {
            val array = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("projectId", item.projectId)
                    put("projectName", item.projectName)
                    put("format", item.format.name)
                    put("version", item.version)
                    put("timestamp", item.timestamp)
                    put("fileSizeFormatted", item.fileSizeFormatted)
                    put("filePath", item.filePath)
                    put("reasonNote", item.reasonNote)
                }
                array.put(obj)
            }
            backupsFile.writeText(array.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun performAutoBackup(project: AddonProject, note: String = "สำรองข้อมูลอัตโนมัติ"): BackupSnapshot? {
        return try {
            val backupResult = MinecraftPackCompiler.compileProject(
                context = context,
                project = project,
                targetOutputDir = backupsDir
            )
            val snapshot = BackupSnapshot(
                id = "backup_${System.currentTimeMillis()}",
                projectId = project.id,
                projectName = project.name,
                format = backupResult.format,
                version = project.version,
                timestamp = System.currentTimeMillis(),
                fileSizeFormatted = backupResult.formattedSize,
                filePath = backupResult.file.absolutePath,
                reasonNote = note
            )

            val currentBackups = loadBackups().toMutableList()
            currentBackups.add(0, snapshot)
            // Limit to last 30 backups
            val trimmed = currentBackups.take(30)
            saveBackups(trimmed)
            snapshot
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteBackup(backupId: String): List<BackupSnapshot> {
        val current = loadBackups().toMutableList()
        val found = current.find { it.id == backupId }
        if (found != null) {
            val file = File(found.filePath)
            if (file.exists()) file.delete()
            current.remove(found)
            saveBackups(current)
        }
        return current
    }

    fun restoreBackup(backup: BackupSnapshot): Boolean {
        // Look if the project still exists
        val projects = loadProjects().toMutableList()
        val existingIndex = projects.indexOfFirst { it.id == backup.projectId }
        if (existingIndex >= 0) {
            // Updated timestamp
            projects[existingIndex] = projects[existingIndex].copy(
                lastModified = System.currentTimeMillis()
            )
            saveProjects(projects)
            return true
        }
        return false
    }

    fun getExportedPacks(): List<File> {
        if (!packsDir.exists()) return emptyList()
        return packsDir.listFiles()?.filter {
            it.isFile && (it.name.endsWith(".mcpack") || it.name.endsWith(".mcaddon") || it.name.endsWith(".zip"))
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private fun createInitialSampleProjects(): List<AddonProject> {
        // 1. Weapons Project
        val weaponProject = AddonProject(
            id = "proj_weapons_ruby",
            name = "ดาบรูบี้และคทาสุริยะ (Ruby Blades)",
            author = "MC Craft Master",
            version = "1.0.0",
            description = "เพิ่มดาบรูบี้พลังโจมตี 14 หน่วย พร้อมประกายแสงเวทมนตร์และคทาสุริยะยิงลูกไฟ",
            targetVersion = MinecraftVersion.V_1_21_40,
            exportFormat = ExportFormat.MCADDON,
            genre = AddonGenre.WEAPON_TOOLS,
            items = listOf(
                CustomItemConfig(
                    id = "item_ruby_sword",
                    identifier = "custom:ruby_broadsword",
                    displayName = "ดาบรูบี้ประกายแสง (Ruby Broadsword)",
                    attackDamage = 14,
                    durability = 2200,
                    maxStackSize = 1,
                    hasGlint = true,
                    handEquipped = true,
                    iconEmoji = "🗡️",
                    primaryColorHex = "#E11D48",
                    categoryTag = "Equipment",
                    recipe = RecipeConfig(
                        isEnabled = true,
                        resultCount = 1,
                        grid = listOf(
                            CraftingSlot(0, "", ""), CraftingSlot(1, "Diamond", "💎"), CraftingSlot(2, "", ""),
                            CraftingSlot(3, "", ""), CraftingSlot(4, "Diamond", "💎"), CraftingSlot(5, "", ""),
                            CraftingSlot(6, "", ""), CraftingSlot(7, "Stick", "🥢"), CraftingSlot(8, "", "")
                        )
                    )
                ),
                CustomItemConfig(
                    id = "item_solar_staff",
                    identifier = "custom:solar_staff",
                    displayName = "คทาสุริยะ (Solar Staff)",
                    attackDamage = 8,
                    durability = 850,
                    maxStackSize = 1,
                    hasGlint = true,
                    handEquipped = true,
                    shootProjectile = true,
                    iconEmoji = "🪄",
                    primaryColorHex = "#F59E0B",
                    categoryTag = "Equipment"
                )
            ),
            blocks = listOf(
                CustomBlockConfig(
                    id = "block_ruby_ore",
                    identifier = "custom:deepslate_ruby_ore",
                    displayName = "แร่รูบี้หินลึก (Deepslate Ruby Ore)",
                    lightEmission = 8,
                    hardness = 4.5f,
                    explosionResistance = 30f,
                    soundType = "Stone",
                    blockColorHex = "#BE123C",
                    blockEmoji = "💎"
                )
            )
        )

        // 2. Modern 3D Blocks Project
        val blockProject = AddonProject(
            id = "proj_blocks_decor",
            name = "เฟอร์นิเจอร์โมเดิร์น & คริสตัลเรืองแสง",
            author = "ArchitectCrafter",
            version = "1.0.0",
            description = "บล็อกคริสตัลนีออนเรืองแสงระดับ 15 และโต๊ะเก้าอี้โมเดิร์น 3D แบบปรับแต่งได้",
            targetVersion = MinecraftVersion.V_1_21_40,
            exportFormat = ExportFormat.MCPACK,
            genre = AddonGenre.BLOCKS_FURNITURE,
            blocks = listOf(
                CustomBlockConfig(
                    id = "block_neon_lamp",
                    identifier = "custom:neon_crystal_cube",
                    displayName = "โคมไฟคริสตัลนีออน (Neon Crystal Cube)",
                    lightEmission = 15,
                    hardness = 1.0f,
                    explosionResistance = 10f,
                    soundType = "Glass",
                    geometryShape = "3D Custom Model",
                    blockColorHex = "#06B6D4",
                    blockEmoji = "💡",
                    modelBoxes = listOf(
                        VoxelBox("base", "Outer Shell", -7f, 0f, -7f, 14f, 14f, 14f, "#06B6D4"),
                        VoxelBox("core", "Glow Core", -4f, 3f, -4f, 8f, 8f, 8f, "#67E8F9")
                    )
                ),
                CustomBlockConfig(
                    id = "block_oak_chair",
                    identifier = "custom:modern_oak_chair",
                    displayName = "เก้าอี้ไม้โอ๊คโมเดิร์น 3D",
                    lightEmission = 0,
                    hardness = 2.0f,
                    soundType = "Wood",
                    geometryShape = "3D Custom Model",
                    blockColorHex = "#B45309",
                    blockEmoji = "🪑",
                    modelBoxes = listOf(
                        VoxelBox("seat", "Seat Pad", -6f, 6f, -6f, 12f, 2f, 12f, "#B45309"),
                        VoxelBox("back", "Backrest", -6f, 8f, 4f, 12f, 8f, 2f, "#92400E"),
                        VoxelBox("leg1", "Leg FL", -5f, 0f, -5f, 2f, 6f, 2f, "#78350F"),
                        VoxelBox("leg2", "Leg FR", 3f, 0f, -5f, 2f, 6f, 2f, "#78350F"),
                        VoxelBox("leg3", "Leg BL", -5f, 0f, 3f, 2f, 6f, 2f, "#78350F"),
                        VoxelBox("leg4", "Leg BR", 3f, 0f, 3f, 2f, 6f, 2f, "#78350F")
                    )
                )
            )
        )

        // 3. Realistic Shaders Project
        val shaderProject = AddonProject(
            id = "proj_shaders_rtx",
            name = "RTX Lumina Ultra Shader Pack",
            author = "ShaderDev Studios",
            version = "1.0.0",
            description = "แพ็ก Shader แสงเงาสมจริงระดับ RTX ผิวน้ำคลื่นพริ้วไหว ลำแสงแดดยามเย็น และแสงแร่เรืองแสง",
            targetVersion = MinecraftVersion.V_1_21_40,
            exportFormat = ExportFormat.MCPACK,
            genre = AddonGenre.SHADERS_VISUAL,
            selectedShaderPresetId = "shader_rtx_ultra",
            shaderCustomWater = 90,
            shaderCustomBloom = 75,
            shaderCustomGodRays = true
        )

        // 4. Magic Food Project
        val foodProject = AddonProject(
            id = "proj_food_buffs",
            name = "ผลไม้วิเศษ & ซุปพลังเวทย์ (Magic Treats)",
            author = "Chef Steve",
            version = "1.0.0",
            description = "แอปเปิ้ลดาวตก ฟื้นฟูเลือดทันที บัฟวิ่งเร็ว II และมองเห็นในที่มืด 5 นาที",
            targetVersion = MinecraftVersion.V_1_20_80,
            exportFormat = ExportFormat.MCADDON,
            genre = AddonGenre.FOOD_CROPS,
            items = listOf(
                CustomItemConfig(
                    id = "item_star_apple",
                    identifier = "custom:celestial_star_apple",
                    displayName = "แอปเปิ้ลดาวตกวิเศษ (Star Apple)",
                    attackDamage = 1,
                    durability = 0,
                    maxStackSize = 64,
                    isFood = true,
                    nutrition = 8,
                    saturation = 1.2f,
                    canAlwaysEat = true,
                    hasGlint = true,
                    iconEmoji = "🍎",
                    primaryColorHex = "#FBBF24",
                    categoryTag = "Items"
                )
            )
        )

        return listOf(weaponProject, blockProject, shaderProject, foodProject)
    }

    private fun serializeProject(project: AddonProject): JSONObject {
        return JSONObject().apply {
            put("id", project.id)
            put("name", project.name)
            put("author", project.author)
            put("version", project.version)
            put("description", project.description)
            put("targetVersion", project.targetVersion.name)
            put("exportFormat", project.exportFormat.name)
            put("genre", project.genre.name)
            put("selectedShaderPresetId", project.selectedShaderPresetId ?: "")
            put("shaderCustomWater", project.shaderCustomWater)
            put("shaderCustomBloom", project.shaderCustomBloom)
            put("shaderCustomGodRays", project.shaderCustomGodRays)
            put("lastModified", project.lastModified)
            put("isAutoBackupEnabled", project.isAutoBackupEnabled)

            // Items
            val itemsArr = JSONArray()
            for (item in project.items) {
                val itemObj = JSONObject().apply {
                    put("id", item.id)
                    put("identifier", item.identifier)
                    put("displayName", item.displayName)
                    put("attackDamage", item.attackDamage)
                    put("durability", item.durability)
                    put("maxStackSize", item.maxStackSize)
                    put("isFood", item.isFood)
                    put("nutrition", item.nutrition)
                    put("saturation", item.saturation.toDouble())
                    put("canAlwaysEat", item.canAlwaysEat)
                    put("hasGlint", item.hasGlint)
                    put("handEquipped", item.handEquipped)
                    put("shootProjectile", item.shootProjectile)
                    put("iconEmoji", item.iconEmoji)
                    put("primaryColorHex", item.primaryColorHex)
                    put("categoryTag", item.categoryTag)

                    // Recipe
                    val recObj = JSONObject().apply {
                        put("isEnabled", item.recipe.isEnabled)
                        put("resultCount", item.recipe.resultCount)
                        val gridArr = JSONArray()
                        for (slot in item.recipe.grid) {
                            gridArr.put(JSONObject().apply {
                                put("index", slot.index)
                                put("itemName", slot.itemName)
                                put("itemIconChar", slot.itemIconChar)
                            })
                        }
                        put("grid", gridArr)
                    }
                    put("recipe", recObj)
                }
                itemsArr.put(itemObj)
            }
            put("items", itemsArr)

            // Blocks
            val blocksArr = JSONArray()
            for (b in project.blocks) {
                val bObj = JSONObject().apply {
                    put("id", b.id)
                    put("identifier", b.identifier)
                    put("displayName", b.displayName)
                    put("lightEmission", b.lightEmission)
                    put("explosionResistance", b.explosionResistance.toDouble())
                    put("friction", b.friction.toDouble())
                    put("hardness", b.hardness.toDouble())
                    put("soundType", b.soundType)
                    put("geometryShape", b.geometryShape)
                    put("isFlammable", b.isFlammable)
                    put("dropsSelf", b.dropsSelf)
                    put("blockColorHex", b.blockColorHex)
                    put("blockEmoji", b.blockEmoji)

                    val boxesArr = JSONArray()
                    for (box in b.modelBoxes) {
                        boxesArr.put(JSONObject().apply {
                            put("id", box.id)
                            put("name", box.name)
                            put("originX", box.originX.toDouble())
                            put("originY", box.originY.toDouble())
                            put("originZ", box.originZ.toDouble())
                            put("sizeX", box.sizeX.toDouble())
                            put("sizeY", box.sizeY.toDouble())
                            put("sizeZ", box.sizeZ.toDouble())
                            put("colorHex", box.colorHex)
                        })
                    }
                    put("modelBoxes", boxesArr)
                }
                blocksArr.put(bObj)
            }
            put("blocks", blocksArr)
        }
    }

    private fun parseProjectsJson(jsonStr: String): List<AddonProject> {
        val jsonArray = JSONArray(jsonStr)
        val list = mutableListOf<AddonProject>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val targetVer = try {
                MinecraftVersion.valueOf(obj.getString("targetVersion"))
            } catch (e: Exception) {
                MinecraftVersion.V_1_21_40
            }
            val exportFmt = try {
                ExportFormat.valueOf(obj.getString("exportFormat"))
            } catch (e: Exception) {
                ExportFormat.MCPACK
            }
            val genre = try {
                AddonGenre.valueOf(obj.getString("genre"))
            } catch (e: Exception) {
                AddonGenre.WEAPON_TOOLS
            }

            // Items
            val itemsList = mutableListOf<CustomItemConfig>()
            val itemsArr = obj.optJSONArray("items") ?: JSONArray()
            for (j in 0 until itemsArr.length()) {
                val itObj = itemsArr.getJSONObject(j)
                val recObj = itObj.optJSONObject("recipe")
                val gridList = mutableListOf<CraftingSlot>()
                if (recObj != null) {
                    val gArr = recObj.optJSONArray("grid") ?: JSONArray()
                    for (k in 0 until gArr.length()) {
                        val sObj = gArr.getJSONObject(k)
                        gridList.add(
                            CraftingSlot(
                                index = sObj.getInt("index"),
                                itemName = sObj.optString("itemName", ""),
                                itemIconChar = sObj.optString("itemIconChar", "")
                            )
                        )
                    }
                }
                while (gridList.size < 9) {
                    gridList.add(CraftingSlot(gridList.size, "", ""))
                }

                itemsList.add(
                    CustomItemConfig(
                        id = itObj.getString("id"),
                        identifier = itObj.getString("identifier"),
                        displayName = itObj.getString("displayName"),
                        attackDamage = itObj.optInt("attackDamage", 10),
                        durability = itObj.optInt("durability", 1000),
                        maxStackSize = itObj.optInt("maxStackSize", 1),
                        isFood = itObj.optBoolean("isFood", false),
                        nutrition = itObj.optInt("nutrition", 4),
                        saturation = itObj.optDouble("saturation", 0.6).toFloat(),
                        canAlwaysEat = itObj.optBoolean("canAlwaysEat", false),
                        hasGlint = itObj.optBoolean("hasGlint", false),
                        handEquipped = itObj.optBoolean("handEquipped", true),
                        shootProjectile = itObj.optBoolean("shootProjectile", false),
                        iconEmoji = itObj.optString("iconEmoji", "⚔️"),
                        primaryColorHex = itObj.optString("primaryColorHex", "#EF4444"),
                        categoryTag = itObj.optString("categoryTag", "Equipment"),
                        recipe = RecipeConfig(
                            isEnabled = recObj?.optBoolean("isEnabled", true) ?: true,
                            resultCount = recObj?.optInt("resultCount", 1) ?: 1,
                            grid = gridList
                        )
                    )
                )
            }

            // Blocks
            val blocksList = mutableListOf<CustomBlockConfig>()
            val blocksArr = obj.optJSONArray("blocks") ?: JSONArray()
            for (b in 0 until blocksArr.length()) {
                val bObj = blocksArr.getJSONObject(b)
                val boxesList = mutableListOf<VoxelBox>()
                val bArr = bObj.optJSONArray("modelBoxes") ?: JSONArray()
                for (k in 0 until bArr.length()) {
                    val boxObj = bArr.getJSONObject(k)
                    boxesList.add(
                        VoxelBox(
                            id = boxObj.getString("id"),
                            name = boxObj.optString("name", "Box $k"),
                            originX = boxObj.optDouble("originX", -8.0).toFloat(),
                            originY = boxObj.optDouble("originY", 0.0).toFloat(),
                            originZ = boxObj.optDouble("originZ", -8.0).toFloat(),
                            sizeX = boxObj.optDouble("sizeX", 16.0).toFloat(),
                            sizeY = boxObj.optDouble("sizeY", 16.0).toFloat(),
                            sizeZ = boxObj.optDouble("sizeZ", 16.0).toFloat(),
                            colorHex = boxObj.optString("colorHex", "#3B82F6")
                        )
                    )
                }

                blocksList.add(
                    CustomBlockConfig(
                        id = bObj.getString("id"),
                        identifier = bObj.getString("identifier"),
                        displayName = bObj.getString("displayName"),
                        lightEmission = bObj.optInt("lightEmission", 0),
                        explosionResistance = bObj.optDouble("explosionResistance", 15.0).toFloat(),
                        friction = bObj.optDouble("friction", 0.6).toFloat(),
                        hardness = bObj.optDouble("hardness", 2.0).toFloat(),
                        soundType = bObj.optString("soundType", "Stone"),
                        geometryShape = bObj.optString("geometryShape", "Full Cube"),
                        isFlammable = bObj.optBoolean("isFlammable", false),
                        dropsSelf = bObj.optBoolean("dropsSelf", true),
                        blockColorHex = bObj.optString("blockColorHex", "#8B5CF6"),
                        blockEmoji = bObj.optString("blockEmoji", "🔮"),
                        modelBoxes = boxesList.ifEmpty {
                            listOf(VoxelBox("box_main", "Cube", -8f, 0f, -8f, 16f, 16f, 16f, "#8B5CF6"))
                        }
                    )
                )
            }

            list.add(
                AddonProject(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    author = obj.optString("author", "Creator"),
                    version = obj.optString("version", "1.0.0"),
                    description = obj.optString("description", ""),
                    targetVersion = targetVer,
                    exportFormat = exportFmt,
                    genre = genre,
                    items = itemsList,
                    blocks = blocksList,
                    selectedShaderPresetId = obj.optString("selectedShaderPresetId").takeIf { it.isNotBlank() },
                    shaderCustomWater = obj.optInt("shaderCustomWater", 80),
                    shaderCustomBloom = obj.optInt("shaderCustomBloom", 65),
                    shaderCustomGodRays = obj.optBoolean("shaderCustomGodRays", true),
                    lastModified = obj.optLong("lastModified", System.currentTimeMillis()),
                    isAutoBackupEnabled = obj.optBoolean("isAutoBackupEnabled", true)
                )
            )
        }
        return list
    }
}
