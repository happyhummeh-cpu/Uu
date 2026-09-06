package com.example.domain.handoff

import com.example.data.model.ServiceCategory
import java.util.UUID

data class HandoffPacket(
    val anonymousSessionId: String,
    val serviceCategory: ServiceCategory,
    val userSummary: String,
    val language: String,
    val optionalLocation: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class HandoffState {
    object Idle : HandoffState()
    data class Drafting(val packet: HandoffPacket) : HandoffState()
    data class Connecting(val packet: HandoffPacket, val estimatedWaitSeconds: Int = 15) : HandoffState()
    data class OperatorUnavailable(val fallbackPhone: String, val fallbackName: String) : HandoffState()
    data class Completed(val message: String) : HandoffState()
}

object HumanHandoffManager {

    fun generateAnonymousId(): String {
        val shortUuid = UUID.randomUUID().toString().substring(0, 6).uppercase()
        return "MM-ANON-$shortUuid"
    }

    fun prepareHandoffPacket(
        category: ServiceCategory,
        queryText: String,
        language: String,
        location: String
    ): HandoffPacket {
        // Sanitize user query: strip any potential phone numbers, emails, or PII
        val sanitized = queryText
            .replace("\\b[0-9]{10}\\b".toRegex(), "[PHONE_REDACTED]")
            .replace("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b".toRegex(), "[EMAIL_REDACTED]")
            .take(150)

        return HandoffPacket(
            anonymousSessionId = generateAnonymousId(),
            serviceCategory = category,
            userSummary = if (sanitized.isNotBlank()) sanitized else "General assistance request",
            language = language,
            optionalLocation = if (location.isNotBlank() && location != "All India") location else "Not specified"
        )
    }
}
