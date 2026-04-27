package eu.homeanthill.ui.screens.profile

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

import eu.homeanthill.R
import eu.homeanthill.api.model.GitHub
import eu.homeanthill.api.model.Profile
import eu.homeanthill.ui.theme.AppTheme

@Composable
fun ProfileScreen(
  profileUiState: ProfileViewModel.ProfileUiState,
  apiTokenUiState: ProfileViewModel.ApiTokenUiState,
  profileViewModel: ProfileViewModel
) {
  var showRegenDialog by remember { mutableStateOf(false) }
  val profile = (profileUiState as? ProfileViewModel.ProfileUiState.Idle)?.profile
  val view = LocalView.current

  // block screenshots in this page
  DisposableEffect(Unit) {
    val window = if (view.isInEditMode) null else (view.context as? Activity)?.window
    window?.setFlags(
      WindowManager.LayoutParams.FLAG_SECURE,
      WindowManager.LayoutParams.FLAG_SECURE
    )
    onDispose {
      window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
      profileViewModel.resetApiToken()
    }
  }

  if (showRegenDialog) {
    RegenerateTokenDialog(
      onDismissRequest = { showRegenDialog = false },
      onConfirm = {
        profileViewModel.regenApiToken(profile?.id)
        showRegenDialog = false
      }
    )
  }

  ProfileContent(
    profileUiState = profileUiState,
    apiTokenUiState = apiTokenUiState,
    onRegenToken = { _ -> showRegenDialog = true },
    onLogout = { profileViewModel.logout() }
  )
}

@Composable
fun RegenerateTokenDialog(
  onDismissRequest: () -> Unit,
  onConfirm: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    icon = {
      Icon(
        painterResource(id = R.drawable.warning_24dp),
        contentDescription = null,
        tint = Color(0xFFFD7E13),
        modifier = Modifier.size(32.dp)
      )
    },
    title = {
      Text(
        text = stringResource(R.string.regen_api_token_title),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
      )
    },
    text = {
      Column {
        Text(
          text = buildAnnotatedString {
            val text = stringResource(R.string.regen_api_token_warning_1)
            val boldText = stringResource(R.string.critical_operation)
            val index = text.indexOf(boldText)
            if (index != -1) {
              append(text.substring(0, index))
              withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(boldText)
              }
              append(text.substring(index + boldText.length))
            } else {
              append(text)
            }
          },
          color = Color.White,
          fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = buildAnnotatedString {
            val text = stringResource(R.string.regen_api_token_warning_2)
            val boldText = stringResource(R.string.manually_reprogram)
            val index = text.indexOf(boldText)
            if (index != -1) {
              append(text.substring(0, index))
              withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(boldText)
              }
              append(text.substring(index + boldText.length))
            } else {
              append(text)
            }
          },
          color = Color.White,
          fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = stringResource(R.string.regen_api_token_confirm_question),
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        modifier = Modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFFBD5700),
          contentColor = Color.White
        ),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = stringResource(R.string.regen_api_token_confirm_btn), fontWeight = FontWeight.Bold)
        }
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismissRequest,
        modifier = Modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.Transparent,
          contentColor = Color.White
        ),
      ) {
        Text(
          text = stringResource(R.string.cancel),
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      }
    }
  )
}

@Composable
fun ProfileContent(
  profileUiState: ProfileViewModel.ProfileUiState,
  apiTokenUiState: ProfileViewModel.ApiTokenUiState,
  onRegenToken: (String?) -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
        .fillMaxSize()
        .background(Color.Black)
        .padding(16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier
          .verticalScroll(rememberScrollState())
          .padding(vertical = 16.dp)
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF121212),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2C))
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          when (profileUiState) {
            is ProfileViewModel.ProfileUiState.Loading -> {
              CircularProgressIndicator(color = Color(0xFFBD5700))
            }

            is ProfileViewModel.ProfileUiState.Error -> {
              Text(text = profileUiState.errorMessage, color = Color.Red)
            }

            is ProfileViewModel.ProfileUiState.Idle -> {
              val profile = profileUiState.profile
              AsyncImage(
                model = profile?.github?.avatarURL,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
              )

              Spacer(modifier = Modifier.height(24.dp))

              Text(
                text = profile?.github?.name ?: "",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )

              Text(
                text = profile?.github?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E)
              )

              Spacer(modifier = Modifier.height(24.dp))
              HorizontalDivider(color = Color(0xFF2C2C2C))
              Spacer(modifier = Modifier.height(24.dp))

              Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                  text = stringResource(id = R.string.profile_api_token),
                  style = MaterialTheme.typography.titleMedium,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                  modifier = Modifier
                      .fillMaxWidth()
                      .height(56.dp)
                      .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                      .border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                      .padding(horizontal = 16.dp),
                  contentAlignment = Alignment.Center
                ) {
                  when (apiTokenUiState) {
                    is ProfileViewModel.ApiTokenUiState.Loading -> {
                      CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFFBD5700)
                      )
                    }

                    is ProfileViewModel.ApiTokenUiState.Error -> {
                      Text(
                        text = "********-****-****-****-************",
                        color = Color(0xFF9E9E9E),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                      )
                    }

                    is ProfileViewModel.ApiTokenUiState.Idle -> {
                      Text(
                        text = apiTokenUiState.apiToken,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                  onClick = { onRegenToken(profile?.id) },
                  modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBD5700),
                    contentColor = Color.White
                  ),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      painter = painterResource(id = R.drawable.key_24px),
                      contentDescription = null,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = stringResource(id = R.string.profile_regen_apitoken),
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = stringResource(id = R.string.profile_api_token_caption),
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF757575)
                )
              }

              Spacer(modifier = Modifier.height(24.dp))
              HorizontalDivider(color = Color(0xFF2C2C2C))
              Spacer(modifier = Modifier.height(24.dp))

              Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFB71C1C),
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    painter = painterResource(id = R.drawable.ic_logout),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = stringResource(id = R.string.profile_logout),
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
  AppTheme(darkTheme = true) {
    ProfileContent(
      profileUiState = ProfileViewModel.ProfileUiState.Idle(
        Profile(
          id = "1",
          createdAt = "",
          modifiedAt = "",
          fcmToken = null,
          github = GitHub(
            id = 1L,
            login = "john.doe",
            name = "John Doe",
            email = "john.doe@example.com",
            avatarURL = "https://avatars.githubusercontent.com/u/1?v=4"
          )
        )
      ),
      apiTokenUiState = ProfileViewModel.ApiTokenUiState.Idle("********-****-****-****-************"),
      onRegenToken = {},
      onLogout = {}
    )
  }
}
