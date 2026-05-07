package com.toro.database

import org.jetbrains.exposed.sql.Table

object Conversations : Table("conversations") {
    val id = varchar("id", 50)
    val user1Id = varchar("user1_id", 50) references Users.id
    val user2Id = varchar("user2_id", 50) references Users.id
    
    val lastMessage = varchar("last_message", 500).nullable()
    val lastUpdated = long("last_updated")

    override val primaryKey = PrimaryKey(id)
}