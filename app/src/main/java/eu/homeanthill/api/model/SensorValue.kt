package eu.homeanthill.api.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// ------------------------------------------
// Classes with aggregated data to represent device features with values
@Parcelize
data class DeviceValue(
  @SerializedName("device") val device: Device,
  @SerializedName("sensorFeatureValues") val sensorFeatureValues: List<FeatureValue>,
  @SerializedName("controllerFeatureValues") val controllerFeatureValues: List<FeatureValue>,
) : Parcelable

@Parcelize
data class FeatureValue(
  @SerializedName("feature") val feature: Feature,
  @SerializedName("value") val value: Number,
  @SerializedName("createdAt") val createdAt: Number,
  @SerializedName("modifiedAt") val modifiedAt: Number,
) : Parcelable


// ------------------------------------------
// temporary object to return the PostSetFeatureDeviceValue API
// response to the main FeatureScreen to show a SnackBar
data class SendValueResult(
  val message: String,
  val isError: Boolean,
)

// ------------------------------------------
// classes used as body for API requests
@Parcelize
data class DeviceFeatureValueResponse(
  @SerializedName("featureUuid") val featureUuid: String,
  @SerializedName("type") val type: String,
  @SerializedName("name") val name: String,
  @SerializedName("value") var value: Number,
  @SerializedName("createdAt") val createdAt: Number,
  @SerializedName("modifiedAt") val modifiedAt: Number,
) : Parcelable

@Parcelize
data class PostSetFeatureDeviceValue(
  @SerializedName("featureUuid") val featureUuid: String,
  @SerializedName("type") val type: String,
  @SerializedName("name") val name: String,
  @SerializedName("value") val value: Number,
) : Parcelable