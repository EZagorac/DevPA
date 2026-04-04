package com.devpa.app.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BriefingRepository @Inject constructor(
    private val api: ClaudeApiService
) {
    private val systemPrompt = """
        You are a sharp daily briefing assistant for a solo indie game developer (Unity + general indie) 
        building a portfolio to get hired. Keep it SHORT and punchy — under 160 words. Plain text only:
        1. One energizing opener.
        2. Numbered task priority list, most impactful first, with one short reason each.
        3. One "power tip" for career momentum today.
        No markdown, no headers.
    """.trimIndent()

    suspend fun generateBriefing(
        tasks: String,
        portfolioDone: Int,
        portfolioTotal: Int,
        bestStreak: Int
    ): Result<String> {
        return try {
            val prompt = "Tasks today: $tasks\nPortfolio: $portfolioDone/$portfolioTotal done. Best streak: $bestStreak days. Give me my morning briefing."
            val response = api.sendMessage(
                request = ClaudeRequest(
                    system = systemPrompt,
                    messages = listOf(ClaudeMessage(content = prompt))
                )
            )
            val text = response.content.firstOrNull { it.type == "text" }?.text
            if (text != null) Result.success(text)
            else Result.failure(Exception("Empty response from Claude"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
