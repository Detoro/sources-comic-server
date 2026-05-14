package com.toro.database

import org.jetbrains.exposed.sql.Table

object Tags : Table("tags") {
    val id = varchar("id", 50)
    val postId = varchar("post_id", 50).references(Posts.id)
    val content = text("content")

    override val primaryKey = PrimaryKey(id)
}