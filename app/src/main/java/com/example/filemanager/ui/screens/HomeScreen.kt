package com.example.filemanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import kotlinx.coroutines.launch
import android.widget.Toast

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

    // Storage Stats
    var storageStats by remember { mutableStateOf<Pair<Long, Long>?>(null) }
    var categorySizes by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            storageStats = com.example.filemanager.utils.StorageUtils.getMainStorageStats()
            
            val root = android.os.Environment.getExternalStorageDirectory()
            val images = com.example.filemanager.utils.StorageUtils.getCategorySize(root, setOf("jpg", "jpeg", "png", "gif", "webp"))
            val audio = com.example.filemanager.utils.StorageUtils.getCategorySize(root, setOf("mp3", "wav", "aac", "ogg"))
            val video = com.example.filemanager.utils.StorageUtils.getCategorySize(root, setOf("mp4", "mkv", "avi", "mov"))
            
            categorySizes = mapOf("Images" to images, "Audio" to audio, "Videos" to video)
        }
    }

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
                title = { Text("FileExplorer", color = androidx.compose.ui.graphics.Color.White) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF2196F3) // Cx Blue
                ),
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val url = updateManager.checkForUpdate()
                            if (url != null) {
                                updateUrl = url
                                showUpdateDialog = true
                            } else {
                                Toast.makeText(context, "No updates found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Check Update", tint = androidx.compose.ui.graphics.Color.White)
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
                .androidx.compose.foundation.verticalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Storage Dashboard
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF03A9F4))
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Gauge (Simplified as Text/Progress for now)
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                         val used = storageStats?.first ?: 0L
                         val total = storageStats?.second ?: 1L
                         val percent = (used.toFloat() / total.toFloat()) * 100
                         
                         androidx.compose.material3.CircularProgressIndicator(
                             progress = { used.toFloat() / total.toFloat() },
                             modifier = Modifier.androidx.compose.foundation.layout.size(120.dp),
                             color = androidx.compose.ui.graphics.Color.White,
                             trackColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f),
                         )
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Text("${percent.toInt()}%", color = androidx.compose.ui.graphics.Color.White, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
                             Text("Used", color = androidx.compose.ui.graphics.Color.White)
                         }
                    }
                    
                    // Stats Column
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Images: ${com.example.filemanager.utils.StorageUtils.formatSize(categorySizes["Images"] ?: 0)}", color = androidx.compose.ui.graphics.Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text("Audio: ${com.example.filemanager.utils.StorageUtils.formatSize(categorySizes["Audio"] ?: 0)}", color = androidx.compose.ui.graphics.Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text("Videos: ${com.example.filemanager.utils.StorageUtils.formatSize(categorySizes["Videos"] ?: 0)}", color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid Shortcuts
            Text("Local", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            
            val gridItems = listOf(
                Triple("Main Storage", Icons.Filled.Storage, onNavigateToFileExplorer),
                Triple("Downloads", Icons.Filled.Download, { /* TODO: Open Downloads */ }),
                Triple("Recycle Bin", Icons.Filled.Delete, onNavigateToRecycleBin),
                Triple("Safe Folder", Icons.Filled.Lock, onNavigateToSafeFolder)
            )

            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                shrinkWrap = true,
                physics = androidx.compose.foundation.layout.NeverScrollableScrollPhysics(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(gridItems.size) { index ->
                    val item = gridItems[index]
                    androidx.compose.material3.Card(
                        onClick = item.third,
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(item.second, contentDescription = item.first, tint = androidx.compose.ui.graphics.Color(0xFFFF9800), modifier = Modifier.androidx.compose.foundation.layout.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(item.first, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
