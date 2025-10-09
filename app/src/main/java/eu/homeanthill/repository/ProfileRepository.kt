package eu.homeanthill.repository

import java.io.IOException

import eu.homeanthill.api.model.ProfileAPITokenResponse
import eu.homeanthill.api.model.Profile
import eu.homeanthill.api.requests.ProfileServices

class ProfileRepository(private val profileService: ProfileServices) {
  suspend fun repoGetProfile(): Profile {
    val result = profileService.getProfile()
    if (result.isSuccessful) {
      return result.body()!!
    } else {
      throw IOException("Error repoGetProfile")
    }
  }

  suspend fun repoPostRegenAPIToken(id: String): ProfileAPITokenResponse {
    val result = profileService.postRegenApiToken(id)
    if (result.isSuccessful) {
      return result.body()!!
    } else {
      throw IOException("Error repoPostRegenAPIToken")
    }
  }
}