package com.example.farmshop

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class AppViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Data Produk
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products
    private var allProductsBackup = listOf<Product>()

    // Data Keranjang
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    // Status UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess

    private val _isUserLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    // ROLE USER (Default: customer)
    private val _userRole = MutableStateFlow("customer")
    val userRole: StateFlow<String> = _userRole

    init {
        fetchProducts()
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _isUserLoggedIn.value = user != null
            if (user != null) {
                fetchUserRole() // Ambil role pas login
            }
        }
    }

    // --- 1. LOGIKA ROLE & AUTH ---
    private fun fetchUserRole() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Ambil role dari database, kalau ga ada anggap customer
                    val role = document.getString("role") ?: "customer"
                    _userRole.value = role
                    Log.d("FarmShop", "User Role: $role")
                }
            }
    }

    fun registerUser(email: String, pass: String, onSuccess: () -> Unit, onFail: (String) -> Unit) {
        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    // Simpan data user dengan role default "customer"
                    val userData = hashMapOf("email" to email, "role" to "customer")
                    db.collection("users").document(uid).set(userData)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            onSuccess()
                        }
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
                onFail(it.message ?: "Gagal")
            }
    }

    fun logout() {
        auth.signOut()
        _userRole.value = "customer" // Reset role
    }

    // --- 2. LOGIKA PRODUK & SEARCH ---
    private fun fetchProducts() {
        db.collection("products").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val list = snapshot.documents.map { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id) ?: Product()
                }
                allProductsBackup = list
                _products.value = list
            }
        }
    }

    fun searchProducts(query: String) {
        if (query.isEmpty()) {
            _products.value = allProductsBackup
        } else {
            _products.value = allProductsBackup.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
    }

    fun getProductById(productId: String): Product? {
        return allProductsBackup.find { it.id == productId }
    }

    // --- 3. LOGIKA KERANJANG ---
    fun listenToCart() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("cart")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _cartItems.value = snapshot.documents.map { doc ->
                        doc.toObject(CartItem::class.java) ?: CartItem()
                    }
                }
            }
    }

    fun addToCart(product: Product, onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val cartRef = db.collection("users").document(uid).collection("cart").document(product.id)

        cartRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentQty = document.getLong("quantity")?.toInt() ?: 1
                cartRef.update("quantity", currentQty + 1).addOnSuccessListener { onSuccess() }
            } else {
                val newItem = CartItem(product.id, product.name, product.price, product.imageUrl, 1)
                cartRef.set(newItem).addOnSuccessListener { onSuccess() }
            }
        }
    }

    fun updateCartQuantity(item: CartItem, change: Int) {
        val uid = auth.currentUser?.uid ?: return
        val newQty = item.quantity + change
        val cartRef = db.collection("users").document(uid).collection("cart").document(item.productId)

        if (newQty > 0) cartRef.update("quantity", newQty) else cartRef.delete()
    }

    fun calculateTotalPrice(): Double = _cartItems.value.sumOf { it.price * it.quantity }

    // --- 4. LOGIKA UPLOAD ---
    fun uploadProduct(name: String, price: Double, desc: String, imageUri: Uri?) {
        _isLoading.value = true
        if (imageUri != null) {
            val filename = UUID.randomUUID().toString()
            val ref = storage.reference.child("product_images/$filename")
            ref.putFile(imageUri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveToFirestore(name, price, desc, uri.toString())
                }
            }.addOnFailureListener { _isLoading.value = false }
        } else {
            saveToFirestore(name, price, desc, "")
        }
    }

    private fun saveToFirestore(name: String, price: Double, desc: String, imageUrl: String) {
        val newProduct = Product(name = name, price = price, description = desc, imageUrl = imageUrl)
        db.collection("products").add(newProduct)
            .addOnSuccessListener {
                _isLoading.value = false
                _uploadSuccess.value = true
            }
            .addOnFailureListener { _isLoading.value = false }
    }

    fun resetUploadState() {
        _uploadSuccess.value = false
    }
    // --- FITUR DELETE ---
    fun deleteProduct(productId: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        db.collection("products").document(productId)
            .delete()
            .addOnSuccessListener {
                _isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { _isLoading.value = false }
    }

    // --- FITUR EDIT ---
    fun updateProduct(productId: String, name: String, price: Double, desc: String, newImageUri: Uri?, oldImageUrl: String, onSuccess: () -> Unit) {
        _isLoading.value = true

        if (newImageUri != null) {
            // Jika user ganti foto, upload foto baru dulu
            val filename = UUID.randomUUID().toString()
            val ref = storage.reference.child("product_images/$filename")
            ref.putFile(newImageUri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    updateFirestore(productId, name, price, desc, uri.toString(), onSuccess)
                }
            }
        } else {
            // Jika tidak ganti foto, pakai URL lama
            updateFirestore(productId, name, price, desc, oldImageUrl, onSuccess)
        }
    }

    private fun updateFirestore(id: String, name: String, price: Double, desc: String, imageUrl: String, onSuccess: () -> Unit) {
        val updates = mapOf(
            "name" to name,
            "price" to price,
            "description" to desc,
            "imageUrl" to imageUrl
        )
        db.collection("products").document(id).update(updates)
            .addOnSuccessListener {
                _isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { _isLoading.value = false }
    }
}