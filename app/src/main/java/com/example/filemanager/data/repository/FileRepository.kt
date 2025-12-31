package com.example.filemanager.data.repository

import android.content.Context
import android.provider.MediaStore
import android.os.Environment
import com.example.filemanager.data.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository(private val context: Context) {

    suspend fun getFiles(path: String?): List<FileItem> {
        return withContext(Dispatchers.IO) {
            val directory = if (path != null) File(path) else Environment.getExternalStorageDirectory()
            val pathStr = directory.absolutePath
            
            // Breadcrumbs calculation
            val rootPath = Environment.getExternalStorageDirectory().absolutePath
            val breadcrumbs = if (pathStr.startsWith(rootPath)) {
                pathStr.removePrefix(rootPath).split("/").filter { it.isNotEmpty() }
            } else {
                listOf(pathStr)
            }

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
                        lastModified = file.lastModified(),
                        itemCount = if (file.isDirectory) (file.listFiles()?.size ?: 0) else 0
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

    suspend fun getFilesByCategory(category: String): List<FileItem> {
        return withContext(Dispatchers.IO) {
            val uri = when (category) {
                "Photo" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "Video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "Audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                "Document" -> MediaStore.Files.getContentUri("external")
                else -> MediaStore.Files.getContentUri("external")
            }

            val projection = arrayOf(
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE
            )

            val selection = when (category) {
                "Photo" -> null
                "Video" -> null
                "Audio" -> null
                "Document" -> "${MediaStore.Files.FileColumns.MIME_TYPE} = 'application/pdf' OR ${MediaStore.Files.FileColumns.MIME_TYPE} = 'application/msword' OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'application/vnd.openxmlformats-officedocument%'"
                "APK" -> "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.apk'"
                "Archive" -> "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.zip' OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.rar' OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.7z'"
                else -> null
            }

            val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

            val items = mutableListOf<FileItem>()
            context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataIndex)
                    val name = cursor.getString(nameIndex)
                    val size = cursor.getLong(sizeIndex)
                    val date = cursor.getLong(dateIndex) * 1000 // Convert to ms

                    items.add(FileItem(
                        file = File(path),
                        name = name ?: File(path).name,
                        path = path,
                        isDirectory = false,
                        size = size,
                        lastModified = date
                    ))
                }
            }
            items
        }
    }

    suspend fun getAlbums(category: String): List<FileItem> {
        return withContext(Dispatchers.IO) {
            val uri = when (category) {
                "Photo" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "Video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                else -> return@withContext emptyList<FileItem>()
            }

            val projection = arrayOf(
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA
            )
            
            val albums = mutableMapOf<String, String>() // Bucket Name to Representative Image Path
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val bucketIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
                val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

                while (cursor.moveToNext()) {
                    val bucketName = cursor.getString(bucketIndex) ?: "Unknown"
                    val path = cursor.getString(dataIndex)
                    if (!albums.containsKey(bucketName)) {
                        albums[bucketName] = path
                    }
                }
            }

            albums.map { (name, path) ->
                FileItem(
                    file = File(File(path).parent ?: ""),
                    name = name,
                    path = File(path).parent ?: "",
                    isDirectory = true,
                    size = 0,
                    lastModified = 0
                )
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
