package com.toro.database

import org.jetbrains.exposed.sql.Table

object ReadingProgress : Table("reading_progress") {
    val userId = varchar("user_id", 50).references(Users.id)
    val chapterId = varchar("chapter_id", 50).references(Chapters.id)
    val lastReadPageIndex = integer("last_read_page_index").default(0)
    val lastReadTimestamp = long("last_read_timestamp")

    override val primaryKey = PrimaryKey(userId, chapterId)
}