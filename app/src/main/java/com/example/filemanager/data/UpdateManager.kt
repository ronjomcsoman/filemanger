package com.example.filemanager.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {

    suspend fun checkForUpdate(): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Replace with your repo details
                val url = URL("https://api.github.com/repos/ronjomcsoman/filemanger/releases/latest")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == 200) {
                    val stream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(stream))
                    val buffer = StringBuffer()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        buffer.append(line)
                    }
                    val json = JSONObject(buffer.toString())
                    // Assuming tag_name is like "v1.1" or "1.1"
                    val latestVersion = json.getString("tag_name")
                    val downloadUrl = json.getJSONArray("assets")
                        .getJSONObject(0)
                        .getString("browser_download_url")

                    // Simple version check logic (string comparison for now)
                    // In a real app, use BuildConfig.VERSION_NAME vs tag_name properly
                    // For demo: verify if we found a release url
                    return@withContext downloadUrl
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext null
        }
    }

    fun downloadAndInstall(downloadUrl: String) {
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("Downloading Update")
            .setDescription("Downloading latest version...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // Listen for download completion
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId == id) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                            val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            val uriString = cursor.getString(uriIndex)
                            installApk(Uri.parse(uriString))
                        }
                    }
                    context.unregisterReceiver(this)
                }
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
    }

    private fun installApk(uri: Uri) {
        try {
            // Need to convert file:// to content:// using FileProvider
            val file = File(uri.path!!) // This might need adjustment based on how DownloadManager returns URI
            // Safe hack: DownloadManager returns file:// usually for getDestinationInExternalPublicDir
            // But we can reconstruct path
            val actualFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "update.apk")
            
            if (actualFile.exists()) {
                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    actualFile
                )

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
