package eu.homeanthill.api.model

import com.google.gson.annotations.SerializedName

data class LoggedUser(
    @SerializedName("fcmToken") val fcmToken: String?
)