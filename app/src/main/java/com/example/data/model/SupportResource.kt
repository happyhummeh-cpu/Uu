package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VerificationStatus {
    VERIFIED,
    NEEDS_REVERIFICATION,
    INACTIVE
}

enum class ServiceCategory(val displayName: String, val hindiName: String, val punjabiName: String) {
    EMOTIONAL_COUNSELLING("Emotional / Counselling", "भावनात्मक / परामर्श", "ਭਾਵਨਾਤਮਕ / ਕੌਂਸਲਿੰਗ"),
    EMERGENCY_ASSISTANCE("Emergency Assistance", "आपातकालीन सहायता", "ਐਮਰਜੈਂਸੀ ਸਹਾਇਤਾ"),
    LEGAL_ASSISTANCE("Legal Assistance", "कानूनी सहायता", "ਕਾਨੂੰਨੀ ਸਹਾਇਤਾ"),
    SOCIAL_SUPPORT("Social Support & Shelter", "सामाजिक सहायता और आश्रय", "ਸਮਾਜਿਕ ਸਹਾਇਤਾ ਅਤੇ ਆਸਰਾ"),
    EDUCATION_INFO("Education & Information", "शिक्षा और जानकारी", "ਸਿੱਖਿਆ ਅਤੇ ਜਾਣਕਾਰੀ"),
    GENERAL_SUPPORT("General Support", "सामान्य सहायता", "ਆਮ ਸਹਾਇਤਾ")
}

@Entity(tableName = "support_resources")
data class SupportResource(
    @PrimaryKey val id: String,
    val organizationName: String,
    val serviceCategory: ServiceCategory,
    val phone: String,
    val alternatePhone: String = "",
    val website: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "National",
    val locationScope: String = "National", // "National", "State-Level", "Local"
    val supportedLanguages: String = "English, Hindi", // Comma-separated for Room query simplicity
    val availability: String = "24/7 Free & Confidential",
    val eligibility: String = "All individuals seeking support",
    val sourceAuthority: String,
    val description: String,
    val verificationDate: String, // e.g. "2026-02-20"
    val verificationStatus: VerificationStatus = VerificationStatus.VERIFIED,
    val verificationMethod: String = "Official Gazette Verification & Direct Telephonic Check",
    val reviewer: String = "SIH Verification Lead",
    val notes: String = "Verified operational via official hotline testing",
    val keywords: String = "",
    val isEmergencyService: Boolean = false,
    val isActive: Boolean = true
)

@Entity(tableName = "query_audit_logs")
data class QueryAuditLog(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val anonymousSessionId: String,
    val sanitizedQuery: String,
    val detectedCategory: ServiceCategory,
    val detectedLanguage: String,
    val confidenceScore: Float,
    val isUrgentFlagged: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
