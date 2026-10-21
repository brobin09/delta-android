package com.swordfish.lemuroid.app.shared.library

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Delta-style import: copies the picked game files into the app's own library folder
 * and asks the library to rescan, so games show up sorted by system. It also reports
 * what happened so a failed import is never silent.
 */
object RomImporter {
    data class CopyResult(
        val copied: List<String>,
        val errors: List<String>,
    )

    suspend fun importAndReport(
        context: Context,
        db: RetrogradeDatabase,
        uris: List<Uri>,
    ) {
        val appContext = context.applicationContext
        toast(appContext, "Importing ${uris.size} file(s)…")

        val before = countGames(db)
        val result = copyRoms(appContext, uris)

        if (result.copied.isEmpty()) {
            val reason = result.errors.firstOrNull() ?: "unknown error"
            toast(appContext, "Could not import: $reason")
            return
        }

        toast(appContext, "Copied ${result.copied.size} file(s). Scanning…")
        LibraryIndexScheduler.scheduleLibrarySync(appContext)

        // The scan runs in the background, so wait (up to 2 minutes) for new games to appear.
        var after = before
        var waited = 0
        while (waited < MAX_WAIT_SECONDS) {
            delay(1000)
            waited++
            after = countGames(db)
            if (after > before) {
                delay(2000)
                after = countGames(db)
                break
            }
        }

        val summary = systemSummary(db)
        if (after > before) {
            toast(appContext, "Added ${after - before} game(s). Library: $summary")
        } else {
            val names = result.copied.joinToString(", ")
            toast(
                appContext,
                "Copied $names but no new games were recognized. Library: $summary",
            )
        }
    }

    private suspend fun countGames(db: RetrogradeDatabase): Int =
        withContext(Dispatchers.IO) {
            db.gameDao().selectSystemsWithCount().first().sumOf { it.count }
        }

    private suspend fun systemSummary(db: RetrogradeDatabase): String =
        withContext(Dispatchers.IO) {
            val systems = db.gameDao().selectSystemsWithCount().first()
            if (systems.isEmpty()) {
                "0 games"
            } else {
                systems.joinToString(", ") { "${it.count} ${it.systemId}" }
            }
        }

    private suspend fun toast(
        context: Context,
        message: String,
    ) = withContext(Dispatchers.Main) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    suspend fun copyRoms(
        context: Context,
        uris: List<Uri>,
    ): CopyResult =
        withContext(Dispatchers.IO) {
            val romsDirectory = DirectoriesManager(context).getInternalRomsDirectory()
            val copied = mutableListOf<String>()
            val errors = mutableListOf<String>()

            uris.forEach { uri ->
                runCatching { copyToDirectory(context, uri, romsDirectory) }
                    .onSuccess { copied.add(it) }
                    .onFailure { errors.add(it.message ?: it.javaClass.simpleName) }
            }

            CopyResult(copied, errors)
        }

    private fun copyToDirectory(
        context: Context,
        uri: Uri,
        directory: File,
    ): String {
        val name = displayName(context, uri) ?: error("could not read the file name")
        val target = uniqueFile(directory, name)
        val input = context.contentResolver.openInputStream(uri) ?: error("could not open $name")
        input.use { source ->
            target.outputStream().use { sink -> source.copyTo(sink) }
        }
        return target.name
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

    private const val MAX_WAIT_SECONDS = 120
}
