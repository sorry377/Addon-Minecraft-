package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator.InstallerHelper
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.PacksCatalogScreen
import com.example.ui.screens.ProjectEditorScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.ShaderGalleryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AddonStudioViewModel
import com.example.viewmodel.AppScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: AddonStudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: AddonStudioViewModel) {
    val activeScreen by viewModel.activeScreen.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()
    val notificationMessage by viewModel.notificationMessage.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val distributionResult by viewModel.activeDistributionResult.collectAsState()

    // Handle Android system back button when in editor
    BackHandler(enabled = activeScreen == AppScreen.PROJECT_EDITOR) {
        viewModel.navigateTo(AppScreen.PROJECTS_LIST)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (activeScreen != AppScreen.PROJECT_EDITOR) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = activeScreen == AppScreen.PROJECTS_LIST,
                        onClick = { viewModel.navigateTo(AppScreen.PROJECTS_LIST) },
                        icon = { Icon(Icons.Default.Build, contentDescription = "Addons") },
                        label = { Text("สร้าง Addon", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_addons_studio")
                    )

                    NavigationBarItem(
                        selected = activeScreen == AppScreen.PACKS_CATALOG,
                        onClick = { viewModel.navigateTo(AppScreen.PACKS_CATALOG) },
                        icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "Packs") },
                        label = { Text("คลัง Packs แจก", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_packs_catalog")
                    )

                    NavigationBarItem(
                        selected = activeScreen == AppScreen.SHADERS_GALLERY,
                        onClick = { viewModel.navigateTo(AppScreen.SHADERS_GALLERY) },
                        icon = { Icon(Icons.Default.WbSunny, contentDescription = "Shaders") },
                        label = { Text("คลัง Shader", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_shaders")
                    )

                    NavigationBarItem(
                        selected = activeScreen == AppScreen.FILE_MANAGER,
                        onClick = { viewModel.navigateTo(AppScreen.FILE_MANAGER) },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Files") },
                        label = { Text("จัดการไฟล์", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_files")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeScreen) {
                AppScreen.PROJECTS_LIST -> {
                    ProjectsScreen(
                        viewModel = viewModel,
                        projects = projects
                    )
                }
                AppScreen.PROJECT_EDITOR -> {
                    if (currentProject != null) {
                        ProjectEditorScreen(
                            viewModel = viewModel,
                            project = currentProject!!
                        )
                    } else {
                        ProjectsScreen(
                            viewModel = viewModel,
                            projects = projects
                        )
                    }
                }
                AppScreen.PACKS_CATALOG -> {
                    PacksCatalogScreen(viewModel = viewModel)
                }
                AppScreen.SHADERS_GALLERY -> {
                    ShaderGalleryScreen(viewModel = viewModel)
                }
                AppScreen.FILE_MANAGER -> {
                    FileManagerScreen(viewModel = viewModel)
                }
            }

            // Notification Banner Popup
            AnimatedVisibility(
                visible = notificationMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✨", fontSize = 20.sp)
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = notificationMessage ?: "",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Auto dismiss notification after 3.5 seconds
            LaunchedEffect(notificationMessage) {
                if (notificationMessage != null) {
                    delay(3500)
                    viewModel.clearNotification()
                }
            }
        }
    }

    // Distribution Download Success Modal Dialog
    if (distributionResult != null) {
        val dist = distributionResult!!
        val context = androidx.compose.ui.platform.LocalContext.current

        AlertDialog(
            onDismissRequest = { viewModel.dismissDistributionDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📥", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ดาวน์โหลดไฟล์พร้อมแจกแล้ว!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = dist.file.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ขนาดไฟล์: ${dist.formattedSize} • นามสกุล: ${dist.format.extension}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = "📍 ที่อยู่ไฟล์: บันทึกในโฟลเดอร์ Downloads/MC_Addons บนมือถือเรียบร้อยแล้ว",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 คำแนะนำสำหรับเพื่อนที่ได้รับไฟล์:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                            Text(
                                text = "ให้เพื่อนแตะเปิดไฟล์ .${dist.file.extension} แล้วเลือกเปิดด้วย Minecraft ในเครื่อง จากนั้นรอเกม Import และเปิดใช้งานใน World Settings ได้ทันที",
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action: Share / Distribute
                    Button(
                        onClick = {
                            InstallerHelper.sharePackForDistribution(
                                context = context,
                                file = dist.file,
                                packTitle = dist.packTitle
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("แชร์แจกเพื่อนทันที (LINE, Discord, ฯลฯ)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Action: Install to Own Minecraft
                    OutlinedButton(
                        onClick = {
                            InstallerHelper.installToMinecraft(context, dist.file)
                            viewModel.dismissDistributionDialog()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ติดตั้งเข้าเกม Minecraft ตัวเอง", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDistributionDialog() }) {
                    Text("ปิดหน้าต่าง")
                }
            }
        )
    }
}
