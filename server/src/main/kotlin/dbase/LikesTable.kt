package com.toro.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Likes : Table("likes") {
    val id = varchar("id", 50)
    val userId = varchar("user_id", 50).references(Users.id, onDelete = ReferenceOption.CASCADE)
    val postId = varchar("post_id", 50).references(Posts.id, onDelete = ReferenceOption.CASCADE)
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}