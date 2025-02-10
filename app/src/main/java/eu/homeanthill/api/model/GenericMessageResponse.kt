package eu.homeanthill.api.model

import com.google.gson.annotations.SerializedName

data class GenericMessageResponse(
    @SerializedName("message") val message: String,
)