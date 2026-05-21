package com.example.zenfarm.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FarmRepository {
    val db = FirebaseFirestore.getInstance()

    // Users
    suspend fun getUser(email: String): User? {
        val snapshot = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()
        return if (snapshot.isEmpty) null else snapshot.documents[0].toObject(User::class.java)
    }

    suspend fun createUser(user: User) {
        val ref = db.collection("users").document()
        val userWithId = user.copy(userId = ref.id)
        ref.set(userWithId).await()
    }
    
    suspend fun updateUserSaldo(userId: String, newSaldo: Int) {
        db.collection("users").document(userId).update("saldo", newSaldo).await()
    }

    suspend fun getUserById(userId: String): User? {
        val doc = db.collection("users").document(userId).get().await()
        return doc.toObject(User::class.java)
    }
    
    suspend fun updateUserPassword(userId: String, newPassword: String) {
        db.collection("users").document(userId).update("password", newPassword).await()
    }

    suspend fun getSilsilahById(silsilahId: String): Silsilah? {
        val snapshot = db.collection("silsilah").document(silsilahId).get().await()
        return snapshot.toObject(Silsilah::class.java)
    }

    suspend fun createSilsilah(silsilah: Silsilah): String {
        val ref = db.collection("silsilah").document()
        val silsilahId = ref.id
        val silsilahWithId = silsilah.copy(silsilahId = silsilahId)
        ref.set(silsilahWithId).await()
        return silsilahId
    }

    suspend fun getGlobalSilsilahs(): List<Silsilah> {
        val snapshot = db.collection("silsilah")
            .whereEqualTo("status", "GLOBAL")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Silsilah::class.java) }
    }
    
    suspend fun updateSilsilah(silsilah: Silsilah) {
        db.collection("silsilah").document(silsilah.silsilahId).set(silsilah).await()
    }

    suspend fun getSilsilahByOwner(ownerId: String): List<Silsilah> {
        val snapshot = db.collection("silsilah")
            .whereEqualTo("ownerId", ownerId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Silsilah::class.java) }
    }

    suspend fun getSilsilahByPengurus(pengurusId: String): List<Silsilah> {
        val snapshot = db.collection("silsilah")
            .whereEqualTo("pengurusId", pengurusId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Silsilah::class.java) }
    }

    // Hewan
    suspend fun addHewan(hewan: Hewan): String {
        val ref = db.collection("hewan").document()
        val hewanId = ref.id
        val hewanWithId = hewan.copy(hewanId = hewanId)
        ref.set(hewanWithId).await()
        return hewanId
    }

    suspend fun getHewanBySilsilah(silsilahId: String): List<Hewan> {
        return try {
            val snapshot = db.collection("hewan")
                .whereEqualTo("silsilahId", silsilahId)
                .get()
                .await()
            
            // Sort locally to avoid mandatory composite indexes on Firestore
            snapshot.documents.mapNotNull { it.toObject(Hewan::class.java) }
                .sortedBy { it.tanggalLahir }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateHewan(hewan: Hewan) {
        db.collection("hewan").document(hewan.hewanId).set(hewan).await()
    }

    suspend fun deleteHewan(hewanId: String) {
        db.collection("hewan").document(hewanId).delete().await()
    }

    // Penjualan
    suspend fun createPenjualan(penjualan: Penjualan) {
        val ref = db.collection("penjualan").document()
        val penjualanWithId = penjualan.copy(penjualanId = ref.id)
        ref.set(penjualanWithId).await()
    }

    suspend fun updatePenjualan(penjualan: Penjualan) {
        db.collection("penjualan").document(penjualan.penjualanId).set(penjualan).await()
    }

    suspend fun getPenjualanPendingByOwner(ownerId: String): List<Penjualan> {
        val silsilahs = getSilsilahByOwner(ownerId).map { it.silsilahId }
        if (silsilahs.isEmpty()) return emptyList()
        
        val all = mutableListOf<Penjualan>()
        silsilahs.chunked(10).forEach { chunk ->
            val snapshot = db.collection("penjualan")
                .whereIn("silsilahId", chunk)
                .whereEqualTo("status", "PENDING")
                .get()
                .await()
            all.addAll(snapshot.documents.mapNotNull { it.toObject(Penjualan::class.java) })
        }
        return all
    }

    suspend fun getPenjualanRiwayat(userId: String, role: String): List<Penjualan> {
        return try {
            if (role == "Pemilik") {
                val silsilahs = getSilsilahByOwner(userId).map { it.silsilahId }
                if (silsilahs.isEmpty()) return emptyList()
                
                val all = mutableListOf<Penjualan>()
                silsilahs.chunked(10).forEach { chunk ->
                    val snapshot = db.collection("penjualan")
                        .whereIn("silsilahId", chunk)
                        .get()
                        .await()
                    all.addAll(snapshot.documents.mapNotNull { it.toObject(Penjualan::class.java) })
                }
                all.filter { it.status != "PENDING" }
            } else {
                val snapshot = db.collection("penjualan")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                snapshot.documents.mapNotNull { it.toObject(Penjualan::class.java) }
                    .filter { it.status != "PENDING" }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getHewanByUser(userId: String, role: String): List<Hewan> {
        return try {
            val silsilahs = if (role == "Pemilik") {
                getSilsilahByOwner(userId)
            } else {
                getSilsilahByPengurus(userId)
            }
            
            if (silsilahs.isEmpty()) return emptyList()
            
            val silsilahIds = silsilahs.map { it.silsilahId }
            
            // Firestore whereIn limit is 10 or 30 depending on version/config
            // We chunk it to be safe (using 10 as safe baseline)
            val allHewans = mutableListOf<Hewan>()
            silsilahIds.chunked(10).forEach { chunk ->
                val snapshot = db.collection("hewan")
                    .whereIn("silsilahId", chunk)
                    .get()
                    .await()
                allHewans.addAll(snapshot.documents.mapNotNull { it.toObject(Hewan::class.java) })
            }
            
            allHewans.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
