package toro.sources.models

import kotlinx.serialization.Serializable


@Serializable
data class Chapter(
    val id: String,
    val comicId: String,
    val chapterTitle: String,
    val chapterNumber: Float? = null,            // Float allows for decimal issues (e.g., Chapter 10.5)
    val lastReadPageIndex: Int = 0,      // Crucial for bookmarking where the user left off
    val isDownloaded: Boolean = false    // True if the remote chapter was cached for offline reading
)