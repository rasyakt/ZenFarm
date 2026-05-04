package com.example.zenfarm.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.zenfarm.viewmodel.FarmViewModel
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

    val context = androidx.compose.ui.platform.LocalContext.current
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
            } catch (e: Exception) {
                // Optional: handle error
            }
        }
    }
    val isLoading by farmViewModel.isLoading.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tambah Anak") },
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
            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                label = { Text("Nama Anak") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tanggalLahir,
                onValueChange = { tanggalLahir = it },
                label = { Text("Tanggal Lahir (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Jenis Kelamin:", style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = jenisKelamin == "Jantan", 
                    onClick = { jenisKelamin = "Jantan" },
                    enabled = !isLoading
                )
                Text("Jantan")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = jenisKelamin == "Betina", 
                    onClick = { jenisKelamin = "Betina" },
                    enabled = !isLoading
                )
                Text("Betina")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = harga,
                onValueChange = { if (it.all { char -> char.isDigit() }) harga = it },
                label = { Text("Harga Modal") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hak Pembagian:", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                availableHakOptions.forEach { option ->
                    FilterChip(
                        selected = hakPembagian == option,
                        onClick = { hakPembagian = option },
                        label = { Text(option.replace("_", " ")) },
                        enabled = !isLoading && availableHakOptions.size > 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Pilih Induk Jantan (Opsional):", style = MaterialTheme.typography.labelMedium)
            Box {
                OutlinedButton(
                    onClick = { fatherDropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && availableFathers.isNotEmpty()
                ) {
                    val selectedName = availableFathers.find { it.hewanId == selectedJantanId }?.nama ?: "Tidak ada (Atau pilih Jantan)"
                    Text(selectedName)
                }
                DropdownMenu(
                    expanded = fatherDropdownExpanded,
                    onDismissRequest = { fatherDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    DropdownMenuItem(
                        text = { Text("Tidak ada / Kosong") },
                        onClick = { 
                            selectedJantanId = null
                            fatherDropdownExpanded = false
                        }
                    )
                    availableFathers.forEach { father ->
                        DropdownMenuItem(
                            text = { Text(father.nama) },
                            onClick = { 
                                selectedJantanId = father.hewanId
                                fatherDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            if (availableFathers.isEmpty()) {
                Text("Tidak ada jantan hidup yang tersedia.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    enabled = !isLoading
                ) {
                    Text("Pilih Foto Hewan")
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (fotoUri.isNotEmpty()) {
                    Text("Foto tersimpan", color = MaterialTheme.colorScheme.primary)
                }
            }
            
            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
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
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Menyimpan..." else "Simpan Anak")
            }
        }
    }
}

