package com.example.filemanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filemanager.data.model.FileItem
import com.example.filemanager.data.repository.RecycleBinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecycleBinViewModel : ViewModel() {
    private val repository = RecycleBinRepository()

    private val _deletedFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val deletedFiles: StateFlow<List<FileItem>> = _deletedFiles.asStateFlow()

    init {
        loadDeletedFiles()
    }

    fun loadDeletedFiles() {
        viewModelScope.launch {
            _deletedFiles.value = repository.getRecycleBinFiles()
        }
    }

    fun restoreFile(fileItem: FileItem) {
        viewModelScope.launch {
            if (repository.restoreFile(fileItem)) {
                loadDeletedFiles()
            }
        }
    }

    fun deletePermanently(fileItem: FileItem) {
        viewModelScope.launch {
            if (repository.deletePermanently(fileItem)) {
                loadDeletedFiles()
            }
        }
    }
}
