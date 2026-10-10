package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.generator.MinecraftPackCompiler
import com.example.model.AddonGenre
import com.example.model.AddonProject
import com.example.model.ExportFormat
import com.example.model.MinecraftVersion
import com.example.storage.ProjectRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MC Addon Studio", appName)
    }

    @Test
    fun `compile mcpack generates valid file`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val project = AddonProject(
            id = "test_proj",
            name = "Test Weapon Pack",
            author = "TestAuthor",
            version = "1.0.0",
            description = "Test description",
            targetVersion = MinecraftVersion.V_1_21_40,
            exportFormat = ExportFormat.MCPACK,
            genre = AddonGenre.WEAPON_TOOLS
        )

        val result = MinecraftPackCompiler.compileProject(context, project)
        assertTrue(result.file.exists())
        assertTrue(result.file.length() > 0)
        assertTrue(result.file.name.endsWith(".mcpack"))
    }

    @Test
    fun `curated packs exist and can compile to mcaddon`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ProjectRepository(context)
        assertFalse(repo.curatedPacks.isEmpty())

        val firstBundle = repo.curatedPacks.first()
        val project = firstBundle.toAddonProject()
        val result = MinecraftPackCompiler.compileProject(context, project, ExportFormat.MCADDON)

        assertTrue(result.file.exists())
        assertTrue(result.file.length() > 0)
        assertTrue(result.file.name.endsWith(".mcaddon"))
    }
}
