package com.toro

import kotlinx.rpc.annotations.Rpc
import com.toro.models.Comic


@Rpc
interface SampleService {
    suspend fun hello(data: Comic): String
}

class SampleServiceImpl : SampleService {
    override suspend fun hello(comic: Comic): String {
        return "Server: ${comic.title}"
    }
}
