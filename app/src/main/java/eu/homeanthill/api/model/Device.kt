package eu.homeanthill.api.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

enum class Format {
  @SerializedName("bool")
  BOOL,
  @SerializedName("int")
  INT,
  @SerializedName("float")
  FLOAT,
  @SerializedName("list")
  LIST
}

// ------------------------------------------
// Classes with aggregated data to represent the list of devices groped by homes and rooms
@Parcelize
data class MyDevicesList(
  @SerializedName("unassignedDevices") val unassignedDevices: List<Device>,
  @SerializedName("homeDevices") val homeDevices: List<HomeWithDevices>,
) : Parcelable

@Parcelize
data class HomeWithDevices(
  @SerializedName("home") val home: Home,
  @SerializedName("rooms") val rooms: List<RoomSplitDevices>,
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
  @SerializedName("name") val name: String?,
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
  @SerializedName("order") val order: Int,
  @SerializedName("unit") val unit: String,
  @SerializedName("spec") val spec: Spec = Spec(),
  @SerializedName("notificationSilenced") val notificationSilenced: Boolean = false,
) : Parcelable

@Parcelize
data class Spec(
  @SerializedName("format") val format: Format = Format.FLOAT,
  @SerializedName("min") val min: Float? = null,
  @SerializedName("max") val max: Float? = null,
  @SerializedName("step") val step: Float? = null,
  @SerializedName("list") val list: List<SpecListItem>? = null,
) : Parcelable

@Parcelize
data class SpecListItem(
  @SerializedName("value") val value: Int,
  @SerializedName("text") val text: String,
) : Parcelable


// ------------------------------------------
// classes used as body for API requests
@Parcelize
data class PutDevice(
  @SerializedName("name") val name: String,
  @SerializedName("homeId") val homeId: String,
  @SerializedName("roomId") val roomId: String,
) : Parcelable

data class PutFeatureNotification(
  @SerializedName("notificationSilenced") val notificationSilenced: Boolean,
)
