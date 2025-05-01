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
data class SensorValue(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("value") val value: Number,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("modifiedAt") val modifiedAt: String,
) : Parcelable

// TODO this class should be removed to migrate Device values to a lise of features
@Parcelize
data class ControllerValue(
    @SerializedName("on") val on: Boolean,
    @SerializedName("temperature") val temperature: Int,
    @SerializedName("mode") val mode: Int,
    @SerializedName("fanSpeed") val fanSpeed: Int,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("modifiedAt") val modifiedAt: String,
) : Parcelable

@Parcelize
data class PostSetDeviceValue(
    @SerializedName("on") val on: Boolean,
    @SerializedName("temperature") val temperature: Int,
    @SerializedName("mode") val mode: Int,
    @SerializedName("fanSpeed") val fanSpeed: Int,
) : Parcelable