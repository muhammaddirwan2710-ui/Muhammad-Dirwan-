package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PortalDashboard
import com.example.ui.PortalViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Enable edge-to-edge full bleed drawing support
    enableEdgeToEdge()
    
    setContent {
      MyApplicationTheme {
        // Core background surface
        Surface(
          modifier = Modifier.fillMaxSize()
        ) {
          // Instantiate our central state management VM
          val portalViewModel: PortalViewModel = viewModel()
          
          PortalDashboard(viewModel = portalViewModel)
        }
      }
    }
  }
}
