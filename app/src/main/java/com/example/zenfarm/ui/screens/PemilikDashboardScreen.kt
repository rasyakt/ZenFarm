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
            // ── Custom Header with Gradient ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF43A047), FarmGreenDark)
                        )
                    )
                    .padding(20.dp)
            ) {
                // Decorative background circles
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.White.copy(alpha = 0.07f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .align(Alignment.BottomStart)
                        .background(Color.White.copy(alpha = 0.07f), CircleShape)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Profile Icon
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(4.dp)
                                .clickable {
                                    if (!isNavigating) {
                                        isNavigating = true
                                        navController.navigate("profile")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.profile_pemilik),
                                contentDescription = "Profile Pemilik",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Halo, ${user?.name ?: "User"}!",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "👑 Pemilik Peternakan",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp
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
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // ── Quick Action Cards (2x2 Grid) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Global & Dompet — IntrinsicSize.Min agar tinggi seragam
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Global Marketplace Card ──
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = FarmBlue.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
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
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(FarmBlue.copy(alpha = 0.13f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌐", fontSize = 26.sp)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "GLOBAL",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FarmBlue,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Pasar Ternak",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // ── Dompet Card ──
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = FarmOrange.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
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
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFFF8E1), Color(0xFFFFECC7))
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(FarmOrange.copy(alpha = 0.13f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💰", fontSize = 26.sp)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Dompet",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FarmOrange,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Rp ${user?.saldo ?: 0}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Row 2: Pending & Silsilah
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val hasPending = pendingPenjualans.isNotEmpty()
                    val pendingAccent = if (hasPending) FarmRed else FarmGreen
                    val pendingGradient = if (hasPending)
                        listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2))
                    else
                        listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))

                    // ── Pending Card ──
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = pendingAccent.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(pendingGradient))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(pendingAccent.copy(alpha = 0.13f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (hasPending) "🔔" else "✅", fontSize = 26.sp)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Pending",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = pendingAccent,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "${pendingPenjualans.size} Menunggu",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // ── Silsilah Card ──
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = FarmGreen.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(FarmGreen.copy(alpha = 0.13f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌿", fontSize = 26.sp)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Silsilah",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FarmGreenDark,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "${silsilahs.size} Total",
                                    fontSize = 12.sp,
                                    color = Color.Gray
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
                // ── PENDING APPROVAL SECTION ──
                if (pendingPenjualans.isNotEmpty()) {
                    item {
                        Text(
                            "🔔 Persetujuan Penjualan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmRed,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                                                snackbarHostState.showSnackbar("Penjualan disetujui! Saldo bertambah.")
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📋 Silsilah Saya",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenDark
                        )
                    }
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
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Text(
                                "Klik + untuk mendaftarkan silsilah baru",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(silsilahs) { s ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                                .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = FarmGreenDark.copy(alpha = 0.5f))
                                .clickable {
                                    if (!isNavigating) {
                                        isNavigating = true
                                        navController.navigate("silsilah_detail/${s.silsilahId}")
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFF0F4F8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.domba_icon_list1_01),
                                        contentDescription = "Sheep Icon",
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        s.namaSilsilah,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = FarmGreenDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val statusColor = when (s.status.lowercase()) {
                                        "aktif" -> FarmGreenLight
                                        "selesai" -> FarmBlue
                                        else -> FarmOrange
                                    }
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = statusColor.copy(alpha = 0.15f)
                                        )
                                    ) {
                                        Text(
                                            text = s.status.uppercase(),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                            color = statusColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text("▶", color = FarmGreen, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }

        // ── Daftar Silsilah Dialog ──
        if (showDaftarSilsilah) {
            DaftarSilsilahDialog(
                isLoading = isLoading,
                onDismiss = { showDaftarSilsilah = false },
                onConfirm = { silsilahNama, hewanNama, jenisKelamin, tanggalLahir, fotoUri ->
                    user?.let { u ->
                        farmViewModel.daftarSilsilah(
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
                            }
                        )
                    }
                }
            )
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
        margin > 0 -> Color(0xFF4CAF50)
        margin < 0 -> FarmRed
        else -> Color.Gray
    }
    val marginText = when {
        margin > 0 -> "Untung: Rp $margin"
        margin < 0 -> "Rugi: Rp ${-margin}"
        else -> "Tidak memiliki keuntungan"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color.Gray.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                        Text("🐄", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            penjualan.hewanNama.ifEmpty { "Hewan #${penjualan.hewanId.take(6)}" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "Diajukan oleh Pengurus",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmOrangeSurface)
                ) {
                    Text(
                        "PENDING",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = FarmOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Price details row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F9FA))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Modal", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        "Rp ${penjualan.hargaModal}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FarmBrown
                    )
                }
                Text("→", color = Color.Gray, modifier = Modifier.align(Alignment.CenterVertically))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Jual", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        "Rp ${penjualan.hargaJual}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FarmGreen
                    )
                }
                Text("=", color = Color.Gray, modifier = Modifier.align(Alignment.CenterVertically))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Margin", fontSize = 11.sp, color = marginColor)
                    Text(
                        marginText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = marginColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Buyer info
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pembeli", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        penjualan.buyerName.ifEmpty { "N/A" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Kontak", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        penjualan.buyerPhone.ifEmpty { "N/A" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
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
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FarmRed)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = FarmRed,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("❌ Tolak", fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Button(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            onApprove()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(12.dp),
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
                        Text("✅ Setuju", fontWeight = FontWeight.SemiBold)
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FarmRed,
                            focusedLabelColor = FarmRed,
                            cursorColor = FarmRed
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
                val file = java.io.File(context.filesDir, "hewan_root_${System.currentTimeMillis()}.jpg")
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
                        label = { Text("Nama Silsilah (ex: Sapi Limosin A)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FarmGreen,
                            focusedLabelColor = FarmGreen,
                            cursorColor = FarmGreen
                        )
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FarmGreen,
                            focusedLabelColor = FarmGreen,
                            cursorColor = FarmGreen
                        )
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FarmGreen,
                            focusedLabelColor = FarmGreen,
                            cursorColor = FarmGreen
                        )
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
