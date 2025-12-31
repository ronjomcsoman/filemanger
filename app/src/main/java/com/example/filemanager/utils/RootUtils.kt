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
            // Execute 'ls -l' (simplified parsing for now)
            // Ideally use 'ls -ln' for numeric IDs or just 'ls' and handle folders
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls -F \"$path\""))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line.isNullOrBlank()) continue
                
                // ls -F appends / to directories and * to executables
                val rawName = line!!.trim()
                val isDirectory = rawName.endsWith("/")
                val name = if (isDirectory) rawName.dropLast(1) else rawName
                
                // Skip . and ..
                if (name == "." || name == "..") continue

                val fullPath = if (path.endsWith("/")) "$path$name" else "$path/$name"
                
                // For root files, we might not get size/date easily without ls -l parsing
                // Using placeholders or simple java File checks if readable (might fail if truly restricted)
                // For now, simple mapping
                files.add(FileItem(
                    file = File(fullPath),
                    name = name,
                    path = fullPath,
                    isDirectory = isDirectory,
                    size = 0, // Difficult to get efficiently from simple ls
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
