package com.example.farmflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.farmflow.viewmodel.AuthViewModel
import com.example.farmflow.viewmodel.FarmViewModel
import com.example.farmflow.ui.screens.LoginScreen
import com.example.farmflow.ui.screens.RegisterScreen

import com.example.farmflow.ui.screens.PemilikDashboardScreen
import com.example.farmflow.ui.screens.PengurusDashboardScreen
import com.example.farmflow.ui.screens.GlobalScreen
import com.example.farmflow.ui.screens.SilsilahCanvasScreen
import com.example.farmflow.ui.screens.TambahAnakScreen
import com.example.farmflow.ui.screens.WalletScreen
import com.example.farmflow.ui.screens.ProfileScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    farmViewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController, 
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    if (role == "Pemilik") {
                        navController.navigate("dashboard_pemilik") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("dashboard_pengurus") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }
        
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard_pemilik") {
            PemilikDashboardScreen(authViewModel, farmViewModel, navController)
        }

        composable("dashboard_pengurus") {
            PengurusDashboardScreen(authViewModel, farmViewModel, navController)
        }
        
        composable("global") {
            GlobalScreen(authViewModel, farmViewModel, navController)
        }
        
        composable("dompet") {
            WalletScreen(authViewModel, farmViewModel, navController)
        }

        composable("profile") {
            ProfileScreen(authViewModel, farmViewModel, navController)
        }
        
        composable(
            "silsilah_detail/{silsilahId}",
            arguments = listOf(navArgument("silsilahId") { type = NavType.StringType })
        ) { backStackEntry ->
            val silsilahId = backStackEntry.arguments?.getString("silsilahId") ?: ""
            SilsilahCanvasScreen(silsilahId, authViewModel, farmViewModel, navController)
        }
        
        composable(
            "tambah_anak/{silsilahId}?parentId={parentId}",
            arguments = listOf(
                navArgument("silsilahId") { type = NavType.StringType },
                navArgument("parentId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val silsilahId = backStackEntry.arguments?.getString("silsilahId") ?: ""
            val parentId = backStackEntry.arguments?.getString("parentId")
            TambahAnakScreen(silsilahId, parentId, farmViewModel, navController)
        }

        composable(
            "tambah_pasangan/{silsilahId}?indukBetinaId={indukBetinaId}",
            arguments = listOf(
                navArgument("silsilahId") { type = NavType.StringType },
                navArgument("indukBetinaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val silsilahId = backStackEntry.arguments?.getString("silsilahId") ?: ""
            val indukBetinaId = backStackEntry.arguments?.getString("indukBetinaId") ?: ""
            com.example.farmflow.ui.screens.TambahJantanScreen(silsilahId, indukBetinaId, farmViewModel, navController)
        }
    }
}
