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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    navController: NavController,
    isBottomNav: Boolean = false
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

    val totalUntung = riwayatPenjualan.sumOf {
        val p = it.hargaJual - it.hargaModal
        if (p > 0) p else 0
    }
    val totalRugi = riwayatPenjualan.sumOf {
        val p = it.hargaJual - it.hargaModal
        if (p < 0) -p else 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
            // ── Custom Header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp)
                    .background(FarmGreen)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!isBottomNav) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Column {
                        Text(
                            text = "Dompet Saya",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Riwayat Keuangan",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // ── Premium Virtual Card ──
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(24.dp),
                                clip = true,
                                spotColor = FarmOrange.copy(alpha = 0.5f)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = FarmOrange,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(24.dp)
                        ) {
                            // Background decorative circle
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 35.dp, y = (-35).dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .align(Alignment.BottomStart)
                                    .offset(x = (-25).dp, y = 25.dp)
                                    .background(Color.White.copy(alpha = 0.07f), CircleShape)
                            )

                            Column {
                                // Card header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "ZenFarm",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.CreditCard,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Balance
                                Text(
                                    "Total Saldo",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Rp ${user?.saldo ?: 0}",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 30.sp,
                                    letterSpacing = (-0.5).sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
                                Spacer(modifier = Modifier.height(16.dp))

                                // Stats row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Rounded.BarChart,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "${riwayatPenjualan.size}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            "Transaksi",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(48.dp)
                                            .background(Color.White.copy(alpha = 0.3f))
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Rounded.TrendingUp,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Rp $totalUntung",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "Keuntungan",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(48.dp)
                                            .background(Color.White.copy(alpha = 0.3f))
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Rounded.TrendingDown,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Rp $totalRugi",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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
                }

                // ── Riwayat Header ──
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = FarmGreenDark,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Riwayat Penjualan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenDark
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // ── Content ──
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = FarmGreen)
                        }
                    }
                } else if (riwayatPenjualan.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.Inbox,
                                    contentDescription = null,
                                    tint = TextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Belum ada riwayat penjualan",
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    items(riwayatPenjualan) { penjualan ->
                        val profit = penjualan.hargaJual - penjualan.hargaModal
                        val (badgeColor, badgeBg, badgeText) = when {
                            profit < 0 -> Triple(FarmRed, FarmRedSurface, "RUGI")
                            profit > 0 -> Triple(FarmGreen, FarmGreenSurface, "UNTUNG")
                            else -> Triple(TextSecondary, DividerGray, "IMPAS")
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    clip = true,
                                    spotColor = badgeColor.copy(alpha = 0.2f)
                                ),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite)
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
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(FarmGreenSurface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Pets,
                                                contentDescription = null,
                                                tint = FarmGreen,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                penjualan.hewanNama.ifEmpty { "Hewan #${penjualan.hewanId.take(8)}" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                dateFormatter.format(penjualan.createdAt.toDate()),
                                                fontSize = 11.sp,
                                                color = TextHint,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    
                                    // Profit/Loss badge
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
                                HorizontalDivider(color = DividerGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Buyer info
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SurfaceLight)
                                        .padding(10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Pembeli",
                                            fontSize = 11.sp,
                                            color = TextHint,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            penjualan.buyerName.ifEmpty { "N/A" },
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Kontak",
                                            fontSize = 11.sp,
                                            color = TextHint,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            penjualan.buyerPhone.ifEmpty { "N/A" },
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Price comparison row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceLight)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Modal",
                                            fontSize = 11.sp,
                                            color = TextHint,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Rp ${penjualan.hargaModal}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = FarmBrown
                                        )
                                    }
                                    Text(
                                        "→",
                                        color = TextHint,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Terjual",
                                            fontSize = 11.sp,
                                            color = TextHint,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Rp ${penjualan.hargaJual}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = FarmGreen
                                        )
                                    }
                                    Text(
                                        "=",
                                        color = TextHint,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        val profitAmt = penjualan.hargaJual - penjualan.hargaModal
                                        val marginLabel = when {
                                            profitAmt < 0 -> "Rugi"
                                            profitAmt > 0 -> "Untung"
                                            else -> "Impas"
                                        }
                                        val marginColor = when {
                                            profitAmt < 0 -> FarmRed
                                            profitAmt > 0 -> FarmGreen
                                            else -> TextSecondary
                                        }
                                        Text(
                                            marginLabel,
                                            fontSize = 11.sp,
                                            color = marginColor,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Rp ${if (profitAmt < 0) -profitAmt else profitAmt}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = marginColor
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
