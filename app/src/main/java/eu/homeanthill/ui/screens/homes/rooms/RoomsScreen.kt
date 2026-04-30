package eu.homeanthill.ui.screens.homes.rooms

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController

import eu.homeanthill.R
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.Room
import eu.homeanthill.ui.components.ItemActionButtons
import eu.homeanthill.ui.theme.AppTheme

data class RoomItemObj(
  val id: String = "", val value: Boolean = false
)

data class RoomEditObj(
  val id: String = "", val name: String = "", val floor: Int = 0, val value: Boolean = false
)

data class HomeEditObj(
  val id: String = "", val name: String = "", val location: String = "", val value: Boolean = false
)

data class HomeDeleteObj(
  val id: String = "", val value: Boolean = false
)

@Composable
fun RoomsScreen(
  roomsUiState: RoomsViewModel.RoomsUiState,
  roomsViewModel: RoomsViewModel,
  navController: NavController,
) {
  val home = navController.previousBackStackEntry?.savedStateHandle?.get<Home>("home")

  LaunchedEffect(Unit) {
    roomsViewModel.loadHomes()
  }

  RoomsContent(
    roomsUiState = roomsUiState,
    homeId = home?.id,
    onNavigateBack = { navController.popBackStack() },
    onEditHome = { id, name, location ->
      roomsViewModel.editHome(id, name, location)
    },
    onDeleteHome = { id ->
      roomsViewModel.deleteHome(id)
      navController.popBackStack()
    },
    onCreateRoom = { id, name, floor ->
      roomsViewModel.createRoom(id, name, floor)
    },
    onUpdateRoom = { id, rid, name, floor ->
      roomsViewModel.updateRoom(id, rid, name, floor)
    },
    onDeleteRoom = { id, rid ->
      roomsViewModel.deleteRoom(id, rid)
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsContent(
  roomsUiState: RoomsViewModel.RoomsUiState,
  homeId: String?,
  onNavigateBack: () -> Unit,
  onEditHome: (id: String, name: String, location: String) -> Unit,
  onDeleteHome: (id: String) -> Unit,
  onCreateRoom: (id: String, name: String, floor: Int) -> Unit,
  onUpdateRoom: (id: String, rid: String, name: String, floor: Int) -> Unit,
  onDeleteRoom: (id: String, rid: String) -> Unit,
) {
  val showNewRoomDialog = remember { mutableStateOf(false) }
  var showEditRoomDialog by remember { mutableStateOf(value = RoomEditObj()) }
  var showDeleteRoomDialog by remember { mutableStateOf(value = RoomItemObj()) }

  var showEditHomeDialog by remember { mutableStateOf(value = HomeEditObj()) }
  var showDeleteHomeDialog by remember { mutableStateOf(value = HomeDeleteObj()) }

  val currHomeFromState = when (roomsUiState) {
    is RoomsViewModel.RoomsUiState.Idle -> roomsUiState.homes.find { it.id == homeId }
    else -> null
  }

  // Home Edit Dialog
  if (showEditHomeDialog.value) {
    HomeEditDialog(
      dialogText = stringResource(R.string.homes_edit_title),
      saveText = stringResource(R.string.save),
      cancelText = stringResource(R.string.cancel),
      homeEditObj = showEditHomeDialog,
      onDismissRequest = { showEditHomeDialog = showEditHomeDialog.copy(value = false) },
      onConfirmation = { id, name, location ->
        onEditHome(id, name, location)
        showEditHomeDialog = showEditHomeDialog.copy(value = false)
      })
  }

  // Home Delete Dialog
  if (showDeleteHomeDialog.value) {
    HomeDeleteDialog(
      dialogTitle = stringResource(R.string.homes_delete_title),
      dialogText = stringResource(R.string.home_delete_confirm_complex),
      confirmText = stringResource(R.string.yes),
      dismissText = stringResource(R.string.no),
      onDismissRequest = { showDeleteHomeDialog = showDeleteHomeDialog.copy(value = false) },
      onConfirmation = {
        onDeleteHome(showDeleteHomeDialog.id)
        showDeleteHomeDialog = showDeleteHomeDialog.copy(value = false)
      })
  }

  // Room Dialogs
  if (showNewRoomDialog.value) {
    NewRoomDialog(
      dialogText = stringResource(R.string.room_new_title),
      saveText = stringResource(R.string.save),
      cancelText = stringResource(R.string.cancel),
      onDismissRequest = { showNewRoomDialog.value = false },
      onConfirmation = { name, floor ->
        val floorValue = floor.toIntOrNull()
        if (homeId != null && floorValue != null) {
          onCreateRoom(homeId, name, floorValue)
          showNewRoomDialog.value = false
        }
      },
    )
  }

  if (showEditRoomDialog.value) {
    EditRoomDialog(
      dialogText = stringResource(R.string.room_edit_title),
      saveText = stringResource(R.string.save),
      cancelText = stringResource(R.string.cancel),
      roomEditObj = showEditRoomDialog,
      onDismissRequest = { showEditRoomDialog = showEditRoomDialog.copy(value = false) },
      onConfirmation = { name, floor ->
        val floorValue = floor.toIntOrNull()
        if (homeId != null && floorValue != null) {
          onUpdateRoom(homeId, showEditRoomDialog.id, name, floorValue)
          showEditRoomDialog = showEditRoomDialog.copy(value = false)
        }
      })
  }

  if (showDeleteRoomDialog.value) {
    DeleteRoomDialog(
      dialogTitle = stringResource(R.string.room_delete_title),
      dialogText = stringResource(R.string.room_delete_confirm),
      confirmText = stringResource(R.string.yes),
      dismissText = stringResource(R.string.no),
      onDismissRequest = { showDeleteRoomDialog = showDeleteRoomDialog.copy(value = false) },
      onConfirmation = {
        if (homeId != null) {
          onDeleteRoom(homeId, showDeleteRoomDialog.id)
          showDeleteRoomDialog = showDeleteRoomDialog.copy(value = false)
        }
      })
  }

  Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
    TopAppBar(
      title = {
      Text(
        stringResource(R.string.home_details), color = MaterialTheme.colorScheme.tertiary
      )
    },
      navigationIcon = {
        IconButton(onClick = onNavigateBack) {
          Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            tint = MaterialTheme.colorScheme.tertiary
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
  }, content = { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      when (roomsUiState) {
        is RoomsViewModel.RoomsUiState.Error -> {
          Text(text = roomsUiState.errorMessage, color = MaterialTheme.colorScheme.error)
        }

        is RoomsViewModel.RoomsUiState.Loading -> {
          CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
        }

        is RoomsViewModel.RoomsUiState.Idle -> {
          if (currHomeFromState != null) {
            // Home Details Card
            HomeDetailsHeader(
              home = currHomeFromState, onEdit = {
                showEditHomeDialog = HomeEditObj(
                  id = currHomeFromState.id,
                  name = currHomeFromState.name,
                  location = currHomeFromState.location,
                  value = true
                )
              })

            Spacer(modifier = Modifier.height(24.dp))

            Text(
              text = stringResource(R.string.rooms_title),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
            )

            if (currHomeFromState.rooms.isNullOrEmpty()) {
              Text(
                stringResource(R.string.rooms_empty),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
              )
            } else {
              currHomeFromState.rooms.forEach { r ->
                RoomCard(
                  room = r,
                  onEdit = {
                    showEditRoomDialog =
                      RoomEditObj(id = r.id, name = r.name, floor = r.floor, value = true)
                  },
                  onDelete = {
                    showDeleteRoomDialog = RoomItemObj(id = r.id, value = true)
                  },
                )
              }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            Spacer(modifier = Modifier.height(32.dp))

            // Delete Home Button
            Button(
              onClick = {
                showDeleteHomeDialog = HomeDeleteObj(id = currHomeFromState.id, value = true)
              },
              colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.tertiary,
                containerColor = MaterialTheme.colorScheme.error
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.homes_delete_title), fontWeight = FontWeight.Bold)
              }
            }
            Spacer(modifier = Modifier.height(24.dp))
          }
        }
      }
    }
  }, floatingActionButton = {
    FloatingActionButton(
      onClick = { showNewRoomDialog.value = true },
      containerColor = MaterialTheme.colorScheme.secondary,
      contentColor = MaterialTheme.colorScheme.tertiary,

      ) {
      Icon(Icons.Default.Add, contentDescription = stringResource(R.string.rooms_add))
    }
  })
}

@Composable
fun HomeDetailsHeader(
  home: Home, onEdit: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Business,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.size(32.dp)
      )
      Spacer(modifier = Modifier.width(16.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = home.name,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.tertiary
        )
        Text(
          text = home.location,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        )
      }
      ItemActionButtons(onEdit = onEdit)
    }
  }
}

