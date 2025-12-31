package com.example.filemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.filemanager.ui.screens.HomeScreen
import com.example.filemanager.ui.screens.FileExplorerScreen

import com.example.filemanager.ui.screens.SafeFolderScreen
import com.example.filemanager.ui.screens.RecycleBinScreen

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object FileExplorer : Screen("file_explorer?path={path}") {
        fun createRoute(path: String? = null) = if (path != null) "file_explorer?path=$path" else "file_explorer"
    }
    object RecycleBin : Screen("recycle_bin")
    object SafeFolder : Screen("safe_folder")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToFileExplorer = { path -> navController.navigate(Screen.FileExplorer.createRoute(path)) },
                onNavigateToRecycleBin = { navController.navigate(Screen.RecycleBin.route) },
                onNavigateToSafeFolder = { navController.navigate(Screen.SafeFolder.route) }
            )
        }
        composable(
            route = Screen.FileExplorer.route,
            arguments = listOf(navArgument("path") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path")
            FileExplorerScreen(
                initialPath = path,
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
