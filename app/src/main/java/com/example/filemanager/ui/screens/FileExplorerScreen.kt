package com.example.filemanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.example.filemanager.ui.viewmodel.ViewMode
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.filemanager.data.model.FileItem
import com.example.filemanager.ui.viewmodel.FileExplorerViewModel
import java.io.File

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileExplorerScreen(
    initialPath: String? = null,
    category: String? = null,
    onBack: () -> Unit,
    viewModel: FileExplorerViewModel = viewModel()
) {
    // Initialize with path if provided and not already set
    androidx.compose.runtime.LaunchedEffect(initialPath, category) {
        if (category != null) {
            viewModel.filterByCategory(category)
        } else if (initialPath != null && viewModel.currentPath.value == null) {
            viewModel.loadFiles(initialPath)
        } else if (viewModel.currentPath.value == null) {
            viewModel.loadFiles(null)
        }
    }

    val files by viewModel.files.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()

    // Handle Back Press
    BackHandler {
        if (!viewModel.navigateUp()) {
            onBack()
        }
    }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<FileItem?>(null) }
    var renameText by remember { mutableStateOf("") }

    if (showRenameDialog && selectedFile != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename File") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("New Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    selectedFile?.let { viewModel.renameFile(it, renameText) }
                    showRenameDialog = false
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog && selectedFile != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete File") },
            text = { Text("Are you sure you want to delete ${selectedFile?.name}?") },
            confirmButton = {
                Button(onClick = {
                    selectedFile?.let { viewModel.deleteFile(it) }
                    showDeleteDialog = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    var isSearching by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    val currentCategory by viewModel.currentCategory.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF0097A7)) // Cyan/Teal background
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Color(0xFF00acc1))
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = when {
                                category != null -> category
                                initialPath != null -> "Internal Storage"
                                else -> "File Explorer"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (!viewModel.navigateUp()) {
                                onBack()
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Search */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // Breadcrumbs or Category Tabs
                if (category == null) {
                    // Breadcrumbs for Internal Storage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Home", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.clickable { viewModel.loadFiles(null) })
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                        Text(currentPath?.let { File(it).name } ?: "Internal Storage", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                } else if (category == "Photo" || category == "Video") {
                    // Tabs for Media
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TabItem(text = "Images", active = viewMode == com.example.filemanager.ui.viewmodel.ViewMode.FILES) {
                            viewModel.setViewMode(com.example.filemanager.ui.viewmodel.ViewMode.FILES)
                        }
                        TabItem(text = "Albums", active = viewMode == com.example.filemanager.ui.viewmodel.ViewMode.ALBUMS) {
                            viewModel.setViewMode(com.example.filemanager.ui.viewmodel.ViewMode.ALBUMS)
                        }
                    }
                } else if (category == "Document") {
                    // Filters for Documents
                    val filters = listOf("All", "Pdf", "Word", "Excel", "PPT", "Other")
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filters) { filter ->
                            FilterChip(text = filter, active = activeFilter == filter) {
                                viewModel.setFilter(filter)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(files) { file ->
                FileItemRow(
                    file = file,
                    onClick = { viewModel.onFileClicked(file) },
                    onLongClick = {
                        selectedFile = file
                    },
                    onRename = {
                        selectedFile = file
                        renameText = file.name
                        showRenameDialog = true
                    },
                    onMoveToSafe = {
                        viewModel.moveToSafe(file)
                    },
                    onDelete = {
                        selectedFile = file
                        showDeleteDialog = true
                    }
                )
            }
        }
    }
}

@Composable
fun TabItem(text: String, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = if (active) Color.White else Color.White.copy(alpha = 0.6f),
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
        if (active) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(40.dp)
                    .height(2.dp)
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun FilterChip(text: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (active) Color.White.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemRow(
    file: FileItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRename: () -> Unit,
    onMoveToSafe: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    expanded = true
                    onLongClick()
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail or Icon
        if (file.isDirectory) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFFF9C4), RoundedCornerShape(8.dp)), // Light yellow for folders
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFBC02D), modifier = Modifier.size(32.dp))
            }
        } else {
            // Document icons or basic file icon
            val icon = when (file.extension.lowercase()) {
                "pdf" -> Icons.Default.PictureAsPdf
                "doc", "docx" -> Icons.Default.Description
                "apk" -> Icons.Default.Android
                else -> Icons.Default.InsertDriveFile
            }
            val tint = when (file.extension.lowercase()) {
                "pdf" -> Color.Red
                "apk" -> Color(0xFF4CAF50)
                else -> Color.Gray
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = file.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = if (file.isDirectory) "${file.itemCount} Files" else "${android.text.format.Formatter.formatShortFileSize(androidx.compose.ui.platform.LocalContext.current, file.size)} • ${file.formattedDate}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        if (file.isDirectory) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        } else {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.Gray)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(text = { Text("Rename") }, onClick = { expanded = false; onRename() })
            DropdownMenuItem(text = { Text("Move to Safe Folder") }, onClick = { expanded = false; onMoveToSafe() })
            DropdownMenuItem(text = { Text("Delete") }, onClick = { expanded = false; onDelete() })
        }
    }
}
