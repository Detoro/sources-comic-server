package com.toro

import com.toro.database.AuthorSubscriptions
import com.toro.database.Bookmarks
import com.toro.database.Comics
import com.toro.database.Chapters
import com.toro.database.ChatMessages
import com.toro.database.ChatRequests
import com.toro.database.ComicSubscriptions
import com.toro.database.ReadingProgress
import com.toro.database.Users
import com.toro.database.Posts
import com.toro.database.Comments
import com.toro.database.Conversations
import com.toro.database.Pages
import com.toro.database.Likes
import com.toro.database.Friends
import com.toro.database.Tags
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val driverClassName = "org.postgresql.Driver"
        val password = "HorizonZeroDawn!0"
        val user = "postgres"
        val jdbcURL = "jdbc:postgresql://db.ripvgjmoukblkacvraad.supabase.co:5432/postgres?sslmode=require"

        val pool = hikari(jdbcURL, user, password, driverClassName)

        try {
            Database.connect(
                pool
            )

            println("🔓 [DEBUG 2] Database.connect() succeeded! Opening transaction...")

            transaction {

                exec("SELECT 'Hello from Ktor, Supabase can hear you!';") { resultSet ->
                    if (resultSet.next()) {
                        val responseMessage = resultSet.getString(1)
                        println("🎉 SUPABASE RESPONDED: $responseMessage")
                    }
                }

                SchemaUtils.createMissingTablesAndColumns(
                    Users,
                    Comics,
                    Chapters,
                    ReadingProgress,
                    ChatMessages,
                    ChatRequests,
                    Posts,
                    Comments,
                    Pages,
                    Friends,
                    ComicSubscriptions,
                    AuthorSubscriptions,
                    Conversations,
                    Bookmarks,
                    Likes,
                    Tags
                )
            }

            println("✅ [DEBUG 3] SUCCESS: Tables generated in Supabase!")

        } catch (e: Exception) {
            println("❌ [DEBUG ERROR] The connection crashed: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun hikari(url: String, user: String, pass: String, driver: String): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = driver
        config.jdbcUrl = url
        config.username = user
        config.password = pass
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}