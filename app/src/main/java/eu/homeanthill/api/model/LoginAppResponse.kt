package eu.homeanthill.api.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("loginURL") val loginURL: String,
)