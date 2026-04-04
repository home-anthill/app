package eu.homeanthill.ui.screens.devices.editdevice

import java.io.IOException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.PutDevice
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.repository.HomesRepository

class EditDeviceViewModel(
  private val homesRepository: HomesRepository,
  private val devicesRepository: DevicesRepository
) : ViewModel() {
  companion object {
    private const val LOAD_DELAY_MS = 500L
  }

  sealed class EditDeviceUiState {
    data class Idle(val homes: List<Home>) : EditDeviceUiState()
    data object Loading : EditDeviceUiState()
    data class Error(val errorMessage: String) : EditDeviceUiState()
  }

  private val _editDeviceUiState = MutableStateFlow<EditDeviceUiState>(
    EditDeviceUiState.Idle(
      emptyList()
    )
  )
  val editDeviceUiState: StateFlow<EditDeviceUiState> = _editDeviceUiState

  init {
    init()
  }

  fun assignDevice(id: String, homeId: String, roomId: String) {
    viewModelScope.launch {
      try {
        devicesRepository.repoAssignDeviceToHomeRoom(
          id = id,
          body = PutDevice(homeId = homeId, roomId = roomId)
        )
        init()
      } catch (err: IOException) {
        _editDeviceUiState.emit(EditDeviceUiState.Error(err.message.toString()))
      }
    }
  }

  fun deleteDevice(id: String) {
    viewModelScope.launch {
      try {
        devicesRepository.repoDeleteDevice(id = id)
      } catch (err: IOException) {
        _editDeviceUiState.emit(EditDeviceUiState.Error(err.message.toString()))
      }
    }
  }

  private fun init() {
    viewModelScope.launch {
      _editDeviceUiState.emit(EditDeviceUiState.Loading)
      delay(LOAD_DELAY_MS)

      try {
        val homes: List<Home> = homesRepository.repoGetHomes()
        _editDeviceUiState.emit(EditDeviceUiState.Idle(homes))
      } catch (err: IOException) {
        _editDeviceUiState.emit(EditDeviceUiState.Error(err.message.toString()))
      }
    }
  }
}