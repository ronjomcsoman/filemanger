package com.example.filemanager.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToFileExplorer: () -> Unit,
    onNavigateToRecycleBin: () -> Unit,
    onNavigateToSafeFolder: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val updateManager = remember { com.example.filemanager.data.UpdateManager(context) }
    var updateUrl by remember { mutableStateOf<String?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    if (showUpdateDialog && updateUrl != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Update Available") },
            text = { Text("A new version is available on GitHub. Download and install now?") },
            confirmButton = {
                Button(onClick = {
                    showUpdateDialog = false
                    updateUrl?.let { updateManager.downloadAndInstall(it) }
                }) { Text("Update") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showUpdateDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("File Manager") },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val url = updateManager.checkForUpdate()
                            if (url != null) {
                                updateUrl = url
                                showUpdateDialog = true
                            } else {
                                androidx.widget.Toast.makeText(context, "No updates found", androidx.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Refresh, contentDescription = "Check Update")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Welcome to File Manager")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToFileExplorer) {
                Text("Internal Storage")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToRecycleBin) {
                Text("Recycle Bin")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToSafeFolder) {
                Text("Safe Folder")
            }
        }
    }
}
