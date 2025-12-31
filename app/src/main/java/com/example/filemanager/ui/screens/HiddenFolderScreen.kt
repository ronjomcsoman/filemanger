package com.example.filemanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.filemanager.utils.BiometricHelper
import com.example.filemanager.utils.HiddenFolderHelper
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenFolderScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isUnlocked by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf<List<File>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isUnlocked) {
            BiometricHelper.showBiometricPrompt(
                context as androidx.fragment.app.FragmentActivity,
                onSuccess = {
                    isUnlocked = true
                    items = HiddenFolderHelper.listFiles(context)
                },
                onError = { _, _ -> onBack() }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Private Space") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!isUnlocked) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp))
                    Text("Locked", style = MaterialTheme.typography.headlineSmall)
                    Button(onClick = {
                        BiometricHelper.showBiometricPrompt(
                            context as androidx.fragment.app.FragmentActivity,
                            onSuccess = {
                                isUnlocked = true
                                items = HiddenFolderHelper.listFiles(context)
                            },
                            onError = { _, _ -> }
                        )
                    }) {
                        Text("Unlock")
                    }
                }
            }
        } else {
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hidden files yet.")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    items(items) { file ->
                        HiddenFileItem(file = file, onDelete = {
                            HiddenFolderHelper.deleteFile(context, file)
                            items = HiddenFolderHelper.listFiles(context)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun HiddenFileItem(file: File, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Description, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(file.name, style = MaterialTheme.typography.bodyLarge)
            Text("${file.length()} bytes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
        }
    }
    Divider()
}
