package com.toro.database

import org.jetbrains.exposed.sql.Table

object AuthorSubscriptions : Table("author_subscriptions") {
    val userId = varchar("user_id", 50) references Users.id
    val authorName = varchar("author_name", 255)

    override val primaryKey = PrimaryKey(userId, authorName)
}