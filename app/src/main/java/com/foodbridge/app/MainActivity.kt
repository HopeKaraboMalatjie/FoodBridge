package com.foodbridge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.foodbridge.app.ui.navigation.FoodBridgeNavGraph
import com.foodbridge.app.ui.theme.FoodBridgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodBridgeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FoodBridgeNavGraph(app = application as FoodBridgeApp)
                }
            }
        }
    }
}
