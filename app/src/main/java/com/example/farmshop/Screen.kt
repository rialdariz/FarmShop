package com.example.farmshop
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home", "Beranda", Icons.Default.Home)
    object Cart : Screen("cart", "Keranjang", Icons.Default.ShoppingCart)
    object Profile : Screen("profile", "Profil", Icons.Default.Person)
    object AddProduct : Screen("add_product")
    object Detail : Screen("detail/{productId}") { fun createRoute(id: String) = "detail/$id" }

    object EditProduct : Screen("edit/{productId}") {
        fun createRoute(id: String) = "edit/$id"
    }
}