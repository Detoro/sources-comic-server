package com.toro.database

import org.jetbrains.exposed.sql.Table
import com.toro.models.ChatStatus

object ChatRequests : Table("chat_requests") {
    val id = varchar("id", 50)
    val senderId = varchar("sender_id", 50).references(Users.id)
    val receiverId = varchar("receiver_id", 50).references(Users.id)
    val status = enumerationByName("status", 20, ChatStatus::class).default(ChatStatus.PENDING)
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}