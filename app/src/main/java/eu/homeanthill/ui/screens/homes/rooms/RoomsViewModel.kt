package eu.homeanthill.ui.screens.homes.rooms

import java.io.IOException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import eu.homeanthill.BuildConfig
import eu.homeanthill.repository.HomesRepository
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.RoomRequest

class RoomsViewModel(
  private val homesRepository: HomesRepository
) : ViewModel() {
  companion object {
    private const val TAG = "RoomsViewModel"
    private const val LOAD_DELAY_MS = 500L
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

  fun createRoom(id: String, name: String, floor: Int) {
    viewModelScope.launch {
      try {
        homesRepository.repoPostRoom(id, RoomRequest(name, floor))
        init()
      } catch (err: IOException) {
        _roomsUiState.emit(RoomsUiState.Error(err.message.toString()))
      }
    }
  }

  fun updateRoom(id: String, rid: String, name: String, floor: Int) {
    viewModelScope.launch {
      try {
        homesRepository.repoPutRoom(id, rid, RoomRequest(name, floor))
        init()
      } catch (err: IOException) {
        _roomsUiState.emit(RoomsUiState.Error(err.message.toString()))
      }
    }
  }

  fun deleteRoom(id: String, rid: String) {
    viewModelScope.launch {
      try {
        homesRepository.repoDeleteRoom(id, rid)
        init()
      } catch (err: IOException) {
        _roomsUiState.emit(RoomsUiState.Error(err.message.toString()))
      }
    }
  }

  private fun init() {
    viewModelScope.launch {
      _roomsUiState.emit(RoomsUiState.Loading)
      delay(LOAD_DELAY_MS)

      try {
        val homes: List<Home> = homesRepository.repoGetHomes()
        if (BuildConfig.DEBUG) Log.d(TAG, "init - homes = $homes")
        _roomsUiState.emit(RoomsUiState.Idle(homes))
      } catch (err: IOException) {
        _roomsUiState.emit(RoomsUiState.Error(err.message.toString()))
      }
    }
  }
}