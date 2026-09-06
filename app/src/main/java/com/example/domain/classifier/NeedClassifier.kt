package com.example.domain.classifier

import com.example.data.model.ServiceCategory

data class ClassificationResult(
    val category: ServiceCategory,
    val confidence: Float,
    val isLowConfidence: Boolean,
    val detectedLanguage: String,
    val matchExplanation: String,
    val matchedKeywords: List<String>
) {
    val confidenceScore: Float get() = confidence
    val requiresClarification: Boolean get() = isLowConfidence
}

open class NeedClassifier {

    companion object : NeedClassifier()

    private val CONFIDENCE_THRESHOLD = 0.42f

    // 1. Emotional & Counselling Support keywords (Multilingual: EN, HI, PA, Hinglish)
    private val emotionalKeywords = listOf(
        // English
        "talk", "someone to talk to", "counselling", "counseling", "counselor", "psychologist", "depressed",
        "depression", "sad", "anxious", "anxiety", "lonely", "loneliness", "stress", "stressed", "grief",
        "grieving", "trauma", "hopeless", "mental health", "breakdown", "overwhelmed", "cry", "crying",
        // Hindi (Devanagari)
        "बात करनी है", "बात करना", "परामर्श", "काउंसलिंग", "उदास", "उदासी", "परेशान", "तनाव", "चिंता",
        "मनोवैज्ञानिक", "अकेला", "अकेलापन", "रो रहा", "रो रही", "दुख", "दुखी", "मानसिक स्वास्थ्य",
        // Punjabi (Gurmukhi)
        "ਗੱਲ ਕਰਨੀ", "ਗੱਲਬਾਤ", "ਕੌਂਸਲਿੰਗ", "ਸਲਾਹ", "ਉਦਾਸ", "ਪਰੇਸ਼ਾਨ", "ਤਣਾਅ", "ਚਿੰਤਾ", "ਦੁਖੀ", "ਰੋਣਾ", "ਮਨੋਵਿਗਿਆਨੀ",
        // Hinglish & Romanized
        "baat karni hai", "koi baat karne wala", "pareshan hoon", "bahut pareshan", "rona aa raha hai",
        "depress", "dil ghabra raha", "udas hoon", "stress ho raha", "mental peace", "tension"
    )

    // 2. Emergency Assistance keywords
    private val emergencyKeywords = listOf(
        // English
        "emergency", "danger", "in danger", "attack", "attacking", "immediate help", "urgent", "police",
        "ambulance", "fire", "disaster", "flood", "earthquake", "trapped", "violence now", "bleeding", "rescue",
        // Hindi (Devanagari)
        "आपातकाल", "खतरा", "खतरे में", "हमला", "पुलिस", "तुरंत मदद", "एम्बुलेंस", "बाढ़", "भूकंप", "फंसा हुआ", "बचाओ", "जान का खतरा",
        // Punjabi (Gurmukhi)
        "ਐਮਰਜੈਂਸੀ", "ਖ਼ਤਰਾ", "ਹਮਲਾ", "ਪੁਲਿਸ", "ਤੁਰੰਤ ਮਦਦ", "ਬਚਾਓ", "ਹੜ੍ਹ", "ਭੂਚਾਲ", "ਫਸੇ ਹੋਏ",
        // Hinglish & Romanized
        "urgent help", "khatra hai", "police chahiye", "bachao", "jaan ka khatra", "hamla hua",
        "trapped hoon", "flood trapped", "turant madad", "emergency hai", "accident"
    )

    // 3. Legal Assistance keywords
    private val legalKeywords = listOf(
        // English
        "legal", "lawyer", "advocate", "court", "fir", "police complaint", "rights", "divorce", "maintenance",
        "domestic violence legal", "justice", "statutory", "legal representation", "nalsa", "complaint",
        // Hindi (Devanagari)
        "कानूनी", "कानून", "वकील", "अदालत", "कोर्ट", "एफआईआर", "शिकायत", "घरेलू हिंसा", "अधिकार", "न्याय",
        // Punjabi (Gurmukhi)
        "ਕਾਨੂੰਨੀ", "ਕਾਨੂੰਨ", "ਵਕੀਲ", "ਅਦਾਲਤ", "ਸ਼ਿਕਾਇਤ", "ਹੱਕ", "ਇਨਸਾਫ਼", "ਐਫਆਈਆਰ",
        // Hinglish & Romanized
        "kanooni", "kanoon", "vakeel", "vakil", "court case", "fir darj", "police report", "rights janna", "legal aid"
    )

    // 4. Social Support & Shelter keywords
    private val socialSupportKeywords = listOf(
        // English
        "shelter", "safe place", "safe house", "homeless", "nowhere to go", "runaway", "food and shelter",
        "child helpline", "abandoned", "senior citizen care", "stay tonight", "protection shelter",
        // Hindi (Devanagari)
        "आश्रय", "रहने की जगह", "सुरक्षित जगह", "बेघर", "बच्चा", "वृद्ध", "बुजुर्ग", "घर से निकाल दिया", "मदद और सहारा",
        // Punjabi (Gurmukhi)
        "ਆਸਰਾ", "ਰਹਿਣ ਦੀ ਥਾਂ", "ਬੇਘਰ", "ਬੱਚਾ", "ਬਜ਼ੁਰਗ", "ਸੁਰੱਖਿਅਤ ਥਾਂ",
        // Hinglish & Romanized
        "shelter chahiye", "rehne ki jagah", "ghar se nikal", "stay karne ki jagah", "orphan", "bache ki madad", "sakhi centre"
    )

