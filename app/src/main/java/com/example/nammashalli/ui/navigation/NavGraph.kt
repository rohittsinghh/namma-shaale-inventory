package com.example.nammashalli.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.nammashalli.ui.assets.*
import com.example.nammashalli.ui.auth.*
import com.example.nammashalli.ui.dashboard.DashboardScreen
import com.example.nammashalli.ui.healthcheck.*
import com.example.nammashalli.ui.issues.*
import com.example.nammashalli.ui.profile.ProfileScreen
import com.example.nammashalli.ui.repairs.RepairRequestsScreen
import com.example.nammashalli.ui.reports.ReportScreen
import com.example.nammashalli.ui.theme.GreenPrimary

private data class BottomTab(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab(Screen.Dashboard, "Dashboard", Icons.Default.Dashboard),
    BottomTab(Screen.AssetList, "Assets", Icons.Default.Inventory),
    BottomTab(Screen.RepairRequests, "Repairs", Icons.Default.Build),
    BottomTab(Screen.Report, "Reports", Icons.Default.Assessment),
    BottomTab(Screen.Profile, "Profile", Icons.Default.Person)
)

private val tabRoutes = bottomTabs.map { it.screen.route }.toSet()

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val pendingCount by mainViewModel.pendingRepairCount.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != null && currentRoute in tabRoutes) {
                NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    bottomTabs.forEach { tab ->
                        val selected = currentRoute == tab.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (tab.screen == Screen.RepairRequests && pendingCount > 0) {
                                    BadgedBox(badge = {
                                        Badge { Text(pendingCount.coerceAtMost(99).toString(), fontSize = 9.sp) }
                                    }) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GreenPrimary,
                                selectedTextColor = GreenPrimary,
                                indicatorColor = GreenPrimary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onNavigateToSignIn = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.SignIn.route) {
                SignInScreen(
                    onNavigateToOtp = { phone, otp ->
                        navController.navigate(Screen.Otp.createRoute(phone, otp))
                    },
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
                )
            }

            composable(
                route = Screen.Otp.route,
                arguments = listOf(
                    navArgument("phone") { type = NavType.StringType },
                    navArgument("otp") { type = NavType.StringType }
                )
            ) { backStack ->
                val phone = backStack.arguments?.getString("phone") ?: ""
                val generatedOtp = backStack.arguments?.getString("otp") ?: ""
                OtpScreen(
                    phone = phone,
                    generatedOtp = generatedOtp,
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Tab root screens (no back button) ──────────────────────────

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToAssetList = {
                        navController.navigate(Screen.AssetList.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    onNavigateToAssetRegister = { navController.navigate(Screen.AssetRegister.route) },
                    onNavigateToHealthCheck = { navController.navigate(Screen.HealthCheckSelect.route) },
                    onNavigateToRepairs = {
                        navController.navigate(Screen.RepairRequests.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    onNavigateToIssues = { navController.navigate(Screen.IssueList.route) },
                    onNavigateToReport = {
                        navController.navigate(Screen.Report.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    onLogout = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.AssetList.route) {
                AssetListScreen(
                    onNavigateToRegister = { navController.navigate(Screen.AssetRegister.route) },
                    onNavigateToDetails = { id -> navController.navigate(Screen.AssetDetails.createRoute(id)) }
                    // onBack = null → no back button shown (tab root)
                )
            }

            composable(Screen.RepairRequests.route) {
                RepairRequestsScreen()
                // onBack = null → no back button shown (tab root)
            }

            composable(Screen.Report.route) {
                ReportScreen()
                // onBack = null → no back button shown (tab root)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ── Sub-screens (have back buttons) ────────────────────────────

            composable(Screen.AssetRegister.route) {
                AssetRegisterScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate(Screen.AssetList.route) {
                            popUpTo(Screen.AssetRegister.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.AssetDetails.route,
                arguments = listOf(navArgument("assetId") { type = NavType.LongType })
            ) { backStack ->
                val assetId = backStack.arguments?.getLong("assetId") ?: return@composable
                AssetDetailsScreen(
                    assetId = assetId,
                    onBack = { navController.popBackStack() },
                    onNavigateToHealthCheck = { navController.navigate(Screen.HealthCheckSelect.route) },
                    onNavigateToIssueLog = { navController.navigate(Screen.IssueLog.route) }
                )
            }

            composable(Screen.HealthCheckSelect.route) {
                HealthCheckSelectScreen(
                    onBack = { navController.popBackStack() },
                    onStartCheck = { navController.navigate(Screen.HealthCheck.route) },
                    navController = navController
                )
            }

            composable(Screen.HealthCheck.route) {
                HealthCheckScreen(
                    onNavigateToSummary = { navController.navigate(Screen.HealthCheckSummary.route) },
                    onBack = { navController.popBackStack() },
                    navController = navController
                )
            }

            composable(Screen.HealthCheckSummary.route) {
                HealthCheckSummaryScreen(
                    onDone = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.HealthCheckSelect.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    navController = navController
                )
            }

            composable(Screen.IssueLog.route) {
                IssueLogScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }

            composable(Screen.IssueList.route) {
                IssueListScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToLog = { navController.navigate(Screen.IssueLog.route) }
                )
            }
        }
    }
}
