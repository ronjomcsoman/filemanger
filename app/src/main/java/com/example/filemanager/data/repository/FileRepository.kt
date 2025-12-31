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
            val pathStr = directory.absolutePath
            
            val isRestricted = pathStr.contains("/Android/data") || 
                             pathStr.contains("/Android/obb") || 
                             pathStr == "/data" || 
                             pathStr.startsWith("/data/") ||
                             pathStr == "/system" ||
                             pathStr == "/"
            
            // Try standard listing first if not obviously restricted
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
                val rootFiles = com.example.filemanager.utils.RootUtils.listFiles(pathStr)
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

    // Recycle bin directory (app private external storage)
    private val recycleBinDir: File = File(Environment.getExternalStorageDirectory(), "recycle_bin")

    init {
        if (!recycleBinDir.exists()) {
            recycleBinDir.mkdirs()
        }
    }

    /**
     * Move a file or directory to the recycle bin.
     * Returns true if successful.
     */
    suspend fun moveToRecycleBin(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            if (!recycleBinDir.exists()) return@withContext false
            val dest = File(recycleBinDir, file.name)
            val finalDest = if (dest.exists()) {
                File(recycleBinDir, "${file.name}_" + System.currentTimeMillis())
            } else dest
            file.renameTo(finalDest)
        }
    }

    /**
     * List items in recycle bin.
     */
    suspend fun listRecycleBin(): List<FileItem> {
        return withContext(Dispatchers.IO) {
            if (!recycleBinDir.exists()) return@withContext emptyList<FileItem>()
            recycleBinDir.listFiles()?.map { file ->
                FileItem(
                    file = file,
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    size = if (file.isDirectory) 0 else file.length(),
                    lastModified = file.lastModified()
                )
            } ?: emptyList()
        }
    }

    /**
     * Restore a file from recycle bin to a target directory.
     */
    suspend fun restoreFromRecycleBin(file: File, targetDir: File): Boolean {
        return withContext(Dispatchers.IO) {
            if (!targetDir.isDirectory) return@withContext false
            val dest = File(targetDir, file.name)
            val finalDest = if (dest.exists()) {
                File(targetDir, "${file.name}_" + System.currentTimeMillis())
            } else dest
            file.renameTo(finalDest)
        }
    }
}
