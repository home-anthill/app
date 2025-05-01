package eu.homeanthill.repository

import java.io.IOException

import eu.homeanthill.api.model.OnlineValue
import eu.homeanthill.api.requests.OnlineServices

class OnlineRepository(private val onlineService: OnlineServices) {
    suspend fun repoGetOnlineValues(id: String): OnlineValue {
        val result = onlineService.getOnlineValues(id)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw Exception(IOException("Error repoGetOnlineValues"))
        }
    }
}