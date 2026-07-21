package eu.homeanthill.api.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class OnlineValue(
  @SerializedName("createdAt") val createdAt: String,
  @SerializedName("modifiedAt") val modifiedAt: String,
  @SerializedName("currentTime") val currentTime: String,
) : Parcelable

data class OnlineDeviceStatus(
  @SerializedName("createdAt") val createdAt: String,
  @SerializedName("modifiedAt") val modifiedAt: String,
  @SerializedName("currentTime") val currentTime: String,
  @SerializedName("device") val device: Device,
)
