package com.example.nammashalli.network

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface GroqApiService {
    @POST("openai/v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GroqRequest
    ): Response<GroqResponse>

    @GET("openai/v1/models")
    suspend fun getModels(
        @Header("Authorization") authorization: String
    ): Response<JsonObject>
}

data class GroqRequest(
    val model: String = "llama-3.1-8b-instant",
    val messages: List<GroqMessage>,
    @SerializedName("max_tokens") val maxTokens: Int = 500,
    val temperature: Double = 0.7
)

data class GroqMessage(val role: String, val content: String)

data class GroqResponse(
    val choices: List<GroqChoice>
)

data class GroqChoice(
    val message: GroqMessage,
    @SerializedName("finish_reason") val finishReason: String
)
