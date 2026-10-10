package com.example.model

data class AddonPackBundle(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val author: String,
    val version: String,
    val categoryBadge: String, // "อาวุธระดับเทพ", "เฟอร์นิเจอร์ 3D", "อาหาร & เอาชีวิตรอด", "เกราะไททัน", "Mega Bundle"
    val genre: AddonGenre,
    val minVersion: MinecraftVersion,
    val defaultFormat: ExportFormat = ExportFormat.MCADDON,
    val downloadCount: String,
    val items: List<CustomItemConfig>,
    val blocks: List<CustomBlockConfig>,
    val highlights: List<String>,
    val bannerDrawableRes: Int? = null,
    val isHot: Boolean = false
) {
    fun toAddonProject(): AddonProject {
        return AddonProject(
            id = "remix_${id}_${System.currentTimeMillis()}",
            name = title,
            author = author,
            version = version,
            description = description,
            targetVersion = minVersion,
            exportFormat = defaultFormat,
            genre = genre,
            items = items,
            blocks = blocks,
            lastModified = System.currentTimeMillis()
        )
    }
}
