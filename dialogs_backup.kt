// ─────────────────────────────────────────────────────────────────────────────
// Pending Approval Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PendingApprovalCard(
    penjualan: Penjualan,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    
    val margin = penjualan.hargaJual - penjualan.hargaModal
    val marginColor = when {
        margin > 0 -> FarmGreen
        margin < 0 -> FarmRed
        else -> TextSecondary
    }
    val marginText = when {
        margin > 0 -> "Untung: Rp $margin"
        margin < 0 -> "Rugi: Rp ${-margin}"
        else -> "Impas"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FarmOrangeSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_cow),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            penjualan.hewanNama.ifEmpty { "Hewan #${penjualan.hewanId.take(6)}" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Diajukan oleh Pengurus",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                Card(
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmOrangeSurface)
                ) {
                    Text(
                        "PENDING",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = FarmOrange,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerGray)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Price details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceLight)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Modal",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHint
                    )
                    Text(
                        "Rp ${penjualan.hargaModal}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmBrown
                    )
                }
                Text("→", color = TextHint, modifier = Modifier.align(Alignment.CenterVertically))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Jual",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHint
                    )
                    Text(
                        "Rp ${penjualan.hargaJual}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmGreen
                    )
                }
                Text("=", color = TextHint, modifier = Modifier.align(Alignment.CenterVertically))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Margin",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHint
                    )
                    Text(
                        marginText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = marginColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Buyer info
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Pembeli",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHint
                    )
                    Text(
                        penjualan.buyerName.ifEmpty { "N/A" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Kontak",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHint
                    )
                    Text(
                        penjualan.buyerPhone.ifEmpty { "N/A" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            onReject()
                        }
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FarmRed)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = FarmRed,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Tolak", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Button(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            onApprove()
                        }
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FarmGreen,
                        disabledContainerColor = FarmGreen.copy(alpha = 0.5f)
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Setuju", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reject Dialog with Reason Picker
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RejectPenjualanDialog(
    penjualan: Penjualan,
    onReject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val reasonOptions = listOf(
        "Jangan dulu dijual",
        "Harga terlalu murah",
        "Belum cukup umur",
        "Hewan masih produktif",
        "Alasan lain"
    )
    var selectedReason by remember { mutableStateOf("") }
    var customReason by remember { mutableStateOf("") }
    
    val finalReason = if (selectedReason == "Alasan lain") customReason else selectedReason

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("❌", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Tolak Penjualan",
                    fontWeight = FontWeight.Bold,
                    color = FarmRed
                )
            }
        },
        text = {
            Column {
                Text(
                    "Hewan: ${penjualan.hewanNama.ifEmpty { penjualan.hewanId.take(8) }}",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Harga Jual: Rp ${penjualan.hargaJual}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Pilih alasan penolakan:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                reasonOptions.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = FarmRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(reason, fontSize = 14.sp)
                    }
                }
                
                // Custom reason input
                if (selectedReason == "Alasan lain") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        label = { Text("Tulis alasan Anda...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = false,
                        maxLines = 3,
                        colors = standardTextFieldColors(
                            focusedBorderColor = FarmRed,
                            unfocusedBorderColor = DividerGray
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onReject(finalReason) },
                enabled = finalReason.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FarmRed,
                    disabledContainerColor = FarmRed.copy(alpha = 0.3f)
                )
            ) {
                Text("Tolak Penjualan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}

@Composable
fun DaftarSilsilahDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (namaSilsilah: String, namaHewan: String, jenisKelamin: String, tanggalLahir: String, fotoUri: String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var namaSilsilah by remember { mutableStateOf("") }
    var namaHewan by remember { mutableStateOf("") }
    var jenisKelamin by remember { mutableStateOf("Betina") }
    var tanggalLahir by remember { mutableStateOf("2024-01-01") }
    var fotoUri by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = java.io.File(context.filesDir, "induk_utama_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                fotoUri = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        onDismissRequest = if (isLoading) ({}) else onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (step == 1) "🌿" else "🐄",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (step == 1) "Mulai Silsilah Baru" else "Data Induk Pertama",
                    fontWeight = FontWeight.Bold,
                    color = FarmGreenDark
                )
            }
        },
        text = {
            Column {
                // Step indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (step >= 1) FarmGreen else Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(3.dp)
                            .align(Alignment.CenterVertically)
                            .background(if (step >= 2) FarmGreen else Color.LightGray)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (step >= 2) FarmGreen else Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (step == 1) {
                    Text(
                        "Tentukan nama untuk silsilah/garis keturunan ini.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = namaSilsilah,
                        onValueChange = { namaSilsilah = it },
                        label = { Text("Nama Silsilah (ex: Sapi Limosin)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = standardTextFieldColors()
                    )
                } else {
                    Text(
                        "Input data hewan sebagai induk utama silsilah ini.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = namaHewan,
                        onValueChange = { namaHewan = it },
                        label = { Text("Nama Hewan Induk") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = standardTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Jenis Kelamin:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = jenisKelamin == "Jantan", 
                            onClick = { jenisKelamin = "Jantan" },
                            enabled = !isLoading,
                            colors = RadioButtonDefaults.colors(selectedColor = FarmGreen)
                        )
                        Text("Jantan")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = jenisKelamin == "Betina", 
                            onClick = { jenisKelamin = "Betina" },
                            enabled = !isLoading,
                            colors = RadioButtonDefaults.colors(selectedColor = FarmGreen)
                        )
                        Text("Betina")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tanggalLahir,
                        onValueChange = { tanggalLahir = it },
                        label = { Text("Tgl Lahir Induk (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = standardTextFieldColors()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { launcher.launch("image/*") },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
                        ) {
                            Text("📸 Pilih Foto Induk")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (fotoUri.isNotEmpty()) {
                            Text("✅ Foto terpilih", color = FarmGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = FarmGreen
                    )
                }
            }
        },
        confirmButton = {
            if (step == 1) {
                Button(
                    onClick = { step = 2 },
                    enabled = namaSilsilah.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
                ) { Text("Lanjut ke Step 2") }
            } else {
                Button(
                    onClick = { onConfirm(namaSilsilah, namaHewan, jenisKelamin, tanggalLahir, fotoUri) },
                    enabled = namaHewan.isNotBlank() && tanggalLahir.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
                ) { Text("Simpan & Lihat Pohon") }
            }
        },
        dismissButton = {
            if (step == 2) {
                TextButton(
                    onClick = { step = 1 },
                    enabled = !isLoading
                ) { Text("Kembali", color = FarmGreen) }
            } else {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) { Text("Batal", color = Color.Gray) }
            }
        }
    )
}
