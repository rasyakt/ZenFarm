package com.example.zenfarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenfarm.data.FarmRepository
import com.example.zenfarm.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = FarmRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun login(email: String, password: String, onRoleDetermined: (String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val foundUser = repository.getUser(email)
                if (foundUser != null && foundUser.password == password) {
                    _user.value = foundUser
                    onRoleDetermined(foundUser.role)
                } else {
                    _error.value = "Email atau Password salah."
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun register(name: String, email: String, password: String, role: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val existing = repository.getUser(email)
                if (existing != null) {
                    _error.value = "Email sudah terdaftar."
                    return@launch
                }
                
                val newUser = User(
                    name = name,
                    email = email,
                    password = password,
                    role = role
                )
                repository.createUser(newUser)
                
                // Set as logged in immediately or require login? 
                // We just trigger onSuccess
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
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
