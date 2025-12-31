package com.example.filemanager.utils

import com.example.filemanager.data.model.FileItem
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object RootUtils {

    fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c ls")
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun listFiles(path: String): List<FileItem> {
        val files = mutableListOf<FileItem>()
        try {
            // Use 'ls -1 -F' for name only with directory indicators
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls -1 -F \"$path\""))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line.isNullOrBlank()) continue
                
                val rawName = line!!.trim()
                if (rawName == "." || rawName == "..") continue

                val isDirectory = rawName.endsWith("/")
                val name = if (isDirectory) rawName.dropLast(1) else rawName

                val fullPath = if (path.endsWith("/")) "$path$name" else "$path/$name"
                
                files.add(FileItem(
                    file = File(fullPath),
                    name = name,
                    path = fullPath,
                    isDirectory = isDirectory,
                    size = 0,
                    lastModified = System.currentTimeMillis()
                ))
            }
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return files
    }
}