@Composable
fun RoomCard(
  room: Room, onEdit: () -> Unit, onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(40.dp)
          .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(2.dp))
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = room.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.tertiary
        )
        Text(
          text = "${stringResource(R.string.room_floor)} ${room.floor}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        )
      }
      ItemActionButtons(onEdit = onEdit, onDelete = onDelete)
    }
  }
}

@Composable
fun HomeEditDialog(
  dialogText: String,
  saveText: String,
  cancelText: String,
  homeEditObj: HomeEditObj,
  onDismissRequest: () -> Unit,
  onConfirmation: (id: String, name: String, location: String) -> Unit,
) {
  var name by rememberSaveable(homeEditObj.id) { mutableStateOf(homeEditObj.name) }
  var location by rememberSaveable(homeEditObj.id) { mutableStateOf(homeEditObj.location) }

  val isSaveEnabled = name.trim().isNotEmpty() && location.trim().isNotEmpty()

  Dialog(onDismissRequest = { onDismissRequest() }) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = dialogText,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = { onDismissRequest() }) {
            Text(
              text = cancelText, color = MaterialTheme.colorScheme.tertiary
            )
          }
          TextButton(
            onClick = { onConfirmation(homeEditObj.id, name, location) }, enabled = isSaveEnabled
          ) {
            Text(
              text = saveText,
              color = if (isSaveEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.tertiary.copy(
                alpha = 0.5f
              ),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
fun HomeDeleteDialog(
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
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
      Column(modifier = Modifier.padding(24.dp)) {
        Text(
          text = dialogTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = dialogText,
          color = MaterialTheme.colorScheme.tertiary,
          style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismissRequest) {
            Text(
              text = dismissText, color = MaterialTheme.colorScheme.tertiary
            )
          }
          Spacer(modifier = Modifier.width(16.dp))
          Button(
            onClick = onConfirmation,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.tertiary
            ),
          ) {
            Text(text = confirmText, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun EditRoomDialog(
  dialogText: String,
  saveText: String,
  cancelText: String,
  roomEditObj: RoomEditObj,
  onDismissRequest: () -> Unit,
  onConfirmation: (name: String, floor: String) -> Unit,
) {
  var name by rememberSaveable(roomEditObj.id) { mutableStateOf(roomEditObj.name) }
  var floor by rememberSaveable(roomEditObj.id) { mutableStateOf(roomEditObj.floor.toString()) }

  val isSaveEnabled = name.trim().isNotEmpty() && floor.toIntOrNull() != null

  Dialog(onDismissRequest = { onDismissRequest() }) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = dialogText,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
        )
        TextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(stringResource(R.string.name)) },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
          value = floor,
          onValueChange = {
            if (it.isEmpty() || it.toIntOrNull() != null) {
              floor = it
            }
          },
          label = { Text(stringResource(R.string.room_floor)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = { onDismissRequest() }) {
            Text(
              text = cancelText, color = MaterialTheme.colorScheme.tertiary
            )
          }
          TextButton(
            onClick = { onConfirmation(name, floor) }, enabled = isSaveEnabled
          ) {
            Text(
              text = saveText,
              color = if (isSaveEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.tertiary.copy(
                alpha = 0.5f
              ),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
fun DeleteRoomDialog(
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
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
      Column(modifier = Modifier.padding(24.dp)) {
        Text(
          text = dialogTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = dialogText,
          color = MaterialTheme.colorScheme.tertiary,
          style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismissRequest) {
            Text(
              text = dismissText, color = MaterialTheme.colorScheme.tertiary
            )
          }
          Spacer(modifier = Modifier.width(16.dp))
          Button(
            onClick = onConfirmation,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.tertiary
            ),
          ) {
            Text(text = confirmText, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun NewRoomDialog(
  dialogText: String,
  saveText: String,
  cancelText: String,
  onDismissRequest: () -> Unit,
  onConfirmation: (name: String, floor: String) -> Unit,
) {
  var name by rememberSaveable { mutableStateOf("") }
  var floor by rememberSaveable { mutableStateOf("") }

  val isSaveEnabled = name.trim().isNotEmpty() && floor.toIntOrNull() != null

  Dialog(onDismissRequest = { onDismissRequest() }) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = dialogText,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
        )
        TextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(stringResource(R.string.name)) },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
          value = floor,
          onValueChange = {
            if (it.isEmpty() || it.toIntOrNull() != null) {
              floor = it
            }
          },
          label = { Text(stringResource(R.string.room_floor)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = { onDismissRequest() }) {
            Text(
              text = cancelText, color = MaterialTheme.colorScheme.tertiary
            )
          }
          TextButton(
            onClick = { onConfirmation(name, floor) }, enabled = isSaveEnabled
          ) {
            Text(
              text = saveText,
              color = if (isSaveEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.tertiary.copy(
                alpha = 0.5f
              ),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun RoomCardPreview() {
  AppTheme {
    RoomCard(
      room = Room(
      id = "1",
      name = "Salotto",
      floor = 1,
      createdAt = "",
      modifiedAt = "",
      devices = emptyList()
    ), onEdit = {}, onDelete = {})
  }
}

@Preview(showBackground = true)
@Composable
fun RoomsContentPreview() {
  val mockHome = Home(
    id = "1", name = "Casa", location = "Torino", rooms = listOf(
      Room(
        id = "1",
        name = "Salotto",
        floor = 1,
        createdAt = "",
        modifiedAt = "",
        devices = emptyList()
      ), Room(
        id = "2", name = "Cucina", floor = 1, createdAt = "", modifiedAt = "", devices = emptyList()
      )
    ), createdAt = "", modifiedAt = ""
  )
  AppTheme {
    RoomsContent(
      roomsUiState = RoomsViewModel.RoomsUiState.Idle(homes = listOf(mockHome)),
      homeId = "1",
      onNavigateBack = {},
      onEditHome = { _, _, _ -> },
      onDeleteHome = { _ -> },
      onCreateRoom = { _, _, _ -> },
      onUpdateRoom = { _, _, _, _ -> },
      onDeleteRoom = { _, _ -> })
  }
}
