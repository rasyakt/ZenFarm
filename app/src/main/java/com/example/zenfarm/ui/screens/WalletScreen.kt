package com.example.zenfarm.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenfarm.data.Penjualan
import com.example.zenfarm.data.Silsilah
import com.example.zenfarm.ui.theme.*
import com.example.zenfarm.viewmodel.AuthViewModel
import com.example.zenfarm.viewmodel.FarmViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private fun computeOwnerEarnings(p: Penjualan): Int {
    val os = p.ownershipSource.uppercase()
    return when {
        os.contains("PENGURUS") -> 0
        os == "PEMILIK" || os == "MUTLAK_PEMILIK" -> p.hargaJual
        else -> {
            val profit = p.hargaJual - p.hargaModal
            if (profit > 0) {
                val share = profit / 2
                val remainder = profit - share * 2
                p.hargaModal + share + remainder
            } else p.hargaJual
        }
    }
}

private fun computePengurusEarnings(p: Penjualan): Int {
    val os = p.ownershipSource.uppercase()
    return when {
        os.contains("PENGURUS") -> p.hargaJual
        os == "PEMILIK" || os == "MUTLAK_PEMILIK" -> 0
        else -> {
            val profit = p.hargaJual - p.hargaModal
            if (profit > 0) profit / 2 else 0
        }
    }
}

private data class SilsilahReport(
    val silsilah: Silsilah,
    val transactions: List<Penjualan>,
    val totalSold: Int,
    val totalRevenue: Int,
    val totalModal: Int,
    val totalProfit: Int,
    val ownerEarnings: Int,
    val pengurusEarnings: Int,
)

private fun buildSilsilahReports(
    riwayat: List<Penjualan>,
    silsilahList: List<Silsilah>,
): List<SilsilahReport> {
    val silsilahMap = silsilahList.associateBy { it.silsilahId }
    val grouped = riwayat.filter { it.status == "SOLD" }.groupBy { it.silsilahId }
    return grouped.map { (silsilahId, txs) ->
        val s = silsilahMap[silsilahId]
        SilsilahReport(
            silsilah = s ?: Silsilah(silsilahId = silsilahId, namaSilsilah = "Silsilah #${silsilahId.take(8)}"),
            transactions = txs,
            totalSold = txs.size,
            totalRevenue = txs.sumOf { it.hargaJual },
            totalModal = txs.sumOf { it.hargaModal },
            totalProfit = txs.sumOf { it.hargaJual - it.hargaModal },
            ownerEarnings = txs.sumOf { computeOwnerEarnings(it) },
            pengurusEarnings = txs.sumOf { computePengurusEarnings(it) },
        )
    }.sortedByDescending { it.totalRevenue }
}

private data class OwnershipGroup(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val surfaceColor: Color,
    val transactions: List<Penjualan>,
    val totalRevenue: Int,
    val totalProfit: Int,
    val ownerEarnings: Int,
    val pengurusEarnings: Int,
)

