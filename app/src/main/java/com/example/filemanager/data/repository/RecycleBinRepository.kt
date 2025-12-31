package com.example.filemanager.data.repository

import android.os.Environment
import com.example.filemanager.data.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.util.Scanner

class RecycleBinRepository {

    private val binDirectory: File
        get() = File(Environment.getExternalStorageDirectory(), ".recycler")

    init {
        if (!binDirectory.exists()) {
            binDirectory.mkdirs()
        }
    }

    suspend fun moveToRecycleBin(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!binDirectory.exists()) binDirectory.mkdirs()

                val timestamp = System.currentTimeMillis()
                val newName = "${timestamp}_${file.name}"
                val destFile = File(binDirectory, newName)
                val metaFile = File(binDirectory, "$newName.meta")

                // Write metadata (Original Path)
                FileWriter(metaFile).use { writer ->
                    writer.write(file.absolutePath)
                }

                // Move file
                file.renameTo(destFile)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun restoreFile(fileItem: FileItem): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = fileItem.file
                val metaFile = File(binDirectory, "${file.name}.meta")
                
                if (metaFile.exists()) {
                    val originalPath = Scanner(metaFile).use { if (it.hasNextLine()) it.nextLine() else null }
                    
                    if (originalPath != null) {
                        val originalFile = File(originalPath)
                        // Ensure parent exists
                        if (!originalFile.parentFile.exists()) {
                            originalFile.parentFile.mkdirs()
                        }
                        
                        if (file.renameTo(originalFile)) {
                            metaFile.delete()
                            return@withContext true
                        }
                    }
                }
                false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun deletePermanently(fileItem: FileItem): Boolean {
        return withContext(Dispatchers.IO) {
            val file = fileItem.file
            val metaFile = File(binDirectory, "${file.name}.meta")
            metaFile.delete()
            file.deleteRecursively()
        }
    }

    suspend fun getRecycleBinFiles(): List<FileItem> {
        return withContext(Dispatchers.IO) {
            if (binDirectory.exists() && binDirectory.isDirectory) {
                binDirectory.listFiles { _, name -> !name.endsWith(".meta") }
                    ?.map { file ->
                        FileItem(
                            file = file,
                            name = file.name, // Logic to strip timestamp if desired, keeping simple for now
                            path = file.absolutePath,
                            isDirectory = file.isDirectory,
                            size = if (file.isDirectory) 0 else file.length(),
                            lastModified = file.lastModified()
                        )
                    }?.sortedByDescending { it.lastModified } ?: emptyList()
            } else {
                emptyList()
            }
        }
    }
}
