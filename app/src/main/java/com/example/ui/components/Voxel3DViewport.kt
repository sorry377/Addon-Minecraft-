package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VoxelBox
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Voxel3DViewport(
    boxes: List<VoxelBox>,
    selectedBoxId: String?,
    modifier: Modifier = Modifier,
    onBoxTapped: ((String) -> Unit)? = null
) {
    var yaw by remember { mutableFloatStateOf(45f) }
    var pitch by remember { mutableFloatStateOf(30f) }
    var zoom by remember { mutableFloatStateOf(10f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    yaw = (yaw + dragAmount.x * 0.5f) % 360f
                    pitch = (pitch - dragAmount.y * 0.5f).coerceIn(5f, 85f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f + 30f

            val radYaw = Math.toRadians(yaw.toDouble())
            val radPitch = Math.toRadians(pitch.toDouble())

            val cosY = cos(radYaw).toFloat()
            val sinY = sin(radYaw).toFloat()
            val cosP = cos(radPitch).toFloat()
            val sinP = sin(radPitch).toFloat()

            // 3D Projection Helper
            fun project(x: Float, y: Float, z: Float): Offset {
                // Rotate Yaw around Y axis
                val x1 = x * cosY - z * sinY
                val z1 = x * sinY + z * cosY
                // Rotate Pitch around X axis
                val y2 = y * cosP - z1 * sinP
                val z2 = y * sinP + z1 * cosP

                val scale = zoom
                return Offset(
                    cx + x1 * scale,
                    cy - y2 * scale
                )
            }

            // Draw Ground Grid (Bedrock coordinate base -8 to +8)
            val gridStep = 4f
            val gridColor = Color(0xFF1E293B)
            for (i in -8..8 step 4) {
                val p1 = project(i.toFloat(), 0f, -8f)
                val p2 = project(i.toFloat(), 0f, 8f)
                drawLine(gridColor, p1, p2, strokeWidth = 1.5f)

                val p3 = project(-8f, 0f, i.toFloat())
                val p4 = project(8f, 0f, i.toFloat())
                drawLine(gridColor, p3, p4, strokeWidth = 1.5f)
            }

            // Draw Coordinate Center Indicator
            val centerPt = project(0f, 0f, 0f)
            drawCircle(Color(0xFF10B981), radius = 3.5f, center = centerPt)

            // Render Boxes in Depth Order (rough painter's algorithm)
            val sortedBoxes = boxes.sortedBy { box ->
                val midX = box.originX + box.sizeX / 2f
                val midZ = box.originZ + box.sizeZ / 2f
                midX * sinY + midZ * cosY
            }

            for (box in sortedBoxes) {
                val x0 = box.originX
                val x1 = box.originX + box.sizeX
                val y0 = box.originY
                val y1 = box.originY + box.sizeY
                val z0 = box.originZ
                val z1 = box.originZ + box.sizeZ

                val baseColor = try {
                    Color(android.graphics.Color.parseColor(box.colorHex))
                } catch (e: Exception) {
                    Color(0xFF3B82F6)
                }

                val topColor = baseColor.copy(alpha = 0.95f)
                val frontColor = baseColor.copy(
                    red = (baseColor.red * 0.8f).coerceIn(0f, 1f),
                    green = (baseColor.green * 0.8f).coerceIn(0f, 1f),
                    blue = (baseColor.blue * 0.8f).coerceIn(0f, 1f),
                    alpha = 0.95f
                )
                val sideColor = baseColor.copy(
                    red = (baseColor.red * 0.6f).coerceIn(0f, 1f),
                    green = (baseColor.green * 0.6f).coerceIn(0f, 1f),
                    blue = (baseColor.blue * 0.6f).coerceIn(0f, 1f),
                    alpha = 0.95f
                )

                val isSelected = box.id == selectedBoxId
                val strokeColor = if (isSelected) Color(0xFFFBBF24) else Color(0xFF0F172A)
                val strokeWidth = if (isSelected) 3f else 1.5f

                // 8 Vertices
                val p000 = project(x0, y0, z0)
                val p100 = project(x1, y0, z0)
                val p101 = project(x1, y0, z1)
                val p001 = project(x0, y0, z1)
                val p010 = project(x0, y1, z0)
                val p110 = project(x1, y1, z0)
                val p111 = project(x1, y1, z1)
                val p011 = project(x0, y1, z1)

                // Top Face (y = y1)
                val topPath = Path().apply {
                    moveTo(p010.x, p010.y)
                    lineTo(p110.x, p110.y)
                    lineTo(p111.x, p111.y)
                    lineTo(p011.x, p011.y)
                    close()
                }
                drawPath(topPath, topColor)
                drawPath(topPath, strokeColor, style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))

                // Front Face (z = z1)
                val frontPath = Path().apply {
                    moveTo(p001.x, p001.y)
                    lineTo(p101.x, p101.y)
                    lineTo(p111.x, p111.y)
                    lineTo(p011.x, p011.y)
                    close()
                }
                drawPath(frontPath, frontColor)
                drawPath(frontPath, strokeColor, style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))

                // Right Face (x = x1)
                val rightPath = Path().apply {
                    moveTo(p100.x, p100.y)
                    lineTo(p101.x, p101.y)
                    lineTo(p111.x, p111.y)
                    lineTo(p110.x, p110.y)
                    close()
                }
                drawPath(rightPath, sideColor)
                drawPath(rightPath, strokeColor, style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
            }
        }

        // Overlay Instructions
        Text(
            text = "ลากนิ้วเพื่อหมุนมุมมอง 3D (Yaw: ${yaw.toInt()}° Pitch: ${pitch.toInt()}°)",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        )

        Text(
            text = "จำนวนชิ้นส่วน: ${boxes.size} กล่อง",
            color = Color(0xFF38BDF8),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        )
    }
}
