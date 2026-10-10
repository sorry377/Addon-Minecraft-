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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AddonGenre
import com.example.model.AddonProject
import com.example.model.CustomBlockConfig
import com.example.model.CustomItemConfig
import com.example.model.ExportFormat
import com.example.model.MinecraftVersion
import com.example.model.VoxelBox
import com.example.ui.components.CraftingGridRecipeView
import com.example.ui.components.Voxel3DViewport
import com.example.viewmodel.AddonStudioViewModel
import com.example.viewmodel.AppScreen
import com.example.viewmodel.EditorTab

val COLOR_PALETTE = listOf(
    "#EF4444", "#F97316", "#F59E0B", "#10B981",
    "#06B6D4", "#3B82F6", "#8B5CF6", "#EC4899",
    "#64748B", "#1E293B", "#FAFAFA", "#B45309"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectEditorScreen(
    viewModel: AddonStudioViewModel,
    project: AddonProject,
    modifier: Modifier = Modifier
) {
    val editorTab by viewModel.editorTab.collectAsState()
    val activeBoxId by viewModel.activeEditingBoxId.collectAsState()

    var selectedItemIndex by remember { mutableIntStateOf(0) }
    var selectedBlockIndex by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Bar
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.PROJECTS_LIST) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "เป้าหมาย: ${project.targetVersion.versionString} • ${project.exportFormat.extension}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick Install Button on Top Bar
                Button(
                    onClick = { viewModel.compileAndInstallCurrent() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("top_bar_install_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ติดตั้ง", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = editorTab.ordinal,
            edgePadding = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            EditorTab.values().forEach { tab ->
                Tab(
                    selected = editorTab == tab,
                    onClick = { viewModel.setEditorTab(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (editorTab == tab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (editorTab) {
                EditorTab.OVERVIEW -> OverviewTabContent(viewModel, project)
                EditorTab.ITEMS -> ItemsTabContent(
                    project = project,
                    selectedIndex = selectedItemIndex,
                    onSelectIndex = { selectedItemIndex = it },
                    onItemUpdated = { viewModel.updateItemInCurrentProject(it) },
                    onAddItem = {
                        val newItem = CustomItemConfig(
                            id = "item_${System.currentTimeMillis()}",
                            identifier = "custom:item_${project.items.size + 1}",
                            displayName = "ไอเทมใหม่ #${project.items.size + 1}",
                            attackDamage = 8,
                            durability = 1000,
                            iconEmoji = "🗡️"
                        )
                        viewModel.addItemToCurrentProject(newItem)
                        selectedItemIndex = project.items.size
                    },
                    onDeleteItem = { id ->
                        viewModel.removeItemFromCurrentProject(id)
                        selectedItemIndex = 0
                    }
                )
                EditorTab.BLOCKS -> BlocksTabContent(
                    project = project,
                    selectedIndex = selectedBlockIndex,
                    onSelectIndex = { selectedBlockIndex = it },
                    onBlockUpdated = { viewModel.updateBlockInCurrentProject(it) },
                    onAddBlock = {
                        val newBlock = CustomBlockConfig(
                            id = "block_${System.currentTimeMillis()}",
                            identifier = "custom:block_${project.blocks.size + 1}",
                            displayName = "บล็อกใหม่ #${project.blocks.size + 1}",
                            lightEmission = 8,
                            hardness = 2.0f,
                            blockColorHex = "#10B981",
                            blockEmoji = "🔮"
                        )
                        viewModel.addBlockToCurrentProject(newBlock)
                        selectedBlockIndex = project.blocks.size
                    },
                    onDeleteBlock = { id ->
                        viewModel.removeBlockFromCurrentProject(id)
                        selectedBlockIndex = 0
                    }
                )
                EditorTab.MODEL_3D -> Model3DTabContent(
                    project = project,
                    selectedBlockIndex = selectedBlockIndex,
                    activeBoxId = activeBoxId,
                    onSelectBox = { viewModel.selectEditingBox(it) },
                    onAddBox = { blockId, box -> viewModel.addVoxelBoxToBlock(blockId, box) },
                    onUpdateBox = { blockId, box -> viewModel.updateVoxelBoxInBlock(blockId, box) },
                    onDeleteBox = { blockId, boxId -> viewModel.removeVoxelBoxFromBlock(blockId, boxId) }
                )
                EditorTab.RECIPE -> RecipeTabContent(
                    project = project,
                    selectedItemIndex = selectedItemIndex,
                    onRecipeChanged = { item, newRecipe ->
                        viewModel.updateItemInCurrentProject(item.copy(recipe = newRecipe))
                    }
                )
                EditorTab.EXPORT -> ExportTabContent(viewModel, project)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewTabContent(viewModel: AddonStudioViewModel, project: AddonProject) {
    var name by remember(project.id) { mutableStateOf(project.name) }
    var author by remember(project.id) { mutableStateOf(project.author) }
    var version by remember(project.id) { mutableStateOf(project.version) }
    var description by remember(project.id) { mutableStateOf(project.description) }
    var targetVersion by remember(project.id) { mutableStateOf(project.targetVersion) }
    var exportFormat by remember(project.id) { mutableStateOf(project.exportFormat) }
    var genre by remember(project.id) { mutableStateOf(project.genre) }
    var autoBackup by remember(project.id) { mutableStateOf(project.isAutoBackupEnabled) }

    var versionExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "ข้อมูลพื้นฐานแอดออน (Manifest Settings)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    viewModel.updateCurrentProject(project.copy(name = it))
                },
                label = { Text("ชื่อโครงการ Addon") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = author,
                    onValueChange = {
                        author = it
                        viewModel.updateCurrentProject(project.copy(author = it))
                    },
                    label = { Text("ผู้สร้าง (Author)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = version,
                    onValueChange = {
                        version = it
                        viewModel.updateCurrentProject(project.copy(version = it))
                    },
                    label = { Text("เวอร์ชันแพ็ก") },
                    modifier = Modifier.width(110.dp)
                )
            }
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    viewModel.updateCurrentProject(project.copy(description = it))
                },
                label = { Text("คำอธิบายแอดออน") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Target Minecraft Version
        item {
            Text(
                text = "เวอร์ชัน Minecraft Bedrock เป้าหมาย:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            ExposedDropdownMenuBox(
                expanded = versionExpanded,
                onExpandedChange = { versionExpanded = !versionExpanded }
            ) {
                OutlinedTextField(
                    value = targetVersion.formatBadge(),
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
                                targetVersion = ver
                                viewModel.updateCurrentProject(project.copy(targetVersion = ver))
                                versionExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Export Format Selector
        item {
            Text(
                text = "นามสกุลไฟล์ส่งออก:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ExportFormat.values().forEach { fmt ->
                    FilterChip(
                        selected = exportFormat == fmt,
                        onClick = {
                            exportFormat = fmt
                            viewModel.updateCurrentProject(project.copy(exportFormat = fmt))
                        },
                        label = { Text(fmt.extension) }
                    )
                }
            }
        }

        // Auto Backup Setting
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ระบบสำรองข้อมูลอัตโนมัติ (Auto Backup)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "บันทึกและสร้างจุดสำรอง Snapshot ทุกครั้งที่มีการแก้ไขหรือติดตั้ง",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoBackup,
                        onCheckedChange = {
                            autoBackup = it
                            viewModel.updateCurrentProject(project.copy(isAutoBackupEnabled = it))
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ItemsTabContent(
    project: AddonProject,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    onItemUpdated: (CustomItemConfig) -> Unit,
    onAddItem: () -> Unit,
    onDeleteItem: (String) -> Unit
) {
    if (project.items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "⚔️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "ยังไม่มีไอเทมในโครงการนี้", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onAddItem) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("เพิ่มไอเทมใหม่")
                }
            }
        }
        return
    }

    val currentItem = project.items.getOrNull(selectedIndex) ?: project.items.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Items Horizontal Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "รายการไอเทม (${project.items.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Button(
                    onClick = onAddItem,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("เพิ่มไอเทม", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(project.items.indices.toList()) { idx ->
                    val it = project.items[idx]
                    FilterChip(
                        selected = idx == selectedIndex,
                        onClick = { onSelectIndex(idx) },
                        label = { Text("${it.iconEmoji} ${it.displayName}") }
                    )
                }
            }
        }

        // Item Card Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = currentItem.iconEmoji, fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentItem.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = currentItem.identifier,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (project.items.size > 1) {
                        IconButton(onClick = { onDeleteItem(currentItem.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Basic Info
        item {
            OutlinedTextField(
                value = currentItem.displayName,
                onValueChange = { onItemUpdated(currentItem.copy(displayName = it)) },
                label = { Text("ชื่อที่แสดงในเกม (Display Name)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = currentItem.identifier,
                onValueChange = { onItemUpdated(currentItem.copy(identifier = it)) },
                label = { Text("รหัสไอเทม (Identifier เช่น custom:ruby_sword)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Emoji Selector
        item {
            Text(text = "ไอคอนสัญลักษณ์:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            val emojis = listOf("🗡️", "⚔️", "🪄", "🏹", "🪓", "⛏️", "🛡️", "🍎", "🥩", "💎", "🔮", "🧪")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                items(emojis) { em ->
                    Surface(
                        onClick = { onItemUpdated(currentItem.copy(iconEmoji = em)) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (currentItem.iconEmoji == em) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = if (currentItem.iconEmoji == em) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = em, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        // No-code Combat & Durability Sliders
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "พลังโจมตี & ความทนทาน (Combat Stats)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "พลังโจมตี: ${currentItem.attackDamage} หัวใจ (Damage)", fontSize = 13.sp)
                    Slider(
                        value = currentItem.attackDamage.toFloat(),
                        onValueChange = { onItemUpdated(currentItem.copy(attackDamage = it.toInt())) },
                        valueRange = 0f..50f,
                        steps = 49
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = "ความทนทาน: ${currentItem.durability} ครั้ง (Durability)", fontSize = 13.sp)
                    Slider(
                        value = currentItem.durability.toFloat(),
                        onValueChange = { onItemUpdated(currentItem.copy(durability = it.toInt())) },
                        valueRange = 0f..5000f,
                        steps = 99
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = "ขนาดกองสูงสุด: ${currentItem.maxStackSize} ชิ้น (Max Stack)", fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        listOf(1, 16, 64).forEach { size ->
                            FilterChip(
                                selected = currentItem.maxStackSize == size,
                                onClick = { onItemUpdated(currentItem.copy(maxStackSize = size)) },
                                label = { Text("$size ชิ้น") }
                            )
                        }
                    }
                }
            }
        }

        // Magic Glint & Hand Equipped Switches
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("แสงสะท้อนเวทมนตร์ (Enchanted Glint)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("ไอเทมมีเอฟเฟกต์แสงวิบวับเปล่งประกายเหมือนร่ายมนตร์", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = currentItem.hasGlint,
                            onCheckedChange = { onItemUpdated(currentItem.copy(hasGlint = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ถือแสดงในมือเป็น 3D (Hand Equipped)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("ตัวละครจะถือไอเทมชี้ตรงเหมือนอาวุธจริง", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = currentItem.handEquipped,
                            onCheckedChange = { onItemUpdated(currentItem.copy(handEquipped = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ยิงกระสุนพลัง (Shoot Projectile)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("สามารถแตะคลิกขวา/กดค้างเพื่อยิงลูกพลัง", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = currentItem.shootProjectile,
                            onCheckedChange = { onItemUpdated(currentItem.copy(shootProjectile = it)) }
                        )
                    }
                }
            }
        }

        // Food & Edible Properties
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("เป็นอาหารกินได้ (Edible Food)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("เปิดเพื่อให้ผู้เล่นสามารถเคี้ยวกินไอเทมนี้ได้", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = currentItem.isFood,
                            onCheckedChange = { onItemUpdated(currentItem.copy(isFood = it)) }
                        )
                    }

                    if (currentItem.isFood) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "ค่าความอิ่ม: ${currentItem.nutrition} ขาแกะ (Nutrition)", fontSize = 13.sp)
                        Slider(
                            value = currentItem.nutrition.toFloat(),
                            onValueChange = { onItemUpdated(currentItem.copy(nutrition = it.toInt())) },
                            valueRange = 1f..20f,
                            steps = 19
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("กินได้แม้หลอดอาหารจะเต็มแล้ว", fontSize = 12.sp)
                            Switch(
                                checked = currentItem.canAlwaysEat,
                                onCheckedChange = { onItemUpdated(currentItem.copy(canAlwaysEat = it)) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun BlocksTabContent(
    project: AddonProject,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    onBlockUpdated: (CustomBlockConfig) -> Unit,
    onAddBlock: () -> Unit,
    onDeleteBlock: (String) -> Unit
) {
    if (project.blocks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🧱", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "ยังไม่มีบล็อกในโครงการนี้", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onAddBlock) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("เพิ่มบล็อกใหม่")
                }
            }
        }
        return
    }

    val currentBlock = project.blocks.getOrNull(selectedIndex) ?: project.blocks.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "รายการบล็อก (${project.blocks.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Button(
                    onClick = onAddBlock,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("เพิ่มบล็อก", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(project.blocks.indices.toList()) { idx ->
                    val b = project.blocks[idx]
                    FilterChip(
                        selected = idx == selectedIndex,
                        onClick = { onSelectIndex(idx) },
                        label = { Text("${b.blockEmoji} ${b.displayName}") }
                    )
                }
            }
        }

        // Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val blockColor = try {
                        Color(android.graphics.Color.parseColor(currentBlock.blockColorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(blockColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = currentBlock.blockEmoji, fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentBlock.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = currentBlock.identifier,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (project.blocks.size > 1) {
                        IconButton(onClick = { onDeleteBlock(currentBlock.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Names
        item {
            OutlinedTextField(
                value = currentBlock.displayName,
                onValueChange = { onBlockUpdated(currentBlock.copy(displayName = it)) },
                label = { Text("ชื่อบล็อกที่แสดงในเกม (Display Name)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = currentBlock.identifier,
                onValueChange = { onBlockUpdated(currentBlock.copy(identifier = it)) },
                label = { Text("รหัสบล็อก (Identifier เช่น custom:neon_block)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Geometry & Shape
        item {
            Text(text = "รูปทรงของบล็อก (Geometry Shape):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                listOf("Full Cube", "Slab (ครึ่งบล็อก)", "3D Custom Model").forEach { shape ->
                    FilterChip(
                        selected = currentBlock.geometryShape == shape,
                        onClick = { onBlockUpdated(currentBlock.copy(geometryShape = shape)) },
                        label = { Text(shape) }
                    )
                }
            }
        }

        // Light Emission Slider with Visual Glow!
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ระดับการเปล่งแสง (Light Emission): ${currentBlock.lightEmission} / 15",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        // Glow circle preview
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFBBF24).copy(alpha = (currentBlock.lightEmission / 15f).coerceIn(0.1f, 1f)))
                        )
                    }

                    Slider(
                        value = currentBlock.lightEmission.toFloat(),
                        onValueChange = { onBlockUpdated(currentBlock.copy(lightEmission = it.toInt())) },
                        valueRange = 0f..15f,
                        steps = 14
                    )
                    Text(
                        text = if (currentBlock.lightEmission == 0) "ไม่เรืองแสง (ปกติ)" else if (currentBlock.lightEmission < 10) "แสงสลัวแบบคบเพลิง Redstone" else "สว่างจ้าดุจโคมไฟ/Sea Lantern",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Physical properties: Hardness & Friction & Blast Resistance
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "คุณสมบัติทางกายภาพ (Physics & Mining)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "ความแข็งในการขุด: ${String.format("%.1f", currentBlock.hardness)} วิ (Hardness)", fontSize = 13.sp)
                    Slider(
                        value = currentBlock.hardness,
                        onValueChange = { onBlockUpdated(currentBlock.copy(hardness = it)) },
                        valueRange = 0.2f..20f
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = "ความต้านทานแรงระเบิด: ${String.format("%.0f", currentBlock.explosionResistance)} (Blast Resistance)", fontSize = 13.sp)
                    Slider(
                        value = currentBlock.explosionResistance,
                        onValueChange = { onBlockUpdated(currentBlock.copy(explosionResistance = it)) },
                        valueRange = 0f..100f
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = "ความลื่นผิวสัมผัส (Friction): ${String.format("%.2f", currentBlock.friction)} (0.6 ปกติ, 0.98 น้ำแข็ง)", fontSize = 13.sp)
                    Slider(
                        value = currentBlock.friction,
                        onValueChange = { onBlockUpdated(currentBlock.copy(friction = it)) },
                        valueRange = 0.1f..1.0f
                    )
                }
            }
        }

        // Sound Type Selector
        item {
            Text(text = "เสียงเวลาเดิน/ขุดบล็อก (Sound Type):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            val sounds = listOf("Stone", "Wood", "Metal", "Glass", "Slime", "Amethyst")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                items(sounds) { s ->
                    FilterChip(
                        selected = currentBlock.soundType == s,
                        onClick = { onBlockUpdated(currentBlock.copy(soundType = s)) },
                        label = { Text(s) }
                    )
                }
            }
        }

        // Color Theme
        item {
            Text(text = "สีโทนหลักของบล็อก:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                items(COLOR_PALETTE) { hex ->
                    val c = Color(android.graphics.Color.parseColor(hex))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(c)
                            .clickable { onBlockUpdated(currentBlock.copy(blockColorHex = hex)) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentBlock.blockColorHex.equals(hex, ignoreCase = true)) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun Model3DTabContent(
    project: AddonProject,
    selectedBlockIndex: Int,
    activeBoxId: String?,
    onSelectBox: (String?) -> Unit,
    onAddBox: (String, VoxelBox) -> Unit,
    onUpdateBox: (String, VoxelBox) -> Unit,
    onDeleteBox: (String, String) -> Unit
) {
    val block = project.blocks.getOrNull(selectedBlockIndex) ?: project.blocks.firstOrNull()

    if (block == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("กรุณาสร้างหรือเลือกบล็อกในแถบ 'บล็อก' ก่อนเพื่อออกแบบโมเดล 3D")
        }
        return
    }

    val activeBox = block.modelBoxes.find { it.id == activeBoxId } ?: block.modelBoxes.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 3D Viewport Header
        item {
            Text(
                text = "โมเดล 3D แบบกล่อง (Voxel Model): ${block.displayName}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Voxel3DViewport(
                boxes = block.modelBoxes,
                selectedBoxId = activeBox?.id,
                onBoxTapped = { onSelectBox(it) }
            )
        }

        // Box list selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ชิ้นส่วนกล่อง (${block.modelBoxes.size})",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Button(
                    onClick = {
                        val newBox = VoxelBox(
                            id = "box_${System.currentTimeMillis()}",
                            name = "กล่อง #${block.modelBoxes.size + 1}",
                            originX = -4f,
                            originY = 8f,
                            originZ = -4f,
                            sizeX = 8f,
                            sizeY = 8f,
                            sizeZ = 8f,
                            colorHex = "#3B82F6"
                        )
                        onAddBox(block.id, newBox)
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("เพิ่มกล่องใหม่", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(block.modelBoxes) { box ->
                    FilterChip(
                        selected = box.id == activeBox?.id,
                        onClick = { onSelectBox(box.id) },
                        label = { Text(box.name) }
                    )
                }
            }
        }

        // Active Box Editor Controls
        if (activeBox != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ปรับแต่งชิ้นส่วน: ${activeBox.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (block.modelBoxes.size > 1) {
                                IconButton(onClick = { onDeleteBox(block.id, activeBox.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete box", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Box Name
                        OutlinedTextField(
                            value = activeBox.name,
                            onValueChange = { onUpdateBox(block.id, activeBox.copy(name = it)) },
                            label = { Text("ชื่อชิ้นส่วน") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Size Sliders
                        Text(text = "ขนาด กว้าง x สูง x ลึก (Size W x H x D):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                        Text(text = "ความกว้าง (Width X): ${activeBox.sizeX.toInt()} px", fontSize = 11.sp)
                        Slider(
                            value = activeBox.sizeX,
                            onValueChange = { onUpdateBox(block.id, activeBox.copy(sizeX = it)) },
                            valueRange = 1f..16f,
                            steps = 14
                        )

                        Text(text = "ความสูง (Height Y): ${activeBox.sizeY.toInt()} px", fontSize = 11.sp)
                        Slider(
                            value = activeBox.sizeY,
                            onValueChange = { onUpdateBox(block.id, activeBox.copy(sizeY = it)) },
                            valueRange = 1f..16f,
                            steps = 14
                        )

                        Text(text = "ความลึก (Depth Z): ${activeBox.sizeZ.toInt()} px", fontSize = 11.sp)
                        Slider(
                            value = activeBox.sizeZ,
                            onValueChange = { onUpdateBox(block.id, activeBox.copy(sizeZ = it)) },
                            valueRange = 1f..16f,
                            steps = 14
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Position Origin
                        Text(text = "ตำแหน่งจุดกำเนิด (Position Origin X, Y, Z):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                        Text(text = "ตำแหน่งแกน Y (ความสูงจากพื้น): ${activeBox.originY.toInt()}", fontSize = 11.sp)
                        Slider(
                            value = activeBox.originY,
                            onValueChange = { onUpdateBox(block.id, activeBox.copy(originY = it)) },
                            valueRange = 0f..16f,
                            steps = 15
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Color Picker
                        Text(text = "สีกระเบื้องกล่องนี้:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                            items(COLOR_PALETTE) { hex ->
                                val c = Color(android.graphics.Color.parseColor(hex))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                        .clickable { onUpdateBox(block.id, activeBox.copy(colorHex = hex)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (activeBox.colorHex.equals(hex, ignoreCase = true)) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun RecipeTabContent(
    project: AddonProject,
    selectedItemIndex: Int,
    onRecipeChanged: (CustomItemConfig, com.example.model.RecipeConfig) -> Unit
) {
    val item = project.items.getOrNull(selectedItemIndex) ?: project.items.firstOrNull()

    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("ไม่มีไอเทมในโครงการนี้ กรุณาเพิ่มไอเทมก่อนสร้างสูตรคราฟต์")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "สร้างสูตรคราฟต์ไอเทม: ${item.displayName}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        item {
            CraftingGridRecipeView(
                recipe = item.recipe,
                resultItemName = item.displayName,
                resultItemEmoji = item.iconEmoji,
                onRecipeChanged = { onRecipeChanged(item, it) }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 เคล็ดลับการคราฟต์:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "สูตรคราฟต์นี้จะถูกแปลเป็นสูตร JSON มาตรฐานสำหรับ Minecraft Bedrock โดยอัตโนมัติ ผู้เล่นในเกมสามารถนำวัตถุดิบวางบนโต๊ะ Crafting Table ตามช่องที่กำหนดเพื่อสร้างไอเทมนี้ได้ทันที!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ExportTabContent(viewModel: AddonStudioViewModel, project: AddonProject) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "ติดตั้งเข้าเกมและส่งออกไฟล์ (Build & Install)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "พร้อมสร้างไฟล์โครงสร้างที่ถูกต้องสำหรับ Minecraft Bedrock บนมือถือ",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Primary Big Install Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A2F))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981)
                        ) {
                            Text(
                                text = "แนะนำ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${project.exportFormat.extension} • v${project.targetVersion.versionString}",
                            fontSize = 12.sp,
                            color = Color(0xFFA7F3D0)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ติดตั้งตรงเข้าสู่ Minecraft",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "แอปจะทำการคอมไพล์แพ็ก สร้าง UUID และเปิดเข้าเกม Minecraft บนมือถือเพื่อ Import Addon โดยอัตโนมัติทันที",
                        fontSize = 13.sp,
                        color = Color(0xFFD1FAE5),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.compileAndInstallCurrent() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("export_screen_install_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "กดติดตั้งเข้าเกม Minecraft ทันที",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.downloadProjectForDistribution(project, project.exportFormat, true)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("export_screen_download_distribute_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📥 ดาวน์โหลดไฟล์พร้อมแจกต่อ (Download & Share)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Other Export Formats
        item {
            Text(
                text = "เลือกส่งออกด้วยรูปแบบอื่น:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { viewModel.compileAndInstallCurrent(ExportFormat.MCPACK) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("ส่งออก .mcpack", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.compileAndInstallCurrent(ExportFormat.MCADDON) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("ส่งออก .mcaddon", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.compileAndInstallCurrent(ExportFormat.ZIP) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("ส่งออก .zip", fontSize = 12.sp)
                }
            }
        }

        // Share & Downloads Options
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { viewModel.compileAndShareCurrent() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("แชร์ไฟล์ไปยังแอปอื่น")
                }

                Button(
                    onClick = { viewModel.compileAndExportToDownloads() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("เซฟไปที่ Downloads")
                }
            }
        }

        // Manual Backup Snapshot
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "สร้างจุดสำรองข้อมูลเดี๋ยวนี้ (Manual Snapshot)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "เก็บสำเนาสถานะปัจจุบันไว้เพื่อกู้คืนได้ทุกเมื่อ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { viewModel.triggerManualBackup() },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("สำรอง", fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
