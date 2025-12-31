package com.example.filemanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val updateManager = remember { com.example.filemanager.data.UpdateManager(context) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About FileExplorer") },
            text = {
                Column {
                    Text("Developer: techwithron", style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("A modern, powerful file manager designed with security and simplicity in mind.", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Version: 1.0.0", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }

    if (showPrivacyDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Security & Privacy") },
            text = {
                Text("Your privacy is our priority. FileExplorer does not collect or upload your personal files. Safe Folder data is encrypted locally and protected by biometric authentication.")
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showPrivacyDialog = false }) { Text("Understood") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SettingsItem(
                title = "Check for Updates", 
                subtitle = "Load latest version from GitHub",
                icon = Icons.Default.Update, 
                onClick = {
                    scope.launch {
                        val url = updateManager.checkForUpdate()
                        if (url != null) {
                             android.widget.Toast.makeText(context, "Update found! Starting download...", android.widget.Toast.LENGTH_SHORT).show()
                             updateManager.downloadAndInstall(url)
                        } else {
                             android.widget.Toast.makeText(context, "You are on the latest version", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
            Divider()
            SettingsItem("About App", "Developer info and details", Icons.Default.Info, onClick = { showAboutDialog = true })
            Divider()
            SettingsItem("Security & Privacy", "Data handling and biometrics", Icons.Default.Security, onClick = { showPrivacyDialog = true })
            
            Spacer(Modifier.weight(1f))
            
            Text(
                "Version 1.0.0",
                modifier = Modifier.padding(16.dp),
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                color = androidx.compose.ui.graphics.Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(title: String, subtitle: String? = null, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { subtitle?.let { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    )
}
