package com.example.filemanager.utils

import android.os.Environment
import android.os.StatFs
import java.io.File

object StorageUtils {

    fun getMainStorageStats(): Pair<Long, Long> {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalSize = totalBlocks * blockSize
        val availableSize = availableBlocks * blockSize
        val usedSize = totalSize - availableSize

        return Pair(usedSize, totalSize)
    }

    fun formatSize(size: Long): String {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            size >= gb -> String.format("%.2f GB", size.toDouble() / gb)
            size >= mb -> String.format("%.2f MB", size.toDouble() / mb)
            size >= kb -> String.format("%.2f KB", size.toDouble() / kb)
            else -> "$size B"
        }
    }

    fun getCategorySize(root: File, extensions: Set<String>): Long {
        var size: Long = 0
        if (root.isDirectory) {
            val children = root.listFiles()
            if (children != null) {
                for (child in children) {
                    // Skip hidden folders like .recycler, .safe
                    if (child.name.startsWith(".")) continue
                    size += getCategorySize(child, extensions)
                }
            }
        } else {
            if (extensions.contains(root.extension.lowercase())) {
                size = root.length()
            }
        }
        return size
    }
}
