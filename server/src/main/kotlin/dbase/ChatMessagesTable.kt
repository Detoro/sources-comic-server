package com.toro.database

import org.jetbrains.exposed.sql.Table

object ChatMessages : Table("chat_messages") {
    val id = varchar("id", 50)
    val conversationId = varchar("conversation_id", 100)
    val senderId = varchar("sender_id", 50).references(Users.id)
    val content = text("content")
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}