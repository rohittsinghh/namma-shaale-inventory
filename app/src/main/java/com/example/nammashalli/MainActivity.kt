package com.example.nammashalli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.nammashalli.ui.navigation.NavGraph
import com.example.nammashalli.ui.navigation.Screen
import com.example.nammashalli.ui.theme.GreenPrimary
import com.example.nammashalli.ui.theme.NammaShalliTheme
import com.example.nammashalli.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NammaShalliTheme {
                val isLoggedIn by sessionManager.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)
                when (val loggedIn = isLoggedIn) {
                    null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                    else -> {
                        val startDest = if (loggedIn) Screen.Dashboard.route else Screen.SignIn.route
                        val navController = rememberNavController()
                        NavGraph(navController = navController, startDestination = startDest)
                    }
                }
            }
        }
    }
}
