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
    object FileExplorer : Screen("file_explorer?path={path}&category={category}") {
        fun createRoute(path: String? = null, category: String? = null) = buildString {
            append("file_explorer")
            val params = mutableListOf<String>()
            path?.let { params.add("path=${java.net.URLEncoder.encode(it, "UTF-8")}") }
            category?.let { params.add("category=$it") }
            if (params.isNotEmpty()) {
                append("?")
                append(params.joinToString("&"))
            }
        }
    }
    object RecycleBin : Screen("recycle_bin")
    object SafeFolder : Screen("safe_folder")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToFileExplorer = { path -> navController.navigate(Screen.FileExplorer.createRoute(path)) },
                onNavigateToRecycleBin = { navController.navigate(Screen.RecycleBin.route) },
                onNavigateToSafeFolder = { navController.navigate(Screen.SafeFolder.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToCategory = { category -> 
                    navController.navigate(Screen.FileExplorer.createRoute(category = category))
                }
            )
        }
        composable(
            arguments = listOf(
                navArgument("path") { type = NavType.StringType; nullable = true },
                navArgument("category") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path")
            val category = backStackEntry.arguments?.getString("category")
            FileExplorerScreen(
                initialPath = path,
                category = category,
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
        composable(Screen.Settings.route) {
            com.example.filemanager.ui.screens.SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
