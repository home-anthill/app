package eu.homeanthill.api.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// ------------------------------------------
// Classes with aggregated data to represent device features with values
@Parcelize
data class DeviceValue(
  @SerializedName("device") val device: Device,
  @SerializedName("featureValues") val featureValues: List<FeatureValue>,
) : Parcelable

@Parcelize
data class FeatureValue(
  @SerializedName("feature") val feature: Feature,
  @SerializedName("value") val value: Number,
  @SerializedName("createdAt") val createdAt: String,
  @SerializedName("modifiedAt") val modifiedAt: String,
) : Parcelable

// ------------------------------------------
// classes used as body for API requests
@Parcelize
data class DeviceFeatureValueResponse(
  @SerializedName("featureUuid") val featureUuid: String,
  @SerializedName("type") val type: String,
  @SerializedName("name") val name: String,
  @SerializedName("value") val value: Number,
  @SerializedName("createdAt") val createdAt: String,
  @SerializedName("modifiedAt") val modifiedAt: String,
) : Parcelable

@Parcelize
data class PostSetFeatureDeviceValue(
  @SerializedName("featureUuid") val featureUuid: String,
  @SerializedName("type") val type: String,
  @SerializedName("name") val name: String,
  @SerializedName("value") val value: Number,
) : Parcelable