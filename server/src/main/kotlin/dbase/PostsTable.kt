package com.toro.database

import com.toro.database.Users
import org.jetbrains.exposed.sql.Table

object Posts : Table("posts") {
    val id = varchar("id", 50)
    val authorId = varchar("author_id", 50).references(Users.id)
    val content = text("content")
    val timestamp = long("timestamp")
    val likesCount = integer("likes_count").default(0)

    override val primaryKey = PrimaryKey(id)
}