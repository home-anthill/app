package eu.homeanthill.ui.screens.homes.rooms

import java.io.IOException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import eu.homeanthill.repository.HomesRepository
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.RoomRequest
import eu.homeanthill.api.model.UpdateHome

class RoomsViewModel(
    private val homesRepository: HomesRepository
) : ViewModel() {
    companion object {
        private const val TAG = "RoomsViewModel"
    }

    sealed class RoomsUiState {
        data class Idle(val homes: List<Home>) : RoomsUiState()
        data object Loading : RoomsUiState()
        data class Error(val errorMessage: String) : RoomsUiState()
    }

    private val _roomsUiState =
        MutableStateFlow<RoomsUiState>(RoomsUiState.Idle(emptyList()))
    val roomsUiState: StateFlow<RoomsUiState> = _roomsUiState

    init {
        init()
    }

    suspend fun createRoom(id: String, name: String, floor: Number) {
        homesRepository.repoPostRoom(id, RoomRequest(name, floor))
        init()
    }

    suspend fun updateRoom(id: String, rid: String, name: String, floor: Number) {
        homesRepository.repoPutRoom(id, rid, RoomRequest(name, floor))
        init()
    }

    suspend fun deleteRoom(id: String, rid: String) {
        homesRepository.repoDeleteRoom(id, rid)
        init()
    }

    private fun init() {
        viewModelScope.launch {
            _roomsUiState.emit(RoomsUiState.Loading)
            delay(500)

            try {
                val homes: List<Home> = homesRepository.repoGetHomes()
                Log.d(TAG, "init - homes = $homes")
                _roomsUiState.emit(RoomsUiState.Idle(homes))
            } catch (err: IOException) {
                _roomsUiState.emit(RoomsUiState.Error(err.message.toString()))
            }
        }
    }
}