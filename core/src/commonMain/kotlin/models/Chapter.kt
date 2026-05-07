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


val mockChapters = listOf(
    Chapter(id = "ch_1", comicId = "comic_1", chapterTitle = "Issue #1: Awakening", lastReadPageIndex = 12, isDownloaded = true),
    Chapter(id = "ch_2", comicId = "comic_1", chapterTitle = "Issue #2: The Grid", lastReadPageIndex = 0, isDownloaded = false),
    Chapter(id = "ch_3", comicId = "comic_1", chapterTitle = "Issue #3: Override", lastReadPageIndex = 0, isDownloaded = false),
    Chapter(id = "ch_4", comicId = "comic_2", chapterTitle = "Volume 1: The Nebula", lastReadPageIndex = 0, isDownloaded = false),
    Chapter(id = "ch_1", comicId = "comic_3", chapterTitle = "Volume 1: The Nebula", lastReadPageIndex = 50, isDownloaded = true)
)