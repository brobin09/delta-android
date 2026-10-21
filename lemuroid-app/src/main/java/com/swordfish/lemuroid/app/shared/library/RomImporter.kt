package com.swordfish.lemuroid.app.shared.library

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Delta-style import: copies the picked game files into the app's own library folder
 * and asks the library to rescan, so games show up sorted by system.
 */
object RomImporter {
    suspend fun importRoms(
        context: Context,
        uris: List<Uri>,
    ): Int =
        withContext(Dispatchers.IO) {
            val appContext = context.applicationContext
            val romsDirectory = DirectoriesManager(appContext).getInternalRomsDirectory()

            val imported =
                uris.count { uri ->
                    runCatching { copyToDirectory(appContext, uri, romsDirectory) }.getOrDefault(false)
                }

            if (imported > 0) {
                LibraryIndexScheduler.scheduleLibrarySync(appContext)
            }
            imported
        }

    private fun copyToDirectory(
        context: Context,
        uri: Uri,
        directory: File,
    ): Boolean {
        val name = displayName(context, uri) ?: return false
        val target = uniqueFile(directory, name)
        val input = context.contentResolver.openInputStream(uri) ?: return false
        input.use { source ->
            target.outputStream().use { sink -> source.copyTo(sink) }
        }
        return true
    }

    private fun displayName(
        context: Context,
        uri: Uri,
    ): String? {
        val raw =
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                ?: uri.lastPathSegment
        // Keep only the file name so a picked path can never escape the library folder.
        return raw?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
    }

    private fun uniqueFile(
        directory: File,
        name: String,
    ): File {
        var candidate = File(directory, name)
        if (!candidate.exists()) return candidate

        val base = name.substringBeforeLast('.', name)
        val extension = name.substringAfterLast('.', "").let { if (it.isEmpty()) "" else ".$it" }
        var counter = 1
        while (candidate.exists()) {
            candidate = File(directory, "$base ($counter)$extension")
            counter++
        }
        return candidate
    }
}
