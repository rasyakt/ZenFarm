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
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
fun TambahAnakScreen(
    silsilahId: String,
    parentId: String?,
    farmViewModel: FarmViewModel,
    navController: NavController
) {
    var nama by remember { mutableStateOf("") }
    var jenisKelamin by remember { mutableStateOf("Jantan") }
    var harga by remember { mutableStateOf("0") }
    var tanggalLahir by remember { mutableStateOf("2024-01-01") }
    
    val currentHewans by farmViewModel.hewans.collectAsState()
    val availableFathers = currentHewans.filter { 
        it.jenisKelamin.equals("JANTAN", ignoreCase = true) && 
        it.status.equals("HIDUP", ignoreCase = true) && 
        it.hewanId != parentId &&
        (it.pasanganId == null || it.pasanganId == parentId)
    }
    var selectedJantanId by remember { mutableStateOf<String?>(null) }
    var fatherDropdownExpanded by remember { mutableStateOf(false) }
    val mother = currentHewans.find { it.hewanId == parentId }
    val availableHakOptions = farmViewModel.getAvailableHakPembagian(mother, currentHewans)
    
    var hakPembagian by remember { 
        mutableStateOf(availableHakOptions.firstOrNull() ?: "Pemilik")
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
                val file = java.io.File(context.filesDir, "hewan_${System.currentTimeMillis()}.jpg")
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
                title = { Text("Tambah Anak Baru", fontWeight = FontWeight.Bold) },
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
            Text("Identitas Anak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                     OutlinedTextField(
                         value = nama,
                         onValueChange = { nama = it },
                         label = { Text("Nama Anak") },
                         leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                         modifier = Modifier.fillMaxWidth(),
                         enabled = !isLoading,
                         shape = RoundedCornerShape(12.dp),
                         colors = standardTextFieldColors()
                     )
                     
                     OutlinedTextField(
                         value = tanggalLahir,
                         onValueChange = { tanggalLahir = it },
                         label = { Text("Tanggal Lahir") },
                         placeholder = { Text("YYYY-MM-DD") },
                         leadingIcon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null) },
                         modifier = Modifier.fillMaxWidth(),
                         enabled = !isLoading,
                         shape = RoundedCornerShape(12.dp),
                         colors = standardTextFieldColors()
                     )

                    Column {
                        Text("Jenis Kelamin", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val genderOptions = listOf(
                                Triple("Jantan", Icons.Rounded.Male, Color(0xFF1976D2)),
                                Triple("Betina", Icons.Rounded.Female, Color(0xFFD81B60))
                            )
                            genderOptions.forEach { (label, icon, color) ->
                                val isSelected = jenisKelamin == label
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clickable { if (!isLoading) jenisKelamin = label },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) color.copy(alpha = 0.1f) else Color.White
                                    ),
                                    border = BorderStroke(1.5.dp, if (isSelected) color else Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(icon, contentDescription = null, tint = if (isSelected) color else Color.Gray, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(label, color = if (isSelected) color else Color.Black, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Keuangan & Kepemilikan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

                     Column {
                         Text("Hak Pembagian", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                             availableHakOptions.forEach { option ->
                                 FilterChip(
                                     selected = hakPembagian == option,
                                     onClick = { hakPembagian = option },
                                     label = { Text(option.replace("_", " ")) },
                                     enabled = !isLoading && availableHakOptions.size > 1,
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

            Text("Silsilah & Dokumentasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("Induk Jantan (Opsional)", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                        Box {
                            OutlinedButton(
                                onClick = { fatherDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading && availableFathers.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                val selectedName = availableFathers.find { it.hewanId == selectedJantanId }?.nama ?: "Pilih Induk Jantan"
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(selectedName)
                                }
                            }
                            DropdownMenu(
                                expanded = fatherDropdownExpanded,
                                onDismissRequest = { fatherDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Tidak ada (Kosong)") },
                                    onClick = { selectedJantanId = null; fatherDropdownExpanded = false }
                                )
                                availableFathers.forEach { father ->
                                    DropdownMenuItem(
                                        text = { Text(father.nama) },
                                        onClick = { selectedJantanId = father.hewanId; fatherDropdownExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Foto Hewan", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(8.dp))
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
                        if (fotoUri.isNotEmpty()) {
                            Text("Foto siap digunakan", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (nama.isBlank() || tanggalLahir.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Nama dan Tanggal Lahir wajib diisi") }
                        return@Button
                    }
                    if (parentId == null) {
                        scope.launch { snackbarHostState.showSnackbar("Parent ID tidak valid") }
                        return@Button
                    }
                    
                    farmViewModel.tambahAnak(
                        context = context,
                        silsilahId = silsilahId,
                        nama = nama,
                        jenisKelamin = jenisKelamin,
                        harga = harga.toIntOrNull() ?: 0,
                        hakPembagian = hakPembagian,
                        fotoUri = fotoUri,
                        tanggalLahirStr = tanggalLahir,
                        indukBetinaId = parentId,
                        indukJantanId = selectedJantanId,
                        ownershipSource = hakPembagian.uppercase().replace(" ", "_"),
                        onSuccess = { 
                            scope.launch {
                                snackbarHostState.showSnackbar("Anak berhasil ditambahkan!")
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
                enabled = !isLoading,
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
                        Text("Simpan Data Anak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TambahAnakDialog(
    silsilahId: String,
    parentId: String?,
    farmViewModel: FarmViewModel,
    onDismiss: () -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var jenisKelamin by remember { mutableStateOf("Jantan") }
    var harga by remember { mutableStateOf("0") }
    var tanggalLahir by remember { mutableStateOf("2024-01-01") }

    val currentHewans by farmViewModel.hewans.collectAsState()
    val availableFathers = currentHewans.filter {
        it.jenisKelamin.equals("JANTAN", ignoreCase = true) &&
        it.status.equals("HIDUP", ignoreCase = true) &&
        it.hewanId != parentId &&
        (it.pasanganId == null || it.pasanganId == parentId)
    }
    var selectedJantanId by remember { mutableStateOf<String?>(null) }
    var fatherDropdownExpanded by remember { mutableStateOf(false) }
    val mother = currentHewans.find { it.hewanId == parentId }
    val availableHakOptions = farmViewModel.getAvailableHakPembagian(mother, currentHewans)

    var hakPembagian by remember {
        mutableStateOf(availableHakOptions.firstOrNull() ?: "Pemilik")
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
                val file = java.io.File(context.filesDir, "hewan_${System.currentTimeMillis()}.jpg")
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF2E7D32),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Pets, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Tambah Anak Baru", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = "Tutup", tint = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text("Identitas Anak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                             OutlinedTextField(
                                 value = nama,
                                 onValueChange = { nama = it },
                                 label = { Text("Nama Anak") },
                                 leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                                 modifier = Modifier.fillMaxWidth(),
                                 enabled = !isLoading,
                                 shape = RoundedCornerShape(12.dp),
                                 colors = standardTextFieldColors()
                             )
                             OutlinedTextField(
                                 value = tanggalLahir,
                                 onValueChange = { tanggalLahir = it },
                                 label = { Text("Tanggal Lahir") },
                                 placeholder = { Text("YYYY-MM-DD") },
                                 leadingIcon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null) },
                                 modifier = Modifier.fillMaxWidth(),
                                 enabled = !isLoading,
                                 shape = RoundedCornerShape(12.dp),
                                 colors = standardTextFieldColors()
                             )
                            Column {
                                Text("Jenis Kelamin", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    val genderOptions = listOf(
                                        Triple("Jantan", Icons.Rounded.Male, Color(0xFF1976D2)),
                                        Triple("Betina", Icons.Rounded.Female, Color(0xFFD81B60))
                                    )
                                    genderOptions.forEach { (label, icon, color) ->
                                        val isSelected = jenisKelamin == label
                                        Card(
                                            modifier = Modifier.weight(1f).height(52.dp).clickable { if (!isLoading) jenisKelamin = label },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = if (isSelected) color.copy(alpha = 0.1f) else Color.White),
                                            border = BorderStroke(1.5.dp, if (isSelected) color else Color(0xFFE2E8F0))
                                        ) {
                                            Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                                Icon(icon, contentDescription = null, tint = if (isSelected) color else Color.Gray, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(label, color = if (isSelected) color else Color.Black, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Keuangan & Kepemilikan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                             Column {
                                 Text("Hak Pembagian", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                     availableHakOptions.forEach { option ->
                                         FilterChip(
                                             selected = hakPembagian == option,
                                             onClick = { hakPembagian = option },
                                             label = { Text(option.replace("_", " ")) },
                                             enabled = !isLoading && availableHakOptions.size > 1,
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

                    Text("Silsilah & Dokumentasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("Induk Jantan (Opsional)", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                                Box {
                                    OutlinedButton(
                                        onClick = { fatherDropdownExpanded = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isLoading && availableFathers.isNotEmpty(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        val selectedName = availableFathers.find { it.hewanId == selectedJantanId }?.nama ?: "Pilih Induk Jantan"
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(selectedName)
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = fatherDropdownExpanded,
                                        onDismissRequest = { fatherDropdownExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.9f)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Tidak ada (Kosong)") },
                                            onClick = { selectedJantanId = null; fatherDropdownExpanded = false }
                                        )
                                        availableFathers.forEach { father ->
                                            DropdownMenuItem(
                                                text = { Text(father.nama) },
                                                onClick = { selectedJantanId = father.hewanId; fatherDropdownExpanded = false }
                                            )
                                        }
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Foto Hewan", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                                Spacer(modifier = Modifier.height(8.dp))
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
                                        AsyncImage(model = fotoUri, contentDescription = "Foto Terpilih", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        Box(Modifier.align(Alignment.BottomEnd).padding(4.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)).padding(4.dp)) {
                                            Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    } else {
                                        Icon(Icons.Rounded.Pets, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                    }
                                }
                                if (fotoUri.isNotEmpty()) {
                                    Text("Foto siap digunakan", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (nama.isBlank() || tanggalLahir.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Nama dan Tanggal Lahir wajib diisi") }
                                return@Button
                            }
                            if (parentId == null) {
                                scope.launch { snackbarHostState.showSnackbar("Parent ID tidak valid") }
                                return@Button
                            }
                            farmViewModel.tambahAnak(
                                context = context, silsilahId = silsilahId, nama = nama, jenisKelamin = jenisKelamin,
                                harga = harga.toIntOrNull() ?: 0, hakPembagian = hakPembagian, fotoUri = fotoUri,
                                tanggalLahirStr = tanggalLahir, indukBetinaId = parentId, indukJantanId = selectedJantanId,
                                ownershipSource = hakPembagian.uppercase().replace(" ", "_"),
                                onSuccess = { scope.launch { onDismiss() } },
                                onError = { errorMsg -> scope.launch { snackbarHostState.showSnackbar(errorMsg) } }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simpan Data Anak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
