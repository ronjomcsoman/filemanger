package com.example.filemanager.data.repository

import android.os.Environment
import com.example.filemanager.data.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository {

    suspend fun getFiles(path: String?): List<FileItem> {
        return withContext(Dispatchers.IO) {
            val directory = if (path != null) File(path) else Environment.getExternalStorageDirectory()
            val isRestricted = path?.contains("Android/data") == true || path?.contains("Android/obb") == true
            
            // Try standard listing first
            var files = if (directory.exists() && directory.isDirectory && !isRestricted) {
                directory.listFiles()?.map { file ->
                    FileItem(
                        file = file,
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = file.isDirectory,
                        size = if (file.isDirectory) 0 else file.length(),
                        lastModified = file.lastModified()
                    )
                } ?: emptyList()
            } else {
                emptyList()
            }

            // Fallback to RootUtils if empty and potentially restricted or root available
            if ((files.isEmpty() || isRestricted) && com.example.filemanager.utils.RootUtils.isRootAvailable()) {
                val rootFiles = com.example.filemanager.utils.RootUtils.listFiles(directory.absolutePath)
                if (rootFiles.isNotEmpty()) {
                    files = rootFiles
                }
            }

            files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        }
    }

    suspend fun renameFile(file: File, newName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val newFile = File(file.parent, newName)
            if (newFile.exists()) return@withContext false
            file.renameTo(newFile)
        }
    }

    suspend fun deleteFile(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        }
    }
}
