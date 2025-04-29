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
// basic classes to represent a Value
@Parcelize
data class Value(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("value") val value: Number,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("modifiedAt") val modifiedAt: String,
) : Parcelable
