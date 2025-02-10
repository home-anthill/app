package eu.homeanthill.ui.screens.homes.homeslist

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
import eu.homeanthill.api.model.NewHome
import eu.homeanthill.api.model.UpdateHome

class HomesListViewModel(
    private val homesRepository: HomesRepository
) : ViewModel() {
    companion object {
        private const val TAG = "HomesListViewModel"
    }

    sealed class HomesUiState {
        data class Idle(val homes: List<Home>) : HomesUiState()
        data object Loading : HomesUiState()
        data class Error(val errorMessage: String) : HomesUiState()
    }

    private val _homesUiState = MutableStateFlow<HomesUiState>(HomesUiState.Idle(emptyList()))
    val homesUiState: StateFlow<HomesUiState> = _homesUiState

    init {
        init()
    }

    suspend fun createHome(name: String, location: String) {
        homesRepository.repoPostHome(NewHome(name = name, location = location, rooms = listOf()))
        init()
    }

    suspend fun editHome(id: String, name: String, location: String) {
        homesRepository.repoPutHome(id, UpdateHome(name = name, location = location))
        init()
    }

    suspend fun deleteHome(id: String) {
        homesRepository.repoDeleteHome(id)
        init()
    }

    private fun init() {
        viewModelScope.launch {
            _homesUiState.emit(HomesUiState.Loading)
            delay(500)

            try {
                val homes: List<Home> = homesRepository.repoGetHomes()
                Log.d(TAG, "init - homes = $homes")
                _homesUiState.emit(HomesUiState.Idle(homes))
            } catch (err: IOException) {
                _homesUiState.emit(HomesUiState.Error(err.message.toString()))
            }
        }
    }
}