private fun buildOwnershipGroups(riwayat: List<Penjualan>): List<OwnershipGroup> {
    val sold = riwayat.filter { it.status == "SOLD" }
    val pemilik = sold.filter { it.ownershipSource.uppercase() == "PEMILIK" || it.ownershipSource.uppercase() == "MUTLAK_PEMILIK" }
    val pengurus = sold.filter { it.ownershipSource.uppercase().contains("PENGURUS") }
    val bagiDua = sold.filter {
        val os = it.ownershipSource.uppercase()
        os != "PEMILIK" && os != "MUTLAK_PEMILIK" && !os.contains("PENGURUS")
    }
    return listOf(
        OwnershipGroup("Milik Pemilik", Icons.Rounded.VerifiedUser, FarmBlue, FarmBlueSurface, pemilik,
            pemilik.sumOf { it.hargaJual }, pemilik.sumOf { it.hargaJual - it.hargaModal },
            pemilik.sumOf { computeOwnerEarnings(it) }, pemilik.sumOf { computePengurusEarnings(it) }),
        OwnershipGroup("Milik Pengurus", Icons.Rounded.Badge, FarmOrange, FarmOrangeSurface, pengurus,
            pengurus.sumOf { it.hargaJual }, pengurus.sumOf { it.hargaJual - it.hargaModal },
            pengurus.sumOf { computeOwnerEarnings(it) }, pengurus.sumOf { computePengurusEarnings(it) }),
        OwnershipGroup("Bagi Dua", Icons.Rounded.Handshake, FarmGreen, FarmGreenSurface, bagiDua,
            bagiDua.sumOf { it.hargaJual }, bagiDua.sumOf { it.hargaJual - it.hargaModal },
            bagiDua.sumOf { computeOwnerEarnings(it) }, bagiDua.sumOf { computePengurusEarnings(it) }),
    )
}

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
    val pendingPenjualans by farmViewModel.pendingPenjualans.collectAsState()
    val silsilahs by farmViewModel.silsilahs.collectAsState()
    val isLoading by farmViewModel.isLoading.collectAsState()

    LaunchedEffect(user) {
        user?.let {
            farmViewModel.fetchSilsilahSaya(it.userId, it.role)
            farmViewModel.fetchRiwayatPenjualan(it.userId, it.role)
            if (it.role == "Pemilik") {
                farmViewModel.fetchPendingPenjualan(it.userId)
            }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val isPemilik = user?.role == "Pemilik"
    val roleColor = if (isPemilik) FarmBlue else FarmOrange
    val roleIcon = if (isPemilik) Icons.Rounded.VerifiedUser else Icons.Rounded.Badge

    val totalProfit = riwayatPenjualan.filter { it.status == "SOLD" }.sumOf { it.hargaJual - it.hargaModal }
    val totalUntung = riwayatPenjualan.filter { it.status == "SOLD" }.sumOf { maxOf(0, it.hargaJual - it.hargaModal) }
    val totalRugi = riwayatPenjualan.filter { it.status == "SOLD" }.sumOf { maxOf(0, -(it.hargaJual - it.hargaModal)) }
    val totalRevenue = riwayatPenjualan.filter { it.status == "SOLD" }.sumOf { it.hargaJual }

    val silsilahReports = remember(riwayatPenjualan, silsilahs) {
        buildSilsilahReports(riwayatPenjualan, silsilahs)
    }
    val ownershipGroups = remember(riwayatPenjualan) {
        buildOwnershipGroups(riwayatPenjualan)
    }

    var expandedSilsilahId by remember { mutableStateOf<String?>(null) }

    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectPenjualan by remember { mutableStateOf<Penjualan?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        // Header
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Laporan Keuangan",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Transparansi Penuh",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                // Role badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(roleIcon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(user?.role ?: "", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── Saldo Card ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .shadow(12.dp, RoundedCornerShape(24.dp), clip = true, spotColor = FarmOrange.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FarmOrange, RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        Box(Modifier.size(140.dp).align(Alignment.TopEnd).offset(x = 35.dp, y = (-35).dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape))
                        Box(Modifier.size(90.dp).align(Alignment.BottomStart).offset(x = (-25).dp, y = 25.dp)
                            .background(Color.White.copy(alpha = 0.07f), CircleShape))

                        Column {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("ZenFarm", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 1.sp)
                                Icon(Icons.Rounded.AccountBalance, null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.height(20.dp))
                            Text("Total Saldo", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "Rp ${user?.saldo ?: 0}",
                                color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.5).sp
                            )
                            Spacer(Modifier.height(20.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
                            Spacer(Modifier.height(16.dp))

                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                                StatCol(Icons.Rounded.BarChart, "${riwayatPenjualan.filter { it.status == "SOLD" }.size}", "Transaksi", Color.White)
                                Box(Modifier.width(1.dp).height(48.dp).background(Color.White.copy(alpha = 0.3f)))
                                StatCol(Icons.Rounded.TrendingUp, "Rp $totalUntung", "Keuntungan", Color.White)
                                Box(Modifier.width(1.dp).height(48.dp).background(Color.White.copy(alpha = 0.3f)))
                                StatCol(Icons.Rounded.TrendingDown, "Rp $totalRugi", "Kerugian", Color.White)
                            }

                            if (isPemilik && pendingPenjualans.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
                                Spacer(Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.HourglassEmpty, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "${pendingPenjualans.size} transaksi menunggu persetujuan",
                                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Ringkasan Per Silsilah ──
            item {
                SectionHeader("Ringkasan Per Silsilah", Icons.Rounded.AccountTree, FarmGreenDark)
            }
            if (silsilahReports.isEmpty()) {
                item {
                    EmptyRow("Belum ada data silsilah")
                }
            } else {
                items(silsilahReports) { report ->
                    val isExpanded = expandedSilsilahId == report.silsilah.silsilahId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { expandedSilsilahId = if (isExpanded) null else report.silsilah.silsilahId }
                            .shadow(3.dp, RoundedCornerShape(16.dp), clip = true),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(FarmGreenSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Pets, null, tint = FarmGreen, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(report.silsilah.namaSilsilah, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    Text(
                                        "${report.totalSold} terjual  |  Rp $totalRevenue total",
                                        fontSize = 11.sp, color = TextHint
                                    )
                                }
                                Icon(
                                    if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    null, tint = TextSecondary, modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                MiniStat("Terjual", "${report.totalSold}", FarmBlue)
                                MiniStat("Modal", "Rp ${report.totalModal}", FarmBrown)
                                MiniStat("Revenue", "Rp ${report.totalRevenue}", FarmGreen)
                                MiniStat("Laba", "Rp ${report.totalProfit}", if (report.totalProfit >= 0) FarmGreen else FarmRed)
                            }

                            if (isPemilik) {
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(FarmGreenSurface).padding(8.dp)) {
                                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Bagian Pemilik", fontSize = 10.sp, color = TextHint)
                                        Text("Rp ${report.ownerEarnings}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FarmBlue)
                                    }
                                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Bagian Pengurus", fontSize = 10.sp, color = TextHint)
                                        Text("Rp ${report.pengurusEarnings}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FarmOrange)
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column {
                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider(color = DividerGray)
                                    Spacer(Modifier.height(8.dp))
                                    report.transactions.forEach { tx ->
                                        CompactTxRow(tx, dateFormatter)
                                        Spacer(Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Rincian Kepemilikan ──
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader("Rincian Kepemilikan", Icons.Rounded.Source, FarmGreenDark)
            }
            items(ownershipGroups.filter { it.transactions.isNotEmpty() }) { group ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                        .shadow(3.dp, RoundedCornerShape(16.dp), clip = true),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(group.surfaceColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(group.icon, null, tint = group.color, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(group.label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Text("${group.transactions.size} transaksi", fontSize = 11.sp, color = TextHint)
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = group.surfaceColor) {
                                Text(
                                    "Rp ${group.totalRevenue}",
                                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = group.color, fontSize = 12.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            MiniStat("Revenue", "Rp ${group.totalRevenue}", group.color)
                            MiniStat("Profit", "Rp ${group.totalProfit}", if (group.totalProfit >= 0) FarmGreen else FarmRed)
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceLight).padding(8.dp)) {
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pemilik Dapat", fontSize = 10.sp, color = TextHint)
                                Text("Rp ${group.ownerEarnings}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = FarmBlue)
                            }
                            Box(Modifier.width(1.dp).height(36.dp).background(DividerGray))
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pengurus Dapat", fontSize = 10.sp, color = TextHint)
                                Text("Rp ${group.pengurusEarnings}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = FarmOrange)
                            }
                        }
                    }
                }
            }

            // ── Transaksi Menunggu (Pemilik Only) ──
            if (isPemilik && pendingPenjualans.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader("Transaksi Menunggu", Icons.Rounded.HourglassEmpty, FarmOrangeDark)
                }
                items(pendingPenjualans) { p ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                            .shadow(3.dp, RoundedCornerShape(16.dp), clip = true),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(FarmYellowSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.PendingActions, null, tint = FarmYellow, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(p.hewanNama.ifEmpty { "Hewan #${p.hewanId.take(8)}" }, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text("Rp ${p.hargaJual}  |  ${p.buyerName.ifEmpty { "N/A" }}", fontSize = 11.sp, color = TextHint)
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = FarmYellowSurface) {
                                    Text("PENDING", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = FarmYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = DividerGray)
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                MiniStat("Modal", "Rp ${p.hargaModal}", FarmBrown)
                                MiniStat("Jual", "Rp ${p.hargaJual}", FarmGreen)
                                val pLabel = if (p.profit >= 0) "Untung" else "Rugi"
                                val pColor = if (p.profit >= 0) FarmGreen else FarmRed
                                MiniStat(pLabel, "Rp ${if (p.profit < 0) -p.profit else p.profit}", pColor)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        rejectPenjualan = p
                                        showRejectDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FarmRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Rounded.Close, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Tolak", fontSize = 13.sp)
                                }
                                Button(
                                    onClick = {
                                        farmViewModel.acceptPenjualan(p, user?.userId ?: "", user?.role ?: "",
                                            onSuccess = {
                                                user?.let { u ->
                                                    farmViewModel.fetchSilsilahSaya(u.userId, u.role)
                                                    farmViewModel.fetchRiwayatPenjualan(u.userId, u.role)
                                                    farmViewModel.fetchPendingPenjualan(u.userId)
                                                    authViewModel.refreshUser(u.userId)
                                                }
                                            },
                                            onError = {})
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = FarmGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Setujui", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ── Riwayat Transaksi ──
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader("Riwayat Transaksi", Icons.Rounded.ReceiptLong, FarmGreenDark)
            }
            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FarmGreen)
                    }
                }
            } else if (riwayatPenjualan.isEmpty()) {
                item {
                    EmptyRow("Belum ada riwayat penjualan")
                }
            } else {
                items(riwayatPenjualan) { penjualan ->
                    val profit = penjualan.hargaJual - penjualan.hargaModal
                    val ownerShare = computeOwnerEarnings(penjualan)
                    val pengurusShare = computePengurusEarnings(penjualan)

                    val os = penjualan.ownershipSource.uppercase()
                    val ownLabel = when {
                        os.contains("PENGURUS") -> "Pengurus"
                        os == "PEMILIK" || os == "MUTLAK_PEMILIK" -> "Pemilik"
                        else -> "Bagi Dua"
                    }
                    val ownColor = when {
                        os.contains("PENGURUS") -> FarmOrange
                        os == "PEMILIK" || os == "MUTLAK_PEMILIK" -> FarmBlue
                        else -> FarmGreen
                    }
                    val ownBg = when {
                        os.contains("PENGURUS") -> FarmOrangeSurface
                        os == "PEMILIK" || os == "MUTLAK_PEMILIK" -> FarmBlueSurface
                        else -> FarmGreenSurface
                    }

                    val (badgeColor, badgeBg, badgeText) = when {
                        profit < 0 -> Triple(FarmRed, FarmRedSurface, "RUGI")
                        profit > 0 -> Triple(FarmGreen, FarmGreenSurface, "UNTUNG")
                        else -> Triple(TextSecondary, DividerGray, "IMPAS")
                    }
                    val statusColor = when (penjualan.status) {
                        "SOLD" -> FarmGreen
                        "PENDING" -> FarmYellow
                        "REJECTED" -> FarmRed
                        "AUTO_CANCEL" -> TextSecondary
                        else -> TextSecondary
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .shadow(3.dp, RoundedCornerShape(16.dp), clip = true, spotColor = badgeColor.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top row
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(ownBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Pets, null, tint = ownColor, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            penjualan.hewanNama.ifEmpty { "Hewan #${penjualan.hewanId.take(8)}" },
                                            fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary
                                        )
                                        Text(
                                            dateFormatter.format(penjualan.createdAt.toDate()),
                                            fontSize = 10.sp, color = TextHint
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = badgeBg) {
                                        Text(
                                            badgeText, Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = ownBg) {
                                        Text(
                                            ownLabel, Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = ownColor, fontSize = 9.sp, fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = DividerGray)
                            Spacer(Modifier.height(10.dp))

                            // Price row
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceLight).padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                            ) {
                                PriceCol("Modal", "Rp ${penjualan.hargaModal}", FarmBrown)
                                Text("→", color = TextHint, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                PriceCol("Terjual", "Rp ${penjualan.hargaJual}", FarmGreen)
                                Text("=", color = TextHint, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                PriceCol("Laba", "Rp ${if (profit < 0) -profit else profit}", if (profit >= 0) FarmGreen else FarmRed)
                            }

                            Spacer(Modifier.height(8.dp))

                            // Ownership share breakdown
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(FarmGreenSurface).padding(8.dp)
                            ) {
                                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Pemilik", fontSize = 10.sp, color = TextHint)
                                    Text("Rp $ownerShare", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FarmBlue)
                                }
                                Box(Modifier.width(1.dp).height(32.dp).background(DividerGray))
                                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Pengurus", fontSize = 10.sp, color = TextHint)
                                    Text("Rp $pengurusShare", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FarmOrange)
                                }
                                Box(Modifier.width(1.dp).height(32.dp).background(DividerGray))
                                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Status", fontSize = 10.sp, color = TextHint)
                                    Text(penjualan.status, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = statusColor)
                                }
                            }

                            // Buyer info
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceLight).padding(8.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text("Pembeli", fontSize = 10.sp, color = TextHint)
                                    Text(penjualan.buyerName.ifEmpty { "N/A" }, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text("Kontak", fontSize = 10.sp, color = TextHint)
                                    Text(penjualan.buyerPhone.ifEmpty { "N/A" }, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Reject Dialog ──
    if (showRejectDialog && rejectPenjualan != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Tolak Penjualan", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Alasan menolak penjualan ${rejectPenjualan!!.hewanNama}:", fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Alasan") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        farmViewModel.rejectPenjualan(rejectPenjualan!!, rejectReason, user?.userId ?: "", user?.role ?: "")
                        showRejectDialog = false
                        rejectReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FarmRed),
                    enabled = rejectReason.isNotBlank()
                ) {
                    Text("Tolak")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun StatCol(icon: ImageVector, value: String, label: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(2.dp))
        Text(value, color = tint, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, color = tint.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
        Text(label, fontSize = 10.sp, color = TextHint)
    }
}

@Composable
private fun PriceCol(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextHint)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun EmptyRow(msg: String) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Inbox, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(6.dp))
            Text(msg, fontWeight = FontWeight.SemiBold, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun CompactTxRow(p: Penjualan, df: SimpleDateFormat) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceLight).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(p.hewanNama.ifEmpty { "Hewan #${p.hewanId.take(8)}" }, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
            Text(df.format(p.createdAt.toDate()), fontSize = 10.sp, color = TextHint)
        }
        Text("Rp ${p.hargaJual}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FarmGreen)
        Spacer(Modifier.width(8.dp))
        val pAmt = p.hargaJual - p.hargaModal
        Text(
            if (pAmt >= 0) "+Rp $pAmt" else "-Rp ${-pAmt}",
            fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
            color = if (pAmt >= 0) FarmGreen else FarmRed
        )
    }
}
