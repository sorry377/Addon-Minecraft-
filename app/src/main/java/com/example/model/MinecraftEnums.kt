package com.example.model

enum class MinecraftVersion(
    val versionString: String,
    val releaseName: String,
    val minEngineVersion: List<Int>,
    val highlights: String,
    val isRecommended: Boolean = false
) {
    V_1_21_40(
        versionString = "1.21.40",
        releaseName = "Tricky Trials (Latest)",
        minEngineVersion = listOf(1, 21, 40),
        highlights = "รองรับ Crafter, Mace, Breeze Mob, Trial Spawner และ Wind Charges ล่าสุด",
        isRecommended = true
    ),
    V_1_21_0(
        versionString = "1.21.0",
        releaseName = "Tricky Trials",
        minEngineVersion = listOf(1, 21, 0),
        highlights = "อัปเดตระบบการต่อสู้ใหม่ ดันเจี้ยนใต้ดิน และบล็อกทองแดงออกไซด์"
    ),
    V_1_20_80(
        versionString = "1.20.80",
        releaseName = "Trails & Tales",
        minEngineVersion = listOf(1, 20, 80),
        highlights = "รองรับตัวนิ่ม (Armadillo), ชุดเกราะสุนัขหมาป่า และ Vaults"
    ),
    V_1_20_0(
        versionString = "1.20.0",
        releaseName = "Trails & Tales",
        minEngineVersion = listOf(1, 20, 0),
        highlights = "ระบบขุดโบราณคดี, ป่าซากุระ Cherry Grove, ตัว Sniffer"
    ),
    V_1_19_50(
        versionString = "1.19.50",
        releaseName = "The Wild Update",
        minEngineVersion = listOf(1, 19, 50),
        highlights = "เมืองใต้ดิน Deep Dark, ม็อบ Warden, ป่าโกงกาง Mangrove, น้อง Allay"
    ),
    V_1_18_30(
        versionString = "1.18.30",
        releaseName = "Caves & Cliffs II",
        minEngineVersion = listOf(1, 18, 30),
        highlights = "ความสูงโลกใหม่ 320 ถึง -64 และถ้ำหินหยด Dripstone"
    ),
    V_1_16_220(
        versionString = "1.16.220",
        releaseName = "Nether Update (Legacy)",
        minEngineVersion = listOf(1, 16, 220),
        highlights = "สำหรับเครื่องสเปกเบา รองรับ Bedrock Engine ดั้งเดิม เสถียรสูง"
    );

    fun formatBadge(): String = "v$versionString • $releaseName"
}

enum class ExportFormat(
    val extension: String,
    val title: String,
    val description: String,
    val mimeType: String
) {
    MCPACK(
        extension = ".mcpack",
        title = "Minecraft Pack (.mcpack)",
        description = "ไฟล์มาตรฐานของ Minecraft แตะหนึ่งครั้งเพื่อเปิดและติดตั้งเข้าตัวเกมโดยตรง",
        mimeType = "application/zip"
    ),
    MCADDON(
        extension = ".mcaddon",
        title = "Minecraft Addon (.mcaddon)",
        description = "รวมทั้ง Resource Pack (โมเดล/พื้นผิว) และ Behavior Pack (ระบบ) ในไฟล์เดียว",
        mimeType = "application/zip"
    ),
    ZIP(
        extension = ".zip",
        title = "ZIP Archive (.zip)",
        description = "สำหรับส่งออกเพื่อแตกไฟล์นำไปแก้ไขเพิ่มเติมบนคอมพิวเตอร์หรือโปรแกรมภายนอก",
        mimeType = "application/zip"
    )
}

enum class AddonGenre(
    val genreId: String,
    val displayName: String,
    val iconName: String,
    val description: String,
    val defaultPrefix: String
) {
    WEAPON_TOOLS(
        genreId = "weapons",
        displayName = "อาวุธ & เครื่องมือเทพ",
        iconName = "gavel",
        description = "ปรับแต่งดาบ ขวาน คทาเวทมนตร์ พลังโจมตี ความทนทาน และแสงสะท้อนเวทมนตร์",
        defaultPrefix = "weapon"
    ),
    BLOCKS_FURNITURE(
        genreId = "blocks",
        displayName = "บล็อก & เฟอร์นิเจอร์ 3D",
        iconName = "view_in_ar",
        description = "สร้างบล็อกโมเดล 3D แบบกล่อง กำหนดค่าแสงสะท้อน ความแข็ง และเสียงบล็อก",
        defaultPrefix = "block"
    ),
    SHADERS_VISUAL(
        genreId = "shaders",
        displayName = "Shader & แสงเงาสมจริง",
        iconName = "wb_sunny",
        description = "แพ็กแสงเงา ท้องฟ้า หมอก น้ำพริ้วไหว แร่เรืองแสง ปรับแต่งมุมกล้องและการแสดงผล",
        defaultPrefix = "shader"
    ),
    FOOD_CROPS(
        genreId = "food",
        displayName = "อาหาร & ยาบัฟวิเศษ",
        iconName = "restaurant",
        description = "อาหารฟื้นฟูหัวใจ บัฟวิ่งเร็ว โจมตีแรง พร้อมเอฟเฟกต์ยาและเสียงเคี้ยว",
        defaultPrefix = "food"
    ),
    ARMOR_GEAR(
        genreId = "armor",
        displayName = "ชุดเกราะ & อุปกรณ์สวมใส่",
        iconName = "shield",
        description = "เกราะเสริมพลัง ป้องกันการระเบิด ป้องกันไฟ และเพิ่มพลังชีวิตสูงสุด",
        defaultPrefix = "armor"
    ),
    ENTITIES_PETS(
        genreId = "entities",
        displayName = "สัตว์เลี้ยง & ม็อบผู้ช่วย",
        iconName = "pets",
        description = "ม็อบคู่ใจช่วยต่อสู้ ค่าพลังชีวิต ความเร็วในการเดิน และความสามารถในการขี่",
        defaultPrefix = "mob"
    )
}
