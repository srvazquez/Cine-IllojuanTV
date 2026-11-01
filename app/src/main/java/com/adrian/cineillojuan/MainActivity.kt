package com.adrian.cineillojuan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adrian.cineillojuan.ui.CineTheme
import com.adrian.cineillojuan.ui.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CineTheme {
                HomeScreen(viewModel<CatalogViewModel>())
            }
        }
    }
}
