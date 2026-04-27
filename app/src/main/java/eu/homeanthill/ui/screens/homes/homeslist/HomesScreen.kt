package eu.homeanthill.ui.screens.homes.homeslist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController

import eu.homeanthill.R
import eu.homeanthill.ui.theme.AppTheme
import eu.homeanthill.api.model.Home
import eu.homeanthill.ui.screens.homes.HomesRoute

@Composable
fun HomesListScreen(
  homesUiState: HomesListViewModel.HomesUiState,
  homesViewModel: HomesListViewModel,
  navController: NavController,
) {
  var isRefreshing by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    homesViewModel.loadHomes()
  }

  LaunchedEffect(homesUiState) {
    if (homesUiState !is HomesListViewModel.HomesUiState.Loading) {
      isRefreshing = false
    }
  }

  HomesListContent(
    homesUiState = homesUiState,
    isRefreshing = isRefreshing,
    onRefresh = {
      isRefreshing = true
      homesViewModel.loadHomes()
    },
    onCreateHome = { name, location ->
      homesViewModel.createHome(name, location)
    },
    onNavigateToDetails = { home ->
      navController.currentBackStackEntry?.savedStateHandle?.set("home", home)
      navController.navigate(route = HomesRoute.EditHome.name)
    }
  )
}

@Composable
fun HomesListContent(
  homesUiState: HomesListViewModel.HomesUiState,
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  onCreateHome: (name: String, location: String) -> Unit,
  onNavigateToDetails: (home: Home) -> Unit,
) {
  val showNewDialog = remember { mutableStateOf(false) }

  if (showNewDialog.value) {
    NewHomeDialog(
      dialogText = stringResource(R.string.homes_new_title),
      saveText = stringResource(R.string.save),
      cancelText = stringResource(R.string.cancel),
      onDismissRequest = {
        showNewDialog.value = false
      },
      onConfirmation = { name, location ->
        onCreateHome(name, location)
        showNewDialog.value = false
      },
    )
  }

  Scaffold(
    containerColor = Color.Black,
    content = { padding ->
      PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = rememberPullToRefreshState(),
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.Top,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          when (homesUiState) {
            is HomesListViewModel.HomesUiState.Error -> {
              Text(
                text = homesUiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
              )
            }

            is HomesListViewModel.HomesUiState.Loading -> {
              if (!isRefreshing) {
                CircularProgressIndicator(color = Color(0xFFBD5700))
              }
            }

            is HomesListViewModel.HomesUiState.Idle -> {
              homesUiState.homes.forEach { home ->
                HomeItemCard(
                  home = home,
                  onClick = { onNavigateToDetails(home) }
                )
              }
            }
          }
        }
      }
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showNewDialog.value = true },
        containerColor = Color(0xFFBD5700),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp)
      ) {
        Icon(
          Icons.Default.Add,
          contentDescription = stringResource(R.string.homes_add),
        )
      }
    }
  )
}

@Composable
fun HomeItemCard(
  home: Home,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp)
      .clickable { onClick() },
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2C))
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(16.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Business,
        contentDescription = null,
        tint = Color(0xFFBD5700),
        modifier = Modifier.size(32.dp)
      )
      Spacer(modifier = Modifier.width(16.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = home.name,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = home.location,
          style = MaterialTheme.typography.bodyMedium,
          color = Color.Gray
        )
      }
      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = Color.Gray
      )
    }
  }
}

@Composable
fun NewHomeDialog(
  dialogText: String,
  saveText: String,
  cancelText: String,
  onDismissRequest: () -> Unit,
  onConfirmation: (name: String, location: String) -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var location by remember { mutableStateOf("") }

  val isSaveEnabled = name.trim().isNotEmpty() && location.trim().isNotEmpty()

  Dialog(onDismissRequest = { onDismissRequest() }) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2C))
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = dialogText,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        )
        TextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(stringResource(R.string.name)) },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
          value = location,
          onValueChange = { location = it },
          label = { Text(stringResource(R.string.location)) },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
        ) {
          TextButton(onClick = { onDismissRequest() }) {
            Text(text = cancelText, color = Color.White)
          }
          TextButton(
            onClick = { onConfirmation(name, location) },
            enabled = isSaveEnabled
          ) {
            Text(
              text = saveText,
              color = if (isSaveEnabled) Color.White else Color.Gray,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
fun DeleteHomeDialog(
  dialogTitle: String,
  dialogText: String,
  confirmText: String,
  dismissText: String,
  onDismissRequest: () -> Unit,
  onConfirmation: () -> Unit,
) {
  Dialog(onDismissRequest = onDismissRequest) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2C))
    ) {
      Column(
        modifier = Modifier.padding(24.dp)
      ) {
        Text(
          text = dialogTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = dialogText,
          color = Color.White,
          style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismissRequest) {
            Text(text = dismissText, color = Color.White)
          }
          Spacer(modifier = Modifier.width(16.dp))
          Button(
            onClick = onConfirmation,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFBD5700),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(text = confirmText, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun HomeItemCardPreview() {
  AppTheme(darkTheme = true) {
    HomeItemCard(
      home = Home(id = "1", name = "Casa", location = "Torino", rooms = emptyList(), createdAt = "", modifiedAt = ""),
      onClick = {}
    )
  }
}

@Preview(showBackground = true)
@Composable
fun HomesListContentPreview() {
  AppTheme(darkTheme = true) {
    HomesListContent(
      homesUiState = HomesListViewModel.HomesUiState.Idle(
        homes = listOf(
          Home(id = "1", name = "Casa", location = "Torino", rooms = emptyList(), createdAt = "", modifiedAt = ""),
          Home(id = "2", name = "Ufficio", location = "Milano", rooms = emptyList(), createdAt = "", modifiedAt = "")
        )
      ),
      isRefreshing = false,
      onRefresh = {},
      onCreateHome = { _, _ -> },
      onNavigateToDetails = { _ -> }
    )
  }
}
