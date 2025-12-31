package com.example.filemanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileExplorerScreen(
    onBack: () -> Unit,
    viewModel: FileExplorerViewModel = viewModel()
) {
    val files by viewModel.files.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = currentPath?.let { File(it).name } ?: "Internal Storage") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!viewModel.navigateUp()) {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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

    Box {
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                contentDescription = if (file.isDirectory) "Folder" else "File",
                tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(text = file.name, style = MaterialTheme.typography.bodyLarge)
                if (!file.isDirectory) {
                    Text(text = "${file.size} bytes", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Rename") },
                onClick = {
                    expanded = false
                    onRename()
                }
            )
            DropdownMenuItem(
                text = { Text("Move to Safe Folder") },
                onClick = {
                    expanded = false
                    onMoveToSafe()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}
