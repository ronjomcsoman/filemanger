package com.example.filemanager.utils

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File

object HiddenFolderHelper {
    private const val HIDDEN_DIR_NAME = "hidden"
    private const val NOMEDIA_FILE = ".nomedia"

    fun getHiddenFolder(context: Context): File {
        // Use external files dir for the app
        val hiddenDir = File(context.getExternalFilesDir(null), HIDDEN_DIR_NAME)
        if (!hiddenDir.exists()) {
            hiddenDir.mkdirs()
        }
        // Ensure .nomedia exists to hide from media scanners
        val nomedia = File(hiddenDir, NOMEDIA_FILE)
        if (!nomedia.exists()) {
            nomedia.createNewFile()
        }
        return hiddenDir
    }

    fun listFiles(context: Context): List<File> {
        val dir = getHiddenFolder(context)
        return dir.listFiles()?.toList() ?: emptyList()
    }

    fun deleteFile(context: Context, file: File): Boolean {
        return file.delete()
    }
}
