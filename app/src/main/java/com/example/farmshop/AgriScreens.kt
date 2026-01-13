package com.example.farmshop

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale
import java.net.URLEncoder

// --- PALET WARNA FARM ---
val FarmGreen = Color(0xFF2E7D32)     // Hijau Tua (Daun)
val FarmLight = Color(0xFFE8F5E9)     // Hijau Sangat Muda (Background)
val FarmAccent = Color(0xFFFF9800)    // Oranye (Buah/Panen)
val FarmText = Color(0xFF1B5E20)      // Teks Hijau Gelap

// --- SEARCH BAR MODERN ---
@Composable
fun ModernSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(55.dp),
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Cari sayur, buah, pupuk...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = FarmGreen) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// --- HOME SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val user = viewModel.auth.currentUser
    val username = user?.email?.split("@")?.get(0)?.replaceFirstChar { it.uppercase() } ?: "Sobat Tani"

    Scaffold(
        containerColor = FarmLight, // Background segar
        floatingActionButton = {
            if (userRole == "admin") {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddProduct.route) },
                    containerColor = FarmGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Jual", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // --- HEADER SECTION ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(listOf(FarmGreen, Color(0xFF43A047))),
                        shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Eco, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("AgriStore", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Halo, $username! 🌾", color = Color(0xFFC8E6C9), fontSize = 16.sp)
                    Spacer(Modifier.height(24.dp))

                    ModernSearchBar(
                        query = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                            viewModel.searchProducts(it)
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- CONTENT SECTION ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hasil Panen Terbaru", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = FarmText)
                Icon(Icons.Default.FilterList, null, tint = FarmGreen)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(products) { product ->
                    FarmProductCard(product) { navController.navigate(Screen.Detail.createRoute(product.id)) }
                }
            }
        }
    }
}

@Composable
fun FarmProductCard(product: Product, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column {
            Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.height(130.dp).fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                // Badge Segar
                Surface(
                    color = FarmAccent,
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        "Segar",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1, color = Color.Black, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(formatRupiah(product.price), color = FarmGreen, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))

                // Tombol Mini Action
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                    Text(" 4.8", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

// --- DETAIL SCREEN MODERN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, viewModel: AppViewModel, productId: String) {
    val product = viewModel.getProductById(productId)
    val userRole by viewModel.userRole.collectAsState() // Cek Role Admin
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialog Konfirmasi Hapus
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Produk?") },
            text = { Text("Produk ini akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProduct(productId) {
                        showDeleteDialog = false
                        Toast.makeText(context, "Produk dihapus", Toast.LENGTH_SHORT).show()
                        navController.popBackStack() // Kembali ke Home
                    }
                }) { Text("Hapus", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    // --- TOMBOL KHUSUS ADMIN ---
                    if (userRole == "admin") {
                        IconButton(onClick = { navController.navigate(Screen.EditProduct.createRoute(productId)) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Blue)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (product != null) {
                // ... (Kode BottomBar sama seperti sebelumnya) ...
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color.White), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.addToCart(product) { Toast.makeText(context, "Masuk Keranjang 🛒", Toast.LENGTH_SHORT).show() } },
                        modifier = Modifier.weight(1f).height(50.dp),
                        border = BorderStroke(1.dp, FarmGreen)
                    ) { Text("+ Keranjang", color = FarmGreen) }

                    Button(
                        onClick = {
                            val phoneNumber = "6281234567890" // GANTI DENGAN NOMOR HP ANDA

                            // 2. Buat Pesan Otomatis
                            val message = "Halo Admin AgriStore 👋,\n" +
                                    "Saya tertarik membeli produk:\n\n" +
                                    "🌾 *${product.name}*\n" +
                                    "💰 Harga: ${formatRupiah(product.price)}\n\n" +
                                    "Apakah stok masih tersedia?"

                            // 3. Encode pesan agar bisa dibaca URL & Buka WhatsApp
                            try {
                                val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${URLEncoder.encode(message, "UTF-8")}"
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(url)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp tidak terinstall atau terjadi kesalahan.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
                    ) { Text("Beli (WA)", color = Color.White) }
                }
            }
        }
    ) { padding ->
        if (product != null) {
            Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
                AsyncImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(300.dp), contentScale = ContentScale.Crop)
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(product.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(formatRupiah(product.price), style = MaterialTheme.typography.headlineSmall, color = Color(0xFF388E3C))
                    Spacer(Modifier.height(16.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))
                    Text("Deskripsi", fontWeight = FontWeight.Bold)
                    Text(product.description)
                }
            }
        } else { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Produk tidak ditemukan") } }
    }
}

// --- ADD PRODUCT (Clean) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, viewModel: AppViewModel) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    val uploadSuccess by viewModel.uploadSuccess.collectAsState()
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri = it }

    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            viewModel.resetUploadState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Tambah Produk Tani") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp).verticalScroll(rememberScrollState())) {
            Card(
                modifier = Modifier.fillMaxWidth().height(220.dp).clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                colors = CardDefaults.cardColors(containerColor = FarmLight),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, FarmGreen)
            ) {
                if (imageUri != null) AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AddPhotoAlternate, null, tint = FarmGreen, modifier = Modifier.size(40.dp)); Text("Foto Produk", color = FarmGreen) } }
            }
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Hasil Panen") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga per Kg/Satuan") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi Kualitas") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 5, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(32.dp))
            Button(onClick = { if (name.isNotEmpty() && price.isNotEmpty() && imageUri != null) viewModel.uploadProduct(name, price.toDoubleOrNull() ?: 0.0, desc, imageUri) }, modifier = Modifier.fillMaxWidth().height(50.dp), enabled = !isLoading, colors = ButtonDefaults.buttonColors(containerColor = FarmGreen), shape = RoundedCornerShape(12.dp)) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Upload Produk")
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(navController: NavController, viewModel: AppViewModel, productId: String) {
    val product = viewModel.getProductById(productId)

    // State untuk form (diisi data lama dulu)
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var desc by remember { mutableStateOf(product?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) } // Uri baru (lokal)

    val isLoading by viewModel.isLoading.collectAsState()
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri = it }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Edit Produk") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            // Area Foto
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp).clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {
                if (imageUri != null) {
                    // Tampilkan foto baru yg dipilih
                    AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    // Tampilkan foto lama dari internet
                    AsyncImage(model = product?.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Produk") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth().height(100.dp))
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotEmpty() && price.isNotEmpty()) {
                        viewModel.updateProduct(
                            productId = productId,
                            name = name,
                            price = price.toDoubleOrNull() ?: 0.0,
                            desc = desc,
                            newImageUri = imageUri,
                            oldImageUrl = product?.imageUrl ?: "",
                            onSuccess = {
                                navController.popBackStack() // Kembali ke Detail
                                navController.popBackStack() // Kembali ke Home (opsional, biar refresh)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White) else Text("Simpan Perubahan")
            }
        }
    }
}
fun formatRupiah(number: Double): String = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(number)