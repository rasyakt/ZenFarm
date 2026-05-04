package com.example.zenfarm.data

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "", // Used only for prototype mockup login
    val role: String = "", // "Pemilik" atau "Pengurus"
    val saldo: Int = 0 // Saldo user dari hasil penjualan
)

data class Silsilah(
    val silsilahId: String = "",
    val namaSilsilah: String = "",
    val ownerId: String = "",
    val pengurusId: String = "",
    val status: String = "GLOBAL", // GLOBAL, aktif, selesai
    val active: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class Hewan(
    val hewanId: String = "",
    val silsilahId: String = "",
    val nama: String = "",
    val tanggalLahir: Timestamp = Timestamp.now(),
    val jenisKelamin: String = "", // "Jantan", "Betina"
    val harga: Int = 0,
    val status: String = "HIDUP", // HIDUP, MATI, DIJUAL
    val indukBetinaId: String? = null,
    val indukJantanId: String? = null,
    val parentId: String? = null, // To enforce tree structure
    val level: Int = 0,
    val hakPembagian: String = "", // "Pemilik", "Pengurus", "Bagi Dua"
    val ownershipSource: String = "", // "PEMILIK", "PENGURUS", "BAGI_DUA"
    val fotoUri: String = "", // Local URI string
    val pasanganId: String? = null, // Mate/Partner pointer
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class Penjualan(
    val penjualanId: String = "",
    val silsilahId: String = "",
    val hewanId: String = "",
    val hewanNama: String = "", // Nama hewan for display
    val userId: String = "", // ID Pengurus yang mengajukan
    val hargaModal: Int = 0,
    val hargaJual: Int = 0,
    val profit: Int = 0,
    val buyerName: String = "",
    val buyerPhone: String = "",
    val status: String = "PENDING", // PENDING, SOLD, REJECTED, AUTO_CANCEL
    val alasanTolak: String = "", // Reason for rejection by Pemilik
    val createdAt: Timestamp = Timestamp.now()
)
