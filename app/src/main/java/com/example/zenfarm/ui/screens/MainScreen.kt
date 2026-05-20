package com.example.zenfarm.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zenfarm.ui.theme.FarmGreen
import com.example.zenfarm.ui.theme.SurfaceLight
import com.example.zenfarm.viewmodel.AuthViewModel
import com.example.zenfarm.viewmodel.FarmViewModel

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Beranda", Icons.Rounded.Home)
    object Global : BottomNavItem("global", "Global", Icons.Rounded.Public)
    object Wallet : BottomNavItem("wallet", "Dompet", Icons.Rounded.AccountBalanceWallet)
    object Profile : BottomNavItem("profile", "Profil", Icons.Rounded.Person)
}

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    farmViewModel: FarmViewModel,
    rootNavController: NavController,
    userRole: String
) {
    val bottomNavController = rememberNavController()
    
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Global,
        BottomNavItem.Wallet,
        BottomNavItem.Profile
    )

    Scaffold(
        containerColor = SurfaceLight,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = selected,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FarmGreen,
                            selectedTextColor = FarmGreen,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = FarmGreen.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                if (userRole == "Pemilik") {
                    PemilikDashboardScreen(authViewModel, farmViewModel, rootNavController)
                } else {
                    PengurusDashboardScreen(authViewModel, farmViewModel, rootNavController)
                }
            }
            composable(BottomNavItem.Global.route) {
                GlobalScreen(authViewModel, farmViewModel, rootNavController, true)
            }
            composable(BottomNavItem.Wallet.route) {
                WalletScreen(authViewModel, farmViewModel, rootNavController, true)
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(authViewModel, farmViewModel, rootNavController, true)
            }
        }
    }
}
