package com.sirazeem.agent.ai

import com.sirazeem.agent.data.AgentStore
import com.sirazeem.agent.model.AgentSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

data class AgentDecision(
    val reply: String?,
    val requiresApproval: Boolean,
    val reason: String
)

class AgentEngine(private val store: AgentStore) {

    suspend fun respond(message: String, studentId: String): AgentDecision {
        val normalized = message.lowercase()

        val bookKnowledge = store.knowledge.value
            .filter { it.active }
            .sortedByDescending { it.priority }
            .firstOrNull {
                it.instruction.contains("english book", ignoreCase = true) &&
                    listOf("book", "order", "price", "buy", "purchase")
                        .any { word -> normalized.contains(word) }
            }

        if (bookKnowledge != null) {
            return AgentDecision(
                reply = "Send your Name, Contact number and Address to order book. Price is 1500 and delivery is free.",
                requiresApproval = false,
                reason = "Matched owner-defined English book order rule."
            )
        }

        val settings = store.settings.value
        if (settings.aiEndpoint.isNotBlank() && settings.aiApiKey.isNotBlank() && settings.aiModel.isNotBlank()) {
            val generated = runCatching {
                callCompatibleChatApi(settings, message)
            }.getOrNull()

            if (!generated.isNullOrBlank()) {
                return AgentDecision(
                    reply = generated.trim(),
                    requiresApproval = settings.approvalMode || !settings.autoReply,
                    reason = "Generated from configured AI endpoint."
                )
            }
        }

        return AgentDecision(
            reply = "I am not sure about this yet. I will check and get back to you.",
            requiresApproval = true,
            reason = "No sufficiently reliable owner-defined rule matched."
        )
    }

    private suspend fun callCompatibleChatApi(
        settings: AgentSettings,
        message: String
    ): String = withContext(Dispatchers.IO) {
        val endpoint = settings.aiEndpoint.trimEnd('/')
        val url = if (endpoint.endsWith("/chat/completions")) endpoint
        else "$endpoint/v1/chat/completions"

        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 20_000
        connection.readTimeout = 30_000
        connection.setRequestProperty("Authorization", "Bearer ${settings.aiApiKey}")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val systemPrompt = """
            You are Sir Azeem's personal student communication agent.
            Speak naturally as Sir Azeem in first person.
            Understand English, Urdu, and Roman Urdu.
            Never invent fees, schedules, policies, payment details, or products.
            If information is not available, say you will check and get back to the student.
            Keep replies natural and concise.
        """.trimIndent()

        val body = """
            {
              "model": ${json(settings.aiModel)},
              "messages": [
                {"role":"system","content":${json(systemPrompt)}},
                {"role":"user","content":${json(message)}}
              ],
              "temperature":0.4
            }
        """.trimIndent()

        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val response = stream.bufferedReader().use { it.readText() }
        if (code !in 200..299) error("AI HTTP $code: $response")

        val marker = "\"content\""
        val start = response.indexOf(marker)
        if (start < 0) error("AI response did not contain content")
        val colon = response.indexOf(':', start)
        val firstQuote = response.indexOf('"', colon + 1)
        val sb = StringBuilder()
        var escaped = false
        for (i in firstQuote + 1 until response.length) {
            val c = response[i]
            if (escaped) {
                sb.append(
                    when (c) {
                        'n' -> '\n'
                        'r' -> '\r'
                        't' -> '\t'
                        '"' -> '"'
                        '\\' -> '\\'
                        else -> c
                    }
                )
                escaped = false
            } else if (c == '\\') {
                escaped = true
            } else if (c == '"') {
                break
            } else {
                sb.append(c)
            }
        }
        sb.toString()
    }

    private fun json(value: String): String =
        "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
}
