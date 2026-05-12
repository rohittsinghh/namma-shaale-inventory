package com.example.nammashalli.ui.navigation

sealed class Screen(val route: String) {
    data object SignUp : Screen("signup")
    data object SignIn : Screen("signin")
    data object Otp : Screen("otp/{phone}/{otp}") {
        fun createRoute(phone: String, otp: String) = "otp/$phone/$otp"
    }
    data object Dashboard : Screen("dashboard")
    data object AssetList : Screen("asset_list")
    data object AssetRegister : Screen("asset_register")
    data object AssetDetails : Screen("asset_details/{assetId}") {
        fun createRoute(assetId: Long) = "asset_details/$assetId"
    }
    data object AssetEdit : Screen("asset_edit/{assetId}") {
        fun createRoute(assetId: Long) = "asset_edit/$assetId"
    }
    data object HealthCheckSelect : Screen("health_check_select")
    data object HealthCheck : Screen("health_check")
    data object HealthCheckSummary : Screen("health_check_summary")
    data object IssueLog : Screen("issue_log")
    data object IssueList : Screen("issue_list")
    data object RepairRequests : Screen("repair_requests")
    data object Report : Screen("report")
    data object Profile : Screen("profile")
}
