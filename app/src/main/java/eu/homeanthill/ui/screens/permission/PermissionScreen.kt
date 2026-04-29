package eu.homeanthill.ui.screens.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

import eu.homeanthill.MainActivity
import eu.homeanthill.R
import eu.homeanthill.ui.theme.AppTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current

  val notificationPermission = rememberPermissionState(
    permission = Manifest.permission.POST_NOTIFICATIONS
  )

  LaunchedEffect(notificationPermission.status.isGranted) {
    if (notificationPermission.status.isGranted) {
      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      }
      context.startActivity(intent)
    }
  }

  val showRationalDialog = remember { mutableStateOf(false) }
  if (showRationalDialog.value) {
    AlertDialog(
      onDismissRequest = {
        showRationalDialog.value = false
      },
      title = {
        Text(
          text = stringResource(R.string.home_permission),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.tertiary,
        )
      },
      text = {
        Text(
          text = stringResource(R.string.home_permission_test),
          style = MaterialTheme.typography.bodyMedium,
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            showRationalDialog.value = false
            val intent = Intent(
              Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
              Uri.fromParts("package", context.packageName, null)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent, null)

          }) {
          Text(
            text = stringResource(R.string.ok),
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showRationalDialog.value = false
          }) {
          Text(
            text = stringResource(R.string.cancel),
          )
        }
      },
    )
  }

  PermissionContent(
    modifier = modifier,
    isPermissionGranted = notificationPermission.status.isGranted,
    shouldShowRationale = notificationPermission.status.shouldShowRationale,
    onRequestPermission = {
      if (notificationPermission.status.shouldShowRationale) {
        showRationalDialog.value = true
      } else {
        notificationPermission.launchPermissionRequest()
      }
    }
  )
}

@Composable
fun PermissionContent(
  modifier: Modifier = Modifier,
  isPermissionGranted: Boolean,
  shouldShowRationale: Boolean,
  onRequestPermission: () -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 64.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        if (!isPermissionGranted) {
          Button(
            onClick = onRequestPermission,
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.secondary,
              contentColor = MaterialTheme.colorScheme.tertiary,
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = stringResource(id = R.string.home_ask_permission),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
          Spacer(modifier = Modifier.height(20.dp))
          Text(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 5.dp),
            text = if (shouldShowRationale) {
              stringResource(R.string.home_permission_rationale)
            } else {
              stringResource(R.string.home_permission_required)
            },
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun PermissionScreenPreview() {
  AppTheme {
    PermissionContent(
      isPermissionGranted = false,
      shouldShowRationale = false,
      onRequestPermission = {}
    )
  }
}

@Preview(showBackground = true)
@Composable
fun PermissionScreenRationalePreview() {
  AppTheme {
    PermissionContent(
      isPermissionGranted = false,
      shouldShowRationale = true,
      onRequestPermission = {}
    )
  }
}
