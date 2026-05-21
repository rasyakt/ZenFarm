package com.example.zenfarm.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenfarm.data.FarmRepository
import com.example.zenfarm.data.Hewan
import com.example.zenfarm.data.Penjualan
import com.example.zenfarm.data.Silsilah
import com.example.zenfarm.data.User
import com.example.zenfarm.utils.ImageUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import kotlin.math.ceil

class FarmViewModel : ViewModel() {
    val repository = FarmRepository()

    private val _silsilahs = MutableStateFlow<List<Silsilah>>(emptyList())
    val silsilahs: StateFlow<List<Silsilah>> = _silsilahs.asStateFlow()

    private val _globalSilsilahs = MutableStateFlow<List<Silsilah>>(emptyList())
    val globalSilsilahs: StateFlow<List<Silsilah>> = _globalSilsilahs.asStateFlow()

    private val _hewans = MutableStateFlow<List<Hewan>>(emptyList())
    val hewans: StateFlow<List<Hewan>> = _hewans.asStateFlow()

    private val _currentSilsilah = MutableStateFlow<Silsilah?>(null)
    val currentSilsilah: StateFlow<Silsilah?> = _currentSilsilah.asStateFlow()

    private val _pendingPenjualans = MutableStateFlow<List<Penjualan>>(emptyList())
    val pendingPenjualans: StateFlow<List<Penjualan>> = _pendingPenjualans.asStateFlow()

    private val _riwayatPenjualan = MutableStateFlow<List<Penjualan>>(emptyList())
    val riwayatPenjualan: StateFlow<List<Penjualan>> = _riwayatPenjualan.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userHewans = MutableStateFlow<List<Hewan>>(emptyList())
    val userHewans: StateFlow<List<Hewan>> = _userHewans.asStateFlow()

