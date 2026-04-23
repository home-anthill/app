package eu.homeanthill.api.model

data class RefreshTokenRequest(
  val refreshToken: String,
)

data class TokenResponse(
  val token: String,
  val refreshToken: String,
)
