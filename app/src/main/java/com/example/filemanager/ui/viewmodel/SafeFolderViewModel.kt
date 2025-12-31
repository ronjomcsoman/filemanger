package com.example.filemanager.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.filemanager.data.model.FileItem
import com.example.filemanager.data.repository.SafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SafeFolderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SafeRepository()
    private val prefs = application.getSharedPreferences("safe_prefs", Context.MODE_PRIVATE)

    private val _safeFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val safeFiles: StateFlow<List<FileItem>> = _safeFiles.asStateFlow()

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _hasPin = MutableStateFlow(prefs.contains("safe_pin"))
    val hasPin: StateFlow<Boolean> = _hasPin.asStateFlow()

    fun setPin(pin: String) {
        prefs.edit().putString("safe_pin", pin).apply()
        _hasPin.value = true
        unlock(pin)
    }

    fun unlock(pin: String): Boolean {
        val storedPin = prefs.getString("safe_pin", "")
        if (storedPin == pin) {
            _isLocked.value = false
            loadSafeFiles()
            return true
        }
        return false
    }

    fun loadSafeFiles() {
        viewModelScope.launch {
            _safeFiles.value = repository.getSafeFiles()
        }
    }

    fun removeFromSafe(fileItem: FileItem) {
        viewModelScope.launch {
            if (repository.removeFromSafe(fileItem)) {
                loadSafeFiles()
            }
        }
    }
}
