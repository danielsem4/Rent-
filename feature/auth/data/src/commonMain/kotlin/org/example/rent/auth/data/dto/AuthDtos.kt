package org.example.rent.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PhoneStartRequest(
    val phone: String = "",
)

@Serializable
data class QrStartRequest(
    val qrToken: String = "",
)

@Serializable
data class VerifyRequest(
    val phone: String? = null,
    val qrToken: String? = null,
    val code: String? = null,
)

/** Resend body: send exactly the single identifier the flow started with (phone OR qrToken). */
@Serializable
data class ResendRequest(
    val phone: String? = null,
    val qrToken: String? = null,
)

/** Logout body: the stored refresh token whose session should be revoked server-side. */
@Serializable
data class LogoutRequest(
    val refreshToken: String = "",
)

/** Generic `{ "ok": true }` body returned by the start, otp/resend and logout endpoints. */
@Serializable
data class OkResponse(
    val ok: Boolean = false,
)

/** `verify` returns `{ worker, accessToken, refreshToken }`; `worker` is ignored for now. */
@Serializable
data class VerifyResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
)
