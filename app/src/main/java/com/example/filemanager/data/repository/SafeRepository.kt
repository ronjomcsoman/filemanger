package com.example.filemanager.data.repository

import android.os.Environment
import com.example.filemanager.data.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SafeRepository {

    private val safeDirectory: File
        get() = File(Environment.getExternalStorageDirectory(), ".safe")

    init {
        if (!safeDirectory.exists()) {
            safeDirectory.mkdirs()
        }
    }

    suspend fun moveToSafe(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!safeDirectory.exists()) safeDirectory.mkdirs()
                val destFile = File(safeDirectory, file.name)
                // In real app, we should encrypt the file content here.
                // For this lightweight version, we just move it to a hidden folder.
                file.renameTo(destFile)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun removeFromSafe(fileItem: FileItem): Boolean {
        // Restore to default download or root if original path not tracked (simplified)
        return withContext(Dispatchers.IO) {
            try {
                val file = fileItem.file
                // Restore to Download folder by default if we don't track original path for Safe Folder
                val destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(destDir, file.name)
                file.renameTo(destFile)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun getSafeFiles(): List<FileItem> {
        return withContext(Dispatchers.IO) {
            if (safeDirectory.exists() && safeDirectory.isDirectory) {
                safeDirectory.listFiles()
                    ?.map { file ->
                        FileItem(
                            file = file,
                            name = file.name,
                            path = file.absolutePath,
                            isDirectory = file.isDirectory,
                            size = if (file.isDirectory) 0 else file.length(),
                            lastModified = file.lastModified()
                        )
                    }?.sortedBy { it.name } ?: emptyList()
            } else {
                emptyList()
            }
        }
    }
}
