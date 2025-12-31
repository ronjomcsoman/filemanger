package com.example.filemanager.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile

object SafHelper {
    private const val PREFS_NAME = "saf_prefs"
    private const val KEY_URI = "saf_root_uri"

    fun launchDirectoryPicker(activity: Activity, requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        activity.startActivityForResult(intent, requestCode)
    }

    fun persistUriPermission(context: Context, uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        // Save to SharedPreferences for later use
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_URI, uri.toString()).apply()
    }

    fun getRootDocumentFile(context: Context): DocumentFile? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriString = prefs.getString(KEY_URI, null) ?: return null
        val uri = Uri.parse(uriString)
        return DocumentFile.fromTreeUri(context, uri)
    }

    fun getDocumentFile(context: Context, path: String): DocumentFile? {
        // Resolve a relative path under the persisted root
        val root = getRootDocumentFile(context) ?: return null
        var current: DocumentFile? = root
        val parts = path.trim('/').split('/')
        for (part in parts) {
            current = current?.findFile(part)
            if (current == null) break
        }
        return current
    }
}
