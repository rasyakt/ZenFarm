package com.example.zenfarm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.zenfarm.data.Hewan
import com.example.zenfarm.viewmodel.AuthViewModel
import com.example.zenfarm.viewmodel.FarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    farmViewModel: FarmViewModel,
    navController: NavController
) {
    val user by authViewModel.user.collectAsState()
    val userHewans by farmViewModel.userHewans.collectAsState()
    val isLoading by farmViewModel.isLoading.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var statusFilter by remember { mutableStateOf("SEMUA") }
    
    val scrollState = rememberScrollState()
    var isNavigating by remember { mutableStateOf(false) }

    // Use backStackEntry to reset isNavigating when returning to this screen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        isNavigating = false
    }

    LaunchedEffect(user) {
        user?.let {
            farmViewModel.fetchUserHewans(it.userId, it.role)
        }
    }

    val filteredHewans = remember(userHewans, statusFilter) {
        if (statusFilter == "SEMUA") userHewans
        else userHewans.filter { it.status.equals(statusFilter, ignoreCase = true) }
    }

    Scaffold(
        containerColor = SurfaceLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // ── Premium Header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = if (user?.role == "Pemilik") 
                                listOf(FarmGreen, FarmGreenDark) 
                            else 
                                listOf(FarmOrange, Color(0xFFE65100))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { 
                            if (!isNavigating) {
                                isNavigating = true
                                navController.popBackStack() 
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(16.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Image
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize().clip(CircleShape).padding(8.dp),
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = user?.name ?: "User Name",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = user?.email ?: "email@example.com",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ── Tabs ──
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = if (user?.role == "Pemilik") FarmGreen else FarmOrange,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = if (user?.role == "Pemilik") FarmGreen else FarmOrange
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Informasi Profil", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Daftar Hewan", fontWeight = FontWeight.SemiBold) }
                )
            }

            // ── Content ──
            Box(modifier = Modifier.weight(1f)) {
                if (selectedTab == 0) {
                    // TAB: PROFILE INFO
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProfileItemCard(
                            icon = "⭐",
                            label = "Peran",
                            value = user?.role ?: "User",
                            color = if (user?.role == "Pemilik") FarmGreen else FarmOrange
                        )

                        ProfileItemCard(
                            icon = "💰",
                            label = "Total Saldo",
                            value = "Rp ${user?.saldo ?: 0}",
                            color = FarmOrange
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (!isNavigating) {
                                    isNavigating = true
                                    authViewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FarmRed,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Keluar dari Akun", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        
                        Text(
                            text = "ZenFarm v1.0.5 - Premium Edition",
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    // TAB: ANIMAL LIST
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Filters
                        val statuses = listOf("SEMUA", "HIDUP", "SAKIT", "MATI", "TERJUAL", "DIPULANGKAN")
                        androidx.compose.foundation.lazy.LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(statuses) { status ->
                                FilterChip(
                                    selected = statusFilter == status,
                                    onClick = { statusFilter = status },
                                    label = { Text(status, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (user?.role == "Pemilik") FarmGreen else FarmOrange,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        if (isLoading && userHewans.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = if (user?.role == "Pemilik") FarmGreen else FarmOrange)
                            }
                        } else if (filteredHewans.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📭", fontSize = 48.sp)
                                    Text("Tidak ada hewan ditemukan", color = Color.Gray)
                                }
                            }
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredHewans) { hewan ->
                                    AnimalListCard(hewan)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimalListCard(hewan: Hewan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon or mini photo placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Gray.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (hewan.jenisKelamin == "JANTAN") "♂️" else "♀️", fontSize = 24.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(hewan.nama, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Usia: ${hewan.level} Generasi", fontSize = 12.sp, color = Color.Gray)
            }
            
            // Status Tag
            val statusColor = when (hewan.status.uppercase()) {
                "HIDUP" -> FarmGreen
                "SAKIT" -> Color(0xFFFBC02D)
                "MATI" -> FarmRed
                "TERJUAL", "DIJUAL" -> FarmBlue
                "DIPULANGKAN" -> Color.Gray
                else -> Color.Gray
            }
            
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f))
            ) {
                Text(
                    text = hewan.status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileItemCard(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, color = Color.Gray, fontSize = 12.sp)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            }
        }
    }
}
