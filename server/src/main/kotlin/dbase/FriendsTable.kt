package com.toro.database

import com.toro.database.Users
import org.jetbrains.exposed.sql.Table

object Friends : Table("friends") {
    val user1Id = varchar("user1_id", 50) references Users.id
    val user2Id = varchar("user2_id", 50) references Users.id

    override val primaryKey = PrimaryKey(user1Id, user2Id)
}