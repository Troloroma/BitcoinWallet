package com.example.bitcoinwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bitcoinwallet.app.app.App
import com.example.bitcoinwallet.common.theme.BitcoinWalletTheme
import com.example.bitcoinwallet.features.main.presentation.MainDestination
import com.example.bitcoinwallet.features.main.presentation.MainViewModel
import com.example.bitcoinwallet.navigation.Destinations
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as App).appComponent.injectMainActivity(this)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            BitcoinWalletTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Destinations.MainGraph.route
                    ) {
                        composable(Destinations.MainGraph.route) {
                            val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
                            MainDestination(
                                viewModel = viewModel,
                            )
                        }
                    }
                }
            }
        }
    }
}
