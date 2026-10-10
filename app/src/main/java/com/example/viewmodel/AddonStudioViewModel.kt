package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.generator.InstallerHelper
import com.example.generator.MinecraftPackCompiler
import com.example.model.AddonGenre
import com.example.model.AddonProject
import com.example.model.BackupSnapshot
import com.example.model.CustomBlockConfig
import com.example.model.CustomItemConfig
import com.example.model.ExportFormat
import com.example.model.MinecraftVersion
import com.example.model.RecipeConfig
import com.example.model.ShaderPreset
import com.example.model.VoxelBox
import com.example.storage.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class AppScreen {
    PROJECTS_LIST,
    PROJECT_EDITOR,
    PACKS_CATALOG,
    SHADERS_GALLERY,
    FILE_MANAGER
}

data class DistributionResult(
    val file: File,
    val packTitle: String,
    val formattedSize: String,
    val format: ExportFormat,
    val isBundle: Boolean = false
)

enum class EditorTab(val title: String, val icon: String) {
    OVERVIEW("ภาพรวม", "info"),
    ITEMS("ไอเทม", "swords"),
    BLOCKS("บล็อก", "cube"),
    MODEL_3D("โมเดล 3D", "cube_3d"),
    RECIPE("คราฟต์ 3x3", "grid"),
    EXPORT("ติดตั้ง & ส่งออก", "download")
}

enum class ShaderLightingTime(val title: String, val emoji: String) {
    DAY("กลางวัน", "☀️"),
    SUNSET("พระอาทิตย์ตก", "🌅"),
    NIGHT("กลางคืน & ถ้ำ", "🌌")
}

class AddonStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)

    private val _projects = MutableStateFlow<List<AddonProject>>(emptyList())
    val projects: StateFlow<List<AddonProject>> = _projects.asStateFlow()

    private val _currentProject = MutableStateFlow<AddonProject?>(null)
    val currentProject: StateFlow<AddonProject?> = _currentProject.asStateFlow()

    private val _activeScreen = MutableStateFlow(AppScreen.PROJECTS_LIST)
    val activeScreen: StateFlow<AppScreen> = _activeScreen.asStateFlow()

    private val _editorTab = MutableStateFlow(EditorTab.OVERVIEW)
    val editorTab: StateFlow<EditorTab> = _editorTab.asStateFlow()

    val shaderPresets: List<ShaderPreset> = repository.shaderPresets

    private val _selectedShader = MutableStateFlow<ShaderPreset>(shaderPresets.first())
    val selectedShader: StateFlow<ShaderPreset> = _selectedShader.asStateFlow()

    private val _shaderPreviewTime = MutableStateFlow(ShaderLightingTime.SUNSET)
    val shaderPreviewTime: StateFlow<ShaderLightingTime> = _shaderPreviewTime.asStateFlow()

    private val _shaderPreviewWaves = MutableStateFlow(true)
    val shaderPreviewWaves: StateFlow<Boolean> = _shaderPreviewWaves.asStateFlow()

    private val _backups = MutableStateFlow<List<BackupSnapshot>>(emptyList())
    val backups: StateFlow<List<BackupSnapshot>> = _backups.asStateFlow()

    private val _exportedFiles = MutableStateFlow<List<File>>(emptyList())
    val exportedFiles: StateFlow<List<File>> = _exportedFiles.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    // 3D Model Editor Selection
    private val _activeEditingBoxId = MutableStateFlow<String?>(null)
    val activeEditingBoxId: StateFlow<String?> = _activeEditingBoxId.asStateFlow()

    // Curated Community Packs
    val curatedPacks: List<com.example.model.AddonPackBundle> = repository.curatedPacks

    // Active Distribution Result for showing Download/Distribute Dialog
    private val _activeDistributionResult = MutableStateFlow<DistributionResult?>(null)
    val activeDistributionResult: StateFlow<DistributionResult?> = _activeDistributionResult.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val list = repository.loadProjects()
            _projects.value = list
            _backups.value = repository.loadBackups()
            _exportedFiles.value = repository.getExportedPacks()
            if (_currentProject.value == null && list.isNotEmpty()) {
                _currentProject.value = list.first()
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _activeScreen.value = screen
    }

    fun setEditorTab(tab: EditorTab) {
        _editorTab.value = tab
    }

    fun selectProject(project: AddonProject) {
        _currentProject.value = project
        _activeScreen.value = AppScreen.PROJECT_EDITOR
    }

    fun createNewProject(
        name: String,
        genre: AddonGenre,
        targetVersion: MinecraftVersion,
        format: ExportFormat
    ) {
        val newId = "proj_${System.currentTimeMillis()}"
        val initialItem = when (genre) {
            AddonGenre.WEAPON_TOOLS -> CustomItemConfig(
                id = "item_${System.currentTimeMillis()}",
                identifier = "custom:${genre.defaultPrefix}_sword",
                displayName = "$name - ดาบหลัก",
                attackDamage = 12,
                durability = 1800,
                iconEmoji = "🗡️",
                primaryColorHex = "#E11D48"
            )
            AddonGenre.FOOD_CROPS -> CustomItemConfig(
                id = "item_${System.currentTimeMillis()}",
                identifier = "custom:${genre.defaultPrefix}_fruit",
                displayName = "$name - ผลไม้วิเศษ",
                attackDamage = 1,
                durability = 0,
                maxStackSize = 64,
                isFood = true,
                nutrition = 6,
                saturation = 0.8f,
                canAlwaysEat = true,
                iconEmoji = "🍇",
                primaryColorHex = "#A855F7"
            )
            else -> CustomItemConfig(
                id = "item_${System.currentTimeMillis()}",
                identifier = "custom:${genre.defaultPrefix}_item",
                displayName = "$name - ไอเทม",
                iconEmoji = "✨",
                primaryColorHex = "#3B82F6"
            )
        }

        val initialBlock = when (genre) {
            AddonGenre.BLOCKS_FURNITURE -> CustomBlockConfig(
                id = "block_${System.currentTimeMillis()}",
                identifier = "custom:${genre.defaultPrefix}_deco",
                displayName = "$name - บล็อกดีไซน์ 3D",
                lightEmission = 10,
                hardness = 2.0f,
                geometryShape = "3D Custom Model",
                blockColorHex = "#10B981",
                blockEmoji = "📦",
                modelBoxes = listOf(
                    VoxelBox("box_1", "ฐานบล็อก", -7f, 0f, -7f, 14f, 12f, 14f, "#10B981"),
                    VoxelBox("box_2", "คริสตัลยอด", -3f, 12f, -3f, 6f, 4f, 6f, "#34D399")
                )
            )
            else -> CustomBlockConfig(
                id = "block_${System.currentTimeMillis()}",
                identifier = "custom:${genre.defaultPrefix}_block",
                displayName = "$name - บล็อก",
                lightEmission = 0,
                blockColorHex = "#64748B",
                blockEmoji = "🧱"
            )
        }

        val newProject = AddonProject(
            id = newId,
            name = name.ifBlank { "แอดออนใหม่" },
            author = "คุณ (Creator)",
            description = "สร้างด้วย MC Addon Studio รองรับ ${targetVersion.versionString}",
            targetVersion = targetVersion,
            exportFormat = format,
            genre = genre,
            items = if (genre == AddonGenre.SHADERS_VISUAL) emptyList() else listOf(initialItem),
            blocks = if (genre == AddonGenre.SHADERS_VISUAL) emptyList() else listOf(initialBlock),
            selectedShaderPresetId = if (genre == AddonGenre.SHADERS_VISUAL) shaderPresets.first().id else null
        )

        val updatedList = repository.saveProject(newProject, triggerAutoBackup = true)
        _projects.value = updatedList
        _currentProject.value = newProject
        _editorTab.value = EditorTab.OVERVIEW
        _activeScreen.value = AppScreen.PROJECT_EDITOR
        _notificationMessage.value = "สร้าง Addon \"${newProject.name}\" สำเร็จแล้ว!"
    }

    fun updateCurrentProject(updated: AddonProject) {
        _currentProject.value = updated
        val list = repository.saveProject(updated, triggerAutoBackup = updated.isAutoBackupEnabled)
        _projects.value = list
        _backups.value = repository.loadBackups()
    }

    fun deleteProject(projectId: String) {
        val updatedList = repository.deleteProject(projectId)
        _projects.value = updatedList
        if (_currentProject.value?.id == projectId) {
            _currentProject.value = updatedList.firstOrNull()
            _activeScreen.value = AppScreen.PROJECTS_LIST
        }
        _notificationMessage.value = "ลบโครงการเรียบร้อยแล้ว"
    }

    fun duplicateProject(projectId: String) {
        val updatedList = repository.duplicateProject(projectId)
        _projects.value = updatedList
        _notificationMessage.value = "คัดลอกโครงการสำเร็จแล้ว"
    }

    // Item Operations
    fun addItemToCurrentProject(item: CustomItemConfig) {
        val current = _currentProject.value ?: return
        val updatedItems = current.items + item
        updateCurrentProject(current.copy(items = updatedItems))
    }

    fun updateItemInCurrentProject(item: CustomItemConfig) {
        val current = _currentProject.value ?: return
        val updatedItems = current.items.map { if (it.id == item.id) item else it }
        updateCurrentProject(current.copy(items = updatedItems))
    }

    fun removeItemFromCurrentProject(itemId: String) {
        val current = _currentProject.value ?: return
        val updatedItems = current.items.filterNot { it.id == itemId }
        updateCurrentProject(current.copy(items = updatedItems))
    }

    // Block Operations
    fun addBlockToCurrentProject(block: CustomBlockConfig) {
        val current = _currentProject.value ?: return
        val updatedBlocks = current.blocks + block
        updateCurrentProject(current.copy(blocks = updatedBlocks))
    }

    fun updateBlockInCurrentProject(block: CustomBlockConfig) {
        val current = _currentProject.value ?: return
        val updatedBlocks = current.blocks.map { if (it.id == block.id) block else it }
        updateCurrentProject(current.copy(blocks = updatedBlocks))
    }

    fun removeBlockFromCurrentProject(blockId: String) {
        val current = _currentProject.value ?: return
        val updatedBlocks = current.blocks.filterNot { it.id == blockId }
        updateCurrentProject(current.copy(blocks = updatedBlocks))
    }

    // 3D Model Voxel Box Operations for selected block
    fun addVoxelBoxToBlock(blockId: String, box: VoxelBox) {
        val current = _currentProject.value ?: return
        val targetBlock = current.blocks.find { it.id == blockId } ?: return
        val updatedBoxes = targetBlock.modelBoxes + box
        updateBlockInCurrentProject(targetBlock.copy(modelBoxes = updatedBoxes))
        _activeEditingBoxId.value = box.id
    }

    fun updateVoxelBoxInBlock(blockId: String, box: VoxelBox) {
        val current = _currentProject.value ?: return
        val targetBlock = current.blocks.find { it.id == blockId } ?: return
        val updatedBoxes = targetBlock.modelBoxes.map { if (it.id == box.id) box else it }
        updateBlockInCurrentProject(targetBlock.copy(modelBoxes = updatedBoxes))
    }

    fun removeVoxelBoxFromBlock(blockId: String, boxId: String) {
        val current = _currentProject.value ?: return
        val targetBlock = current.blocks.find { it.id == blockId } ?: return
        val updatedBoxes = targetBlock.modelBoxes.filterNot { it.id == boxId }
        updateBlockInCurrentProject(targetBlock.copy(modelBoxes = updatedBoxes))
        if (_activeEditingBoxId.value == boxId) {
            _activeEditingBoxId.value = updatedBoxes.firstOrNull()?.id
        }
    }

    fun selectEditingBox(boxId: String?) {
        _activeEditingBoxId.value = boxId
    }

    // Shader Operations
    fun selectShaderPreset(preset: ShaderPreset) {
        _selectedShader.value = preset
    }

    fun setShaderPreviewTime(time: ShaderLightingTime) {
        _shaderPreviewTime.value = time
    }

    fun toggleShaderWaves() {
        _shaderPreviewWaves.update { !it }
    }

    fun applyShaderToProject(preset: ShaderPreset) {
        val current = _currentProject.value ?: return
        val updated = current.copy(
            selectedShaderPresetId = preset.id,
            shaderCustomWater = preset.waterReflectionPercent,
            shaderCustomBloom = preset.bloomIntensityPercent,
            shaderCustomGodRays = preset.sunRaysGodRays
        )
        updateCurrentProject(updated)
        _notificationMessage.value = "นำ Shader \"${preset.title}\" เข้าสู่โปรเจกต์แล้ว!"
    }

    // 1-Tap Install Direct from Shader Gallery
    fun installShaderPresetDirect(preset: ShaderPreset) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val shaderProject = AddonProject(
                id = "temp_shader_${preset.id}",
                name = "${preset.title} Shader",
                author = "Shader Preset",
                version = "1.0.0",
                description = preset.description,
                targetVersion = preset.minVersion,
                exportFormat = ExportFormat.MCPACK,
                genre = AddonGenre.SHADERS_VISUAL,
                selectedShaderPresetId = preset.id,
                shaderCustomWater = preset.waterReflectionPercent,
                shaderCustomBloom = preset.bloomIntensityPercent,
                shaderCustomGodRays = preset.sunRaysGodRays
            )

            val result = MinecraftPackCompiler.compileProject(context, shaderProject, ExportFormat.MCPACK)
            _exportedFiles.value = repository.getExportedPacks()
            val installed = InstallerHelper.installToMinecraft(context, result.file)
            if (installed) {
                _notificationMessage.value = "ส่งไฟล์ ${result.file.name} เข้าสู่ Minecraft เรียบร้อยแล้ว!"
            }
        }
    }

    // Packaging & Installation Actions
    fun compileAndInstallCurrent(format: ExportFormat? = null) {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            val context = getApplication<Application>()
            val result = MinecraftPackCompiler.compileProject(context, proj, format)
            _exportedFiles.value = repository.getExportedPacks()
            if (proj.isAutoBackupEnabled) {
                repository.performAutoBackup(proj, "สำรองข้อมูลก่อนติดตั้งเข้าเกม")
                _backups.value = repository.loadBackups()
            }
            InstallerHelper.installToMinecraft(context, result.file)
            _notificationMessage.value = "สร้างไฟล์ ${result.file.name} (${result.formattedSize}) และส่งเข้า Minecraft!"
        }
    }

    fun compileAndShareCurrent(format: ExportFormat? = null) {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            val context = getApplication<Application>()
            val result = MinecraftPackCompiler.compileProject(context, proj, format)
            _exportedFiles.value = repository.getExportedPacks()
            InstallerHelper.sharePack(context, result.file)
        }
    }

    fun compileAndExportToDownloads(format: ExportFormat? = null) {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            val context = getApplication<Application>()
            val result = MinecraftPackCompiler.compileProject(context, proj, format)
            _exportedFiles.value = repository.getExportedPacks()
            val exported = InstallerHelper.exportToDownloads(context, result.file)
            if (exported != null) {
                _notificationMessage.value = "บันทึกไฟล์ไปที่ Downloads: ${exported.name}"
            }
        }
    }

    fun triggerManualBackup() {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            val snapshot = repository.performAutoBackup(proj, "สำรองข้อมูลแบบทำเอง (Manual Snapshot)")
            if (snapshot != null) {
                _backups.value = repository.loadBackups()
                _notificationMessage.value = "สำรองข้อมูลสำเร็จ: ${snapshot.fileSizeFormatted}"
            }
        }
    }

    fun restoreBackup(backup: BackupSnapshot) {
        viewModelScope.launch {
            val success = repository.restoreBackup(backup)
            if (success) {
                loadData()
                _notificationMessage.value = "กู้คืนข้อมูลโครงการ ${backup.projectName} สำเร็จ!"
            } else {
                _notificationMessage.value = "กู้คืนไม่สำเร็จ ไม่พบโครงการต้นฉบับ"
            }
        }
    }

    fun deleteBackup(backupId: String) {
        viewModelScope.launch {
            _backups.value = repository.deleteBackup(backupId)
            _notificationMessage.value = "ลบไฟล์สำรองข้อมูลเรียบร้อยแล้ว"
        }
    }

    fun openExportedFileInMinecraft(file: File) {
        val context = getApplication<Application>()
        InstallerHelper.installToMinecraft(context, file)
    }

    fun shareExportedFile(file: File) {
        val context = getApplication<Application>()
        InstallerHelper.sharePack(context, file)
    }

    fun deleteExportedFile(file: File) {
        if (file.exists()) {
            file.delete()
            _exportedFiles.value = repository.getExportedPacks()
            _notificationMessage.value = "ลบไฟล์เรียบร้อยแล้ว"
        }
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    // Community Pack Bundles Operations
    fun downloadAndDistributeBundle(
        bundle: com.example.model.AddonPackBundle,
        format: ExportFormat = bundle.defaultFormat,
        openSharePrompt: Boolean = true
    ) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val project = bundle.toAddonProject().copy(exportFormat = format)
            val result = MinecraftPackCompiler.compileProject(context, project, format)
            _exportedFiles.value = repository.getExportedPacks()

            // Save to Downloads folder
            InstallerHelper.exportToDownloads(
                context = context,
                sourceFile = result.file,
                packTitle = bundle.title,
                openSharePrompt = openSharePrompt
            )

            _activeDistributionResult.value = DistributionResult(
                file = result.file,
                packTitle = bundle.title,
                formattedSize = result.formattedSize,
                format = format,
                isBundle = true
            )
            _notificationMessage.value = "ดาวน์โหลด \"${bundle.title}\" ลงโฟลเดอร์ Downloads แล้ว พร้อมแชร์แจกต่อ!"
        }
    }

    fun installBundleDirect(bundle: com.example.model.AddonPackBundle) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val project = bundle.toAddonProject()
            val result = MinecraftPackCompiler.compileProject(context, project, bundle.defaultFormat)
            _exportedFiles.value = repository.getExportedPacks()
            InstallerHelper.installToMinecraft(context, result.file)
            _notificationMessage.value = "ส่งแพ็ก \"${bundle.title}\" เข้าสู่ Minecraft เรียบร้อยแล้ว!"
        }
    }

    fun remixBundleIntoProject(bundle: com.example.model.AddonPackBundle) {
        viewModelScope.launch {
            val newProject = bundle.toAddonProject().copy(
                name = "${bundle.title} (รีมิกซ์)",
                author = "คุณ (Remixer)"
            )
            val updated = repository.saveProject(newProject, triggerAutoBackup = true)
            _projects.value = updated
            _currentProject.value = newProject
            _activeScreen.value = AppScreen.PROJECT_EDITOR
            _editorTab.value = EditorTab.ITEMS
            _notificationMessage.value = "นำเข้าแพ็ก \"${bundle.title}\" สู่ Addon Studio เพื่อปรับแต่งแล้ว!"
        }
    }

    fun downloadProjectForDistribution(
        project: AddonProject,
        format: ExportFormat = project.exportFormat,
        openSharePrompt: Boolean = true
    ) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val result = MinecraftPackCompiler.compileProject(context, project, format)
            _exportedFiles.value = repository.getExportedPacks()

            // Save to public Downloads
            InstallerHelper.exportToDownloads(
                context = context,
                sourceFile = result.file,
                packTitle = project.name,
                openSharePrompt = openSharePrompt
            )

            _activeDistributionResult.value = DistributionResult(
                file = result.file,
                packTitle = project.name,
                formattedSize = result.formattedSize,
                format = format,
                isBundle = false
            )
            _notificationMessage.value = "ดาวน์โหลด \"${project.name}\" พร้อมแจกต่อเรียบร้อยแล้ว!"
        }
    }

    fun dismissDistributionDialog() {
        _activeDistributionResult.value = null
    }
}
