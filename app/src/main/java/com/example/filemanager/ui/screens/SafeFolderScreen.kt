package com.example.filemanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.filemanager.ui.viewmodel.SafeFolderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeFolderScreen(
    onBack: () -> Unit,
    viewModel: SafeFolderViewModel = viewModel()
) {
    val isLocked by viewModel.isLocked.collectAsState()
    val hasPin by viewModel.hasPin.collectAsState()
    val files by viewModel.safeFiles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safe Folder") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLocked) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var pin by remember { mutableStateOf("") }
                var error by remember { mutableStateOf("") }

                if (!hasPin) {
                    Text("Set a PIN for Safe Folder")
                } else {
                    Text("Enter PIN")
                }

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = error.isNotEmpty()
                )
                if (error.isNotEmpty()) {
                    Text(error)
                }
                Button(
                    onClick = {
                        if (!hasPin) {
                            viewModel.setPin(pin)
                        } else {
                            if (!viewModel.unlock(pin)) {
                                error = "Incorrect PIN"
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(if (!hasPin) "Set PIN" else "Unlock")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(files) { file ->
                    FileItemRow(
                        file = file,
                        onClick = {}, // Handle open in safe logic
                        onLongClick = {},
                        onRename = {}, // No rename in safe for now
                        onMoveToSafe = {},
                        onDelete = { viewModel.removeFromSafe(file) } // Use "Delete" icon to restore/remove
                    )
                }
            }
        }
    }
}
