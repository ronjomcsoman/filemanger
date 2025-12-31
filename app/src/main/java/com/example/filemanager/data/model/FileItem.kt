package com.example.filemanager.data.model

import java.io.File
import java.util.Date

data class FileItem(
    val file: File,
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val itemCount: Int = 0,
    val mimeType: String? = null
) {
    val formattedDate: String
        get() = android.text.format.DateFormat.format("dd-MM-yyyy hh:mm a", lastModified).toString()
        
    val extension: String
        get() = name.substringAfterLast('.', "")
}
