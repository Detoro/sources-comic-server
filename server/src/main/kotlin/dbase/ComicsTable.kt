package com.toro.database

import org.jetbrains.exposed.sql.Table

object Comics : Table("comics") {
    val id = varchar("id", 50)
    val title = varchar("title", 255)
    val author = varchar("author", 255)
    val description = text("description")
    val coverImageUrl = varchar("cover_image_url", 500)
    val isLocalSideload = bool("is_local_sideload").default(false)
    val localFilePath = text("local_file_path").nullable()
    val scrollDirection = varchar("scroll_direction", 20).default("VERTICAL")

    override val primaryKey = PrimaryKey(id)
}