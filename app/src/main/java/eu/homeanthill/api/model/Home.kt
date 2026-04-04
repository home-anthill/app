package eu.homeanthill.api.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Home(
  @SerializedName("id") val id: String,
  @SerializedName("name") val name: String,
  @SerializedName("location") val location: String,
  @SerializedName("rooms") val rooms: List<Room>?,
  @SerializedName("createdAt") val createdAt: String,
  @SerializedName("modifiedAt") val modifiedAt: String,
) : Parcelable

data class NewHome(
  @SerializedName("name") val name: String,
  @SerializedName("location") val location: String,
  @SerializedName("rooms") val rooms: List<RoomRequest>,
)

data class UpdateHome(
  @SerializedName("name") val name: String,
  @SerializedName("location") val location: String,
)