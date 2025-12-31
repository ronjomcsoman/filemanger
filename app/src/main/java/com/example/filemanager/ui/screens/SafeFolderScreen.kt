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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedButton
import com.example.filemanager.utils.BiometricHelper
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
                val context = LocalContext.current
                val activity = context as? FragmentActivity

                LaunchedEffect(isLocked, hasPin) {
                    if (isLocked && hasPin && activity != null && BiometricHelper.isBiometricAvailable(context)) {
                        BiometricHelper.authenticate(
                            activity,
                            onSuccess = { viewModel.unlockBiometric() },
                            onError = { error = "Biometric Error: $it" }
                        )
                    }
                }

                if (!hasPin) {
                    Text("Set a PIN for Safe Folder", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2196F3))
                } else {
                    Text("Enter PIN", style = MaterialTheme.typography.headlineSmall)
                }
                
                Spacer(Modifier.height(32.dp))

                // PIN Visual Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    repeat(4) { index ->
                        val isFilled = pin.length > index
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    if (isFilled) Color(0xFF2196F3) else Color.LightGray,
                                    androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }

                if (error.isNotEmpty()) {
                    Text(error, color = Color.Red, modifier = Modifier.padding(bottom = 16.dp))
                }
                
                // Numeric Keypad
                val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "OK")
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                    modifier = Modifier.width(280.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(keys.size) { index ->
                        val key = keys[index]
                        androidx.compose.material3.FilledTonalButton(
                            onClick = {
                                error = ""
                                when (key) {
                                    "C" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                    "OK" -> {
                                        if (pin.length < 4) {
                                            error = "PIN must be 4 digits"
                                        } else {
                                            if (!hasPin) {
                                                viewModel.setPin(pin)
                                            } else {
                                                if (!viewModel.unlock(pin)) {
                                                    error = "Incorrect PIN"
                                                    pin = ""
                                                }
                                            }
                                        }
                                    }
                                    else -> if (pin.length < 4) pin += key
                                }
                            },
                            modifier = Modifier.size(64.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (key == "OK") Color(0xFF4CAF50) else Color(0xFFF5F5F5),
                                contentColor = if (key == "OK") Color.White else Color.Black
                            )
                        ) {
                            Text(key, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }

                if (hasPin && activity != null && BiometricHelper.isBiometricAvailable(context)) {
                    Spacer(Modifier.height(24.dp))
                    TextButton(
                        onClick = {
                            BiometricHelper.authenticate(
                                activity,
                                onSuccess = { viewModel.unlockBiometric() },
                                onError = { error = "Biometric Error: $it" }
                            )
                        }
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Use Biometrics")
                    }
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
