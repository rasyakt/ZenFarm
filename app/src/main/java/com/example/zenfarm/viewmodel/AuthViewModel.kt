package com.example.zenfarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenfarm.data.FarmRepository
import com.example.zenfarm.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import android.util.Patterns

class AuthViewModel : ViewModel() {
    private val repository = FarmRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Helper function untuk validasi email
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Helper function untuk validasi password
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun login(email: String, password: String, onRoleDetermined: (String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            // Validasi input
            if (email.isBlank() || password.isBlank()) {
                _error.value = "Email dan password tidak boleh kosong"
                _loading.value = false
                return@launch
            }
            
            if (!isValidEmail(email)) {
                _error.value = "Format email tidak valid"
                _loading.value = false
                return@launch
            }
            
            try {
                val foundUser = repository.getUser(email)
                if (foundUser != null) {
                    // Verify password dengan BCrypt
                    if (BCrypt.checkpw(password, foundUser.password)) {
                        _user.value = foundUser
                        onRoleDetermined(foundUser.role)
                    } else {
                        _error.value = "Email atau Password salah."
                    }
                } else {
                    _error.value = "Email atau Password salah."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Terjadi kesalahan saat login"
            } finally {
                _loading.value = false
            }
        }
    }

    fun register(name: String, email: String, password: String, role: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            // Validasi input
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                _error.value = "Semua field harus diisi"
                _loading.value = false
                return@launch
            }
            
            if (!isValidEmail(email)) {
                _error.value = "Format email tidak valid"
                _loading.value = false
                return@launch
            }
            
            if (!isValidPassword(password)) {
                _error.value = "Password minimal 6 karakter"
                _loading.value = false
                return@launch
            }
            
            try {
                val existing = repository.getUser(email)
                if (existing != null) {
                    _error.value = "Email sudah terdaftar."
                    _loading.value = false
                    return@launch
                }
                
                // Hash password dengan BCrypt
                val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                
                val newUser = User(
                    name = name,
                    email = email,
                    password = hashedPassword,
                    role = role
                )
                repository.createUser(newUser)
                
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Terjadi kesalahan saat registrasi"
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout() {
        _user.value = null
    }

    fun refreshUser(userId: String) {
        viewModelScope.launch {
            try {
                val updated = repository.getUserById(userId)
                if (updated != null) {
                    _user.value = updated
                }
            } catch (e: Exception) {
                // Background refresh fail is silent but logged
                e.printStackTrace()
            }
        }
    }
}
