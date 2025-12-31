package com.example.filemanager

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.rememberNavController
import com.example.filemanager.ui.navigation.AppNavigation
import com.example.filemanager.ui.theme.FileManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FileManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Permission Check Logic
                    val context = LocalContext.current
                    var hasPermission by remember {
                        mutableStateOf(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                Environment.isExternalStorageManager()
                            } else {
                                true // Handle legacy permissions elsewhere if needed
                            }
                        )
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !hasPermission) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Full File Access Required")
                            Button(onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                    intent.addCategory("android.intent.category.DEFAULT")
                                    intent.data = Uri.parse(String.format("package:%s", context.packageName))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent()
                                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                                    context.startActivity(intent)
                                }
                            }) {
                                Text("Grant Permission")
                            }
                            
                            // Lifecycle observer to refresh state when returning from settings
                            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                            DisposableEffect(lifecycleOwner) {
                                val observer = LifecycleEventObserver { _, event ->
                                    if (event == Lifecycle.Event.ON_RESUME) {
                                        hasPermission = Environment.isExternalStorageManager()
                                    }
                                }
                                lifecycleOwner.lifecycle.addObserver(observer)
                                onDispose {
                                    lifecycleOwner.lifecycle.removeObserver(observer)
                                }
                            }
                        }
                    } else {
                        // Ideally checking READ_EXTERNAL_STORAGE here
                        val navController = rememberNavController()
                        AppNavigation(navController = navController)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ManageStoragePermissionHelper(content: @Composable () -> Unit) {
    var hasPermission by remember { mutableStateOf(Environment.isExternalStorageManager()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = Environment.isExternalStorageManager()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
    }

    if (hasPermission) {
        content()
    } else {
        PermissionRequestScreen()
    }
}

@Composable
fun PermissionRequestScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Device Storage Access Needed")
        Spacer(modifier = Modifier.height(16.dp))
        Text("This app needs full access to manage files on your device.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", context.packageName))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    context.startActivity(intent)
                }
            }
        }) {
            Text("Grant Permission")
        }
    }
}

