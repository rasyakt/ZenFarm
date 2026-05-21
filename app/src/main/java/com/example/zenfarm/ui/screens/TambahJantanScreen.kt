package com.example.zenfarm.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.zenfarm.viewmodel.FarmViewModel
import com.example.zenfarm.ui.theme.standardTextFieldColors
import com.example.zenfarm.ui.theme.CardWhite
import com.example.zenfarm.ui.theme.TextSecondary
import com.example.zenfarm.ui.theme.FarmGreen
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahJantanScreen(
    silsilahId: String,
    indukBetinaId: String,
    farmViewModel: FarmViewModel,
    navController: NavController
) {
    var nama by remember { mutableStateOf("") }
    val jenisKelamin = "JANTAN"
    val status = "HIDUP"
    var harga by remember { mutableStateOf("0") }
    
    val currentHewans by farmViewModel.hewans.collectAsState()
    val availablePejantans = currentHewans.filter { 
        it.jenisKelamin.equals("JANTAN", ignoreCase = true) && 
        it.status.equals("HIDUP", ignoreCase = true) && 
        it.pasanganId == null
    }
    
    var selectedPejantanId by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    
    var hakPembagian by remember { 
        mutableStateOf("Pemilik")
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var fotoUri by remember { mutableStateOf("") } 
    
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = java.io.File(context.filesDir, "hewan_partner_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                fotoUri = file.absolutePath
            } catch (e: Exception) { }
        }
    }
    val isLoading by farmViewModel.isLoading.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tambah Pasangan (Jantan)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { if (!isLoading) navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text("Metode Penambahan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val options = listOf(
                            Triple("Baru", Icons.Rounded.AddCircle, Color(0xFF1976D2)),
                            Triple("Ambil", Icons.Rounded.CheckCircle, Color(0xFF2E7D32))
                        )
                        options.forEach { (label, icon, color) ->
                            val isSelected = if (label == "Baru") selectedPejantanId == null else selectedPejantanId != null
                            val isEmptyAmbil = (label == "Ambil" && availablePejantans.isEmpty())
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clickable {
                                        if (isLoading) return@clickable
                                        if (label == "Ambil" && availablePejantans.isNotEmpty()) {
                                            selectedPejantanId = availablePejantans.first().hewanId
                                            nama = availablePejantans.first().nama
                                        } else if (label == "Ambil" && availablePejantans.isEmpty()) {
                                            scope.launch { snackbarHostState.showSnackbar("Tidak ada pejantan yang tersedia") }
                                        } else {
                                            selectedPejantanId = null
                                            nama = ""
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) color.copy(alpha = 0.1f) else if (isEmptyAmbil) Color(0xFFF8FAFC) else Color.White
                                ),
                                border = BorderStroke(1.5.dp, if (isSelected) color else if (isEmptyAmbil) Color(0xFFE2E8F0) else Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = if (isEmptyAmbil) Color(0xFFCBD5E1) else if (isSelected) color else Color.Gray, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(label, color = if (isEmptyAmbil) Color(0xFF94A3B8) else if (isSelected) color else Color.Black, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                }
                            }
                        }
                    }

                    if (selectedPejantanId != null) {
                        Box {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading && availablePejantans.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                val sel = availablePejantans.find { it.hewanId == selectedPejantanId }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(sel?.nama ?: "Pilih Pejantan", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                                DropdownMenuItem(text = { Text("Input Pejantan Baru") }, onClick = { selectedPejantanId = null; dropdownExpanded = false })
                                availablePejantans.forEach { p ->
                                    DropdownMenuItem(text = { Text(p.nama, maxLines = 1, overflow = TextOverflow.Ellipsis) }, onClick = { 
                                        selectedPejantanId = p.hewanId
                                        nama = p.nama
                                        dropdownExpanded = false
                                    })
                                }
                            }
                        }
                        Text("Data (Harga, Foto, dll) akan menggunakan data pejantan terpilih.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Detail Pejantan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (selectedPejantanId == null) {
                         OutlinedTextField(
                             value = nama,
                             onValueChange = { nama = it },
                             label = { Text("Nama Pejantan Baru") },
                             leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                             modifier = Modifier.fillMaxWidth(),
                             enabled = !isLoading,
                             shape = RoundedCornerShape(12.dp),
                             colors = standardTextFieldColors()
                         )
                         OutlinedTextField(
                             value = harga,
                             onValueChange = { if (it.all { char -> char.isDigit() }) harga = it },
                             label = { Text("Harga Modal (Rp)") },
                             leadingIcon = { Icon(Icons.Rounded.ShoppingCart, contentDescription = null) },
                             modifier = Modifier.fillMaxWidth(),
                             enabled = !isLoading,
                             shape = RoundedCornerShape(12.dp),
                             colors = standardTextFieldColors()
                         )
                    } else {
                        val sel = availablePejantans.find { it.hewanId == selectedPejantanId }
                        DetailCardRow(Icons.Rounded.Person, "Nama", sel?.nama ?: "-", Color(0xFF1976D2))
                        DetailCardRow(Icons.Rounded.ShoppingCart, "Harga", "Rp ${sel?.harga ?: 0}", Color(0xFF1976D2))
                    }

                    Column {
                        Text("Hak Pembagian", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Pemilik", "Pengurus", "Bagi Dua").forEach { option ->
                                 FilterChip(
                                     selected = hakPembagian == option,
                                     onClick = { hakPembagian = option },
                                     label = { Text(option) },
                                     enabled = !isLoading,
                                     shape = RoundedCornerShape(8.dp),
                                     colors = FilterChipDefaults.filterChipColors(
                                         containerColor = CardWhite,
                                         labelColor = TextSecondary,
                                         selectedContainerColor = FarmGreen,
                                         selectedLabelColor = Color.White
                                     )
                                 )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Dokumentasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .border(2.dp, if (fotoUri.isNotEmpty()) Color(0xFF2E7D32) else Color(0xFFE2E8F0), CircleShape)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoUri.isNotEmpty()) {
                            AsyncImage(
                                model = fotoUri,
                                contentDescription = "Foto Terpilih",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(4.dp)
                            ) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Icon(Icons.Rounded.Pets, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        }
                    }
                    Text(if (fotoUri.isNotEmpty()) "Foto Terpilih" else "Pilih Foto Hewan", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (selectedPejantanId == null && nama.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Nama wajib diisi") }
                        return@Button
                    }
                    
                    farmViewModel.tambahPasangan(
                        context = context,
                        silsilahId = silsilahId,
                        indukBetinaId = indukBetinaId,
                        nama = nama,
                        status = status,
                        harga = harga.toIntOrNull() ?: 0,
                        hakPembagian = hakPembagian,
                        fotoUri = fotoUri,
                        ownershipSource = hakPembagian.uppercase().replace(" ", "_"),
                        existingJantanId = selectedPejantanId,
                        onSuccess = { 
                            scope.launch {
                                snackbarHostState.showSnackbar("Pasangan berhasil ditambahkan!")
                                navController.popBackStack() 
                            }
                        },
                        onError = { errorMsg ->
                            scope.launch { snackbarHostState.showSnackbar(errorMsg) }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && ((selectedPejantanId == null && nama.isNotBlank()) || selectedPejantanId != null),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Pasangan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TambahJantanDialog(
    silsilahId: String,
    indukBetinaId: String,
    farmViewModel: FarmViewModel,
    onDismiss: () -> Unit
) {
    var nama by remember { mutableStateOf("") }
    val jenisKelamin = "JANTAN"
    val status = "HIDUP"
    var harga by remember { mutableStateOf("0") }

    val currentHewans by farmViewModel.hewans.collectAsState()
    val availablePejantans = currentHewans.filter {
        it.jenisKelamin.equals("JANTAN", ignoreCase = true) &&
        it.status.equals("HIDUP", ignoreCase = true) &&
        it.pasanganId == null
    }

    var selectedPejantanId by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var hakPembagian by remember {
        mutableStateOf("Pemilik")
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var fotoUri by remember { mutableStateOf("") }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = java.io.File(context.filesDir, "hewan_partner_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                fotoUri = file.absolutePath
            } catch (e: Exception) { }
        }
    }
    val isLoading by farmViewModel.isLoading.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1976D2),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Tambah Pasangan", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                // Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // --- Section: Metode Penambahan ---
                    Text("Metode Penambahan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                val options = listOf(
                                    Triple("Baru", Icons.Rounded.AddCircle, Color(0xFF1976D2)),
                                    Triple("Ambil", Icons.Rounded.CheckCircle, Color(0xFF2E7D32))
                                )
                                options.forEach { (label, icon, color) ->
                                    val isSelected = if (label == "Baru") selectedPejantanId == null else selectedPejantanId != null
                                    val isEmptyAmbil = (label == "Ambil" && availablePejantans.isEmpty())
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clickable {
                                                if (isLoading) return@clickable
                                                if (label == "Ambil" && availablePejantans.isNotEmpty()) {
                                                    selectedPejantanId = availablePejantans.first().hewanId
                                                    nama = availablePejantans.first().nama
                                                } else if (label == "Ambil" && availablePejantans.isEmpty()) {
                                                    scope.launch { snackbarHostState.showSnackbar("Tidak ada pejantan yang tersedia") }
                                                } else {
                                                    selectedPejantanId = null
                                                    nama = ""
                                                }
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) color.copy(alpha = 0.1f) else if (isEmptyAmbil) Color(0xFFF8FAFC) else Color.White
                                        ),
                                        border = BorderStroke(1.5.dp, if (isSelected) color else if (isEmptyAmbil) Color(0xFFE2E8F0) else Color(0xFFE2E8F0))
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(icon, contentDescription = null, tint = if (isEmptyAmbil) Color(0xFFCBD5E1) else if (isSelected) color else Color.Gray, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(label, color = if (isEmptyAmbil) Color(0xFF94A3B8) else if (isSelected) color else Color.Black, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                        }
                                    }
                                }
                            }

                            if (selectedPejantanId != null) {
                                Box {
                                    OutlinedButton(
                                        onClick = { dropdownExpanded = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isLoading && availablePejantans.isNotEmpty(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        val sel = availablePejantans.find { it.hewanId == selectedPejantanId }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(sel?.nama ?: "Pilih Pejantan", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                                        DropdownMenuItem(text = { Text("Input Pejantan Baru") }, onClick = { selectedPejantanId = null; dropdownExpanded = false })
                                        availablePejantans.forEach { p ->
                                            DropdownMenuItem(text = { Text(p.nama, maxLines = 1, overflow = TextOverflow.Ellipsis) }, onClick = { selectedPejantanId = p.hewanId; nama = p.nama; dropdownExpanded = false })
                                        }
                                    }
                                }
                                Text("Data (Harga, Foto, dll) akan menggunakan data pejantan terpilih.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- Section: Detail Pejantan ---
                    Text("Detail Pejantan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (selectedPejantanId == null) {
                                 OutlinedTextField(
                                     value = nama,
                                     onValueChange = { nama = it },
                                     label = { Text("Nama Pejantan Baru") },
                                     leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                                     modifier = Modifier.fillMaxWidth(),
                                     enabled = !isLoading,
                                     shape = RoundedCornerShape(12.dp),
                                     colors = standardTextFieldColors()
                                 )
                                 OutlinedTextField(
                                     value = harga,
                                     onValueChange = { if (it.all { char -> char.isDigit() }) harga = it },
                                     label = { Text("Harga Modal (Rp)") },
                                     leadingIcon = { Icon(Icons.Rounded.ShoppingCart, contentDescription = null) },
                                     modifier = Modifier.fillMaxWidth(),
                                     enabled = !isLoading,
                                     shape = RoundedCornerShape(12.dp),
                                     colors = standardTextFieldColors()
                                 )
                            } else {
                                val sel = availablePejantans.find { it.hewanId == selectedPejantanId }
                                DetailCardRow(Icons.Rounded.Person, "Nama", sel?.nama ?: "-", Color(0xFF1976D2))
                                DetailCardRow(Icons.Rounded.ShoppingCart, "Harga", "Rp ${sel?.harga ?: 0}", Color(0xFF1976D2))
                            }

                            Column {
                                Text("Hak Pembagian", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Pemilik", "Pengurus", "Bagi Dua").forEach { option ->
                                         FilterChip(
                                             selected = hakPembagian == option,
                                             onClick = { hakPembagian = option },
                                             label = { Text(option) },
                                             enabled = !isLoading,
                                             shape = RoundedCornerShape(8.dp),
                                             colors = FilterChipDefaults.filterChipColors(
                                                 containerColor = CardWhite,
                                                 labelColor = TextSecondary,
                                                 selectedContainerColor = FarmGreen,
                                                 selectedLabelColor = Color.White
                                             )
                                         )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- Section: Dokumentasi ---
                    Text("Dokumentasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .border(2.dp, if (fotoUri.isNotEmpty()) Color(0xFF2E7D32) else Color(0xFFE2E8F0), CircleShape)
                                    .clickable { launcher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (fotoUri.isNotEmpty()) {
                                    AsyncImage(
                                        model = fotoUri,
                                        contentDescription = "Foto Terpilih",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)).padding(4.dp)
                                    ) {
                                        Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    Icon(Icons.Rounded.Pets, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                }
                            }
                            Text(if (fotoUri.isNotEmpty()) "Foto Terpilih" else "Pilih Foto Hewan", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (selectedPejantanId == null && nama.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Nama wajib diisi") }
                                return@Button
                            }

                            farmViewModel.tambahPasangan(
                                context = context,
                                silsilahId = silsilahId,
                                indukBetinaId = indukBetinaId,
                                nama = nama,
                                status = status,
                                harga = harga.toIntOrNull() ?: 0,
                                hakPembagian = hakPembagian,
                                fotoUri = fotoUri,
                                ownershipSource = hakPembagian.uppercase().replace(" ", "_"),
                                existingJantanId = selectedPejantanId,
                                onSuccess = {
                                    scope.launch {
                                        onDismiss()
                                    }
                                },
                                onError = { errorMsg ->
                                    scope.launch { snackbarHostState.showSnackbar(errorMsg) }
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isLoading && ((selectedPejantanId == null && nama.isNotBlank()) || selectedPejantanId != null),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simpan Pasangan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Snackbar
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}


@Composable
private fun DetailCardRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
