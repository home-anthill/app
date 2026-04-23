package eu.homeanthill.api.model

data class AppCodeExchangeRequest(
  val code: String,
  val codeVerifier: String,
)
