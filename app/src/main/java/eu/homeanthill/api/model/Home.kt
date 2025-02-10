package eu.homeanthill.api.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Home(
    @SerializedName("id") val id: String,
    @SerializedName("name") var name: String,
    @SerializedName("location") var location: String,
    @SerializedName("rooms") var rooms: List<Room>?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("modifiedAt") val modifiedAt: String,
): Parcelable

data class NewHome(
    @SerializedName("name") var name: String,
    @SerializedName("location") var location: String,
    @SerializedName("rooms") var rooms: List<RoomRequest>,
)

data class UpdateHome(
    @SerializedName("name") var name: String,
    @SerializedName("location") var location: String,
)