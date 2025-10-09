package eu.homeanthill.api.model

import com.google.gson.annotations.SerializedName

data class GitHub(
  @SerializedName("id") val id: Number,
  @SerializedName("login") val login: String,
  @SerializedName("name") val name: String,
  @SerializedName("email") val email: String,
  @SerializedName("avatarURL") val avatarURL: String
)

data class Profile(
  @SerializedName("id") val id: String,
  @SerializedName("createdAt") val createdAt: String,
  @SerializedName("modifiedAt") val modifiedAt: String,
  @SerializedName("github") val github: GitHub,
  @SerializedName("fcmToken") var fcmToken: String?,
)

data class ProfileAPITokenResponse(
  @SerializedName("apiToken") val apiToken: String,
)