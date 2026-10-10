package com.example.generator

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object InstallerHelper {

    private const val MINECRAFT_PACKAGE = "com.mojang.minecraftpe"

    fun installToMinecraft(context: Context, file: File): Boolean {
        if (!file.exists()) {
            Toast.makeText(context, "ไม่พบไฟล์แพ็กที่จะติดตั้ง", Toast.LENGTH_SHORT).show()
            return false
        }

        return try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            // Try direct intent to Minecraft PE
            val mcIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/octet-stream")
                setPackage(MINECRAFT_PACKAGE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val pm = context.packageManager
            val canLaunchDirect = mcIntent.resolveActivity(pm) != null

            if (canLaunchDirect) {
                context.startActivity(mcIntent)
                Toast.makeText(context, "กำลังส่งไฟล์เข้าเกม Minecraft...", Toast.LENGTH_SHORT).show()
                true
            } else {
                // Launch chooser with fallback MIME
                val chooserIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "*/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(chooserIntent, "เปิดและติดตั้งด้วย Minecraft หรือตัวจัดการไฟล์").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                Toast.makeText(context, "เลือก Minecraft เพื่อเริ่มติดตั้งแพ็ก", Toast.LENGTH_SHORT).show()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to share
            sharePack(context, file)
            true
        }
    }

    fun sharePack(context: Context, file: File) {
        sharePackForDistribution(
            context = context,
            file = file,
            packTitle = file.nameWithoutExtension,
            customNote = ""
        )
    }

    fun sharePackForDistribution(
        context: Context,
        file: File,
        packTitle: String,
        customNote: String = ""
    ) {
        if (!file.exists()) {
            Toast.makeText(context, "ไม่พบไฟล์ที่ต้องการแชร์", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            val ext = file.extension

            val shareCaption = buildString {
                append("📥 แจกไฟล์ Addon Minecraft Bedrock: $packTitle\n")
                append("📦 ไฟล์: ${file.name}\n\n")
                if (customNote.isNotBlank()) {
                    append("ℹ️ รายละเอียด: $customNote\n\n")
                }
                append("🎮 วิธีติดตั้งบนมือถือ:\n")
                append("1. แตะเปิดไฟล์ .$ext\n")
                append("2. เลือกเปิดด้วยแอป 'Minecraft'\n")
                append("3. รอเกม Import เสร็จแล้วเปิดใช้งานใน World Settings ได้ทันที!\n\n")
                append("✨ สร้างและจัดการด้วย MC Addon Studio")
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "แจก Addon: ${file.name}")
                putExtra(Intent.EXTRA_TEXT, shareCaption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "แชร์/ส่งไฟล์แจกเพื่อน: ${file.name}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "เกิดข้อผิดพลาดในการแชร์: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Downloads file to the device's public Downloads directory using MediaStore on Android 10+
     * and fallback to standard Downloads folder on earlier versions.
     */
    fun exportToDownloads(
        context: Context,
        sourceFile: File,
        packTitle: String = "",
        openSharePrompt: Boolean = false
    ): File? {
        if (!sourceFile.exists()) {
            Toast.makeText(context, "ไม่พบไฟล์ต้นฉบับ", Toast.LENGTH_SHORT).show()
            return null
        }

        var savedFile: File? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // MediaStore API for Android 10+
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, sourceFile.name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/MC_Addons")
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        FileInputStream(sourceFile).use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    Toast.makeText(
                        context,
                        "ดาวน์โหลดสำเร็จ! บันทึกใน Downloads/MC_Addons/${sourceFile.name} พร้อมแจกต่อ",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                // Pre-Android 10
                val downloadsDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "MC_Addons"
                )
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val destFile = File(downloadsDir, sourceFile.name)
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                savedFile = destFile
                Toast.makeText(
                    context,
                    "ดาวน์โหลดสำเร็จ! บันทึกใน Downloads/MC_Addons/${destFile.name} พร้อมแจกต่อ",
                    Toast.LENGTH_LONG
                ).show()
            }

            if (openSharePrompt) {
                sharePackForDistribution(
                    context = context,
                    file = sourceFile,
                    packTitle = if (packTitle.isNotBlank()) packTitle else sourceFile.nameWithoutExtension
                )
            }

            return savedFile ?: sourceFile
        } catch (e: Exception) {
            e.printStackTrace()
            // Direct copy fallback
            try {
                val directDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(directDir, sourceFile.name)
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Toast.makeText(context, "ดาวน์โหลดไปที่ Downloads แล้ว: ${destFile.name}", Toast.LENGTH_LONG).show()
                if (openSharePrompt) {
                    sharePackForDistribution(context, destFile, packTitle)
                }
                return destFile
            } catch (fallbackEx: Exception) {
                fallbackEx.printStackTrace()
                Toast.makeText(context, "ดาวน์โหลดไม่สำเร็จ: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                return null
            }
        }
    }
}
