package com.example.zenfarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zenfarm.ui.navigation.AppNavigation
import com.example.zenfarm.ui.theme.ZenFarmTheme
import com.example.zenfarm.viewmodel.AuthViewModel
import com.example.zenfarm.viewmodel.FarmViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZenFarmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val authViewModel: AuthViewModel = viewModel()
                    val farmViewModel: FarmViewModel = viewModel()
                    
                    AppNavigation(
                        authViewModel = authViewModel,
                        farmViewModel = farmViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}