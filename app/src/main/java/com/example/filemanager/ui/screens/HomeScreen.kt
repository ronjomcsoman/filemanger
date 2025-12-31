package com.example.filemanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.NavigationDrawerItemDefaults
import kotlinx.coroutines.launch
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToFileExplorer: (String?) -> Unit,
    onNavigateToRecycleBin: () -> Unit,
    onNavigateToSafeFolder: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCategory: (String) -> Unit
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

    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)

    androidx.compose.material3.ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            androidx.compose.material3.ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text("FileExplorer", modifier = Modifier.padding(16.dp), style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
                androidx.compose.material3.Divider()
                
                androidx.compose.material3.NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Filled.Storage, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                androidx.compose.material3.NavigationDrawerItem(
                    label = { Text("Recycle Bin") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() } 
                        onNavigateToRecycleBin() 
                    },
                    icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                androidx.compose.material3.NavigationDrawerItem(
                    label = { Text("Safe Folder") },
                    selected = false,
                    onClick = { 
                         scope.launch { drawerState.close() }
                         onNavigateToSafeFolder()
                    },
                    icon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                androidx.compose.material3.NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { 
                         scope.launch { drawerState.close() }
                         onNavigateToSettings()
                    },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("FileExplorer", color = androidx.compose.ui.graphics.Color.White) },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF2196F3)
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = androidx.compose.ui.graphics.Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            // Trigger search - for now navigate search mode
                            onNavigateToCategory("Search") 
                        }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                )
            }
        ) { paddingValues ->


            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                // Storage Card (Vertical Design)
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clickable { 
                                // Navigate to root or main storage
                                val path = if (com.example.filemanager.utils.RootUtils.isRootAvailable()) "/" else null
                                onNavigateToFileExplorer(path)
                            },
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFF00bcd4) // Cyan style
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val used = storageStats?.first ?: 0L
                            val total = storageStats?.second ?: 1L
                            val progressValue = if (total > 0) used.toFloat() / total.toFloat() else 0f
                            val percent = (progressValue * 100).toInt()

                            androidx.compose.foundation.layout.Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(120.dp)
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    progress = 1f,
                                    modifier = Modifier.size(120.dp),
                                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.1f),
                                    strokeWidth = 10.dp
                                )
                                androidx.compose.material3.CircularProgressIndicator(
                                    progress = progressValue,
                                    modifier = Modifier.size(120.dp),
                                    color = androidx.compose.ui.graphics.Color.Black,
                                    strokeWidth = 10.dp
                                )
                                Text(
                                    text = "$percent%",
                                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Internal Storage",
                                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                            Text(
                                "${com.example.filemanager.utils.StorageUtils.formatSize(used)} / ${com.example.filemanager.utils.StorageUtils.formatSize(total)}",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Category Icons Grid
                val categories = listOf(
                    CategoryData("Photo", Icons.Filled.Image, androidx.compose.ui.graphics.Color(0xFF4CAF50)),
                    CategoryData("Video", Icons.Filled.VideoLibrary, androidx.compose.ui.graphics.Color(0xFF2196F3)),
                    CategoryData("Audio", Icons.Filled.MusicNote, androidx.compose.ui.graphics.Color(0xFFFF9800)),
                    CategoryData("Document", Icons.Filled.Description, androidx.compose.ui.graphics.Color(0xFFFFC107)),
                    CategoryData("Archive", Icons.Filled.Inventory, androidx.compose.ui.graphics.Color(0xFF9C27B0)),
                    CategoryData("APK", Icons.Filled.Android, androidx.compose.ui.graphics.Color(0xFFE91E63))
                )

                items(categories.size) { index ->
                    val category = categories[index]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onNavigateToCategory(category.name) }
                            .padding(8.dp)
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(category.color, androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.name,
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(category.name, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
@Composable
private fun CategoryItem(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color) : CategoryData {
    return CategoryData(name, icon, color)
}

data class CategoryData(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: androidx.compose.ui.graphics.Color)
