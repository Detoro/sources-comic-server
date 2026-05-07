package com.toro.database

import org.jetbrains.exposed.sql.Table

object Comments : Table("comments") {
    val id = varchar("id", 50)
    val postId = varchar("post_id", 50).references(Posts.id)
    val authorId = varchar("author_id", 50).references(Users.id)
    val content = text("content")
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}