package eu.homeanthill.api.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Room(
    @SerializedName("id") val id: String,
    @SerializedName("name") var name: String,
    @SerializedName("floor") var floor: Number,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("modifiedAt") val modifiedAt: String,
    @SerializedName("devices") val devices: List<String>?,
): Parcelable

data class RoomRequest(
    @SerializedName("name") var name: String,
    @SerializedName("floor") var floor: Number,
)