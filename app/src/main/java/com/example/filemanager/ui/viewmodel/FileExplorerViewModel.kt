package com.example.filemanager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

class FileExplorerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FileRepository(application)
    private val recycleBinRepository = RecycleBinRepository()
    private val safeRepository = SafeRepository()
    
    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _currentPath = MutableStateFlow<String?>(null)
    val currentPath: StateFlow<String?> = _currentPath

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NAME_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _currentCategory = MutableStateFlow<String?>(null)
    val currentCategory: StateFlow<String?> = _currentCategory.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.FILES) // FILES or ALBUMS
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _activeFilter = MutableStateFlow("All") // For Documents
    val activeFilter: StateFlow<String> = _activeFilter.asStateFlow()

    private var allFiles: List<FileItem> = emptyList()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilterAndSort()
    }

    fun updateSortOption(option: SortOption) {
        _sortOption.value = option
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        var filteredList = if (_searchQuery.value.isEmpty()) {
            allFiles
        } else {
            allFiles.filter { it.name.contains(_searchQuery.value, ignoreCase = true) }
        }

        filteredList = when (_sortOption.value) {
            SortOption.NAME_ASC -> filteredList.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> filteredList.sortedByDescending { it.name.lowercase() }
            SortOption.SIZE_ASC -> filteredList.sortedBy { it.size }
            SortOption.SIZE_DESC -> filteredList.sortedByDescending { it.size }
            SortOption.DATE_ASC -> filteredList.sortedBy { it.lastModified }
            SortOption.DATE_DESC -> filteredList.sortedByDescending { it.lastModified }
        }
        
        // Always show directories first
        _files.value = filteredList.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    // init {
    //    loadFiles(null)
    // }

    fun loadFiles(path: String?) {
        viewModelScope.launch {
            allFiles = repository.getFiles(path)
            _currentPath.value = path
            applyFilterAndSort()
        }
    }

    fun onFileClicked(fileItem: FileItem) {
        if (fileItem.isDirectory) {
            loadFiles(fileItem.path)
        } else {
            // Handle file open logic later
        }
    }
    fun filterByCategory(category: String) {
        viewModelScope.launch {
            _currentCategory.value = category
            _currentPath.value = "Category: $category"
            
            if (category == "Search") {
                _searchQuery.value = ""
                return@launch
            }
            
            val files = repository.getFilesByCategory(category)
            allFiles = files
            applyFilterAndSort()
        }
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch {
            _viewMode.value = mode
            val category = _currentCategory.value
            if (category != null) {
                if (mode == ViewMode.ALBUMS) {
                    allFiles = repository.getAlbums(category)
                    _files.value = allFiles
                } else {
                    filterByCategory(category)
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _activeFilter.value = filter
        // Logic to filter allFiles based on filter (e.g. PDF, Word)
        viewModelScope.launch {
             val filtered = if (filter == "All") {
                 allFiles
             } else {
                 allFiles.filter { it.extension.equals(filter, ignoreCase = true) }
             }
             _files.value = filtered
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
enum class SortOption {
    NAME_ASC, NAME_DESC, SIZE_ASC, SIZE_DESC, DATE_ASC, DATE_DESC
}

enum class ViewMode {
    FILES, ALBUMS
}
