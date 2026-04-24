package com.example.farmflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmflow.ui.navigation.AppNavigation
import com.example.farmflow.ui.theme.FarmFlowTheme
import com.example.farmflow.viewmodel.AuthViewModel
import com.example.farmflow.viewmodel.FarmViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FarmFlowTheme {
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