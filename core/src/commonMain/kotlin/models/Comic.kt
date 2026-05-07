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

val mockComicDatabase = listOf(
    Comic(
        id = "toro",
        title = "The Neon Samurai",
        author = "K. Reynolds",
        description = "In a cyberpunk future, a lone warrior must protect the last remaining organic city from a rogue AI corporation.",
        coverImageUrl = "https://picsum.photos/seed/cyber/400/600",
        isLocalSideload = false
    ),
    Comic(
        id = "comic_2",
        title = "Astromancers",
        author = "Sarah J. Lin",
        description = "Magic meets astrophysics. Follow a crew of spell-casters as they navigate the dangerous outer rim of the galaxy.",
        coverImageUrl = "https://picsum.photos/seed/space/400/600",
        isLocalSideload = false
    ),
    Comic(
        id = "comic_3",
        title = "Midnight Detectives",
        author = "Marcus Thorne",
        description = "A gritty noir series about two private eyes investigating supernatural occurrences in 1920s Chicago.",
        coverImageUrl = "https://picsum.photos/seed/noir/400/600",
        isLocalSideload = true
    )
)