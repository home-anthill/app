package eu.homeanthill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import eu.homeanthill.ui.theme.AppTheme
import eu.homeanthill.ui.screens.permission.PermissionScreen

class PermissionActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AppTheme(dynamicColor = false) {
        PermissionScreen(

        )
      }
    }
  }
}