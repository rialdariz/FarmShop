package com.example.farmshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: AppViewModel = viewModel()

            // Cek apakah user sedang di halaman login/register agar Navbar disembunyikan
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.Cart.route, Screen.Profile.route)

            // Tentukan start destination berdasarkan status login
            val startDest = if (viewModel.auth.currentUser != null) Screen.Home.route else Screen.Login.route

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        BottomNavigationBar(navController)
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = startDest,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    // --- AUTH ---
                    composable(Screen.Login.route) { LoginScreen(navController, viewModel) }
                    composable(Screen.Register.route) { RegisterScreen(navController, viewModel) }

                    // --- MAIN TABS ---
                    composable(Screen.Home.route) { HomeScreen(navController, viewModel) }
                    composable(Screen.Cart.route) { CartScreen(viewModel) }
                    composable(Screen.Profile.route) { ProfileScreen(navController, viewModel) }

                    // --- FEATURES ---
                    composable(Screen.AddProduct.route) { AddProductScreen(navController, viewModel) }
                    composable(
                        Screen.EditProduct.route,
                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
                    ) {
                        val productId = it.arguments?.getString("productId") ?: ""
                        EditProductScreen(navController, viewModel, productId)
                    }

                    composable(
                        route = Screen.Detail.route,
                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId") ?: ""
                        DetailScreen(navController, viewModel, productId)
                    }
                }
            }
        }
    }
}