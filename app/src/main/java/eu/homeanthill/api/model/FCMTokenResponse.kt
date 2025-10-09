package eu.homeanthill.api.model

import com.google.gson.annotations.SerializedName

data class FCMTokenResponse(
  @SerializedName("message") val message: String,
)