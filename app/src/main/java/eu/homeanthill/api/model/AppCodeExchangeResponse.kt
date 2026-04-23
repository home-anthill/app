package eu.homeanthill.api.model

data class AppCodeExchangeResponse(
  val token: String,
  val refreshToken: String,
)
