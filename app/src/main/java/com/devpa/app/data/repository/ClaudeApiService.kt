package com.devpa.app.data.repository

import com.devpa.app.BuildConfig
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// ── API Data Models ────────────────────────────────────────────────

data class ClaudeRequest(
    val model: String = "claude-sonnet-4-20250514",
    @SerializedName("max_tokens") val maxTokens: Int = 1000,
    val system: String,
    val messages: List<ClaudeMessage>
)

data class ClaudeMessage(
    val role: String = "user",
    val content: String
)

data class ClaudeResponse(
    val content: List<ContentBlock>
)

data class ContentBlock(
    val type: String,
    val text: String?
)

// ── Retrofit Interface ─────────────────────────────────────────────

interface ClaudeApiService {
    @Headers(
        "anthropic-version: 2023-06-01",
        "Content-Type: application/json"
    )
    @POST("v1/messages")
    suspend fun sendMessage(
        @retrofit2.http.Header("x-api-key") apiKey: String = BuildConfig.CLAUDE_API_KEY,
        @Body request: ClaudeRequest
    ): ClaudeResponse
}

// ── Retrofit Client ────────────────────────────────────────────────

object ClaudeApiClient {
    private const val BASE_URL = "https://api.anthropic.com/"

    val service: ClaudeApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClaudeApiService::class.java)
    }
}
