package com.example.zenfarm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenfarm.R
import com.example.zenfarm.ui.theme.*
import com.example.zenfarm.viewmodel.AuthViewModel
import com.example.zenfarm.viewmodel.FarmViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    authViewModel: AuthViewModel,
    farmViewModel: FarmViewModel,
    navController: NavController
) {
    val user by authViewModel.user.collectAsState()
    val riwayatPenjualan by farmViewModel.riwayatPenjualan.collectAsState()
    val isLoading by farmViewModel.isLoading.collectAsState()
    
    LaunchedEffect(user) {
        user?.let {
            farmViewModel.fetchRiwayatPenjualan(it.userId, it.role)
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Scaffold(
        containerColor = SurfaceLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // ── Custom Header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(FarmGreen, FarmGreenDark)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dompet Saya",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // ── Balance Card ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(FarmOrange, FarmOrangeLight, Color(0xFFFDD835))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💰", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Total Saldo Anda",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Rp ${user?.saldo ?: 0}",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color.White.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📊", fontSize = 18.sp)
                                Text(
                                    "${riwayatPenjualan.size}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Transaksi",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val totalUntung = riwayatPenjualan.sumOf { 
                                    val p = it.hargaJual - it.hargaModal
                                    if (p > 0) p else 0
                                }
                                Text("📈", fontSize = 18.sp)
                                Text(
                                    "Rp $totalUntung",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Keuntungan",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val totalRugi = riwayatPenjualan.sumOf { 
                                    val p = it.hargaJual - it.hargaModal
                                    if (p < 0) -p else 0
                                }
                                Text("📉", fontSize = 18.sp)
                                Text(
                                    "Rp $totalRugi",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Kerugian",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // ── Riwayat Header ──
            Text(
                "📋 Riwayat Penjualan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FarmGreenDark,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ── Content ──
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = FarmGreen
                    )
                } else if (riwayatPenjualan.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📭", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Belum ada riwayat penjualan",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(riwayatPenjualan) { penjualan ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Header row
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
                                                    .background(FarmGreenSurface),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🐄", fontSize = 20.sp)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    "ID: ${penjualan.hewanId.take(8)}...",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    dateFormatter.format(penjualan.createdAt.toDate()),
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                        
                                        // Profit badge
                                        val profit = penjualan.hargaJual - penjualan.hargaModal
                                        val (badgeColor, badgeBg, badgeText) = when {
                                            profit < 0 -> Triple(FarmRed, FarmRedSurface, "RUGI")
                                            profit > 0 -> Triple(Color(0xFF4CAF50), FarmGreenSurface, "UNTUNG")
                                            else -> Triple(Color.Gray, Color(0xFFF5F5F5), "IMPAS")
                                        }
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = badgeBg)
                                        ) {
                                            Text(
                                                text = badgeText,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                color = badgeColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider(color = Color(0xFFF0F0F0))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
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
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Price info
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
                                            Text("Terjual", fontSize = 11.sp, color = Color.Gray)
                                            Text(
                                                "Rp ${penjualan.hargaJual}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = FarmGreen
                                            )
                                        }
                                        Text("=", color = Color.Gray, modifier = Modifier.align(Alignment.CenterVertically))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            val profit2 = penjualan.hargaJual - penjualan.hargaModal
                                            if (profit2 < 0) {
                                                Text("Rugi", fontSize = 11.sp, color = FarmRed)
                                                Text(
                                                    "Rp ${-profit2}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = FarmRed
                                                )
                                            } else if (profit2 > 0) {
                                                Text("Untung", fontSize = 11.sp, color = Color(0xFF4CAF50))
                                                Text(
                                                    "Rp $profit2",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF4CAF50)
                                                )
                                            } else {
                                                Text("Margin", fontSize = 11.sp, color = Color.Gray)
                                                Text(
                                                    "Rp 0",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
