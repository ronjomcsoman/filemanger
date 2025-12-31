package com.example.filemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.filemanager.ui.screens.HomeScreen
import com.example.filemanager.ui.screens.FileExplorerScreen

import com.example.filemanager.ui.screens.SafeFolderScreen
import com.example.filemanager.ui.screens.RecycleBinScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object FileExplorer : Screen("file_explorer")
    object RecycleBin : Screen("recycle_bin")
    object SafeFolder : Screen("safe_folder")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToFileExplorer = { navController.navigate(Screen.FileExplorer.route) },
                onNavigateToRecycleBin = { navController.navigate(Screen.RecycleBin.route) },
                onNavigateToSafeFolder = { navController.navigate(Screen.SafeFolder.route) }
            )
        }
        composable(Screen.FileExplorer.route) {
            FileExplorerScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.RecycleBin.route) {
            RecycleBinScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SafeFolder.route) {
            SafeFolderScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
