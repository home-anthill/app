package eu.homeanthill.ui.screens.devices.deviceslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import eu.homeanthill.api.model.Device

import eu.homeanthill.api.model.Home
import eu.homeanthill.ui.screens.homes.HomesRoute

//data class HomeItemObj(
//    val id: String = "",
//    val value: Boolean = false
//)
//
//data class HomeEditObj(
//    val id: String = "",
//    var name: String = "",
//    var location: String = "",
//    val value: Boolean = false
//)

@Composable
fun DevicesListScreen(
    devicesUiState: DevicesListViewModel.DevicesUiState,
    devicesViewModel: DevicesListViewModel,
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
//    val showNewDialog = remember { mutableStateOf(false) }
//    var showEditDialog by remember { mutableStateOf(value = HomeEditObj()) }
//    var showCancelDialog by remember { mutableStateOf(value = HomeItemObj()) }

//    if (showNewDialog.value) {
//        NewHomeDialog(
//            onDismissRequest = {
//                showNewDialog.value = false
//            },
//            onConfirmation = { name, location ->
//                coroutineScope.launch {
//                    devicesViewModel.createHome(name, location)
//                    showNewDialog.value = false
//                }
//            },
//        )
//    }
//    if (showEditDialog.value) {
//        EditHomeDialog(
//            homeEditObj = HomeEditObj(
//                id = showEditDialog.id,
//                name = showEditDialog.name,
//                location = showEditDialog.location,
//            ),
//            onDismissRequest = {
//                showEditDialog =
//                    showEditDialog.copy(id = "", name = "", location = "", value = false)
//            },
//            onConfirmation = { id, name, location ->
//                coroutineScope.launch {
//                    devicesViewModel.editHome(id, name, location)
//                    showEditDialog =
//                        showEditDialog.copy(id = "", name = "", location = "", value = false)
//                }
//            },
//        )
//    }
//    if (showCancelDialog.value) {
//        DeleteHomeDialog(
//            onDismissRequest = {
//                showCancelDialog = showCancelDialog.copy(id = "", value = false)
//            },
//            onConfirmation = {
//                coroutineScope.launch {
//                    devicesViewModel.deleteHome(showCancelDialog.id)
//                    showCancelDialog = showCancelDialog.copy(id = "", value = false)
//                }
//            },
//            dialogTitle = "dialogTitle",
//            dialogText = "dialogText",
//        )
//    }
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
                when (devicesUiState) {
                    is DevicesListViewModel.DevicesUiState.Error -> {
                        Text(
                            text = devicesUiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    is DevicesListViewModel.DevicesUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is DevicesListViewModel.DevicesUiState.Idle -> {
                        devicesUiState.devices.forEach { device ->
                            SimpleCard(
                                device = device,
                                onEdit = {
//                                    showEditDialog = showEditDialog.copy(
//                                        id = home.id,
//                                        name = home.name,
//                                        location = home.location,
//                                        value = true,
//                                    )
                                },
                                onRoomsDetails = {
//                                    navController.currentBackStackEntry?.savedStateHandle?.set("home", home)
//                                    navController.navigate(route = HomesRoute.EditHome.name)
                                },
                                onDelete = {
//                                    showCancelDialog =
//                                        showCancelDialog.copy(id = home.id, value = true)
                                }
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
fun SimpleCard(
    device: Device,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRoomsDetails: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 10.dp, horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Box {
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = { expanded = !expanded }
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    modifier = Modifier.align(Alignment.TopEnd),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit home") },
                        onClick = {
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("View rooms")},
                        onClick = {
                            onRoomsDetails()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete home") },
                        onClick = {
                            onDelete()
                        }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 20.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = device.mac,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = device.model,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

//@Composable
//fun EditHomeDialog(
//    homeEditObj: HomeEditObj,
//    onDismissRequest: () -> Unit,
//    onConfirmation: (id: String, name: String, location: String) -> Unit,
//) {
//    var name by remember { mutableStateOf(homeEditObj.name) }
//    var location by remember { mutableStateOf(homeEditObj.location) }
//
//    Dialog(onDismissRequest = { onDismissRequest() }) {
//        // Draw a rectangle shape with rounded corners inside the dialog
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(375.dp)
//                .padding(16.dp),
//            shape = RoundedCornerShape(16.dp),
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize(),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally,
//            ) {
//                Text(
//                    text = "This is a dialog edit home",
//                    modifier = Modifier.padding(16.dp),
//                )
//                TextField(
//                    value = name,
//                    onValueChange = { name = it },
//                    label = { Text("Name") }
//                )
//                TextField(
//                    value = location,
//                    onValueChange = { location = it },
//                    label = { Text("Location") }
//                )
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center,
//                ) {
//                    TextButton(
//                        onClick = { onDismissRequest() },
//                        modifier = Modifier.padding(8.dp),
//                    ) {
//                        Text("Dismiss")
//                    }
//                    TextButton(
//                        onClick = { onConfirmation(homeEditObj.id, name, location) },
//                        modifier = Modifier.padding(8.dp),
//                    ) {
//                        Text("Confirm")
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun DeleteHomeDialog(
    dialogTitle: String,
    dialogText: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}