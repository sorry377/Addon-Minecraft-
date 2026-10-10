package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShaderPreset
import com.example.viewmodel.AddonStudioViewModel
import com.example.viewmodel.ShaderLightingTime
import kotlin.math.sin

@Composable
fun ShaderGalleryScreen(
    viewModel: AddonStudioViewModel,
    modifier: Modifier = Modifier
) {
    val selectedShader by viewModel.selectedShader.collectAsState()
    val lightingTime by viewModel.shaderPreviewTime.collectAsState()
    val isWavesEnabled by viewModel.shaderPreviewWaves.collectAsState()

    var waterReflectionSlider by remember(selectedShader.id) {
        mutableIntStateOf(selectedShader.waterReflectionPercent)
    }
    var bloomSlider by remember(selectedShader.id) {
        mutableIntStateOf(selectedShader.bloomIntensityPercent)
    }
    var showFineTuning by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ศูนย์รวม Shader Addon",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "พรีวิวแสงเงาสมจริงในแอป พร้อมปุ่มติดตั้งเข้าเกมทันที",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Live Preview Canvas & Real Shader Image Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Preview Visual Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    ) {
                        // Render High-res Screenshot if available
                        if (selectedShader.drawableResId != null) {
                            Image(
                                painter = painterResource(id = selectedShader.drawableResId!!),
                                contentDescription = selectedShader.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Dynamic Gradient Canvas with animated waves
                            ShaderSimulationCanvas(
                                lightingTime = lightingTime,
                                waterReflection = waterReflectionSlider,
                                bloom = bloomSlider,
                                waves = isWavesEnabled
                            )
                        }

                        // Top Badges
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = selectedShader.performanceLevel,
                                    fontSize = 11.sp,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "v${selectedShader.minVersion.versionString}",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Bottom Lighting Time Toggles
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ShaderLightingTime.values().forEach { time ->
                                Surface(
                                    onClick = { viewModel.setShaderPreviewTime(time) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (lightingTime == time) Color(0xFF10B981) else Color.Black.copy(alpha = 0.65f)
                                ) {
                                    Text(
                                        text = "${time.emoji} ${time.title}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lightingTime == time) Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Card Information & Actions
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedShader.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = selectedShader.subtitle,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = selectedShader.description,
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Features Tags
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(selectedShader.tags) { tag ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Text(
                                        text = "✓ $tag",
                                        fontSize = 10.sp,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 1-Tap Direct Install Button
                        Button(
                            onClick = { viewModel.installShaderPresetDirect(selectedShader) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("install_shader_direct_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ติดตั้งเข้าเกม Minecraft ทันที (1-Tap)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.applyShaderToProject(selectedShader) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("นำเข้าสู่ Addon Studio", fontSize = 12.sp, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { showFineTuning = !showFineTuning },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ปรับแต่ง", fontSize = 12.sp, color = Color.White)
                            }
                        }

                        // Fine tuning panel
                        if (showFineTuning) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "ปรับระดับการแสดงผล:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "ความสะท้อนผิวน้ำ: $waterReflectionSlider%", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Slider(
                                        value = waterReflectionSlider.toFloat(),
                                        onValueChange = { waterReflectionSlider = it.toInt() },
                                        valueRange = 0f..100f
                                    )

                                    Text(text = "ความฟุ้งแสงแดด (Bloom): $bloomSlider%", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Slider(
                                        value = bloomSlider.toFloat(),
                                        onValueChange = { bloomSlider = it.toInt() },
                                        valueRange = 0f..100f
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("คลื่นน้ำเคลื่อนไหว (Water Waves)", fontSize = 11.sp, color = Color.White)
                                        Switch(
                                            checked = isWavesEnabled,
                                            onCheckedChange = { viewModel.toggleShaderWaves() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Shader Presets Catalog List
        item {
            Text(
                text = "เลือกรูปแบบ Shader อื่นๆ:",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(viewModel.shaderPresets) { preset ->
            val isSelected = selectedShader.id == preset.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectShaderPreset(preset) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (preset.drawableResId != null) {
                            Image(
                                painter = painterResource(id = preset.drawableResId),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(text = "⚡", fontSize = 26.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = preset.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = preset.subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text(
                            text = "ระดับ: ${preset.performanceLevel}",
                            fontSize = 10.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { viewModel.installShaderPresetDirect(preset) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("ติดตั้ง", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ShaderSimulationCanvas(
    lightingTime: ShaderLightingTime,
    waterReflection: Int,
    bloom: Int,
    waves: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "water_waves")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val skyColors = when (lightingTime) {
            ShaderLightingTime.DAY -> listOf(Color(0xFF38BDF8), Color(0xFF93C5FD), Color(0xFFE0F2FE))
            ShaderLightingTime.SUNSET -> listOf(Color(0xFF7C2D12), Color(0xFFEA580C), Color(0xFFFBBF24))
            ShaderLightingTime.NIGHT -> listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E1B4B))
        }

        // Draw Sky Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = skyColors,
                startY = 0f,
                endY = h * 0.65f
            ),
            size = size
        )

        // Draw Sun/Moon with Bloom Glow
        val sunCenter = Offset(w * 0.75f, h * 0.28f)
        val bloomAlpha = (bloom / 100f).coerceIn(0.2f, 0.9f)
        val sunColor = if (lightingTime == ShaderLightingTime.NIGHT) Color(0xFFE2E8F0) else Color(0xFFFEF08A)

        drawCircle(
            color = sunColor.copy(alpha = bloomAlpha * 0.3f),
            radius = 65f,
            center = sunCenter
        )
        drawCircle(
            color = sunColor.copy(alpha = bloomAlpha * 0.6f),
            radius = 35f,
            center = sunCenter
        )
        drawCircle(
            color = Color.White,
            radius = 16f,
            center = sunCenter
        )

        // Draw Horizon Water with Waves
        val waterY = h * 0.6f
        val waterPath = Path().apply {
            moveTo(0f, waterY)
            val step = 15f
            var x = 0f
            while (x <= w) {
                val waveH = if (waves) sin(x * 0.04 + waveOffset).toFloat() * 4f else 0f
                lineTo(x, waterY + waveH)
                x += step
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        val waterColors = when (lightingTime) {
            ShaderLightingTime.DAY -> listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF075985))
            ShaderLightingTime.SUNSET -> listOf(Color(0xFFB45309), Color(0xFF78350F), Color(0xFF451A03))
            ShaderLightingTime.NIGHT -> listOf(Color(0xFF0C4A6E), Color(0xFF075985), Color(0xFF082F49))
        }

        drawPath(
            path = waterPath,
            brush = Brush.verticalGradient(
                colors = waterColors,
                startY = waterY,
                endY = h
            )
        )

        // Water Reflection Beam
        val reflAlpha = (waterReflection / 100f).coerceIn(0.1f, 0.8f)
        val reflBrush = Brush.verticalGradient(
            colors = listOf(
                sunColor.copy(alpha = reflAlpha * 0.7f),
                sunColor.copy(alpha = 0f)
            ),
            startY = waterY,
            endY = h * 0.95f
        )

        drawRect(
            brush = reflBrush,
            topLeft = Offset(w * 0.68f, waterY),
            size = androidx.compose.ui.geometry.Size(w * 0.15f, h - waterY)
        )
    }
}
