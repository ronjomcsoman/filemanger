package com.example.filemanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filemanager.data.model.FileItem
import com.example.filemanager.data.repository.FileRepository
import com.example.filemanager.data.repository.RecycleBinRepository
import com.example.filemanager.data.repository.SafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class FileExplorerViewModel : ViewModel() {
    private val repository = FileRepository()
    private val recycleBinRepository = RecycleBinRepository()
    private val safeRepository = SafeRepository()
    
    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _currentPath = MutableStateFlow<String?>(null)
    val currentPath: StateFlow<String?> = _currentPath

    // init {
    //    loadFiles(null)
    // }

    fun loadFiles(path: String?) {
        viewModelScope.launch {
            val fileList = repository.getFiles(path)
            _files.value = fileList
            _currentPath.value = path
        }
    }

    fun onFileClicked(fileItem: FileItem) {
        if (fileItem.isDirectory) {
            loadFiles(fileItem.path)
        } else {
            // Handle file open logic later
        }
    }
    
    fun navigateUp(): Boolean {
        val current = _currentPath.value
        if (current != null) {
            val parent = File(current).parent
            // Check if we are at root of external storage
            val root = android.os.Environment.getExternalStorageDirectory().absolutePath
            if (current == root) {
                return false // Exit to home
            }
            loadFiles(parent)
            return true
        }
        return false // Already at root (conceptually, if null was root)
    }

    fun renameFile(fileItem: FileItem, newName: String) {
        viewModelScope.launch {
            if (repository.renameFile(fileItem.file, newName)) {
                loadFiles(_currentPath.value)
            }
        }
    }

    fun deleteFile(fileItem: FileItem) {
        viewModelScope.launch {
            if (recycleBinRepository.moveToRecycleBin(fileItem.file)) {
                loadFiles(_currentPath.value)
            }
        }
    }

    fun moveToSafe(fileItem: FileItem) {
        viewModelScope.launch {
            if (safeRepository.moveToSafe(fileItem.file)) {
                loadFiles(_currentPath.value)
            }
        }
    }
}
