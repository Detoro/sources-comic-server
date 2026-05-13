package com.toro

import com.toro.database.AuthorSubscriptions
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.receive
import io.ktor.server.auth.jwt.*
import com.toro.models.Comic
import com.toro.models.Page
import com.toro.models.ChatMessage
import com.toro.models.ChatRequest
import org.jetbrains.exposed.sql.*
import com.toro.database.Comics
import com.toro.database.Users
import com.toro.database.Comments
import com.toro.database.ChatMessages
import com.toro.database.ChatRequests
import com.toro.database.Bookmarks
import com.toro.database.Conversations
import com.toro.database.ComicSubscriptions
import com.toro.database.Posts
import com.toro.database.Likes
import com.toro.database.Pages
import com.toro.database.Friends
import com.toro.models.CommentRequest
import com.toro.models.ServerResponse
import com.toro.models.Comment
import com.toro.models.Post
import com.toro.models.AuthRequest
import com.toro.models.AuthResponse
import com.toro.models.AuthorRequest
import com.toro.models.ChatStatus
import com.toro.models.Conversation
import com.toro.models.UserProfile
import com.toro.plugins.JwtConfig
import org.mindrot.jbcrypt.BCrypt.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveMultipart
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.io.File
import java.util.UUID

fun Application.configureRouting() {
    routing {
        authenticate("auth-jwt") {
            route("api/comics") {
                get("{comicId}/chapters") {
                    val comicIdParam = call.parameters["comicId"] ?: return@get call.respondText("Missing ID", status = HttpStatusCode.BadRequest)

                    val comic = DatabaseFactory.dbQuery {
                        Comics.select {
                            (Comics.id eq comicIdParam)
                        }.map { row ->
                            Comic(
                                id = row[Comics.id],
                                title = row[Comics.title],
                                author = row[Comics.author],
                                description = row[Comics.description],
                                coverImageUrl = row[Comics.coverImageUrl],
                                isLocalSideload = row[Comics.isLocalSideload],
                                localFilePath = row[Comics.localFilePath],
                                scrollDirection = row[Comics.scrollDirection]
                            )
                        }
                    }

                    if (comic != null) {
                        call.respond(comic)
                    } else {
                        call.respondText("Comic not found")
                    }
                }

                get("chapters/{chapterId}/pages") {
                    val chapterIdParam = call.parameters["chapterId"] ?: return@get call.respondText(
                        "Missing Chapter ID",
                        status = HttpStatusCode.BadRequest
                    )

                    val chapterPages = DatabaseFactory.dbQuery {
                        Pages.select { Pages.chapterId eq chapterIdParam }
                            .orderBy(Pages.pageNumber to SortOrder.ASC)
                            .map { row ->
                                Page(
                                    id = row[Pages.id],
                                    chapterId = row[Pages.chapterId],
                                    pageNumber = row[Pages.pageNumber],
                                    imageUrl = row[Pages.imageUrl]
                                )
                            }
                    }

                    call.respond(chapterPages)
                }

                get("catalog") {
                    val principal = call.principal<JWTPrincipal>()!!
                    val currentUserId = principal!!.payload.getClaim("userId").asString()
                    val comicsList = DatabaseFactory.dbQuery {
                        Comics.selectAll().map { row ->
                            Comic(
                                id = row[Comics.id],
                                title = row[Comics.title],
                                author = row[Comics.author],
                                description = row[Comics.description],
                                coverImageUrl = row[Comics.coverImageUrl],
                                isLocalSideload = row[Comics.isLocalSideload],
                                localFilePath = row[Comics.localFilePath],
                                scrollDirection = row[Comics.scrollDirection]
                            )
                        }
                    }

                    call.respond(comicsList)
                }

                get("search") {
                    val query = call.request.queryParameters["query"]?.lowercase() ?: ""

                    val searchResults = DatabaseFactory.dbQuery {
                        Comics.select {
                            (Comics.title.lowerCase() like "%$query%") or
                                    (Comics.author.lowerCase() like "%$query%")
                        }.map { row ->
                            Comic(
                                id = row[Comics.id],
                                title = row[Comics.title],
                                author = row[Comics.author],
                                description = row[Comics.description],
                                coverImageUrl = row[Comics.coverImageUrl],
                                isLocalSideload = row[Comics.isLocalSideload],
                                localFilePath = row[Comics.localFilePath]
                            )
                        }
                    }

                    call.respond(searchResults)
                }
                post("upload") {
                    val uploadDir = File("uploads")
                    if (!uploadDir.exists()) uploadDir.mkdirs()

                    var title = ""
                    var author = ""
                    var description = ""
                    var comicFileName = ""
                    var coverFileName = ""

                    try {
                        val multipartData = call.receiveMultipart()

                        multipartData.forEachPart { part ->
                            when (part) {
                                is PartData.FormItem -> {
                                    when (part.name) {
                                        "title" -> title = part.value
                                        "author" -> author = part.value
                                        "description" -> description = part.value
                                    }
                                }

                                is PartData.FileItem -> {
                                    val originalName = part.originalFileName ?: "unknown_file"
                                    val uniqueName = "${UUID.randomUUID()}_$originalName"
                                    val file = File(uploadDir, uniqueName)

                                    part.streamProvider().use { input ->
                                        file.outputStream().buffered().use { output ->
                                            input.copyTo(output)
                                        }
                                    }

                                    if (part.name == "file") comicFileName = uniqueName
                                    if (part.name == "cover") coverFileName = uniqueName
                                }

                                else -> {}
                            }
                            part.dispose()
                        }

                        val newComic = Comic(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            author = author,
                            description = description,
                            coverImageUrl = "http://10.0.2.2:8080/uploads/$coverFileName",
                            isLocalSideload = false,
                            localFilePath = ""
                        )

                        DatabaseFactory.dbQuery {
                            Comics.insert { row ->
                                row[Comics.id] = newComic.id
                                row[Comics.title] = newComic.title
                                row[Comics.author] = newComic.author
                                row[Comics.description] = newComic.description
                                row[Comics.coverImageUrl] = newComic.coverImageUrl
                                row[Comics.isLocalSideload] = newComic.isLocalSideload
                                row[Comics.localFilePath] = newComic.localFilePath
                            }
                        }

                        call.respond(
                            HttpStatusCode.Created, mapOf(
                                "message" to "Upload successful!",
                                "comicId" to newComic.id
                            )
                        )

                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Upload failed: ${e.message}"))
                    }
                }
            }
        }

        authenticate("auth-jwt") {
            route("api/subscribe") {

                post("comic/{comicId}") {
                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal!!.payload.getClaim("userId").asString()
                    val comicIdParam = call.parameters["comicId"] ?: return@post call.respondText("Missing ID", status = HttpStatusCode.BadRequest)

                    val isNowSubscribed = DatabaseFactory.dbQuery {
                        val existingSub = ComicSubscriptions.select {
                            (ComicSubscriptions.userId eq currentUserId) and (ComicSubscriptions.comicId eq comicIdParam)
                        }.singleOrNull()

                        if (existingSub != null) {
                            ComicSubscriptions.deleteWhere {
                                (ComicSubscriptions.userId eq currentUserId) and (ComicSubscriptions.comicId eq comicIdParam)
                            }
                            false
                        } else {
                            ComicSubscriptions.insert {
                                it[userId] = currentUserId
                                it[comicId] = comicIdParam
                            }
                            true
                        }
                    }

                    call.respond(mapOf("isSubscribed" to isNowSubscribed))
                }

                post("author") {
                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal!!.payload.getClaim("userId").asString()
                    val request = call.receive<AuthorRequest>()

                    DatabaseFactory.dbQuery {
                        AuthorSubscriptions.insertIgnore {
                            it[userId] = currentUserId
                            it[authorName] = request.authorName
                        }
                    }

                    call.respond(mapOf("message" to "Subscribed to ${request.authorName}"))
                }
            }

            route("api/chat") {
                get("conversations") {
                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal!!.payload.getClaim("userId").asString()

                    val inbox = DatabaseFactory.dbQuery {
                        Conversations.select {
                            (Conversations.user1Id eq currentUserId) or (Conversations.user2Id eq currentUserId)
                        }
                            .orderBy(Conversations.lastUpdated to SortOrder.DESC)
                            .map { row ->
                                val isUser1 = row[Conversations.user1Id] == currentUserId
                                val otherUserId = if (isUser1) row[Conversations.user2Id] else row[Conversations.user1Id]

                                val otherUserName = Users.select { Users.id eq otherUserId }
                                    .map { it[Users.username] }
                                    .singleOrNull() ?: "Unknown User"

                                Conversation(
                                    conversationId = row[Conversations.id],
                                    otherUserId = otherUserId,
                                    otherUserName = otherUserName,
                                    lastMessage = row[Conversations.lastMessage],
                                    timestamp = row[Conversations.lastUpdated]
                                )
                            }
                    }

                    call.respond(inbox)
                }

                get("{conversationId?}/messages") {
                    val conversationIdParam = call.parameters["conversationId"] ?: return@get call.respondText("Missing Conversation ID", status = HttpStatusCode.BadRequest)

                    val messages = DatabaseFactory.dbQuery {
                        ChatMessages
                            .select { ChatMessages.conversationId eq conversationIdParam }
                            .orderBy(ChatMessages.timestamp to SortOrder.ASC)
                            .map { row ->
                                ChatMessage(
                                    id = row[ChatMessages.id],
                                    senderId = row[ChatMessages.senderId],
                                    content = row[ChatMessages.content],
                                    timestamp = row[ChatMessages.timestamp]
                                )
                            }
                    }
                    call.respond(messages)
                }

                post("{conversationId?}/messages") {
                    val conversationIdParam = call.parameters["conversationId"] ?: return@post call.respondText("Missing Conversation ID", status = HttpStatusCode.BadRequest)
                    val receivedMessage = call.receive<ChatMessage>()

                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal!!.payload.getClaim("userId").asString()

                    val newMessageId = UUID.randomUUID().toString()
                    val currentTime = System.currentTimeMillis()

                    DatabaseFactory.dbQuery {
                        ChatMessages.insert {
                            it[id] = newMessageId
                            it[conversationId] = conversationIdParam
                            it[senderId] = currentUserId
                            it[content] = receivedMessage.content
                            it[timestamp] = currentTime
                        }

                        Conversations.update({ Conversations.id eq conversationIdParam }) {
                            it[lastMessage] = receivedMessage.content
                            it[lastUpdated] = currentTime
                        }
                    }

                    call.respond(ServerResponse("Message sent!"))
                }

                route("requests") {
                    get {
                        val principal = call.principal<JWTPrincipal>()
                        val currentUserId = principal!!.payload.getClaim("userId").asString()

                        val requestsList = DatabaseFactory.dbQuery {
                            ChatRequests.join(Users, JoinType.INNER, onColumn = ChatRequests.senderId, otherColumn = Users.id)
                                .select {
                                    (ChatRequests.receiverId eq currentUserId) and
                                            (ChatRequests.status eq ChatStatus.PENDING)
                                }
                                .orderBy(ChatRequests.timestamp to SortOrder.DESC)
                                .map { row ->
                                    ChatRequest(
                                        id = row[ChatRequests.id],
                                        senderId = row[ChatRequests.senderId],
                                        senderName = row[Users.username],
                                        status = row[ChatRequests.status]
                                    )
                                }
                        }
                        call.respond(requestsList)
                    }
                    post("{requestId}/accept") {
                        val principal = call.principal<JWTPrincipal>()
                        val currentUserId = principal!!.payload.getClaim("userId").asString()
                        val requestIdParam = call.parameters["requestId"] ?: return@post call.respond(HttpStatusCode.BadRequest, ServerResponse("Missing Request ID"))

                        val resultMessage = DatabaseFactory.dbQuery {
                            val requestRow = ChatRequests.select { ChatRequests.id eq requestIdParam }.singleOrNull()

                            if (requestRow == null) return@dbQuery "Request not found"

                            if (requestRow[ChatRequests.receiverId] != currentUserId) return@dbQuery "Unauthorized"
                            if (requestRow[ChatRequests.status] != ChatStatus.PENDING) return@dbQuery "Request already processed"

                            val senderId = requestRow[ChatRequests.senderId]

                            ChatRequests.update({ ChatRequests.id eq requestIdParam }) {
                                it[status] = ChatStatus.ACCEPTED
                            }

                            val existingConvo = Conversations.select {
                                ((Conversations.user1Id eq senderId) and (Conversations.user2Id eq currentUserId)) or
                                        ((Conversations.user1Id eq currentUserId) and (Conversations.user2Id eq senderId))
                            }.singleOrNull()

                            if (existingConvo == null) {
                                Conversations.insert {
                                    it[id] = UUID.randomUUID().toString()
                                    it[user1Id] = senderId
                                    it[user2Id] = currentUserId
                                    it[lastMessage] = "Chat request accepted! Say hi."
                                    it[lastUpdated] = System.currentTimeMillis()
                                }
                            }
                            "Success"
                        }

                        if (resultMessage == "Success") {
                            call.respond(ServerResponse("Chat request accepted"))
                        } else {
                            call.respond(HttpStatusCode.BadRequest, ServerResponse(resultMessage))
                        }
                    }

                    post("{requestId}/decline") {
                        val principal = call.principal<JWTPrincipal>()
                        val currentUserId = principal!!.payload.getClaim("userId").asString()
                        val requestIdParam = call.parameters["requestId"] ?: return@post call.respond(HttpStatusCode.BadRequest, ServerResponse("Missing Request ID"))

                        val resultMessage = DatabaseFactory.dbQuery {
                            val requestRow = ChatRequests.select { ChatRequests.id eq requestIdParam }.singleOrNull()

                            if (requestRow == null) return@dbQuery "Request not found"

                            if (requestRow[ChatRequests.receiverId] != currentUserId) return@dbQuery "Unauthorized"

                            ChatRequests.update({ ChatRequests.id eq requestIdParam }) {
                                it[status] = ChatStatus.REJECTED
                            }
                            "Success"
                        }

                        if (resultMessage == "Success") {
                            call.respond(ServerResponse("Chat request declined"))
                        } else {
                            call.respond(HttpStatusCode.BadRequest, ServerResponse(resultMessage))
                        }
                    }
                }
            }

            route("api/community/posts") {

                get {
                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, "Missing user token")

                    val postsList = DatabaseFactory.dbQuery {
                        val postRows = (Posts innerJoin Users)
                            .selectAll()
                            .orderBy(Posts.timestamp to SortOrder.DESC)
                            .toList()

                        val postIds = postRows.map { it[Posts.id] }

                        val likedPostIds = if (postIds.isNotEmpty()) {
                            Likes.select {
                                (Likes.userId eq currentUserId) and (Likes.postId inList postIds)
                            }
                                .map { it[Likes.postId] }
                                .toSet()
                        } else {
                            emptySet()
                        }

                        postRows.map { row ->
                            val currentPostId = row[Posts.id]

                            Post(
                                id = currentPostId,
                                authorId = row[Posts.authorId],
                                authorName = row[Users.username],
                                content = row[Posts.content],
                                timestamp = row[Posts.timestamp],
                                likesCount = row[Posts.likesCount],
                                isLiked = likedPostIds.contains(currentPostId)
                            )
                        }
                    }
                    call.respond(postsList)
                }

                post {
                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal!!.payload.getClaim("userId").asString()
                    val receivedMessage = call.receive<CommentRequest>()
                    val resultMessage = DatabaseFactory.dbQuery {
                        Posts.insert {
                            it[id] = UUID.randomUUID().toString()
                            it[authorId] = currentUserId
                            it[content] = receivedMessage.content
                            it[timestamp] = System.currentTimeMillis()
                        }
                        "Success"
                    }
                    if (resultMessage == "Success") {
                        call.respond(ServerResponse("Post successful"))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ServerResponse(resultMessage))
                    }
                }

                route("{postId}") {
                    get("comments") {
                        val postIdParam = call.parameters["postId"] ?: return@get call.respondText(
                            "Missing ID",
                            status = HttpStatusCode.BadRequest
                        )

                        val commentsList = DatabaseFactory.dbQuery {
                            (Comments innerJoin Users)
                                .select { Comments.postId eq postIdParam }
                                .orderBy(Comments.timestamp to SortOrder.ASC)
                                .map { row ->
                                    Comment(
                                        id = row[Comments.id],
                                        postId = row[Comments.postId],
                                        authorId = row[Comments.authorId],
                                        authorName = row[Users.username],
                                        content = row[Comments.content],
                                        timestamp = row[Comments.timestamp]
                                    )
                                }
                        }
                        call.respond(commentsList)
                    }

                    post("comments") {
                        val postIdParam = call.parameters["postId"] ?: return@post call.respondText(
                            "Missing ID",
                            status = HttpStatusCode.BadRequest
                        )

                        val receivedComment = call.receive<CommentRequest>()

                        val principal = call.principal<JWTPrincipal>()
                        val currentUserId = principal!!.payload.getClaim("userId").asString()

                        val newCommentId = UUID.randomUUID().toString()
                        val currentTime = System.currentTimeMillis()

                        DatabaseFactory.dbQuery {
                            Comments.insert {
                                it[id] = newCommentId
                                it[postId] = postIdParam
                                it[authorId] = currentUserId
                                it[content] = receivedComment.content
                                it[timestamp] = currentTime
                            }
                        }

                        call.respond(ServerResponse("Comment posted successfully!"))
                    }

                    post("like") {
                        val principal = call.principal<JWTPrincipal>()
                        val currentUserId = principal!!.payload.getClaim("userId").asString()
                        val postIdParam = call.parameters["postId"] ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ServerResponse("Missing Post ID")
                        )
                        val currentTime = System.currentTimeMillis()

                        val result = DatabaseFactory.dbQuery {
                            val existing = Likes.select {
                                (Likes.userId eq currentUserId) and (Likes.postId eq postIdParam)
                            }.singleOrNull()

                            if (existing == null) {
                                Likes.insert {
                                    it[id] = UUID.randomUUID().toString()
                                    it[userId] = currentUserId
                                    it[postId] = postIdParam
                                    it[timestamp] = currentTime
                                }
                                Posts.update({ Posts.id eq postIdParam }) {
                                    with(SqlExpressionBuilder) {
                                        it.update(likesCount, likesCount + 1)
                                    }
                                }
                                "Post liked"
                            } else {
                                Likes.deleteWhere {
                                    (Likes.userId eq currentUserId) and (Likes.postId eq postIdParam)
                                }
                                Posts.update({ Posts.id eq postIdParam }) {
                                    with(SqlExpressionBuilder) {
                                        it.update(likesCount, likesCount - 1)
                                    }
                                }
                                "Like removed"
                            }
                        }
                        call.respond(ServerResponse(result))
                    }

                    post("bookmark") {
                        val principal = call.principal<JWTPrincipal>()
                        val currentUserId = principal!!.payload.getClaim("userId").asString()
                        val postIdParam = call.parameters["postId"] ?: return@post call.respond(HttpStatusCode.BadRequest)

                        val result = DatabaseFactory.dbQuery {
                            val existing = Bookmarks.select {
                                (Bookmarks.userId eq currentUserId) and (Bookmarks.postId eq postIdParam)
                            }.singleOrNull()

                            if (existing == null) {
                                Bookmarks.insert {
                                    it[id] = UUID.randomUUID().toString()
                                    it[userId] = currentUserId
                                    it[postId] = postIdParam
                                    it[timestamp] = System.currentTimeMillis()
                                }
                                "Post bookmarked"
                            } else {
                                Bookmarks.deleteWhere {
                                    (Bookmarks.userId eq currentUserId) and (Bookmarks.postId eq postIdParam)
                                }
                                "Bookmark removed"
                            }
                        }

                        call.respond(HttpStatusCode.OK, ServerResponse(result))
                    }
                }
            }
        }
        route("api/auth") {
            post("register") {
                val request = call.receive<AuthRequest>()

                val hashedPass = hashpw(request.password, gensalt())
                val newUserId = UUID.randomUUID().toString()
                val safeUsername = request.username ?: return@post call.respondText(
                    "Username is required for registration",
                    status = HttpStatusCode.BadRequest)

                try {
                    DatabaseFactory.dbQuery {
                        Users.insert {
                            it[id] = newUserId
                            it[username] = safeUsername
                            it[email] = request.email
                            it[passwordHash] = hashedPass
                        }
                    }

                    call.respond(
                        AuthResponse(
                            token = JwtConfig.generateToken(newUserId),
                            userId = newUserId,
                            username = safeUsername,
                        )
                    )
                } catch (e: Exception) {
                    call.respondText("Username or Email already exists", status = HttpStatusCode.Conflict)
                }
            }

            post("login") {
                val request = call.receive<AuthRequest>()

                val userRow = DatabaseFactory.dbQuery {
                    Users.select { Users.email eq request.email }.singleOrNull()
                }

                if (userRow == null) {
                    return@post call.respondText("Invalid email or password", status = HttpStatusCode.Unauthorized)
                }

                val savedHash = userRow[Users.passwordHash]
                val isPasswordCorrect = checkpw(request.password, savedHash)
                val id = userRow[Users.id]
                if (isPasswordCorrect) {
                    call.respond(
                        AuthResponse(
                            token = JwtConfig.generateToken(id),
                            userId = userRow[Users.id],
                            username = userRow[Users.username]
                        )
                    )
                } else {
                    call.respondText("Invalid email or password", status = HttpStatusCode.Unauthorized)
                }
            }
        }
        authenticate("auth-jwt") {
            route("api/users") {
                get("friends") {
                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal!!.payload.getClaim("userId").asString()

                    val friendsList = DatabaseFactory.dbQuery {
                        val friendIds = Friends.select {
                            (Friends.user1Id eq currentUserId) or (Friends.user2Id eq currentUserId)
                        }.map { row ->
                            if (row[Friends.user1Id] == currentUserId) {
                                row[Friends.user2Id]
                            } else {
                                row[Friends.user1Id]
                            }
                        }

                        if (friendIds.isEmpty()) {
                            emptyList<AuthResponse>()
                        } else {
                            Users.select { Users.id inList friendIds }.map { row ->
                                UserProfile(
                                    id = row[Users.id],
                                    username = row[Users.username]
                                )
                            }
                        }
                    }

                    call.respond(friendsList)
                }

                post("avatar") {
                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal!!.payload.getClaim("userId").asString()

                    val uploadDir = File("uploads/avatars")
                    if (!uploadDir.exists()) uploadDir.mkdirs()

                    var fileName = ""
                    val multipartData = call.receiveMultipart()

                    multipartData.forEachPart { part ->
                        if (part is PartData.FileItem) {
                            val ext = File(part.originalFileName ?: "").extension.ifEmpty { "jpg" }

                            fileName = "${currentUserId}.$ext"
                            val file = File(uploadDir, fileName)

                            part.streamProvider().use { input ->
                                file.outputStream().buffered().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                        part.dispose()
                    }

                    val publicUrl = "http://192.168.1.141:8080/uploads/avatars/$fileName"

                    DatabaseFactory.dbQuery {
                        Users.update({ Users.id eq currentUserId }) {
                            it[avatarUrl] = publicUrl
                        }
                    }

                    call.respond(ServerResponse(publicUrl))
                }
            }
        }
    }
}