    // Dashboard Fetchs
    fun fetchSilsilahSaya(userId: String, role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = if (role == "Pemilik") {
                    repository.getSilsilahByOwner(userId)
                } else {
                    repository.getSilsilahByPengurus(userId)
                }
                _silsilahs.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchGlobalSilsilahs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _globalSilsilahs.value = repository.getGlobalSilsilahs()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchHewanSilsilah(silsilahId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _hewans.value = repository.getHewanBySilsilah(silsilahId)
                
                // Also fetch Silsilah metadata
                _currentSilsilah.value = repository.getSilsilahById(silsilahId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun fetchPendingPenjualan(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetched = repository.getPenjualanPendingByOwner(ownerId)
                val activeList = mutableListOf<Penjualan>()

                for (p in fetched) {
                    try {
                        if (!autoCancelIfExpired(p)) {
                            activeList.add(p)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        activeList.add(p)
                    }
                }
                _pendingPenjualans.value = activeList
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchUserHewans(userId: String, role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _userHewans.value = repository.getHewanByUser(userId, role)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun autoCancelIfExpired(penjualan: Penjualan): Boolean {
        val now = System.currentTimeMillis()
        val createdAtMs = penjualan.createdAt.toDate().time
        val diff = now - createdAtMs
        val threeDays = 3 * 24 * 60 * 60 * 1000L

        if (diff > threeDays) {
            val updated = penjualan.copy(status = "AUTO_CANCEL")
            repository.updatePenjualan(updated)
            return true
        }
        return false
    }

    // Role: Pemilik action - Workflow Step 1, 2, 3
    fun daftarSilsilah(
        context: Context,
        silsilahNama: String,
        hewanNama: String,
        jenisKelamin: String,
        tanggalLahirStr: String,
        ownerId: String,
        fotoUri: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        android.util.Log.d("FarmViewModel", "========================================")
        android.util.Log.d("FarmViewModel", "daftarSilsilah CALLED")
        android.util.Log.d("FarmViewModel", "  silsilahNama: $silsilahNama")
        android.util.Log.d("FarmViewModel", "  hewanNama: $hewanNama")
        android.util.Log.d("FarmViewModel", "  jenisKelamin: $jenisKelamin")
        android.util.Log.d("FarmViewModel", "  tanggalLahir: $tanggalLahirStr")
        android.util.Log.d("FarmViewModel", "  ownerId: $ownerId")
        android.util.Log.d("FarmViewModel", "  fotoUri: ${fotoUri.take(100)}...")
        android.util.Log.d("FarmViewModel", "========================================")
        
        viewModelScope.launch {
            try {
                android.util.Log.d("FarmViewModel", "Step 1: Setting isLoading = true")
                _isLoading.value = true
                
                android.util.Log.d("FarmViewModel", "Step 2: Processing photo...")
                // Convert foto ke Base64 (disimpan di Firestore, tidak perlu Firebase Storage)
                val uploadedImageUrl = if (fotoUri.isNotEmpty()) {
                    android.util.Log.d("DaftarSilsilah", "Converting photo to Base64 from URI: $fotoUri")
                    try {
                        val result = ImageUploader.uploadImage(context, fotoUri, "silsilah")
                        android.util.Log.d("DaftarSilsilah", "Photo conversion SUCCESS (length: ${result.length})")
                        result
                    } catch (e: Exception) {
                        android.util.Log.e("DaftarSilsilah", "Photo conversion FAILED: ${e.message}", e)
                        throw e
                    }
                } else {
                    android.util.Log.d("DaftarSilsilah", "No photo provided, skipping...")
                    ""
                }
                
                android.util.Log.d("FarmViewModel", "Step 3: Creating Firestore references...")
                val silsilahRef = repository.db.collection("silsilah").document()
                val hewanRef = repository.db.collection("hewan").document()
                val silsilahId = silsilahRef.id
                android.util.Log.d("FarmViewModel", "  silsilahId: $silsilahId")
                android.util.Log.d("FarmViewModel", "  hewanId: ${hewanRef.id}")

                android.util.Log.d("FarmViewModel", "Step 4: Parsing date...")
                val parsedDate = try {
                    val date = LocalDate.parse(tanggalLahirStr)
                    android.util.Log.d("FarmViewModel", "  Date parsed: $date")
                    date
                } catch (e: Exception) {
                    android.util.Log.e("FarmViewModel", "  Date parse failed, using today: ${e.message}")
                    LocalDate.now()
                }
                
                val birthTimestamp = Timestamp(
                    java.util.Date.from(parsedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                )
                android.util.Log.d("FarmViewModel", "  Timestamp: $birthTimestamp")

                android.util.Log.d("FarmViewModel", "Step 5: Starting Firestore transaction...")
                repository.db.runTransaction { transaction ->
                    android.util.Log.d("FarmViewModel", "  Transaction started")
                    
                    // 1. Create Silsilah
                    val masterSilsilah = Silsilah(
                        silsilahId = silsilahId,
                        namaSilsilah = silsilahNama,
                        ownerId = ownerId,
                        status = "GLOBAL",
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )
                    android.util.Log.d("FarmViewModel", "  Creating Silsilah: $masterSilsilah")
                    transaction.set(silsilahRef, masterSilsilah)
                    android.util.Log.d("FarmViewModel", "  Silsilah set in transaction")

                    // 2. Create Root Animal (induk pertama)
                    val rootHewan = Hewan(
                        hewanId = hewanRef.id,
                        silsilahId = silsilahId,
                        nama = hewanNama,
                        jenisKelamin = jenisKelamin.uppercase(),
                        tanggalLahir = birthTimestamp,
                        status = "HIDUP",
                        hakPembagian = "Pemilik",
                        ownershipSource = "PEMILIK",
                        fotoUri = uploadedImageUrl, // Simpan Base64 string di Firestore
                        parentId = null, // Root parentId MUST be null
                        level = 0,       // Root level is 0
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )
                    android.util.Log.d("FarmViewModel", "  Creating Hewan: nama=${rootHewan.nama}, jk=${rootHewan.jenisKelamin}")
                    transaction.set(hewanRef, rootHewan)
                    android.util.Log.d("FarmViewModel", "  Hewan set in transaction")
                    
                    android.util.Log.d("FarmViewModel", "  Transaction prepared, returning...")
                }.await()

                android.util.Log.d("FarmViewModel", "Step 6: Transaction completed successfully!")
                android.util.Log.d("FarmViewModel", "Step 7: Fetching updated silsilah list...")
                fetchSilsilahSaya(ownerId, "Pemilik")
                
                android.util.Log.d("FarmViewModel", "Step 8: Setting isLoading = false")
                _isLoading.value = false
                
                android.util.Log.d("FarmViewModel", "Step 9: Calling onSuccess callback")
                android.util.Log.d("FarmViewModel", "========================================")
                android.util.Log.d("FarmViewModel", "SUCCESS: Silsilah created with ID: $silsilahId")
                android.util.Log.d("FarmViewModel", "========================================")
                onSuccess(silsilahId)
                
            } catch (e: Exception) {
                android.util.Log.e("FarmViewModel", "========================================")
                android.util.Log.e("FarmViewModel", "ERROR in daftarSilsilah")
                android.util.Log.e("FarmViewModel", "Error type: ${e.javaClass.simpleName}")
                android.util.Log.e("FarmViewModel", "Error message: ${e.message}")
                android.util.Log.e("FarmViewModel", "Stack trace:", e)
                android.util.Log.e("FarmViewModel", "========================================")
                
                // Berikan error message yang lebih spesifik
                val errorMessage = when {
                    e.message?.contains("memproses foto") == true || e.message?.contains("decode") == true -> 
                        "Gagal memproses foto: ${e.message}"
                    e.message?.contains("Network") == true || e.message?.contains("UNAVAILABLE") == true -> 
                        "Koneksi internet bermasalah. Periksa koneksi Anda."
                    e.message?.contains("PERMISSION_DENIED") == true ->
                        "Tidak memiliki izin. Periksa aturan Firestore."
                    else -> 
                        "Gagal mendaftar silsilah: ${e.message ?: "Unknown error"}"
                }
                
                _isLoading.value = false
                android.util.Log.d("FarmViewModel", "isLoading set to false, calling onError")
                onError(errorMessage)
            }
        }
    }

    // Role: Pengurus action
    fun ambilTawaran(silsilah: Silsilah, pengurusId: String, userRole: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (userRole != "Pengurus") {
                onError("Hanya akun Pengurus yang dapat mengklaim silsilah")
                return@launch
            }
            if (silsilah.status != "GLOBAL") {
                onError("Hewan sudah diklaim")
                return@launch
            }
            val updateSilsilah = silsilah.copy(pengurusId = pengurusId, status = "AKTIF")
            repository.updateSilsilah(updateSilsilah)
            fetchGlobalSilsilahs()
            onSuccess()
        }
    }

    // Helper UI Opsi - Deterministic Ownership Quota
    fun getAvailableHakPembagian(indukBetina: Hewan?, allHewan: List<Hewan>): List<String> {
        if (indukBetina == null) return listOf("Pemilik", "Pengurus", "Bagi Dua")

        // Strict inheritance for specific absolute states
        if (indukBetina.hakPembagian == "MUTLAK_PENGURUS") return listOf("MUTLAK_PENGURUS")
        if (indukBetina.hakPembagian == "MUTLAK_PEMILIK") return listOf("MUTLAK_PEMILIK")

        val siblings = allHewan.filter { it.parentId == indukBetina.hewanId }
        val nextTotal = siblings.size + 1
        val limit = ceil(nextTotal / 2.0)

        // Calculate current weighted units
        // Pemilik/Pengurus = 1.0, Bagi Dua = 0.5 for each side
        val curPemilikUnits = siblings.count { it.hakPembagian == "Pemilik" }.toDouble() +
                siblings.count { it.hakPembagian == "Bagi Dua" }.toDouble() * 0.5
        val curPengurusUnits = siblings.count { it.hakPembagian == "Pengurus" }.toDouble() +
                siblings.count { it.hakPembagian == "Bagi Dua" }.toDouble() * 0.5

        val available = mutableListOf<String>()

        // Check Pemilik option
        if (curPemilikUnits + 1.0 <= limit) {
            available.add("Pemilik")
        }

        // Check Pengurus option
        if (curPengurusUnits + 1.0 <= limit) {
            available.add("Pengurus")
        }

        // Check Bagi Dua option (Only if odd total siblings)
        if (nextTotal % 2 != 0) {
            if (curPemilikUnits + 0.5 <= limit && curPengurusUnits + 0.5 <= limit) {
                available.add("Bagi Dua")
            }
        }

        return available
    }

    // Opsi Penghitung Usia
    fun hitungUsia(tanggalLahir: Timestamp): Int {
        val sekarang = LocalDate.now()
        val lahir = tanggalLahir.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        return Period.between(lahir, sekarang).years
    }

    // Rule: Add Offspring (Hybrid Hak Pembagian)
    fun tambahAnak(
        context: Context,
        silsilahId: String,
        nama: String,
        jenisKelamin: String,
        harga: Int,
        hakPembagian: String, 
        fotoUri: String,
        tanggalLahirStr: String,
        indukBetinaId: String?,
        indukJantanId: String?,
        ownershipSource: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            if (fotoUri.isEmpty()) {
                onError("Silakan pilih foto hewan")
                _isLoading.value = false
                return@launch
            }

            // Validasi Tanggal Lahir (menggunakan format YYYY-MM-DD sederhana)
            val parsedDate = try {
                LocalDate.parse(tanggalLahirStr)
            } catch (e: Exception) {
                onError("Format tanggal lahir tidak valid")
                _isLoading.value = false
                return@launch
            }

            if (parsedDate.isAfter(LocalDate.now())) {
                onError("Tanggal lahir tidak valid")
                _isLoading.value = false
                return@launch
            }

            val allHewan = repository.getHewanBySilsilah(silsilahId)

            // Validasi silsilah masih aktif
            val silsilahCheck = repository.getSilsilahById(silsilahId)
            if (silsilahCheck == null || silsilahCheck.status == "SELESAI") {
                onError("Silsilah sudah selesai, tidak dapat menambah anak baru")
                _isLoading.value = false
                return@launch
            }

            // Validasi Duplicate Anak
            if (allHewan.any { it.nama.equals(nama, ignoreCase = true) }) {
                onError("Anak sudah terdaftar")
                _isLoading.value = false
                return@launch
            }

            val parentUtama = indukBetinaId ?: ""
            
            // Validasi circular lineage - check if parent is descendant of any existing animal
            if (parentUtama.isNotEmpty()) {
                val isCircular = allHewan.any { existingAnimal ->
                    isDescendant(
                        potentialParentId = parentUtama,
                        childId = existingAnimal.hewanId,
                        animals = allHewan
                    )
                }
                if (isCircular) {
                    onError("Invalid relationship: circular lineage detected")
                    _isLoading.value = false
                    return@launch
                }
            }

            // Find Parent (Mother) to get its level and validate
            val mother = allHewan.find { it.hewanId == indukBetinaId }
            if (mother == null) {
                onError("Induk Betina tidak ditemukan (parentId tidak valid)")
                _isLoading.value = false
                return@launch
            }
            if (!mother.jenisKelamin.equals("BETINA", ignoreCase = true) || !mother.status.equals("HIDUP", ignoreCase = true)) {
                onError("Hanya BETINA yang HIDUP yang bisa menambah anak")
                _isLoading.value = false
                return@launch
            }
            val parentLevel = mother.level

            // Validasi Induk Jantan (Father)
            if (indukJantanId != null) {
                val father = allHewan.find { it.hewanId == indukJantanId }
                if (father == null) {
                    onError("Induk Jantan tidak ditemukan")
                    _isLoading.value = false
                    return@launch
                }
                if (!father.jenisKelamin.equals("JANTAN", ignoreCase = true) || !father.status.equals("HIDUP", ignoreCase = true)) {
                    // Strict validation rule
                    onError("Induk Jantan harus JANTAN dan HIDUP")
                    _isLoading.value = false
                    return@launch
                }
                if (indukJantanId == indukBetinaId) {
                    onError("Induk Jantan tidak boleh sama dengan Induk Betina")
                    _isLoading.value = false
                    return@launch
                }
                if (father.pasanganId != null && father.pasanganId != indukBetinaId) {
                    onError("Jantan sudah memiliki pasangan (Monogami)")
                    _isLoading.value = false
                    return@launch
                }
            }

            // Upload foto ke Firebase Storage
            val uploadedImageUrl = try {
                ImageUploader.uploadImage(context, fotoUri, "hewan")
            } catch (e: Exception) {
                onError("Gagal upload foto: ${e.message}")
                _isLoading.value = false
                return@launch
            }

            val birthTimestamp = Timestamp(
                java.util.Date.from(parsedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
            )

            val newAnak = Hewan(
                silsilahId = silsilahId,
                nama = nama,
                jenisKelamin = jenisKelamin.uppercase(),
                harga = harga,
                tanggalLahir = birthTimestamp,
                indukBetinaId = indukBetinaId,
                indukJantanId = indukJantanId,
                parentId = indukBetinaId ?: indukJantanId,
                level = parentLevel + 1,
                hakPembagian = hakPembagian,
                ownershipSource = ownershipSource,
                fotoUri = uploadedImageUrl, // Simpan URL dari Firebase Storage
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            val addedHewanId = repository.addHewan(newAnak)
            val savedAnak = newAnak.copy(hewanId = addedHewanId)
            
            // Fast UI update (local state)
            _hewans.value = (_hewans.value + savedAnak).sortedBy { it.tanggalLahir }
            
            fetchHewanSilsilah(silsilahId)
            evalLineageEndTrigger(silsilahId)
            _isLoading.value = false
            onSuccess()
        }
    }

    // Rule: Add Partner (Father) to a Betina
    fun tambahPasangan(
        context: Context,
        silsilahId: String,
        indukBetinaId: String,
        nama: String,
        status: String,
        harga: Int,
        hakPembagian: String,
        ownershipSource: String,
        fotoUri: String,
        existingJantanId: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            // Validasi silsilah masih aktif
            val silsilahCheck = repository.getSilsilahById(silsilahId)
            if (silsilahCheck == null || silsilahCheck.status == "SELESAI") {
                onError("Silsilah sudah selesai, tidak dapat menambah pasangan baru")
                _isLoading.value = false
                return@launch
            }

            val allHewan = repository.getHewanBySilsilah(silsilahId)
            val mother = allHewan.find { it.hewanId == indukBetinaId }
            if (mother == null || !mother.jenisKelamin.equals("BETINA", ignoreCase = true)) {
                onError("Induk betina tidak valid")
                _isLoading.value = false
                return@launch
            }

            // Check for existing partner to unlink
            val oldPasanganId = mother.pasanganId
            val oldPasangan = allHewan.find { it.hewanId == oldPasanganId }

            try {
                // Upload foto jika ada (untuk jantan baru)
                val uploadedImageUrl = if (existingJantanId == null && fotoUri.isNotEmpty()) {
                    try {
                        ImageUploader.uploadImage(context, fotoUri, "hewan")
                    } catch (e: Exception) {
                        onError("Gagal upload foto: ${e.message}")
                        _isLoading.value = false
                        return@launch
                    }
                } else {
                    fotoUri // Existing jantan atau tidak ada foto
                }

                repository.db.runTransaction { transaction ->
                    val motherRef = repository.db.collection("hewan").document(mother.hewanId)
                    val silsilahRef = repository.db.collection("silsilah").document(silsilahId)
                    
                    // MUST READ ALL BEFORE WRITING
                    val silsilahSnap = transaction.get(silsilahRef)
                    val silsilah = silsilahSnap.toObject(Silsilah::class.java)

                    if (existingJantanId != null) {
                        val existingJantan = allHewan.find { it.hewanId == existingJantanId }
                            ?: throw Exception("Pejantan pilihan tidak ditemukan")
                        val jantanRef = repository.db.collection("hewan").document(existingJantan.hewanId)
                        
                        transaction.update(jantanRef, "pasanganId", mother.hewanId)
                        transaction.update(jantanRef, "updatedAt", Timestamp.now())
                        transaction.update(motherRef, "pasanganId", existingJantan.hewanId)
                        transaction.update(motherRef, "updatedAt", Timestamp.now())
                    } else {
                        val jantanRef = repository.db.collection("hewan").document()
                        val newJantan = Hewan(
                            hewanId = jantanRef.id,
                            silsilahId = silsilahId,
                            nama = nama,
                            jenisKelamin = "JANTAN",
                            harga = harga,
                            status = status,
                            hakPembagian = hakPembagian,
                            ownershipSource = ownershipSource,
                            fotoUri = uploadedImageUrl, // Simpan URL dari Firebase Storage
                            parentId = null, 
                            pasanganId = mother.hewanId,
                            level = mother.level,
                            createdAt = Timestamp.now(),
                            updatedAt = Timestamp.now()
                        )
                        transaction.set(jantanRef, newJantan)
                        transaction.update(motherRef, "pasanganId", jantanRef.id)
                        transaction.update(motherRef, "updatedAt", Timestamp.now())
                    }
                    
                    if (oldPasangan != null) {
                        val oldRef = repository.db.collection("hewan").document(oldPasangan.hewanId)
                        transaction.update(oldRef, "pasanganId", null)
                        transaction.update(oldRef, "updatedAt", Timestamp.now())
                    }
                    
                    if (silsilah != null && silsilah.status == "SELESAI") {
                        transaction.update(silsilahRef, "status", "AKTIF")
                    }
                }.await()
                
                fetchHewanSilsilah(silsilahId)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                onError("Gagal menyimpan pasangan: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    // Role: Pengurus / Pemilik -> Hapus Hewan with cascading cleanup
    fun hapusHewan(silsilahId: String, hewanId: String, userRole: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (userRole != "Pengurus" && userRole != "Pemilik") {
                    onError("Hanya Pengurus atau Pemilik yang dapat menghapus hewan")
                    _isLoading.value = false
                    return@launch
                }
                val allHewan = repository.getHewanBySilsilah(silsilahId)
                val target = allHewan.find { it.hewanId == hewanId }

                if (target == null) {
                    onError("Hewan tidak ditemukan")
                    _isLoading.value = false
                    return@launch
                }

                repository.db.runTransaction { transaction ->
                    // Identify if target has a partner
                    if (target.pasanganId != null) {
                        val partner = allHewan.find { it.hewanId == target.pasanganId }
                        if (partner != null) {
                            val partnerRef = repository.db.collection("hewan").document(partner.hewanId)
                            transaction.set(
                                partnerRef,
                                partner.copy(pasanganId = null, updatedAt = Timestamp.now())
                            )
                        }
                    }

                    // Delete the main target
                    val targetRef = repository.db.collection("hewan").document(hewanId)
                    transaction.delete(targetRef)
                }.await()

                fetchHewanSilsilah(silsilahId)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                onError("Gagal menghapus hewan")
                _isLoading.value = false
            }
        }
    }

    // Recursive Ancestor Check to Prevent Circular Lineage
    private fun isDescendant(
        potentialParentId: String,
        childId: String,
        animals: List<Hewan>
    ): Boolean {
        val children = animals.filter {
            it.parentId == childId
        }

        if (children.any { it.hewanId == potentialParentId }) {
            return true
        }

        return children.any {
            isDescendant(
                potentialParentId,
                it.hewanId,
                animals
            )
        }
    }

    fun fetchRiwayatPenjualan(userId: String, role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetched = repository.getPenjualanRiwayat(userId, role)
                // Sort descending by date locally
                _riwayatPenjualan.value = fetched.sortedByDescending { it.createdAt.toDate().time }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Algorithm Penjualan
    // profit = hargaJual - hargaModal
    // bagiHasil = profit / 2
    // saldoPemilik += hargaModal + bagiHasil
    // saldoPengurus += bagiHasil
    fun submitPenjualan(
        hewan: Hewan, 
        hargaJual: Int, 
        pengurusId: String, 
        userRole: String,
        buyerName: String, 
        buyerPhone: String,
        onSuccess: (Boolean) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (userRole != "Pengurus") {
                    onError("Hanya Pengurus yang dapat menjual hewan")
                    _isLoading.value = false
                    return@launch
                }
                val isDirect = hewan.ownershipSource.contains("PENGURUS", ignoreCase = true)
                
                if (isDirect) {
                    // Penjualan Langsung - 100% milik Pengurus (PENGURUS or MUTLAK_PENGURUS)
                    repository.db.runTransaction { transaction ->
                        val hewanRef = repository.db.collection("hewan").document(hewan.hewanId)
                        val pengurusRef = repository.db.collection("users").document(pengurusId)
                        val penjualanRef = repository.db.collection("penjualan").document()
                        
                        // 1. Get current balance
                        val pengurusSnap = transaction.get(pengurusRef)
                        val curSaldo = pengurusSnap.getLong("saldo")?.toInt() ?: 0
                        
                        // 2. Create Penjualan record (Immediate SOLD)
                        val sale = Penjualan(
                            penjualanId = penjualanRef.id,
                            silsilahId = hewan.silsilahId,
                            hewanId = hewan.hewanId,
                            hewanNama = hewan.nama,
                            userId = pengurusId,
                            hargaModal = hewan.harga,
                            hargaJual = hargaJual,
                            profit = hargaJual - hewan.harga,
                            buyerName = buyerName,
                            buyerPhone = buyerPhone,
                            status = "SOLD",
                            createdAt = Timestamp.now()
                        )
                        transaction.set(penjualanRef, sale)
                        
                        // 3. Update Hewan Status
                        transaction.update(hewanRef, "status", "TERJUAL")
                        transaction.update(hewanRef, "updatedAt", Timestamp.now())
                        
                        // 4. Update Saldo (100% to Pengurus)
                        transaction.update(pengurusRef, "saldo", curSaldo + hargaJual)
                    }.await()
                    
                    fetchHewanSilsilah(hewan.silsilahId)
                    fetchRiwayatPenjualan(pengurusId, "Pengurus")
                } else {
                    // Penjualan Titipan (PEMILIK / BAGI_DUA) - Perlu Pending Approval
                    val j = Penjualan(
                        silsilahId = hewan.silsilahId,
                        hewanId = hewan.hewanId,
                        hewanNama = hewan.nama,
                        userId = pengurusId,
                        hargaModal = hewan.harga,
                        hargaJual = hargaJual,
                        profit = hargaJual - hewan.harga,
                        buyerName = buyerName,
                        buyerPhone = buyerPhone,
                        status = "PENDING",
                        createdAt = Timestamp.now()
                    )
                    repository.createPenjualan(j)
                }
                onSuccess(isDirect)
            } catch (e: Exception) {
                onError("Gagal memproses penjualan: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptPenjualan(penjualan: Penjualan, ownerId: String, userRole: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (userRole != "Pemilik") {
                    onError("Hanya Pemilik yang dapat menyetujui penjualan")
                    _isLoading.value = false
                    return@launch
                }
                repository.db.runTransaction { transaction ->
                    val penjualanRef = repository.db.collection("penjualan").document(penjualan.penjualanId)
                    val hewanRef = repository.db.collection("hewan").document(penjualan.hewanId)
                    val silsilahRef = repository.db.collection("silsilah").document(penjualan.silsilahId)
                    
                    val silsilahSnap = transaction.get(silsilahRef)
                    val silsilah = silsilahSnap.toObject(Silsilah::class.java)
                        ?: throw Exception("Silsilah tidak ditemukan")

                    val hewanSnap = transaction.get(hewanRef)
                    val hewan = hewanSnap.toObject(Hewan::class.java)
                        ?: throw Exception("Data hewan tidak ditemukan")

                    val penjualanSnap = transaction.get(penjualanRef)
                    val currentStatus = penjualanSnap.getString("status")
                    if (currentStatus == "SOLD") {
                        throw Exception("Idempotent: Transaksi sudah diproses sebelumnya")
                    }
                    
                    val ownerIdFinal = silsilah.ownerId
                    val pengurusId = penjualan.userId
                    if (ownerIdFinal.isEmpty() || pengurusId.isEmpty()) {
                        throw Exception("Data Silsilah atau user tidak valid")
                    }
                    
                    val ownerRef = repository.db.collection("users").document(ownerIdFinal)
                    val pengurusRef = repository.db.collection("users").document(pengurusId)
                    
                    val ownerSnap = transaction.get(ownerRef)
                    val pengurusSnap = transaction.get(pengurusRef)
                    
                    val curOwnerSaldo = ownerSnap.getLong("saldo")?.toInt() ?: 0
                    val curPengurusSaldo = pengurusSnap.getLong("saldo")?.toInt() ?: 0
                    
                    val profit = penjualan.hargaJual - penjualan.hargaModal
                    var ownerAdd = 0
                    var pengurusAdd = 0
                    
                    // Logic Pembagian berdasarkan ownershipSource
                    val isPengurusOwned = hewan.ownershipSource.contains("PENGURUS", ignoreCase = true)
                    
                    if (isPengurusOwned) {
                        // Just in case a PENGURUS animal enters pending (though UI should prevent it)
                        ownerAdd = 0
                        pengurusAdd = penjualan.hargaJual
                    } else {
                        // Logic Titipan (BAGI_DUA atau PEMILIK)
                        if (profit > 0) {
                            val profitShare = profit / 2
                            ownerAdd = penjualan.hargaModal + profitShare
                            pengurusAdd = profitShare
                        } else {
                            // Rugi atau balik modal, Pemilik ambil apa adanya, Pengurus 0
                            ownerAdd = penjualan.hargaJual
                            pengurusAdd = 0
                        }
                    }
                    
                    transaction.update(ownerRef, "saldo", curOwnerSaldo + ownerAdd)
                    transaction.update(pengurusRef, "saldo", curPengurusSaldo + pengurusAdd)
                    transaction.update(penjualanRef, "status", "SOLD")
                    transaction.update(hewanRef, "status", "TERJUAL")
                    transaction.update(hewanRef, "updatedAt", Timestamp.now())
                }.await()
                
                // Refresh pending list and history for both
                fetchPendingPenjualan(ownerId)
                fetchRiwayatPenjualan(ownerId, "Pemilik")
                val pengurusId = penjualan.userId
                if (pengurusId.isNotEmpty()) {
                    fetchPendingPenjualan(pengurusId)
                    fetchRiwayatPenjualan(pengurusId, "Pengurus")
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Gagal menyetujui: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Pemilik rejects a pending sale with reason
    fun rejectPenjualan(penjualan: Penjualan, alasan: String, ownerId: String, userRole: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (userRole != "Pemilik") {
                    onError("Hanya Pemilik yang dapat menolak penjualan")
                    _isLoading.value = false
                    return@launch
                }
                val updated = penjualan.copy(status = "REJECTED", alasanTolak = alasan)
                repository.updatePenjualan(updated)
                // Refresh pending list
                fetchPendingPenjualan(ownerId)
                onSuccess()
            } catch (e: Exception) {
                onError("Gagal menolak: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun updateStatusHewan(silsilahId: String, hewanId: String, newStatus: String, userRole: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (userRole != "Pengurus") {
                    onError("Hanya Pengurus yang dapat mengubah status hewan")
                    _isLoading.value = false
                    return@launch
                }
                val hewanRef = repository.db.collection("hewan").document(hewanId)
                repository.db.runTransaction { transaction ->
                    transaction.update(hewanRef, "status", newStatus)
                    transaction.update(hewanRef, "updatedAt", Timestamp.now())
                    null
                }.await()
                
                fetchHewanSilsilah(silsilahId)
                evalLineageEndTrigger(silsilahId)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                onError("Gagal update status: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    // Lineage Auto-Termination Trace
    fun evalLineageEndTrigger(silsilahId: String) {
        viewModelScope.launch {
            try {
                val allHewan = repository.getHewanBySilsilah(silsilahId)
                val silsilahRef = repository.db.collection("silsilah").document(silsilahId)
                
                val anyActiveFemaleWithPartner = allHewan.any { 
                    it.jenisKelamin.equals("BETINA", true) && 
                    (it.status.equals("HIDUP", true) || it.status.equals("SAKIT", true)) && 
                    it.pasanganId != null 
                }
                
                if (!anyActiveFemaleWithPartner) {
                    repository.db.runTransaction { transaction ->
                        val snap = transaction.get(silsilahRef)
                        val silsilah = snap.toObject(Silsilah::class.java)
                        if (silsilah != null && silsilah.status == "AKTIF") {
                            transaction.update(silsilahRef, "status", "SELESAI")
                        }
                        null
                    }.await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}



