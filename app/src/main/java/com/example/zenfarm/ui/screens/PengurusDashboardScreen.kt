package com.example.zenfarm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.rounded.*
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
import com.example.zenfarm.ui.theme.*
import com.example.zenfarm.viewmodel.AuthViewModel
import com.example.zenfarm.viewmodel.FarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengurusDashboardScreen(
    authViewModel: AuthViewModel,
    farmViewModel: FarmViewModel,
    navController: NavController
) {
    val user by authViewModel.user.collectAsState()
    val silsilahs by farmViewModel.silsilahs.collectAsState()
    val isLoading by farmViewModel.isLoading.collectAsState()
    var isNavigating by remember { mutableStateOf(false) }

    // Use backStackEntry to reset isNavigating when returning to this screen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        isNavigating = false
    }

    LaunchedEffect(user) {
        user?.let {
            farmViewModel.fetchSilsilahSaya(it.userId, it.role)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
            // ── Custom Header with Gradient ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFB300), Color(0xFFBF360C))
                        )
                    )
                    .statusBarsPadding()
                    .padding(bottom = 20.dp, start = 20.dp, end = 20.dp, top = 16.dp)
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
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Profile Pengurus",
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                tint = Color.White
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Engineering,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Pengurus Ternak",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                }
            }

            // ── Dashboard Stats ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Silsilah — Full-Width Horizontal Banner Card ──
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = FarmGreen.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(FarmGreen.copy(alpha = 0.13f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountTree,
                                        contentDescription = "Silsilah",
                                        tint = FarmGreen,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        "Silsilah Ternak",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FarmGreenDark,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "${silsilahs.size} Diklaim",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            // Count badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(FarmGreen.copy(alpha = 0.18f))
                                    .padding(horizontal = 18.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${silsilahs.size}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FarmGreenDark,
                                    fontSize = 22.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // ── Silsilah List Header ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountTree,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Silsilah Saya",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ── Content ──
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = FarmOrange
                    )
                } else if (silsilahs.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(FarmOrange.copy(alpha = 0.13f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_cow),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Belum ada silsilah diklaim",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Text(
                            "Cek GLOBAL untuk tawaran baru",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(silsilahs) { s ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFE65100).copy(alpha = 0.5f))
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
                                    // Icon
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFFF0F4F8)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_sheep),
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(14.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            s.namaSilsilah,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFFE65100)
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
                                    
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronRight,
                                        contentDescription = null,
                                        tint = FarmOrange,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
}
