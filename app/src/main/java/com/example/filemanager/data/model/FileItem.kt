package com.example.filemanager.data.model

import java.io.File
import java.util.Date

data class FileItem(
    val file: File,
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
) {
    val formattedDate: String
        get() = Date(lastModified).toString() // Simplify for now, use proper formatter later
}
