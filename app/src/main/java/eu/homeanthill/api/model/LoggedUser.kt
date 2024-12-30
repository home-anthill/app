package eu.homeanthill.api.model

import com.google.gson.annotations.SerializedName

data class LoggedUser(
    @SerializedName("apiToken") val apiToken: String?,
    @SerializedName("fcmToken") val fcmToken: String?
)