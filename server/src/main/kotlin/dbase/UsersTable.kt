package com.toro.database

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = varchar("id", 50)
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val avatarUrl = varchar("avatar_url", 500).nullable()

    override val primaryKey = PrimaryKey(id)
}