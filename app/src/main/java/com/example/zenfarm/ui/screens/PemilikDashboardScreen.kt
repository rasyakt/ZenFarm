package com.example.zenfarm.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.zenfarm.R
import com.example.zenfarm.data.Penjualan
import com.example.zenfarm.ui.theme.*
import com.example.zenfarm.viewmodel.AuthViewModel
import com.example.zenfarm.viewmodel.FarmViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PemilikDashboardScreen(
    authViewModel: AuthViewModel,
    farmViewModel: FarmViewModel,
    navController: NavController
) {
    val user by authViewModel.user.collectAsState()
    val silsilahs by farmViewModel.silsilahs.collectAsState()
    val pendingPenjualans by farmViewModel.pendingPenjualans.collectAsState()
    val isLoading by farmViewModel.isLoading.collectAsState()
    var isNavigating by remember { mutableStateOf(false) }

    // Use backStackEntry to reset isNavigating when returning to this screen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        isNavigating = false
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Dialog states
    var showDaftarSilsilah by remember { mutableStateOf(false) }
    var penjualanToReject by remember { mutableStateOf<Penjualan?>(null) }

    LaunchedEffect(user) {
        user?.let {
            farmViewModel.fetchSilsilahSaya(it.userId, it.role)
            farmViewModel.fetchPendingPenjualan(it.userId)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = SurfaceLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDaftarSilsilah = true },
                containerColor = FarmGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.shadow(8.dp, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Silsilah")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // ── Modern Header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(FarmGreen, FarmGreenLight)
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Profile Icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable {
                                    if (!isNavigating) {
                                        isNavigating = true
                                        navController.navigate("profile")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Halo, ${user?.name ?: "User"}",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "👑 Pemilik Peternakan",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Logout button
                    IconButton(
                        onClick = {
                            if (!isNavigating) {
                                isNavigating = true
                                authViewModel.logout()
                                navController.navigate("login") { popUpTo(0) }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── Quick Action Cards ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Global & Dompet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Global Card ──
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        onClick = {
                            if (!isNavigating) {
                                isNavigating = true
                                navController.navigate("global")
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(FarmBlueSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_global),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Global",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmBlue
                                )
                                Text(
                                    "Pasar Ternak",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // ── Dompet Card ──
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        onClick = {
                            if (!isNavigating) {
                                isNavigating = true
                                navController.navigate("dompet")
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(FarmOrangeSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💰", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Dompet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmOrange
                                )
                                Text(
                                    "Rp ${user?.saldo ?: 0}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Row 2: Pending & Silsilah
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val hasPending = pendingPenjualans.isNotEmpty()
                    val pendingColor = if (hasPending) FarmRed else FarmGreen
                    val pendingSurface = if (hasPending) FarmRedSurface else FarmGreenSurface

                    // ── Pending Card ──
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(pendingSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (hasPending) "🔔" else "✅", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Pending",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = pendingColor
                                )
                                Text(
                                    "${pendingPenjualans.size} Menunggu",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // ── Silsilah Card ──
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(FarmGreenSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌿", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Silsilah",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmGreenDark
                                )
                                Text(
                                    "${silsilahs.size} Total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // ── Scrollable List Section (header+cards di atas tetap diam) ──
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // ── Pending Approval Section ──
                if (pendingPenjualans.isNotEmpty()) {
                    item {
                        Text(
                            "Persetujuan Penjualan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmRed,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                    items(pendingPenjualans) { penjualan ->
                        PendingApprovalCard(
                            penjualan = penjualan,
                            onApprove = {
                                user?.let { u ->
                                    farmViewModel.acceptPenjualan(
                                        penjualan = penjualan,
                                        ownerId = u.userId,
                                        onSuccess = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Penjualan disetujui!")
                                            }
                                        },
                                        onError = { msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        }
                                    )
                                }
                            },
                            onReject = { penjualanToReject = penjualan }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // ── Silsilah List Header ──
                item {
                    Text(
                        "Silsilah Saya",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmGreenDark,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                // ── Silsilah Content ──
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = FarmGreen)
                        }
                    }
                } else if (silsilahs.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🐄", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Belum ada silsilah",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Text(
                                "Klik + untuk mendaftarkan silsilah baru",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextHint
                            )
                        }
                    }
                } else {
                    items(silsilahs) { s ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable {
                                    if (!isNavigating) {
                                        isNavigating = true
                                        navController.navigate("silsilah_detail/${s.silsilahId}")
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(FarmGreenSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_sheep),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        s.namaSilsilah,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmGreenDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val statusColor = when (s.status.lowercase()) {
                                        "aktif" -> FarmGreen
                                        "selesai" -> FarmBlue
                                        else -> FarmOrange
                                    }
                                    Card(
                                        shape = RoundedCornerShape(6.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = statusColor.copy(alpha = 0.15f)
                                        )
                                    ) {
                                        Text(
                                            text = s.status.uppercase(),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            color = statusColor,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = FarmGreen,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer(rotationZ = 180f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Daftar Silsilah Dialog ──
        if (showDaftarSilsilah) {
            val context = androidx.compose.ui.platform.LocalContext.current
            var showRetryDialog by remember { mutableStateOf(false) }
            var pendingData by remember { mutableStateOf<Map<String, String>?>(null) }
            
            DaftarSilsilahDialog(
                isLoading = isLoading,
                onDismiss = { showDaftarSilsilah = false },
                onConfirm = { silsilahNama, hewanNama, jenisKelamin, tanggalLahir, fotoUri ->
                    user?.let { u ->
                        farmViewModel.daftarSilsilah(
                            context = context,
                            silsilahNama = silsilahNama,
                            hewanNama = hewanNama,
                            jenisKelamin = jenisKelamin,
                            tanggalLahirStr = tanggalLahir,
                            ownerId = u.userId,
                            fotoUri = fotoUri,
                            onSuccess = { newId ->
                                showDaftarSilsilah = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Silsilah berhasil didaftarkan!")
                                    if (!isNavigating) {
                                        isNavigating = true
                                        navController.navigate("silsilah_detail/$newId")
                                    }
                                }
                            },
                            onError = { msg ->
                                scope.launch {
                                    // Jika error terkait foto, tawarkan opsi skip foto
                                    if (fotoUri.isNotEmpty() && (msg.contains("foto") || msg.contains("Object does not exist"))) {
                                        pendingData = mapOf(
                                            "silsilahNama" to silsilahNama,
                                            "hewanNama" to hewanNama,
                                            "jenisKelamin" to jenisKelamin,
                                            "tanggalLahir" to tanggalLahir
                                        )
                                        showRetryDialog = true
                                    } else {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            }
                        )
                    }
                }
            )
            
            // ── Retry Dialog (Skip Foto) ──
            if (showRetryDialog && pendingData != null) {
                AlertDialog(
                    onDismissRequest = { showRetryDialog = false },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = CardWhite,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Gagal Upload Foto",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = FarmOrange
                            )
                        }
                    },
                    text = {
                        Column {
                            Text(
                                "Foto tidak dapat diupload. Anda bisa:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "• Lanjutkan tanpa foto (bisa ditambahkan nanti)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                "• Coba lagi dengan foto lain",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Lanjutkan tanpa foto
                                user?.let { u ->
                                    farmViewModel.daftarSilsilah(
                                        context = context,
                                        silsilahNama = pendingData!!["silsilahNama"]!!,
                                        hewanNama = pendingData!!["hewanNama"]!!,
                                        jenisKelamin = pendingData!!["jenisKelamin"]!!,
                                        tanggalLahirStr = pendingData!!["tanggalLahir"]!!,
                                        ownerId = u.userId,
                                        fotoUri = "", // Skip foto
                                        onSuccess = { newId ->
                                            showDaftarSilsilah = false
                                            showRetryDialog = false
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Silsilah berhasil didaftarkan tanpa foto!")
                                                if (!isNavigating) {
                                                    isNavigating = true
                                                    navController.navigate("silsilah_detail/$newId")
                                                }
                                            }
                                        },
                                        onError = { msg ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(msg)
                                            }
                                        }
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
                        ) {
                            Text("Lanjutkan Tanpa Foto")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showRetryDialog = false
                                // Kembali ke dialog untuk pilih foto lain
                            }
                        ) {
                            Text("Coba Lagi", color = FarmOrange)
                        }
                    }
                )
            }
        }

        // ── Reject Dialog ──
        if (penjualanToReject != null) {
            RejectPenjualanDialog(
                penjualan = penjualanToReject!!,
                onReject = { alasan ->
                    user?.let { u ->
                        farmViewModel.rejectPenjualan(
                            penjualan = penjualanToReject!!,
                            alasan = alasan,
                            ownerId = u.userId,
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Penjualan ditolak.")
                                }
                            },
                            onError = { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        )
                    }
                    penjualanToReject = null
                },
                onDismiss = { penjualanToReject = null }
            )
        }
    }
}

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
                // Simpan URI langsung untuk diupload nanti
                fotoUri = uri.toString()
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
                        label = { Text("Nama Silsilah (ex: Sapi Limosin A)") },
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
