package com.toro.models

import kotlinx.serialization.Serializable


@Serializable
data class Comic(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val coverImageUrl: String,
    val isLocalSideload: Boolean = false,
    val localFilePath: String? = null,
    val scrollDirection: String = "VERTICAL"
)