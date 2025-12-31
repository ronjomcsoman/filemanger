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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("File Manager") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(text = "Welcome to File Manager")
            Button(onClick = onNavigateToFileExplorer) {
                Text("Go to Internal Storage")
            }
            Button(onClick = onNavigateToRecycleBin) {
                Text("Recycle Bin")
            }
            Button(onClick = onNavigateToSafeFolder) {
                Text("Safe Folder")
            }
        }
    }
}
