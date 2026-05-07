package com.toro.database

import org.jetbrains.exposed.sql.Table

object ComicSubscriptions : Table("comic_subscriptions") {
    val userId = varchar("user_id", 50) references Users.id
    val comicId = varchar("comic_id", 50) references Comics.id

    override val primaryKey = PrimaryKey(userId, comicId)
}