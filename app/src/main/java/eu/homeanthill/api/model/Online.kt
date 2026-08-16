package eu.homeanthill.api.model

import com.google.gson.annotations.SerializedName

enum class OnlineStatus {
  @SerializedName("online") ONLINE,
  @SerializedName("offline") OFFLINE,
  @SerializedName("unknown") UNKNOWN,
}

data class OnlineDeviceStatus(
  @SerializedName("deviceId") val deviceId: String,
  @SerializedName("featureUuid") val featureUuid: String,
  @SerializedName("status") val status: OnlineStatus,
  @SerializedName("createdAt") val createdAt: String?,
  @SerializedName("modifiedAt") val modifiedAt: String?,
  @SerializedName("currentTime") val currentTime: String,
)
