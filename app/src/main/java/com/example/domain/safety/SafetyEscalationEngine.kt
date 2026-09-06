package com.example.domain.safety

data class SafetyAssessment(
    val isUrgent: Boolean,
    val urgentType: UrgentType?,
    val recommendedBannerMessage: String,
    val primaryEmergencyPhone: String,
    val primaryEmergencyName: String,
    val triggeredRule: String
) {
    val requiresImmediateHuman: Boolean get() = isUrgent
    val recommendedHelplineNumber: String get() = primaryEmergencyPhone
    val advisoryMessage: String get() = recommendedBannerMessage
}

enum class UrgentType {
    SELF_HARM_CRISIS,
    PHYSICAL_VIOLENCE_DANGER,
    DISASTER_TRAPPED,
    USER_EXPLICIT_HUMAN_REQUEST
}

open class SafetyEscalationEngine {

    companion object : SafetyEscalationEngine()

    fun evaluateSafety(query: String): SafetyAssessment = evaluate(query)

    // 1. Acute Self-Harm / Suicidal Crisis signals (EN, HI, PA, Hinglish)
    private val selfHarmKeywords = listOf(
        "suicide", "kill myself", "end my life", "ending my life", "end life", "end it all", "want to die", "hurt myself", "harm myself", "take my life",
        "mar jana chahta", "marna chahta", "jaan de dunga", "आत्महत्या", "मरना चाहता", "मरना चाहती", "ਜਾਨ ਦੇਣੀ", "ਖ਼ੁਦਕੁਸ਼ੀ", "ਮਰਨਾ ਚਾਹੁੰਦਾ",
        "cant live anymore", "can't take it anymore", "no reason to live"
    )


    // 2. Imminent Physical Violence / Assault signals
    private val physicalViolenceKeywords = listOf(
        "attacking me", "he is beating me", "beating me", "killing me", "they will kill me", "choke",
        "physical danger", "in extreme danger", "save me now", "मारपीट", "मुझे मार रहा", "जान से मार देगा",
        "ਕੁੱਟ ਰਹੇ ਹਨ", "ਜਾਨ ਦਾ ਖ਼ਤਰਾ", "jaan ka khatra hai", "marpeet ho rahi", "bachao mujhe"
    )

    // 3. Acute Disaster Entrapment signals
    private val disasterEntrapmentKeywords = listOf(
        "trapped in flood", "building collapsing", "trapped under debris", "fire trapped", "drowning",
        "बाढ़ में फंसे", "मलबे में फंसे", "आग में फंसे", "ਹੜ੍ਹ ਵਿੱਚ ਫਸੇ", "ਫਸ ਗਏ ਹਾਂ"
    )

    // 4. Explicit User Request for Immediate Human Support
    private val humanEscalationKeywords = listOf(
        "speak to a human", "talk to human", "real person", "connect me to an agent", "operator",
        "insan se baat karni", "kisi vyakti se baat", "ਕਿਸੇ ਵਿਅਕਤੀ ਨਾਲ ਗੱਲ"
    )

    fun evaluate(query: String): SafetyAssessment {
        val lower = query.lowercase().trim()

        if (lower.isBlank()) {
            return SafetyAssessment(
                isUrgent = false,
                urgentType = null,
                recommendedBannerMessage = "",
                primaryEmergencyPhone = "",
                primaryEmergencyName = "",
                triggeredRule = "Normal"
            )
        }

        // Rule 1: Acute Self-Harm Crisis -> Tele-MANAS (14416) & KIRAN (1800-599-0019)
        for (kw in selfHarmKeywords) {
            if (lower.contains(kw)) {
                return SafetyAssessment(
                    isUrgent = true,
                    urgentType = UrgentType.SELF_HARM_CRISIS,
                    recommendedBannerMessage = "Based on what you shared, we recommend speaking with a trained person right now. Trained counsellors are available 24/7, free and confidential.",
                    primaryEmergencyPhone = "14416",
                    primaryEmergencyName = "Tele-MANAS (24/7 Crisis Counselling)",
                    triggeredRule = "Rule-SH01: Distress intent matched '$kw'"
                )
            }
        }

        // Rule 2: Immediate Physical Danger / Violence -> Emergency 112
        for (kw in physicalViolenceKeywords) {
            if (lower.contains(kw)) {
                return SafetyAssessment(
                    isUrgent = true,
                    urgentType = UrgentType.PHYSICAL_VIOLENCE_DANGER,
                    recommendedBannerMessage = "Based on what you shared, immediate safety support is recommended. You can directly connect with national emergency services.",
                    primaryEmergencyPhone = "112",
                    primaryEmergencyName = "National Emergency Response (112)",
                    triggeredRule = "Rule-PV02: Danger intent matched '$kw'"
                )
            }
        }

        // Rule 3: Disaster Entrapment -> NDMA Disaster Helpline (1078)
        for (kw in disasterEntrapmentKeywords) {
            if (lower.contains(kw)) {
                return SafetyAssessment(
                    isUrgent = true,
                    urgentType = UrgentType.DISASTER_TRAPPED,
                    recommendedBannerMessage = "Disaster rescue coordination is available. Contact disaster management response teams immediately.",
                    primaryEmergencyPhone = "1078",
                    primaryEmergencyName = "NDMA Disaster Response (1078)",
                    triggeredRule = "Rule-DE03: Disaster entrapment matched '$kw'"
                )
            }
        }

        // Rule 4: Explicit Human Request
        for (kw in humanEscalationKeywords) {
            if (lower.contains(kw)) {
                return SafetyAssessment(
                    isUrgent = true,
                    urgentType = UrgentType.USER_EXPLICIT_HUMAN_REQUEST,
                    recommendedBannerMessage = "You requested to speak directly with a person. Connecting you to verified helpline responders.",
                    primaryEmergencyPhone = "14416",
                    primaryEmergencyName = "Direct Verified Human Helpline (Tele-MANAS)",
                    triggeredRule = "Rule-HR04: User requested human operator"
                )
            }
        }

        return SafetyAssessment(
            isUrgent = false,
            urgentType = null,
            recommendedBannerMessage = "",
            primaryEmergencyPhone = "",
            primaryEmergencyName = "",
            triggeredRule = "Normal navigation flow"
        )
    }
}