    // 5. Education & Information keywords
    private val educationKeywords = listOf(
        // English
        "information", "education", "guidance", "scheme", "government scheme", "rules", "awareness", "how to apply",
        "benefits", "documents required",
        // Hindi (Devanagari)
        "जानकारी", "शिक्षा", "मार्गदर्शन", "सरकारी योजना", "नियम", "योजना", "दस्तावेज़",
        // Punjabi (Gurmukhi)
        "ਜਾਣਕਾਰੀ", "ਸਿੱਖਿਆ", "ਸਕੀਮ", "ਸਰਕਾਰੀ ਸਕੀਮ", "ਨਿਯਮ",
        // Hinglish & Romanized
        "jankari chahiye", "scheme kya hai", "sarkari yojana", "guidance", "process kya hai"
    )

    fun classify(query: String): ClassificationResult {
        val cleanQuery = query.lowercase().trim()
        val language = detectLanguage(query)

        if (cleanQuery.isBlank()) {
            return ClassificationResult(
                category = ServiceCategory.GENERAL_SUPPORT,
                confidence = 0.0f,
                isLowConfidence = true,
                detectedLanguage = language,
                matchExplanation = "No input provided. Browse general resources or choose a category below.",
                matchedKeywords = emptyList()
            )
        }

        val categoryScores = mutableMapOf<ServiceCategory, Pair<Float, MutableList<String>>>()

        categoryScores[ServiceCategory.EMOTIONAL_COUNSELLING] = evaluateCategory(cleanQuery, emotionalKeywords, baseWeight = 1.0f)
        categoryScores[ServiceCategory.EMERGENCY_ASSISTANCE] = evaluateCategory(cleanQuery, emergencyKeywords, baseWeight = 1.3f) // High priority for urgent signals
        categoryScores[ServiceCategory.LEGAL_ASSISTANCE] = evaluateCategory(cleanQuery, legalKeywords, baseWeight = 1.1f)
        categoryScores[ServiceCategory.SOCIAL_SUPPORT] = evaluateCategory(cleanQuery, socialSupportKeywords, baseWeight = 1.0f)
        categoryScores[ServiceCategory.EDUCATION_INFO] = evaluateCategory(cleanQuery, educationKeywords, baseWeight = 0.9f)

        // Find top category
        val bestEntry = categoryScores.maxByOrNull { it.value.first }
        val rawScore = bestEntry?.value?.first ?: 0.0f
        val matchedWords = bestEntry?.value?.second ?: emptyList()
        val topCategory = bestEntry?.key ?: ServiceCategory.GENERAL_SUPPORT

        // Normalize confidence between 0.0 and 1.0
        val confidence = (rawScore / 3.0f).coerceIn(0.0f, 1.0f)
        val isLowConfidence = confidence < CONFIDENCE_THRESHOLD

        val explanation = if (isLowConfidence) {
            "Your inquiry needs clarification to pinpoint the right assistance service."
        } else {
            "Identified intent for ${topCategory.displayName} based on terminology (${matchedWords.take(3).joinToString(", ")}) in $language."
        }

        return ClassificationResult(
            category = if (isLowConfidence) ServiceCategory.GENERAL_SUPPORT else topCategory,
            confidence = confidence,
            isLowConfidence = isLowConfidence,
            detectedLanguage = language,
            matchExplanation = explanation,
            matchedKeywords = matchedWords
        )
    }

    private fun evaluateCategory(text: String, keywords: List<String>, baseWeight: Float): Pair<Float, MutableList<String>> {
        var score = 0.0f
        val matches = mutableListOf<String>()

        for (kw in keywords) {
            if (text.contains(kw)) {
                // Multi-word exact phrases carry higher discriminative weight
                val weight = if (kw.contains(" ")) 1.8f else 1.0f
                score += weight * baseWeight
                matches.add(kw)
            }
        }
        return Pair(score, matches)
    }

    fun detectLanguage(text: String): String {
        var devanagariCount = 0
        var gurmukhiCount = 0
        var latinCount = 0

        for (char in text) {
            val code = char.code
            when (code) {
                in 0x0900..0x097F -> devanagariCount++
                in 0x0A00..0x0A7F -> gurmukhiCount++
                in 0x0041..0x005A, in 0x0061..0x007A -> latinCount++
            }
        }

        return when {
            devanagariCount > 2 -> "Hindi"
            gurmukhiCount > 2 -> "Punjabi"
            devanagariCount > 0 && latinCount > 0 -> "Code-mixed (Hinglish)"
            text.contains("hoon", true) || text.contains("chahiye", true) || text.contains("madad", true) || text.contains("karni", true) -> "Code-mixed (Hinglish/Punjlish)"
            else -> "English"
        }
    }
}
