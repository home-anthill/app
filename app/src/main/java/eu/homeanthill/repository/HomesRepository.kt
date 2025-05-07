package eu.homeanthill.repository

import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.NewHome
import eu.homeanthill.api.model.Room
import eu.homeanthill.api.model.RoomRequest
import eu.homeanthill.api.model.UpdateHome
import java.io.IOException

import eu.homeanthill.api.requests.HomesServices

class HomesRepository(private val homesService: HomesServices) {
    suspend fun repoGetHomes(): List<Home> {
        val result = homesService.getHomes()
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw IOException("Error repoGetHomes")
        }
    }

    suspend fun repoPostHome(body: NewHome): Home {
        val result = homesService.postHome(body)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw IOException("Error repoPostHome")
        }
    }

    suspend fun repoPutHome(id: String, body: UpdateHome): GenericMessageResponse {
        val result = homesService.putHome(id, body)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw IOException("Error repoPutHome")
        }
    }

    suspend fun repoDeleteHome(id: String): GenericMessageResponse {
        val result = homesService.deleteHome(id)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw IOException("Error repoDeleteHome")
        }
    }

    suspend fun repoGetRooms(id: String): List<Room> {
        val result = homesService.getRooms(id)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw IOException("Error repoGetRooms")
        }
    }

    suspend fun repoPostRoom(id: String, body: RoomRequest): Room {
        val result = homesService.postRoom(id, body)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw IOException("Error repoPostRoom")
        }
    }

    suspend fun repoPutRoom(id: String, rid: String, body: RoomRequest): GenericMessageResponse {
        val result = homesService.putRoom(id, rid, body)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw IOException("Error repoPutRoom")
        }
    }

    suspend fun repoDeleteRoom(id: String, rid: String): GenericMessageResponse {
        val result = homesService.deleteRoom(id, rid)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw IOException("Error repoDeleteRoom")
        }
    }
}