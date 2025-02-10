package eu.homeanthill.ui.screens.homes.rooms

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.Room
import eu.homeanthill.ui.screens.homes.HomesRoute

@Composable
fun RoomsScreen(
    roomsUiState: RoomsViewModel.RoomsUiState,
    roomsViewModel: RoomsViewModel,
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val showNewDialog = remember { mutableStateOf(false) }
    val home = navController.previousBackStackEntry?.savedStateHandle?.get<Home>("home")
    Log.d("___________", " home $home") // TODO not working, why???

    if (showNewDialog.value) {
        NewRoomDialog(
            onDismissRequest = {
                showNewDialog.value = false
            },
            onConfirmation = { name, floor ->
                coroutineScope.launch {
                    if (home == null) {
                        return@launch
                    }
                    roomsViewModel.createRoom(home.id, name, floor.toInt())
                    showNewDialog.value = false
                }
            },
        )
    }

    Scaffold(
        content = { padding ->
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
                        Text(
                            text = roomsUiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    is RoomsViewModel.RoomsUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is RoomsViewModel.RoomsUiState.Idle -> {
                        val currHome = roomsUiState.homes.find { h -> h.id == home?.id }

                        if (currHome !== null) {
                            Text(
                                text = currHome.name,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = currHome.location,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // iterate over rooms
                            if (currHome.rooms !== null) {
                                currHome.rooms?.forEach { r ->
                                    SimpleCard(
                                        room = r,
                                        onEdit = { name, floor ->
                                            coroutineScope.launch {
                                                roomsViewModel.updateRoom(
                                                    id = currHome.id,
                                                    rid = r.id,
                                                    name,
                                                    floor.toInt()
                                                )
                                            }
                                        },
                                        onDelete = {
                                            coroutineScope.launch {
                                                roomsViewModel.deleteRoom(
                                                    id = currHome.id,
                                                    rid = r.id
                                                )
                                            }
                                        },
                                    )
                                }
                            }
//                            TextButton(
//                                onClick = {
//                                    coroutineScope.launch {
////                                        roomsViewModel.updateHome(
////                                            id = home.id,
////                                            name = newName,
////                                            location = newLocation
////                                        )
//                                        navController.navigate(route = HomesRoute.Homes.name)
//                                    }
//                                }
//                            ) {
//                                Text(text = "Save")
//                            }
//                            TextButton(
//                                onClick = {
//                                    navController.navigate(route = HomesRoute.Homes.name)
//                                }
//                            ) {
//                                Text(text = "Cancel")
//                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showNewDialog.value = true
                },
            ) {
                Icon(Icons.Filled.Add, "Floating action button.")
            }
        }
    )
}

@Composable
fun SimpleCard(
    room: Room,
    onEdit: (name: String, floor: String) -> Unit,
    onDelete: () -> Unit,
) {
    var name: String by remember { mutableStateOf(room.name) }
    var floor: String by remember { mutableStateOf(room.floor.toString()) }

    Card(
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(vertical = 10.dp, horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextField(

                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                )
                TextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Floor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top,
            ) {
                TextButton(
                    onClick = { onDelete() },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text("Delete")
                }
                TextButton(
                    onClick = { onEdit(name, floor) },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun NewRoomDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (name: String, location: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "This is a dialog with buttons and an image.",
                    modifier = Modifier.padding(16.dp),
                )
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                TextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Floor") }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = { onConfirmation(name, floor) },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
