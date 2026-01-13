package com.example.farmshop

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale

// --- CART SCREEN ---
@Composable
fun CartScreen(vm: AppViewModel) {
    val items by vm.cartItems.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.listenToCart() }

    Scaffold(
        containerColor = FarmLight,
        bottomBar = {
            if(items.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), elevation = CardDefaults.cardElevation(10.dp)) {
                    Row(Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(NumberFormat.getCurrencyInstance(Locale("id","ID")).format(vm.calculateTotalPrice()), color = FarmText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Button(onClick = {
                            val phone = "6285173342484"
                            var msg = "Halo, saya mau pesan:\n"
                            items.forEach { msg += "- ${it.name} (${it.quantity}x)\n" }
                            msg += "\nTotal: ${NumberFormat.getCurrencyInstance(Locale("id","ID")).format(vm.calculateTotalPrice())}"
                            val url = "https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(msg)}"
                            try { context.startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }) } catch (e: Exception) {}
                        }, colors = ButtonDefaults.buttonColors(containerColor = FarmGreen), shape = RoundedCornerShape(12.dp)) { Text("Checkout WA") }
                    }
                }
            }
        }
    ) { p ->
        if(items.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Keranjang Kosong", color = FarmGreen) }
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(p)) {
            items(items) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, color = FarmText)
                            Text(NumberFormat.getCurrencyInstance(Locale("id","ID")).format(item.price), fontSize = 12.sp, color = FarmAccent)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { vm.updateCartQuantity(item, -1) }, modifier = Modifier.size(30.dp).background(FarmLight, RoundedCornerShape(4.dp))) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp), tint = FarmGreen) }
                            Text("${item.quantity}", modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { vm.updateCartQuantity(item, 1) }, modifier = Modifier.size(30.dp).background(FarmGreen, RoundedCornerShape(4.dp))) { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color.White) }
                        }
                    }
                }
            }
        }
    }
}

// --- FARM PROFILE SCREEN MODERN ---
@Composable
fun ProfileScreen(nav: NavController, vm: AppViewModel) {
    val user = vm.auth.currentUser
    val role by vm.userRole.collectAsState()
    val username = user?.email?.split("@")?.get(0)?.replaceFirstChar { it.uppercase() } ?: "Petani"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FarmLight)
            .verticalScroll(rememberScrollState())
    ) {
        // HEADER ALAM
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(listOf(FarmGreen, Color(0xFF66BB6A))),
                    shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
                .padding(bottom = 60.dp, top = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .background(Color.White.copy(0.2f), CircleShape)
                        .clip(CircleShape)
                ) {
                    Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(80.dp), tint = Color.White)
                }
                Spacer(Modifier.height(12.dp))
                Text(username, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(user?.email ?: "", fontSize = 14.sp, color = Color(0xFFE8F5E9))
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = if (role == "admin") Color.Red else FarmAccent,
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        if (role == "admin") "Admin Toko" else "Member Setia",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp
                    )
                }
            }
        }

        // MENU
        Column(Modifier.padding(horizontal = 24.dp).offset(y = (-20).dp)) {
            Button(
                onClick = { vm.logout(); nav.navigate(Screen.Login.route) { popUpTo(0) } },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Keluar", fontWeight = FontWeight.Bold) }
        }
    }
}

// --- HELPER COMPONENTS ---
@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = FarmAccent, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = FarmText)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ProfileOptionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = FarmGreen, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, Modifier.weight(1f), color = Color.Black)
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(Screen.Home, Screen.Cart, Screen.Profile)
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon!!, null) },
                label = { Text(screen.title!!) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FarmGreen,
                    selectedTextColor = FarmGreen,
                    indicatorColor = FarmLight
                )
            )
        }
    }
}