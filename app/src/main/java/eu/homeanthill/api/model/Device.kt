package eu.homeanthill.api.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Device(
    @SerializedName("id") val id: String,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("mac") val mac: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("model") val model: String,
    @SerializedName("features") val features: List<Feature>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("modifiedAt") val modifiedAt: String,
): Parcelable

@Parcelize
data class Feature(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("type") val type: String,
    @SerializedName("name") val name: String,
    @SerializedName("enable") val enable: Boolean,
    @SerializedName("order") val order: Number,
    @SerializedName("unit") val unit: String,
): Parcelable

@Parcelize
data class PutDevice(
    @SerializedName("homeId") val homeId: String,
    @SerializedName("roomId") val roomId: String,
): Parcelable
