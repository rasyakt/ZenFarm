package com.example.farmflow.ui.screens

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
import com.example.farmflow.viewmodel.FarmViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahJantanScreen(
    silsilahId: String,
    indukBetinaId: String,
    farmViewModel: FarmViewModel,
    navController: NavController
) {
    var nama by remember { mutableStateOf("") }
    // Fixed UI components
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

    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.filesDir, "hewan_partner_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                fotoUri = file.absolutePath
            } catch (e: Exception) {
                // error fallback
            }
        }
    }
    val isLoading by farmViewModel.isLoading.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tambah Pasangan (Jantan)") },
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
            Text("Cara Penambahan:", style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedPejantanId == null, onClick = { selectedPejantanId = null })
                Text("Input Pejantan Baru")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = selectedPejantanId != null, onClick = { 
                    if (availablePejantans.isNotEmpty()) {
                        selectedPejantanId = availablePejantans.first().hewanId
                        nama = availablePejantans.first().nama
                    }
                }, enabled = availablePejantans.isNotEmpty())
                Text("Ambil dari Anak Jantan")
            }
            
            if (selectedPejantanId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedButton(onClick = { dropdownExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        val sel = availablePejantans.find { it.hewanId == selectedPejantanId }
                        Text(sel?.nama ?: "Pilih Pejantan")
                    }
                    DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                        availablePejantans.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.nama) },
                                onClick = { 
                                    selectedPejantanId = p.hewanId
                                    nama = p.nama
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Text("Data (Harga, Foto, dll) akan menggunakan data anak tersebut.", style = MaterialTheme.typography.bodySmall)
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Pejantan Baru") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = harga,
                    onValueChange = { if (it.all { char -> char.isDigit() }) harga = it },
                    label = { Text("Harga Modal") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hak Pembagian:", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Pemilik", "Pengurus", "Bagi Dua").forEach { option ->
                    FilterChip(
                        selected = hakPembagian == option,
                        onClick = { hakPembagian = option },
                        label = { Text(option) },
                        enabled = !isLoading
                    )
                }
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
                    if (nama.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Nama wajib diisi") }
                        return@Button
                    }
                    
                    farmViewModel.tambahPasangan(
                        silsilahId = silsilahId,
                        indukBetinaId = indukBetinaId,
                        nama = nama,
                        status = status,
                        harga = harga.toIntOrNull() ?: 0,
                        hakPembagian = hakPembagian,
                        fotoUri = fotoUri,
                        ownershipSource = hakPembagian.uppercase().replace(" ", "_"),
                        existingJantanId = selectedPejantanId, // Key change: pass the ID if existing
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
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Menyimpan..." else "Simpan Pasangan")
            }
        }
    }
}
