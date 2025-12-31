package com.example.filemanager.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.filemanager.data.model.FileItem
import com.example.filemanager.ui.viewmodel.RecycleBinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    onBack: () -> Unit,
    viewModel: RecycleBinViewModel = viewModel()
) {
    val files by viewModel.deletedFiles.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<FileItem?>(null) }

    if (showDeleteDialog && selectedFile != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Permanently") },
            text = { Text("Are you sure you want to permanently delete ${selectedFile?.name}? This cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    selectedFile?.let { viewModel.deletePermanently(it) }
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
                title = { Text("Recycle Bin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Recycle Bin is empty")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(files) { file ->
                    RecycleBinItemRow(
                        file = file,
                        onRestore = { viewModel.restoreFile(file) },
                        onDeletePermanent = {
                            selectedFile = file
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecycleBinItemRow(
    file: FileItem,
    onRestore: () -> Unit,
    onDeletePermanent: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { expanded = true },
                    onLongClick = { expanded = true }
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "File",
                tint = Color.Gray,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(text = file.name, style = MaterialTheme.typography.bodyLarge)
                Text(text = "Tap to manage", style = MaterialTheme.typography.bodySmall)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Restore") },
                leadingIcon = { Icon(Icons.Default.Restore, null) },
                onClick = {
                    expanded = false
                    onRestore()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete Permanently") },
                leadingIcon = { Icon(Icons.Default.Delete, null) },
                onClick = {
                    expanded = false
                    onDeletePermanent()
                }
            )
        }
    }
}
