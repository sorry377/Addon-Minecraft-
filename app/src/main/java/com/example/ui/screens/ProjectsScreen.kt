package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AddonGenre
import com.example.model.AddonProject
import com.example.model.ExportFormat
import com.example.model.MinecraftVersion
import com.example.viewmodel.AddonStudioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: AddonStudioViewModel,
    projects: List<AddonProject>,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedGenreFilter by remember { mutableStateOf<AddonGenre?>(null) }
    var projectToDelete by remember { mutableStateOf<AddonProject?>(null) }

    val filteredProjects = remember(projects, selectedGenreFilter) {
        if (selectedGenreFilter == null) projects
        else projects.filter { it.genre == selectedGenreFilter }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E3A2F)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981)
                            ) {
                                Text(
                                    text = "MC BEDROCK ADDON MAKER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "พร้อมติดตั้ง 1-Tap",
                                fontSize = 12.sp,
                                color = Color(0xFFA7F3D0)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "สร้างแอดออน .mcpack และ .mcaddon",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ปรับแต่งบล็อก ไอเทม ทำโมเดล 3D แบบไม่ต้องเขียนโค้ด เลือกรุ่นมายคราฟ และกดติดตั้งเข้าตัวเกมได้ทันที",
                            fontSize = 13.sp,
                            color = Color(0xFFD1FAE5),
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Genre Filters
            item {
                Column {
                    Text(
                        text = "หมวดหมู่แอดออน (Addon Genre)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedGenreFilter == null,
                                onClick = { selectedGenreFilter = null },
                                label = { Text("ทั้งหมด (${projects.size})") }
                            )
                        }
                        items(AddonGenre.values()) { genre ->
                            FilterChip(
                                selected = selectedGenreFilter == genre,
                                onClick = {
                                    selectedGenreFilter = if (selectedGenreFilter == genre) null else genre
                                },
                                label = { Text(genre.displayName) }
                            )
                        }
                    }
                }
            }

            // Projects List
            if (filteredProjects.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📦", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ยังไม่มีแอดออนในหมวดหมู่นี้",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "แตะปุ่มด้านล่างเพื่อเริ่มสร้างแอดออนชิ้นแรกของคุณ",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("สร้าง Addon ใหม่")
                            }
                        }
                    }
                }
            } else {
                items(filteredProjects, key = { it.id }) { project ->
                    ProjectCardItem(
                        project = project,
                        onOpen = { viewModel.selectProject(project) },
                        onInstall = {
                            viewModel.selectProject(project)
                            viewModel.compileAndInstallCurrent()
                        },
                        onDownloadDistribute = {
                            viewModel.downloadProjectForDistribution(project, project.exportFormat, true)
                        },
                        onShare = {
                            viewModel.selectProject(project)
                            viewModel.compileAndShareCurrent()
                        },
                        onDuplicate = { viewModel.duplicateProject(project.id) },
                        onDelete = { projectToDelete = project }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB Create Button
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = Color(0xFF10B981),
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_addon_fab")
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "Create Addon")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "สร้าง Addon", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Create Addon Dialog with Genre & Version Selector
    if (showCreateDialog) {
        CreateAddonDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, genre, targetVersion, format ->
                viewModel.createNewProject(name, genre, targetVersion, format)
                showCreateDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (projectToDelete != null) {
        val target = projectToDelete!!
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("ลบโครงการ Addon?") },
            text = { Text("คุณแน่ใจหรือไม่ว่าต้องการลบ \"${target.name}\"? ข้อมูลทั้งหมดจะถูกลบออกจากตัวเครื่อง") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(target.id)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ลบถาวร")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
fun ProjectCardItem(
    project: AddonProject,
    onOpen: () -> Unit,
    onInstall: () -> Unit,
    onDownloadDistribute: () -> Unit,
    onShare: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault()) }
    val timeFormatted = remember(project.lastModified) { dateFormat.format(Date(project.lastModified)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("project_card_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Genre & Version Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (project.genre) {
                        AddonGenre.WEAPON_TOOLS -> Color(0xFFE11D48)
                        AddonGenre.BLOCKS_FURNITURE -> Color(0xFF059669)
                        AddonGenre.SHADERS_VISUAL -> Color(0xFF2563EB)
                        AddonGenre.FOOD_CROPS -> Color(0xFFD97706)
                        AddonGenre.ARMOR_GEAR -> Color(0xFF7C3AED)
                        AddonGenre.ENTITIES_PETS -> Color(0xFF0D9488)
                    }
                ) {
                    Text(
                        text = project.genre.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = project.exportFormat.extension,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "v${project.targetVersion.versionString}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Project Title & Description
            Text(
                text = project.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = project.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stats info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ไอเทม: ${project.items.size} ชิ้น • บล็อก: ${project.blocks.size} บล็อก",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = timeFormatted,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Row: Install to Minecraft & Download to Distribute
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary 1-Tap Install into Minecraft
                Button(
                    onClick = onInstall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("install_to_mc_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Install",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ติดตั้งเข้าเกม",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Download & Share for Distribution
                Button(
                    onClick = onDownloadDistribute,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("download_share_proj_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Download & Share",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ดาวน์โหลดแจกต่อ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Secondary Row: Edit, Duplicate, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpen,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("แก้ไข", fontSize = 12.sp)
                }

                Row {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDuplicate) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAddonDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, genre: AddonGenre, version: MinecraftVersion, format: ExportFormat) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf(AddonGenre.WEAPON_TOOLS) }
    var selectedVersion by remember { mutableStateOf(MinecraftVersion.V_1_21_40) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.MCADDON) }

    var versionExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "สร้างโครงการ Addon ใหม่", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("ชื่อแอดออน (Addon Name)") },
                        placeholder = { Text("เช่น ดาบสายฟ้าในตำนาน") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_addon_name_input")
                    )
                }

                // Genre selection
                item {
                    Text(
                        text = "เลือกแนวแอดออน (Addon Genre):",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "การเลือกแนวที่ต่างกันจะปรับการตั้งค่าและเทมเพลตเริ่มต้นให้เหมาะสมโดยอัตโนมัติ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AddonGenre.values().forEach { genre ->
                            val isSelected = selectedGenre == genre
                            Surface(
                                onClick = { selectedGenre = genre },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = genre.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = genre.description,
                                            fontSize = 11.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Target Minecraft Version
                item {
                    Text(
                        text = "เวอร์ชัน Minecraft ที่รองรับ:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    ExposedDropdownMenuBox(
                        expanded = versionExpanded,
                        onExpandedChange = { versionExpanded = !versionExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedVersion.formatBadge(),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = versionExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = versionExpanded,
                            onDismissRequest = { versionExpanded = false }
                        ) {
                            MinecraftVersion.values().forEach { ver ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(ver.formatBadge(), fontWeight = FontWeight.Bold)
                                            Text(ver.highlights, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedVersion = ver
                                        versionExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Export Format
                item {
                    Text(
                        text = "นามสกุลไฟล์ที่ต้องการส่งออก:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ExportFormat.values().forEach { fmt ->
                            val isSelected = selectedFormat == fmt
                            Surface(
                                onClick = { selectedFormat = fmt },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = fmt.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = fmt.description,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = name.ifBlank { selectedGenre.displayName }
                    onConfirm(finalName, selectedGenre, selectedVersion, selectedFormat)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.testTag("confirm_create_addon_btn")
            ) {
                Text("เริ่มสร้างแอดออน", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก")
            }
        }
    )
}
