package com.kazvoeten.omadketonics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.kazvoeten.omadketonics.ui.theme.OmadKetonicsTheme

@AndroidEntryPoint(ComponentActivity::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OmadKetonicsTheme(dynamicColor = false) {
                OmadAppRoot()
            }
        }
    }
}
