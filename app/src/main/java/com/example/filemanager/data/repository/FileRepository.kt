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
            
            if (directory.exists() && directory.isDirectory) {
                directory.listFiles()?.map { file ->
                    FileItem(
                        file = file,
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = file.isDirectory,
                        size = if (file.isDirectory) 0 else file.length(),
                        lastModified = file.lastModified()
                    )
                }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
            } else {
                emptyList()
            }
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
