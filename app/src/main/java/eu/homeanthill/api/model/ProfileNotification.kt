package eu.homeanthill.api.model

import com.google.gson.annotations.SerializedName

data class ProfileNotificationResponse(
  @SerializedName("notifications") val notifications: List<ProfileNotification>,
)

data class ProfileNotification(
  @SerializedName("id") val id: String,
  @SerializedName("sentAt") val sentAt: Long,
  @SerializedName("title") val title: String,
  @SerializedName("body") val body: String,
  @SerializedName("deviceCount") val deviceCount: Long,
  @SerializedName("devices") val devices: List<Device>,
  @SerializedName("provider") val provider: String,
  @SerializedName("providerMessageId") val providerMessageId: String,
)
