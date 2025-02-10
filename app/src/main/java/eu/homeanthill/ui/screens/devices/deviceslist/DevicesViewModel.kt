package eu.homeanthill.ui.screens.devices.deviceslist

import java.io.IOException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.Device
import eu.homeanthill.repository.DevicesRepository

class DevicesListViewModel(
    private val devicesRepository: DevicesRepository
) : ViewModel() {
    companion object {
        private const val TAG = "DevicesListViewModel"
    }

    sealed class DevicesUiState {
        data class Idle(val devices: List<Device>) : DevicesUiState()
        data object Loading : DevicesUiState()
        data class Error(val errorMessage: String) : DevicesUiState()
    }

    private val _deviceUiState = MutableStateFlow<DevicesUiState>(DevicesUiState.Idle(emptyList()))
    val devicesUiState: StateFlow<DevicesUiState> = _deviceUiState

    init {
        init()
    }

//    suspend fun createHome(name: String, location: String) {
//        homesRepository.repoPostHome(NewHome(name = name, location = location, rooms = listOf()))
//        init()
//    }
//
//    suspend fun editHome(id: String, name: String, location: String) {
//        homesRepository.repoPutHome(id, UpdateHome(name = name, location = location))
//        init()
//    }
//
//    suspend fun deleteHome(id: String) {
//        homesRepository.repoDeleteHome(id)
//        init()
//    }

    private fun init() {
        viewModelScope.launch {
            _deviceUiState.emit(DevicesUiState.Loading)
            delay(500)

            try {
                val devices: List<Device> = devicesRepository.repoGetDevices()
                Log.d(TAG, "init - devices = $devices")
                _deviceUiState.emit(DevicesUiState.Idle(devices))
            } catch (err: IOException) {
                _deviceUiState.emit(DevicesUiState.Error(err.message.toString()))
            }
        }
    }
}