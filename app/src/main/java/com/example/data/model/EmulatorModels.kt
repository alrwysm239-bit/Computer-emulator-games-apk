package com.example.data.model

enum class GameFileFormat(val extension: String, val badge: String) {
    EXE("exe", "EXE"),
    ZIP("zip", "ZIP"),
    ISO("iso", "ISO"),
    RAR("rar", "RAR"),
    SEVEN_Z("7z", "7Z"),
    FOLDER("dir", "DIR"),
}

enum class EngineType(val labelEn: String, val labelAr: String) {
    AUTO("Auto (Smart Detect)", "تلقائي (اكتشاف ذكي)"),
    WINE_BOX64("Wine + Box86/Box64", "Wine + Box86/Box64"),
    DOSBOX_X("DOSBox-X", "DOSBox-X"),
    SCUMMVM("ScummVM", "ScummVM"),
}

enum class WindowsCompatibility(val labelEn: String, val labelAr: String) {
    AUTO("Auto", "تلقائي"),
    WIN_95("Windows 95", "ويندوز 95"),
    WIN_98("Windows 98 SE", "ويندوز 98 SE"),
    WIN_XP("Windows XP SP3", "ويندوز XP SP3"),
    MS_DOS("MS-DOS 6.22", "MS-DOS 6.22"),
}

enum class PerformanceMode(val labelEn: String, val labelAr: String, val targetFpsDefault: Int) {
    BALANCED("Balanced", "توازن", 60),
    PERFORMANCE("Performance", "أداء عالي", 60),
    QUALITY("Quality", "جودة عالية", 60),
    BATTERY_SAVER("Low Battery", "توفير البطارية", 30),
}

enum class AspectRatioMode(val labelEn: String, val labelAr: String, val ratio: Float?) {
    RATIO_4_3("4:3 Classic", "4:3 كلاسيكي", 4f / 3f),
    RATIO_16_9("16:9 Wide", "16:9 عريض", 16f / 9f),
    FULL_SCREEN("Fullscreen Stretch", "ملء الشاشة", null),
    INTEGER_SCALE("Integer Pixel", "نسبة بيكسل دقيقة", 4f / 3f),
}

enum class ShaderFilter(val labelEn: String, val labelAr: String) {
    NONE("Raw Sharp", "حادة بدون فلتر"),
    CRT_SCANLINES("CRT Scanlines", "فلتر شاشة CRT"),
    PIXEL_PERFECT("Pixel Perfect", "بيكسل مثالي"),
    BILINEAR_SMOOTH("Bilinear Smooth", "تنعيم ناعم"),
}

enum class AudioChip(val labelEn: String, val labelAr: String) {
    SOUNDBLASTER_16("Sound Blaster 16", "Sound Blaster 16"),
    ADLIB_OPL3("AdLib OPL3 FM", "AdLib OPL3 FM"),
    ROLAND_MIDI("MIDI SoundFont", "MIDI (SoundFont)"),
    PC_SPEAKER("PC Speaker", "سماعة النظام القديمة"),
}

enum class DxWrapper(val labelEn: String, val labelAr: String) {
    AUTO("Auto Layer", "طبقة تلقائية"),
    DXVK_D3D9("DirectX 3-9 (DXVK)", "DirectX 3-9 (DXVK)"),
    CNC_DDRAW("cnc-ddraw (DirectDraw)", "cnc-ddraw (DirectDraw)"),
    DGVOODOO_2("dgVoodoo 2", "dgVoodoo 2"),
    NGLIDE("nGlide (3dfx Glide)", "nGlide (3dfx Glide)"),
    OPENGL_TRANSLATION("OpenGL Translation", "طبقة ترجمة OpenGL"),
}

enum class TouchControlMode(val labelEn: String, val labelAr: String) {
    GAMEPAD("Virtual Gamepad", "يد تحكم افتراضية"),
    DIRECT_MOUSE("Direct Touch Mouse", "ماوس لمس مباشر"),
    TRACKPAD_MOUSE("Trackpad Mouse", "لوحة لمس للماوس"),
    FULL_KEYBOARD("PC Keyboard", "لوحة مفاتيح كاملة"),
}

enum class GameCategory(val labelEn: String, val labelAr: String) {
    ALL("All", "الكل"),
    RACING("Racing", "سباقات"),
    STRATEGY("Strategy", "استراتيجية"),
    ACTION("Action", "أكشن"),
    ADVENTURE("Adventure", "مغامرات"),
    DOS_CLASSIC("DOS Classic", "كلاسيكيات DOS"),
}

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    ARABIC("ar", "العربية (RTL)"),
}

enum class ThemePreset(val labelEn: String, val labelAr: String) {
    NEON_DARK("Neon Purple / Cyan", "الوضع الداكن نيون"),
    CYBER_AMOLED("Cyber AMOLED", "أسود AMOLED"),
    RETRO_SYNTH("Retro Synthwave", "ريتروايف سينث"),
}

data class ResolutionOption(val width: Int, val height: Int) {
    val label: String get() = "${width}x${height}"
}

val SupportedResolutions = listOf(
    ResolutionOption(320, 200),
    ResolutionOption(640, 480),
    ResolutionOption(800, 600),
    ResolutionOption(1024, 768),
    ResolutionOption(1280, 720),
    ResolutionOption(1920, 1080),
)

data class RuntimePluginInfo(
    val id: String,
    val name: String,
    val version: String,
    val latestVersion: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val sizeMb: Int,
    val isInstalled: Boolean,
)

data class StoreCatalogItem(
    val id: String,
    val title: String,
    val titleAr: String,
    val subtitleEn: String,
    val subtitleAr: String,
    val licenseBadge: String,
    val format: GameFileFormat,
    val recommendedEngine: EngineType,
    val compatibility: WindowsCompatibility,
    val category: GameCategory,
    val sizeMb: Float,
    val downloadUrl: String,
    val executableName: String,
    val tileColorIndex: Int,
)

data class DownloadTaskState(
    val itemId: String,
    val progress: Float,
    val statusTextEn: String,
    val statusTextAr: String,
    val isDownloading: Boolean,
    val isCompleted: Boolean,
    val errorMessage: String? = null,
)
