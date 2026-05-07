package com.toro.models

import kotlinx.serialization.Serializable

@Serializable
data class ServerResponse(
    val message: String
)

@Serializable
data class AuthorRequest(val authorName: String)