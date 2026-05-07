package com.toro.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    // Gemini said not to hardcode
    private const val secret = "toro-secret-key-to-be-changed-in-production"
    private const val issuer = "toro-comic-server"
    private const val audience = "toro-sources-app"
    private const val validityInMs = 36_000_00 * 24

    val algorithm: Algorithm = Algorithm.HMAC512(secret)

    val verifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(userId: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId) // We embed their unique ID directly into the token!
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(algorithm)
    }
}