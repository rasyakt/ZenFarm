package com.example.zenfarm.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import com.example.zenfarm.data.FarmRepository
import com.example.zenfarm.data.Hewan
import com.example.zenfarm.data.User
import com.example.zenfarm.viewmodel.AuthViewModel
import com.example.zenfarm.viewmodel.FarmViewModel
import kotlinx.coroutines.launch

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Constants
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
private const val MIN_SCALE = 0.2f
private const val MAX_SCALE = 2.5f
private val MIN_CHILD_SPACING = 120.dp
private val NODE_WIDTH = 180.dp
private val CENTER_SPACING = NODE_WIDTH + MIN_CHILD_SPACING
private val PARTNER_CONN_WIDTH = 68.dp // 36dp icon + 2*16dp padding
private val PAIR_WIDTH = NODE_WIDTH + PARTNER_CONN_WIDTH + NODE_WIDTH

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Main Screen
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilsilahCanvasScreen(
    silsilahId: String,
    authViewModel: AuthViewModel,
    farmViewModel: FarmViewModel,
    navController: NavController
) {
    val user by authViewModel.user.collectAsState()
    val hewans by farmViewModel.hewans.collectAsState()
    val isLoading by farmViewModel.isLoading.collectAsState()

    val isPemilik = user?.role?.equals("Pemilik", ignoreCase = true) == true
    val isPengurus = user?.role?.equals("Pengurus", ignoreCase = true) == true

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Dialog states
    var hewanToSell by remember { mutableStateOf<Hewan?>(null) }
    var sellPriceText by remember { mutableStateOf("") }
    var buyerNameText by remember { mutableStateOf("") }
    var buyerPhoneText by remember { mutableStateOf("") }
    
    var partnerInfoPair by remember { mutableStateOf<Pair<Hewan, Hewan>?>(null) }
    var hewanToView by remember { mutableStateOf<Hewan?>(null) }
    var showSellerContact by remember { mutableStateOf(false) }
    var sellerUser by remember { mutableStateOf<User?>(null) }

    // Canvas transform states
    var scale by remember { mutableFloatStateOf(1f) }
    val offsetAnimatable = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var contentSize by remember { mutableStateOf(IntSize.Zero) }

    // Repository for fetching seller info
    val repository = remember { FarmRepository() }

    LaunchedEffect(silsilahId) {
        farmViewModel.fetchHewanSilsilah(silsilahId)
    }

    // Helper: clamp offsets so tree stays partially visible
    fun clampOffset(offset: Offset, currentScale: Float): Offset {
        if (contentSize.width == 0 || contentSize.height == 0) return offset
        val scaledW = contentSize.width * currentScale
        val scaledH = contentSize.height * currentScale
        val maxX = (scaledW / 2f + viewportSize.width / 2f)
        val maxY = (scaledH / 2f + viewportSize.height / 2f)
        return Offset(
            x = offset.x.coerceIn(-maxX, maxX),
            y = offset.y.coerceIn(-maxY, maxY)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            val currentSilsilah by farmViewModel.currentSilsilah.collectAsState()
            val silsilahStatus = currentSilsilah?.status?.uppercase() ?: "..."
            val silsilahColor = if (silsilahStatus == "AKTIF") Color(0xFF2E7D32) else Color(0xFFD32F2F)

            TopAppBar(
                title = { 
                    Column {
                        Text("Pohon Silsilah", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Status: $silsilahStatus", 
                            style = MaterialTheme.typography.labelSmall,
                            color = silsilahColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Brush.radialGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE8F5E9))))
                .farmGridPattern()
                .onGloballyPositioned { viewportSize = it.size }
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (hewans.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Data induk belum tersedia. Mohon periksa data silsilah.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                val rootAnimal = hewans.find { it.parentId == null }
                if (rootAnimal != null) {
                    // Canvas gesture container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val newScale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                                    scale = newScale
                                    val newOffset = clampOffset(
                                        offsetAnimatable.value + pan,
                                        newScale
                                    )
                                    scope.launch {
                                        offsetAnimatable.snapTo(newOffset)
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        // Reset to center & default zoom
                                        scale = 1f
                                        scope.launch {
                                            offsetAnimatable.animateTo(
                                                Offset.Zero,
                                                animationSpec = spring()
                                            )
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = offsetAnimatable.value.x
                                    translationY = offsetAnimatable.value.y
                                }
                                .wrapContentSize(unbounded = true)
                                .onGloballyPositioned { contentSize = it.size }
                                .padding(24.dp)
                        ) {
                            FamilyTreeLevel(
                                betina = rootAnimal,
                                allHewans = hewans,
                                silsilahId = silsilahId,
                                navController = navController,
                                isPengurus = isPengurus,
                                isPemilik = isPemilik,
                                onSellClick = { hewanToSell = it },
                                onAddChildClick = {
                                    navController.navigate("tambah_anak/$silsilahId?parentId=${it.hewanId}")
                                },
                                onPartnerClick = { female, male ->
                                    partnerInfoPair = female to male
                                },
                                onViewDetailClick = { hewanToView = it },
                                onNodeTap = { coords ->
                                    // Animate canvas to center the tapped node
                                    val nodePos = coords.positionInRoot()
                                    val nodeCenterX = nodePos.x + coords.size.width / 2f
                                    val nodeCenterY = nodePos.y + coords.size.height / 2f
                                    val vpCenterX = viewportSize.width / 2f
                                    val vpCenterY = viewportSize.height / 2f
                                    val targetOffset = clampOffset(
                                        Offset(
                                            vpCenterX - nodeCenterX * scale,
                                            vpCenterY - nodeCenterY * scale
                                        ),
                                        scale
                                    )
                                    scope.launch {
                                        offsetAnimatable.animateTo(
                                            targetOffset,
                                            animationSpec = spring()
                                        )
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Text("Root tidak ditemukan. Silsilah rusak.", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // â”€â”€ Detail & Status Dialog (Both roles) â”€â”€
        if (hewanToView != null) {
            AnimalDetailDialog(
                hewan = hewanToView!!,
                isPengurus = isPengurus,
                onUpdateStatus = { newStatus ->
                    farmViewModel.updateStatusHewan(
                        silsilahId = silsilahId,
                        hewanId = hewanToView!!.hewanId,
                        newStatus = newStatus,
                        userRole = user?.role ?: "",
                        onSuccess = { hewanToView = null }
                    )
                },
                onDismiss = { hewanToView = null }
            )
        }

        // â”€â”€ Sell Dialog (Pengurus flow â€“ with confirmation + margin) â”€â”€
        if (hewanToSell != null && isPengurus) {
            var confirmStep by remember { mutableIntStateOf(1) }
            
            if (confirmStep == 1) {
                // Step 1: Confirmation
                AlertDialog(
                    onDismissRequest = { hewanToSell = null; confirmStep = 1 },
                    shape = RoundedCornerShape(20.dp),
                    title = { Text("Konfirmasi Penjualan", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Yakin ingin menjual hewan \"${hewanToSell?.nama}\"?")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Harga Modal: Rp ${hewanToSell?.harga ?: 0}", color = Color.Gray)
                        }
                    },
                    confirmButton = {
                        Button(onClick = { confirmStep = 2 }) {
                            Text("Ya, Lanjutkan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { hewanToSell = null; confirmStep = 1 }) { Text("Batal") }
                    }
                )
            } else {
                // Step 2: Fill sale details
                val hargaModal = hewanToSell?.harga ?: 0
                val hargaJualInt = sellPriceText.toIntOrNull() ?: 0
                val margin = hargaJualInt - hargaModal
                
                AlertDialog(
                    onDismissRequest = { hewanToSell = null; confirmStep = 1 },
                    shape = RoundedCornerShape(20.dp),
                    title = { Text("Detail Penjualan", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Hewan: ${hewanToSell?.nama}", fontWeight = FontWeight.SemiBold)
                            Text("Harga Modal: Rp $hargaModal", color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = sellPriceText,
                                onValueChange = { sellPriceText = it },
                                label = { Text("Harga Jual") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            // Margin display
                            if (sellPriceText.isNotBlank() && hargaJualInt > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val marginColor = when {
                                    margin > 0 -> Color(0xFF4CAF50)
                                    margin < 0 -> Color(0xFFD32F2F)
                                    else -> Color.Gray
                                }
                                val marginText = when {
                                    margin > 0 -> "Untung: Rp $margin"
                                    margin < 0 -> "Rugi: Rp ${-margin}"
                                    else -> "Tidak memiliki keuntungan"
                                }
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = marginColor.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = marginText,
                                        color = marginColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = buyerNameText,
                                onValueChange = { buyerNameText = it },
                                label = { Text("Nama Pembeli") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = buyerPhoneText,
                                onValueChange = { buyerPhoneText = it },
                                label = { Text("Telepon Pembeli") },
                                placeholder = { Text("08xxx...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            enabled = sellPriceText.isNotBlank() && buyerNameText.isNotBlank() && buyerPhoneText.isNotBlank(),
                            onClick = {
                                hewanToSell?.let {
                                    user?.let { u ->
                                        farmViewModel.submitPenjualan(
                                            it, 
                                            hargaJualInt, 
                                            u.userId,
                                            u.role,
                                            buyerNameText,
                                            buyerPhoneText,
                                            onSuccess = { isDirect ->
                                                val msg = if (isDirect) "Penjualan berhasil diproses secara langsung!" else "Pengajuan penjualan berhasil terkirim!"
                                                scope.launch { snackbarHostState.showSnackbar(msg) }
                                                // Refresh balance
                                                authViewModel.refreshUser(u.userId)
                                            }
                                        )
                                    }
                                }
                                hewanToSell = null
                                confirmStep = 1
                                buyerNameText = ""
                                buyerPhoneText = ""
                                sellPriceText = ""
                            }
                        ) { Text("Ajukan Penjualan") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmStep = 1 }) { Text("Kembali") }
                    }
                )
            }
        }

        // â”€â”€ Sell Dialog (Pemilik flow â€“ shows seller contact) â”€â”€
        if (hewanToSell != null && isPemilik) {
            // Fetch pengurus info
            LaunchedEffect(hewanToSell) {
                val silsilahList = repository.getSilsilahByOwner(user?.userId ?: "")
                val silsilah = silsilahList.find { it.silsilahId == silsilahId }
                if (silsilah != null && silsilah.pengurusId.isNotEmpty()) {
                    sellerUser = repository.getUserById(silsilah.pengurusId)
                }
                showSellerContact = true
            }

            if (showSellerContact) {
                SellerContactDialog(
                    hewanName = hewanToSell?.nama ?: "",
                    sellerUser = sellerUser,
                    onDismiss = {
                        hewanToSell = null
                        showSellerContact = false
                        sellerUser = null
                    }
                )
            }
        }

        // â”€â”€ Partner Info Dialog â”€â”€
        if (partnerInfoPair != null) {
            PartnerInfoDialog(
                female = partnerInfoPair!!.first,
                male = partnerInfoPair!!.second,
                onDismiss = { partnerInfoPair = null }
            )
        }
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Helpers
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Recursively calculates the total width required for a complete family branch.
 */
@Composable
fun calculateSubtreeWidth(betina: Hewan, allHewans: List<Hewan>): androidx.compose.ui.unit.Dp {
    val isPair = betina.jenisKelamin.equals("BETINA", ignoreCase = true) && betina.pasanganId != null
    val rawOwnWidth = if (isPair) PAIR_WIDTH else NODE_WIDTH
    
    // Filter legitimate children (Betina or unpaired males)
    val children = allHewans.filter { 
        it.parentId == betina.hewanId && 
        (it.jenisKelamin.equals("BETINA", ignoreCase = true) || it.pasanganId == null) 
    }
    
    if (children.isEmpty()) return rawOwnWidth
    
    var totalChildrenWidth = 0.dp
    children.forEachIndexed { index, child ->
        totalChildrenWidth += calculateSubtreeWidth(child, allHewans)
        if (index < children.size - 1) {
            totalChildrenWidth += MIN_CHILD_SPACING
        }
    }
    
    // The branch must be at least as wide as its header node(s), and at least as wide as its combined children.
    return maxOf(rawOwnWidth, totalChildrenWidth)
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Family Tree Level â€“ Recursive layout for each mother + partner + children
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun FamilyTreeLevel(
    betina: Hewan,
    allHewans: List<Hewan>,
    silsilahId: String,
    navController: NavController,
    isPengurus: Boolean,
    isPemilik: Boolean,
    onSellClick: (Hewan) -> Unit,
    onAddChildClick: (Hewan) -> Unit,
    onPartnerClick: (Hewan, Hewan) -> Unit,
    onViewDetailClick: (Hewan) -> Unit,
    onNodeTap: (LayoutCoordinates) -> Unit
) {
    val children = allHewans.filter { 
        it.parentId == betina.hewanId && 
        (it.jenisKelamin.equals("BETINA", ignoreCase = true) || it.pasanganId == null) 
    }.sortedBy { it.tanggalLahir }
    
    val father = if (betina.pasanganId != null) allHewans.find { it.hewanId == betina.pasanganId } else null
    val ownSubtreeWidth = calculateSubtreeWidth(betina, allHewans)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        modifier = Modifier
            .width(ownSubtreeWidth)
            .padding(vertical = 8.dp)
    ) {
        // â”€â”€ Parent row: Female âœ• Male â”€â”€
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HewanNode(
                hewan = betina,
                isMate = false,
                silsilahId = silsilahId,
                navController = navController,
                isPengurus = isPengurus,
                isPemilik = isPemilik,
                onSellClick = { onSellClick(betina) },
                onAddChildClick = { onAddChildClick(betina) },
                onViewDetailClick = { onViewDetailClick(betina) },
                onNodeTap = onNodeTap
            )

            if (father != null) {
                Box(
                    modifier = Modifier
                        .zIndex(10f)
                        .padding(horizontal = 16.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF673AB7))
                        .clickable { onPartnerClick(betina, father) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Partner link",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                HewanNode(
                    hewan = father,
                    isMate = true,
                    silsilahId = silsilahId,
                    navController = navController,
                    isPengurus = isPengurus,
                    isPemilik = isPemilik,
                    onSellClick = { onSellClick(father) },
                    onAddChildClick = { onAddChildClick(father) },
                    onViewDetailClick = { onViewDetailClick(father) },
                    onNodeTap = onNodeTap
                )
            }
        }

        // â”€â”€ Children branch â”€â”€
        if (children.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .background(Color.Gray)
            )

            if (children.size == 1) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(Color.Gray)
                )
                val child = children[0]
                if (child.jenisKelamin.equals("BETINA", ignoreCase = true)) {
                    FamilyTreeLevel(child, allHewans, silsilahId, navController, isPengurus, isPemilik, onSellClick, onAddChildClick, onPartnerClick, onViewDetailClick, onNodeTap)
                } else {
                    HewanNode(
                        hewan = child, isMate = false, silsilahId = silsilahId,
                        navController = navController, isPengurus = isPengurus,
                        isPemilik = isPemilik,
                        onSellClick = { onSellClick(child) },
                        onAddChildClick = { onAddChildClick(child) },
                        onViewDetailClick = { onViewDetailClick(child) },
                        onNodeTap = onNodeTap
                    )
                }
            } else {
                // Calculate widths of each child's subtree for precise connector positioning
                val childSubtreeWidths = children.map { calculateSubtreeWidth(it, allHewans) }
                
                TreeBranchConnector(
                    children = children, 
                    childSubtreeWidths = childSubtreeWidths,
                    totalWidth = ownSubtreeWidth
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(MIN_CHILD_SPACING),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.wrapContentWidth()
                ) {
                    children.forEach { child ->
                        if (child.jenisKelamin.equals("BETINA", ignoreCase = true)) {
                            FamilyTreeLevel(child, allHewans, silsilahId, navController, isPengurus, isPemilik, onSellClick, onAddChildClick, onPartnerClick, onViewDetailClick, onNodeTap)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(NODE_WIDTH)) {
                                HewanNode(
                                    hewan = child, isMate = false, silsilahId = silsilahId,
                                    navController = navController, isPengurus = isPengurus,
                                    isPemilik = isPemilik,
                                    onSellClick = { onSellClick(child) },
                                    onAddChildClick = { onAddChildClick(child) },
                                    onViewDetailClick = { onViewDetailClick(child) },
                                    onNodeTap = onNodeTap
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Tree Branch Connector â€“ Horizontal bar with vertical drops for N children
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun TreeBranchConnector(
    children: List<Hewan>, 
    childSubtreeWidths: List<androidx.compose.ui.unit.Dp>,
    totalWidth: androidx.compose.ui.unit.Dp
) {
    val density = LocalDensity.current
    
    val widthsPx = childSubtreeWidths.map { with(density) { it.toPx() } }
    val spacingPx = with(density) { MIN_CHILD_SPACING.toPx() }

    // Calculate centers of each child subtree relative to the Row start
    val childCenters = mutableListOf<Float>()
    var currentX = 0f
    widthsPx.forEach { w ->
        childCenters.add(currentX + w / 2f)
        currentX += w + spacingPx
    }

    // The entire Row width might be smaller than the Parent row width (if parent has partner)
    // We adjust currentX to be the total Row width used by children
    val actualRowWidthPx = currentX - spacingPx
    val parentCenterPx = with(density) { totalWidth.toPx() } / 2f
    
    // Offset to align logic centers with the parent center
    val startOffsetPx = parentCenterPx - (actualRowWidthPx / 2f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        val strokeWidth = 2.dp.toPx()
        val color = Color.Gray
        val bottomY = size.height

        if (childCenters.isNotEmpty()) {
            childCenters.forEach { x ->
                val adjustedX = startOffsetPx + x
                val path = Path()
                path.moveTo(parentCenterPx, 0f)
                path.cubicTo(
                    parentCenterPx, size.height / 2f,
                    adjustedX, size.height / 2f,
                    adjustedX, bottomY
                )
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Hewan Node â€“ Individual animal card
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun HewanNode(
    hewan: Hewan,
    isMate: Boolean,
    silsilahId: String,
    navController: NavController,
    isPengurus: Boolean,
    isPemilik: Boolean,
    onSellClick: () -> Unit,
    onAddChildClick: () -> Unit,
    onViewDetailClick: () -> Unit,
    onNodeTap: (LayoutCoordinates) -> Unit
) {
    val isJantan = hewan.jenisKelamin.equals("JANTAN", ignoreCase = true)
    val isBetina = hewan.jenisKelamin.equals("BETINA", ignoreCase = true)
    val isHidup = hewan.status.equals("HIDUP", ignoreCase = true)
    val isSakit = hewan.status.equals("SAKIT", ignoreCase = true)
    val isActive = isHidup || isSakit

    val mainColor = if (isJantan) Color(0xFF1976D2) else Color(0xFFD81B60)
    val darkBg = if (isJantan) Color(0xFF0D47A1) else Color(0xFF880E4F)
    val gradientBrush = Brush.verticalGradient(colors = listOf(mainColor, darkBg))

    val statusColor = when {
        isHidup -> Color(0xFF4CAF50)
        isSakit -> Color(0xFFFF9800)
        hewan.status.equals("MATI", true) -> Color(0xFFF44336)
        hewan.status.equals("TERJUAL", true) -> Color(0xFF607D8B)
        else -> Color(0xFF9E9E9E)
    }

    var nodeCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Card(
        modifier = Modifier
            .requiredWidth(200.dp)
            .padding(8.dp)
            .onGloballyPositioned { nodeCoords = it }
            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = mainColor.copy(alpha = 0.4f))
            .clickable { 
                nodeCoords?.let { onNodeTap(it) }
                onViewDetailClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, mainColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .background(gradientBrush)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // â”€â”€ Badges Row â”€â”€
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gender Badge
                Surface(
                    shape = RoundedCornerShape(100),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (isJantan) Icons.Rounded.Male else Icons.Rounded.Female,
                            contentDescription = null,
                            tint = mainColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = hewan.jenisKelamin.uppercase(),
                            color = mainColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(100),
                    color = statusColor.copy(alpha = 0.9f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.8f))
                        )
                        Text(
                            text = hewan.status.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // â”€â”€ Photo â”€â”€
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(2.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (hewan.fotoUri.isNotEmpty()) {
                    val isBase64 = !hewan.fotoUri.startsWith("content:") &&
                                   !hewan.fotoUri.startsWith("file:") &&
                                   (hewan.fotoUri.length > 500 || !hewan.fotoUri.startsWith("/"))

                    if (isBase64) {
                        val imageBitmap = com.example.zenfarm.utils.rememberBase64Image(hewan.fotoUri)
                        if (imageBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = imageBitmap,
                                contentDescription = "Foto ${hewan.nama}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Pets,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    } else {
                        val imageModel = remember(hewan.fotoUri) {
                            when {
                                hewan.fotoUri.startsWith("/") -> java.io.File(hewan.fotoUri)
                                hewan.fotoUri.startsWith("content:") || hewan.fotoUri.startsWith("file:") ->
                                    android.net.Uri.parse(hewan.fotoUri)
                                else -> hewan.fotoUri
                            }
                        }
                        coil.compose.AsyncImage(
                            model = imageModel,
                            contentDescription = "Foto ${hewan.nama}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = com.example.zenfarm.R.drawable.ic_cow),
                            placeholder = painterResource(id = com.example.zenfarm.R.drawable.ic_cow)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Pets,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // â”€â”€ Name â”€
            Text(
                text = hewan.nama,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // â”€â”€ Ownership Badge â”€â”€
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VerifiedUser,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = hewan.hakPembagian,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // â”€â”€ Action Buttons â”€â”€

            // Jual button (Pengurus only)
            if (isPengurus) {
                Button(
                    onClick = onSellClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    enabled = isActive,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.3f),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Jual",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // View Details (both roles)
            OutlinedButton(
                onClick = onViewDetailClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.4f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Detail",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            // Modification actions (Pengurus only, Betina only)
            if (isPengurus && isBetina) {
                Spacer(modifier = Modifier.height(6.dp))

                val partnerText = if (hewan.pasanganId != null) "Ganti Pasangan" else "Tambah Pasangan"
                OutlinedButton(
                    onClick = { navController.navigate("tambah_pasangan/$silsilahId?indukBetinaId=${hewan.hewanId}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    enabled = isActive,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        partnerText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onAddChildClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    enabled = isActive,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddCircle,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Tambah Anak",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}




@Composable
fun PartnerInfoDialog(female: Hewan, male: Hewan, onDismiss: () -> Unit) {
    val dateFormatter = remember {
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Informasi Pasangan", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Female info
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("â™€ Betina", fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
                        Text("Nama: ${female.nama}")
                        Text("Status: ${female.status}")
                    }
                }

                // Male info
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("â™‚ Jantan", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                        Text("Nama: ${male.nama}")
                        Text("Status: ${male.status}")
                    }
                }

                HorizontalDivider()

                // Relationship metadata
                Text(
                    "Tanggal Pasangan: ${dateFormatter.format(male.createdAt.toDate())}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Catatan: Tidak ada catatan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Animal Detail Dialog (Interactive with Status form)
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun AnimalDetailDialog(
    hewan: Hewan, 
    isPengurus: Boolean, 
    onUpdateStatus: (String) -> Unit, 
    onDismiss: () -> Unit
) {
    val dateFormatter = remember {
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    }
    val statusOptions = listOf("HIDUP", "SAKIT", "MATI", "DIPULANGKAN", "HILANG")
    var selectedStatus by remember { mutableStateOf(hewan.status.uppercase()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Detail Hewan", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (hewan.fotoUri.isNotEmpty()) {
                    // Check if it's a Base64 string
                    val isBase64 = !hewan.fotoUri.startsWith("content:") && 
                                   !hewan.fotoUri.startsWith("file:") && 
                                   (hewan.fotoUri.length > 500 || !hewan.fotoUri.startsWith("/"))
                    
                    android.util.Log.d("AnimalDetailDialog", "=== IMAGE DISPLAY ===")
                    android.util.Log.d("AnimalDetailDialog", "Hewan: ${hewan.nama}")
                    android.util.Log.d("AnimalDetailDialog", "fotoUri length: ${hewan.fotoUri.length}")
                    android.util.Log.d("AnimalDetailDialog", "isBase64: $isBase64")
                    android.util.Log.d("AnimalDetailDialog", "First 100 chars: ${hewan.fotoUri.take(100)}")
                    
                    if (isBase64) {
                        android.util.Log.d("AnimalDetailDialog", "Using Base64 decoder")
                        // Use custom Base64 decoder
                        val imageBitmap = com.example.zenfarm.utils.rememberBase64Image(hewan.fotoUri)
                        
                        if (imageBitmap != null) {
                            android.util.Log.d("AnimalDetailDialog", "âœ… Image decoded successfully")
                            androidx.compose.foundation.Image(
                                bitmap = imageBitmap,
                                contentDescription = "Foto ${hewan.nama}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            android.util.Log.e("AnimalDetailDialog", "âŒ Image decode failed - showing placeholder")
                            // Fallback to placeholder if decode fails
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = com.example.zenfarm.R.drawable.ic_cow),
                                    contentDescription = "Placeholder",
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    } else {
                        android.util.Log.d("AnimalDetailDialog", "Using Coil for file/URI")
                        // Use Coil for file paths and URIs
                        val imageModel = remember(hewan.fotoUri) {
                            when {
                                hewan.fotoUri.startsWith("/") -> java.io.File(hewan.fotoUri)
                                hewan.fotoUri.startsWith("content:") || hewan.fotoUri.startsWith("file:") -> 
                                    android.net.Uri.parse(hewan.fotoUri)
                                else -> hewan.fotoUri
                            }
                        }
                        coil.compose.AsyncImage(
                            model = imageModel,
                            contentDescription = "Foto ${hewan.nama}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = com.example.zenfarm.R.drawable.ic_cow),
                            placeholder = painterResource(id = com.example.zenfarm.R.drawable.ic_cow)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                DetailRow("Nama", hewan.nama)
                DetailRow("Jenis Kelamin", hewan.jenisKelamin.uppercase())
                DetailRow("Tanggal Lahir", dateFormatter.format(hewan.tanggalLahir.toDate()))
                DetailRow("Status", hewan.status.uppercase())
                DetailRow("Hak Pembagian", hewan.hakPembagian)
                DetailRow("Sumber Kepemilikan", hewan.ownershipSource)
                if (hewan.harga > 0) {
                    DetailRow("Harga Modal", "Rp ${hewan.harga}")
                }
                
                // Form Update Status
                if (isPengurus && !hewan.status.equals("TERJUAL", ignoreCase = true)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Ubah Status Cepat:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    
                    // Render status options
                    Column(modifier = Modifier.fillMaxWidth()) {
                        statusOptions.forEach { statusOpt ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedStatus == statusOpt,
                                    onClick = { selectedStatus = statusOpt }
                                )
                                Text(
                                    text = statusOpt, 
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isPengurus && selectedStatus != hewan.status.uppercase() && !hewan.status.equals("TERJUAL", true)) {
                Button(onClick = { onUpdateStatus(selectedStatus) }) {
                    Text("Update Status")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Tutup") }
            }
        },
        dismissButton = {
            if (isPengurus && selectedStatus != hewan.status.uppercase() && !hewan.status.equals("TERJUAL", true)) {
                TextButton(onClick = onDismiss) { Text("Batal") }
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            "$label:",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(130.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Seller Contact Dialog (for Pemilik on "Jual" tap)
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun SellerContactDialog(hewanName: String, sellerUser: User?, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val hasEmail = sellerUser != null && sellerUser.email.isNotEmpty()
    // User model has no phone field â€“ future-proofed
    val hasPhone = false // No phone field in current User model

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Informasi Penjual", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Kontak pengurus untuk hewan \"$hewanName\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                HorizontalDivider()

                DetailRow("Nama", sellerUser?.name ?: "Tidak tersedia")
                DetailRow("Email", sellerUser?.email ?: "Tidak tersedia")
                DetailRow("Telepon", "Tidak tersedia")
                DetailRow("Lokasi", "Tidak tersedia")

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Button(
                    onClick = {
                        if (hasEmail) {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${sellerUser!!.email}")
                                putExtra(Intent.EXTRA_SUBJECT, "Penjualan Hewan: $hewanName")
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasEmail,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (hasEmail) "Send Email" else "Email Tidak Tersedia")
                }

                Button(
                    onClick = {
                        // Future: when phone field exists
                        // val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone"))
                        // context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasPhone,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (hasPhone) "Chat WhatsApp" else "WhatsApp Tidak Tersedia")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// Extension for background grid pattern
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
fun Modifier.farmGridPattern(
    dotRadius: Float = 2f,
    spacing: Float = 60f,
    circleColor: Color = Color.LightGray.copy(alpha = 0.4f)
) = this.drawBehind {
    val canvasWidth = size.width
    val canvasHeight = size.height
    var x = 0f
    while (x < canvasWidth) {
        var y = 0f
        while (y < canvasHeight) {
            drawCircle(color = circleColor, radius = dotRadius, center = Offset(x, y))
            y += spacing
        }
        x += spacing
    }
}
