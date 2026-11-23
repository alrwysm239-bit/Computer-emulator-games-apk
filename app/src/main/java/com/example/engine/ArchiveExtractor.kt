package com.example.engine

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.example.data.model.DxWrapper
import com.example.data.model.EngineType
import com.example.data.model.GameFileFormat
import com.example.data.model.WindowsCompatibility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipInputStream

data class ImportedGameInspection(
    val suggestedTitle: String,
    val format: GameFileFormat,
    val extractedDirPath: String,
    val primaryExecutablePath: String,
    val allExecutables: List<String>,
    val hasSetupExe: Boolean,
    val detectedEngine: EngineType,
    val detectedCompatibility: WindowsCompatibility,
    val recommendedWrapper: DxWrapper,
    val inspectionNotes: String,
)

class ArchiveExtractor(private val context: Context) {

    suspend fun importUri(
        uri: Uri,
        isDirectoryTree: Boolean = false,
        preferredEngine: EngineType = EngineType.AUTO,
        preferredCompatibility: WindowsCompatibility = WindowsCompatibility.AUTO,
    ): Result<ImportedGameInspection> = withContext(Dispatchers.IO) {
        try {
            val gamesRoot = File(context.filesDir, "games").apply { mkdirs() }
            val sessionId = "game_${System.currentTimeMillis()}"
            val targetDir = File(gamesRoot, sessionId).apply { mkdirs() }

            if (isDirectoryTree) {
                val treeDocId = DocumentsContract.getTreeDocumentId(uri)
                val folderName = treeDocId.substringAfterLast('/').substringAfterLast(':').ifBlank { "Imported Game" }
                copyDocumentTree(uri, treeDocId, targetDir)
                return@withContext Result.success(
                    inspectDirectory(
                        rawName = folderName,
                        format = GameFileFormat.FOLDER,
                        dir = targetDir,
                        preferredEngine = preferredEngine,
                        preferredCompatibility = preferredCompatibility,
                    )
                )
            }

            val displayName = resolveDisplayName(uri) ?: "retro_game.exe"
            val ext = displayName.substringAfterLast('.', "").lowercase()
            val format = when (ext) {
                "exe", "com", "bat" -> GameFileFormat.EXE
                "zip" -> GameFileFormat.ZIP
                "iso" -> GameFileFormat.ISO
                "rar" -> GameFileFormat.RAR
                "7z" -> GameFileFormat.SEVEN_Z
                else -> GameFileFormat.EXE
            }

            val copiedSource = File(targetDir, displayName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(copiedSource).use { output ->
                    input.copyTo(output, bufferSize = 32 * 1024)
                }
            } ?: return@withContext Result.failure(IllegalStateException("Unable to open selected file URI."))

            when (format) {
                GameFileFormat.ZIP -> extractZipSafely(copiedSource, targetDir)
                GameFileFormat.ISO -> extractIso9660(copiedSource, targetDir)
                GameFileFormat.RAR, GameFileFormat.SEVEN_Z -> inspectCompressedHeader(copiedSource, format, targetDir)
                GameFileFormat.EXE, GameFileFormat.FOLDER -> Unit
            }

            Result.success(
                inspectDirectory(
                    rawName = displayName.substringBeforeLast('.'),
                    format = format,
                    dir = targetDir,
                    preferredEngine = preferredEngine,
                    preferredCompatibility = preferredCompatibility,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) return cursor.getString(idx)
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')
    }

    private fun extractZipSafely(zipFile: File, targetDir: File) {
        val canonicalTarget = targetDir.canonicalPath
        ZipInputStream(BufferedInputStream(zipFile.inputStream())).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val outFile = File(targetDir, entry.name)
                if (!outFile.canonicalPath.startsWith(canonicalTarget + File.separator)) {
                    throw SecurityException("Blocked unsafe ZIP path: ${entry.name}")
                }
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos -> zis.copyTo(fos) }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    /**
     * Pure-Kotlin ISO-9660 Primary Volume Descriptor & root/subdirectory extractor.
     */
    private fun extractIso9660(isoFile: File, targetDir: File) {
        if (isoFile.length() < 36 * 1024) return
        RandomAccessFile(isoFile, "r").use { raf ->
            val sectorSize = 2048L
            val pvdOffset = 16L * sectorSize
            val pvd = ByteArray(2048)
            raf.seek(pvdOffset)
            raf.readFully(pvd)
            val signature = String(pvd, 1, 5, Charsets.US_ASCII)
            if (signature != "CD001") return

            val rootLba = ByteBuffer.wrap(pvd, 158, 4).order(ByteOrder.LITTLE_ENDIAN).int.toLong()
            val rootSize = ByteBuffer.wrap(pvd, 166, 4).order(ByteOrder.LITTLE_ENDIAN).int
            extractIsoDirectory(raf, rootLba * sectorSize, rootSize, targetDir, depth = 0)
        }
    }

    private fun extractIsoDirectory(
        raf: RandomAccessFile,
        offset: Long,
        size: Int,
        outDir: File,
        depth: Int,
    ) {
        if (depth > 6 || size <= 0 || offset + size > raf.length()) return
        val dirBytes = ByteArray(size)
        raf.seek(offset)
        raf.readFully(dirBytes)

        var pos = 0
        while (pos < dirBytes.size) {
            val recLen = dirBytes[pos].toInt() and 0xFF
            if (recLen == 0) {
                val nextSector = ((pos / 2048) + 1) * 2048
                if (nextSector >= dirBytes.size) break
                pos = nextSector
                continue
            }
            if (pos + recLen > dirBytes.size || recLen < 34) break
            val extentLba = ByteBuffer.wrap(dirBytes, pos + 2, 4).order(ByteOrder.LITTLE_ENDIAN).int.toLong()
            val dataLen = ByteBuffer.wrap(dirBytes, pos + 10, 4).order(ByteOrder.LITTLE_ENDIAN).int
            val flags = dirBytes[pos + 25].toInt() and 0xFF
            val idLen = dirBytes[pos + 32].toInt() and 0xFF
            val name = if (idLen > 0 && pos + 33 + idLen <= dirBytes.size) {
                String(dirBytes, pos + 33, idLen, Charsets.UTF_8).substringBefore(';')
            } else ""

            val isSpecial = idLen == 1 && (dirBytes[pos + 33] == 0.toByte() || dirBytes[pos + 33] == 1.toByte())
            if (!isSpecial && name.isNotBlank()) {
                val safeName = name.replace("/", "_").replace("..", "_")
                val target = File(outDir, safeName)
                val isDir = (flags and 0x02) != 0
                if (isDir) {
                    target.mkdirs()
                    extractIsoDirectory(raf, extentLba * 2048L, dataLen, target, depth + 1)
                } else if (dataLen in 1..(64 * 1024 * 1024)) {
                    val fileOffset = extentLba * 2048L
                    if (fileOffset + dataLen <= raf.length()) {
                        val buffer = ByteArray(minOf(dataLen, 8192))
                        raf.seek(fileOffset)
                        FileOutputStream(target).use { fos ->
                            var remaining = dataLen
                            while (remaining > 0) {
                                val read = raf.read(buffer, 0, minOf(buffer.size, remaining))
                                if (read <= 0) break
                                fos.write(buffer, 0, read)
                                remaining -= read
                            }
                        }
                    }
                }
            }
            pos += recLen
        }
    }

    private fun inspectCompressedHeader(archiveFile: File, format: GameFileFormat, targetDir: File) {
        val header = ByteArray(16)
        archiveFile.inputStream().use { it.read(header) }
        val manifest = File(targetDir, "archive_manifest.txt")
        val magicHex = header.joinToString(" ") { "%02X".format(it) }
        manifest.writeText("Format: ${format.badge}\nSource: ${archiveFile.name}\nHeader: $magicHex\n")
    }

    private fun copyDocumentTree(treeUri: Uri, documentId: String, targetDir: File) {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
        context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            while (cursor.moveToNext()) {
                val childId = cursor.getString(idCol)
                val name = cursor.getString(nameCol) ?: continue
                val mime = cursor.getString(mimeCol) ?: ""
                if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                    val subDir = File(targetDir, name).apply { mkdirs() }
                    copyDocumentTree(treeUri, childId, subDir)
                } else {
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId)
                    val outFile = File(targetDir, name)
                    context.contentResolver.openInputStream(docUri)?.use { input ->
                        FileOutputStream(outFile).use { output -> input.copyTo(output) }
                    }
                }
            }
        }
    }

    private fun inspectDirectory(
        rawName: String,
        format: GameFileFormat,
        dir: File,
        preferredEngine: EngineType,
        preferredCompatibility: WindowsCompatibility,
    ): ImportedGameInspection {
        val allFiles = dir.walkTopDown().filter { it.isFile }.toList()
        val executables = allFiles.filter { f ->
            val ext = f.extension.lowercase()
            ext == "exe" || ext == "bat" || ext == "com"
        }.sortedBy { f ->
            when {
                f.name.equals("setup.exe", ignoreCase = true) || f.name.equals("install.exe", ignoreCase = true) -> 2
                f.name.contains("unins", ignoreCase = true) -> 3
                else -> 0
            }
        }

        val hasSetup = executables.any {
            it.name.equals("setup.exe", ignoreCase = true) || it.name.equals("install.exe", ignoreCase = true)
        }
        val primaryExe = executables.firstOrNull {
            !it.name.equals("setup.exe", ignoreCase = true) && !it.name.equals("install.exe", ignoreCase = true)
        } ?: executables.firstOrNull() ?: allFiles.firstOrNull() ?: File(dir, "$rawName.exe")

        val isScumm = allFiles.any {
            val n = it.name.lowercase()
            n.endsWith(".cpt") || n.startsWith("monkey.") || n == "scummvm.ini"
        }

        val binaryAnalysis = analyzeExecutableHeader(primaryExe)
        val detectedEngine = when {
            preferredEngine != EngineType.AUTO -> preferredEngine
            isScumm -> EngineType.SCUMMVM
            binaryAnalysis.isDosOnly -> EngineType.DOSBOX_X
            else -> EngineType.WINE_BOX64
        }

        val detectedCompat = when {
            preferredCompatibility != WindowsCompatibility.AUTO -> preferredCompatibility
            detectedEngine == EngineType.DOSBOX_X -> WindowsCompatibility.MS_DOS
            binaryAnalysis.peSubsystemMajor <= 4 -> WindowsCompatibility.WIN_98
            else -> WindowsCompatibility.WIN_XP
        }

        val wrapper = when {
            detectedEngine == EngineType.DOSBOX_X -> DxWrapper.NGLIDE
            binaryAnalysis.mentionsDirectDraw -> DxWrapper.CNC_DDRAW
            else -> DxWrapper.DXVK_D3D9
        }

        val prettyTitle = rawName
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
            .ifBlank { "Retro PC Game" }

        return ImportedGameInspection(
            suggestedTitle = prettyTitle,
            format = format,
            extractedDirPath = dir.absolutePath,
            primaryExecutablePath = primaryExe.absolutePath,
            allExecutables = executables.map { it.absolutePath },
            hasSetupExe = hasSetup,
            detectedEngine = detectedEngine,
            detectedCompatibility = detectedCompat,
            recommendedWrapper = wrapper,
            inspectionNotes = binaryAnalysis.note,
        )
    }

    private data class BinaryAnalysis(
        val isDosOnly: Boolean,
        val peSubsystemMajor: Int,
        val mentionsDirectDraw: Boolean,
        val note: String,
    )

    private fun analyzeExecutableHeader(file: File): BinaryAnalysis {
        if (!file.exists() || file.length() < 64) {
            return BinaryAnalysis(isDosOnly = false, peSubsystemMajor = 4, mentionsDirectDraw = false, note = "Virtual container image")
        }
        return try {
            val bytes = ByteArray(minOf(file.length().toInt(), 8192))
            file.inputStream().use { it.read(bytes) }
            val hasMz = bytes[0] == 'M'.code.toByte() && bytes[1] == 'Z'.code.toByte()
            if (!hasMz) {
                return BinaryAnalysis(isDosOnly = false, peSubsystemMajor = 5, mentionsDirectDraw = false, note = "Archive / ISO payload")
            }
            val peOffset = ByteBuffer.wrap(bytes, 0x3C, 4).order(ByteOrder.LITTLE_ENDIAN).int
            val isPe = peOffset in 64..(bytes.size - 24) &&
                bytes[peOffset] == 'P'.code.toByte() &&
                bytes[peOffset + 1] == 'E'.code.toByte()
            val textAscii = String(bytes, Charsets.ISO_8859_1)
            val hasDd = textAscii.contains("DDRAW", ignoreCase = true) || textAscii.contains("DirectDraw", ignoreCase = true)
            if (!isPe) {
                BinaryAnalysis(isDosOnly = true, peSubsystemMajor = 0, mentionsDirectDraw = false, note = "16-bit MS-DOS MZ binary")
            } else {
                BinaryAnalysis(isDosOnly = false, peSubsystemMajor = 4, mentionsDirectDraw = hasDd, note = "32-bit Windows PE executable")
            }
        } catch (_: Exception) {
            BinaryAnalysis(isDosOnly = false, peSubsystemMajor = 4, mentionsDirectDraw = false, note = "Standard x86 binary")
        }
    }
}

fun encodeExecutablesJson(paths: List<String>): String {
    val arr = JSONArray()
    paths.forEach { arr.put(it) }
    return arr.toString()
}

fun decodeExecutablesJson(json: String): List<String> {
    return try {
        val arr = JSONArray(json)
        List(arr.length()) { idx -> arr.getString(idx) }
    } catch (_: Exception) {
        emptyList()
    }
}
