package eu.homeanthill.api.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// ------------------------------------------
// Classes with aggregated data to represent the list of devices groped by homes and rooms
@Parcelize
data class MyDevicesList(
  @SerializedName("unassignedDevices") val unassignedDevices: List<Device>,
  @SerializedName("homeDevices") val homeDevices: MutableList<HomeWithDevices>,
) : Parcelable

@Parcelize
data class HomeWithDevices(
  @SerializedName("home") val home: Home,
  @SerializedName("rooms") var rooms: List<RoomSplitDevices>,
) : Parcelable

@Parcelize
data class RoomSplitDevices(
  @SerializedName("room") val room: Room,
  @SerializedName("controllerDevices") val controllerDevices: List<Device>,
  @SerializedName("sensorDevices") val sensorDevices: List<Device>,
) : Parcelable
// ------------------------------------------

// ------------------------------------------
// basic classes to represent a Device
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
) : Parcelable

@Parcelize
data class Feature(
  @SerializedName("uuid") val uuid: String,
  @SerializedName("type") val type: String,
  @SerializedName("name") val name: String,
  @SerializedName("enable") val enable: Boolean,
  @SerializedName("order") val order: Number,
  @SerializedName("unit") val unit: String,
) : Parcelable

// ------------------------------------------
// classes used as body for API requests
@Parcelize
data class PutDevice(
  @SerializedName("homeId") val homeId: String,
  @SerializedName("roomId") val roomId: String,
) : Parcelable