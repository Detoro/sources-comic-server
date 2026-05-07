package com.toro.database

import org.jetbrains.exposed.sql.Table

object Chapters : Table("chapters") {
    val id = varchar("id", 50)
    val comicId = varchar("comic_id", 50).references(Comics.id)
    val chapterTitle = varchar("chapter_title", 255)
    val chapterNumber = float("chapter_number").nullable()

    val storageBucketPath = varchar("storage_bucket_path", 500)

    override val primaryKey = PrimaryKey(id)
}