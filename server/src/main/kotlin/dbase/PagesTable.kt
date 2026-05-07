package com.toro.database

import org.jetbrains.exposed.sql.Table

object Pages : Table("pages") {
    val id = varchar("id", 50)
    val chapterId = varchar("chapter_id", 50)
    val pageNumber = integer("page_number")
    val imageUrl = varchar("image_url", 500)

    override val primaryKey = PrimaryKey(id)
}