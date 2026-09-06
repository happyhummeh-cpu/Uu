package com.example

import com.example.data.db.AppDatabase
import com.example.data.model.ServiceCategory
import com.example.data.model.VerificationStatus
import com.example.domain.classifier.NeedClassifier
import com.example.domain.handoff.HumanHandoffManager
import com.example.domain.safety.SafetyEscalationEngine
import com.example.domain.safety.UrgentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MindMatrixCoreTests {

    // 1. Multilingual Support-Need Classification Tests
    @Test
    fun testClassification_English_EmotionalSupport() {
        val result = NeedClassifier.classify("I am feeling overwhelmed and deeply anxious, I need someone to talk to")
        assertEquals(ServiceCategory.EMOTIONAL_COUNSELLING, result.category)
        assertTrue(result.confidence >= 0.4f)
        assertEquals("English", result.detectedLanguage)
    }

    @Test
    fun testClassification_Hindi_EmotionalSupport() {
        val result = NeedClassifier.classify("मुझे किसी से बात करनी है, बहुत बेचैनी और तनाव हो रहा है")
        assertEquals(ServiceCategory.EMOTIONAL_COUNSELLING, result.category)
        assertEquals("Hindi", result.detectedLanguage)
    }

    @Test
    fun testClassification_Punjabi_EmotionalSupport() {
        val result = NeedClassifier.classify("ਮੈਨੂੰ ਕਿਸੇ ਨਾਲ ਗੱਲ ਕਰਨੀ ਹੈ, ਬਹੁਤ ਪਰੇਸ਼ਾਨੀ ਹੈ")
        assertEquals(ServiceCategory.EMOTIONAL_COUNSELLING, result.category)
        assertEquals("Punjabi", result.detectedLanguage)
    }

    @Test
    fun testClassification_CodeMixed_Hinglish() {
        val result = NeedClassifier.classify("Main bahut pareshan hoon, panic attack aa raha hai, urgently baat karni hai")
        assertEquals(ServiceCategory.EMOTIONAL_COUNSELLING, result.category)
    }

    @Test
    fun testClassification_LegalAssistance() {
        val resultEn = NeedClassifier.classify("I need a free legal aid lawyer for court help")
        assertEquals(ServiceCategory.LEGAL_ASSISTANCE, resultEn.category)

        val resultHi = NeedClassifier.classify("मुझे कानूनी सहायता और वकील की जरूरत है")
        assertEquals(ServiceCategory.LEGAL_ASSISTANCE, resultHi.category)
    }

    @Test
    fun testClassification_SocialAndShelter() {
        val result = NeedClassifier.classify("I have nowhere safe to stay tonight, need emergency shelter or hostel")
        assertEquals(ServiceCategory.SOCIAL_SUPPORT, result.category)
    }

    // 2. Safety Layer & Non-Clinical Directive Tests
    @Test
    fun testSafety_SelfHarmCrisis_Escalation() {
        val assessment = SafetyEscalationEngine.evaluate("I feel like ending my life, please help")
        assertTrue(assessment.isUrgent)
        assertEquals(UrgentType.SELF_HARM_CRISIS, assessment.urgentType)
        assertEquals("14416", assessment.primaryEmergencyPhone)
        // Must NOT attempt medical diagnosis
        assertFalse(assessment.recommendedBannerMessage.contains("diagnose"))
        assertFalse(assessment.recommendedBannerMessage.contains("patient"))
    }

    @Test
    fun testSafety_PhysicalViolence_Escalation() {
        val assessment = SafetyEscalationEngine.evaluate("Someone is beating me right now, physical danger")
        assertTrue(assessment.isUrgent)
        assertEquals(UrgentType.PHYSICAL_VIOLENCE_DANGER, assessment.urgentType)
        assertEquals("112", assessment.primaryEmergencyPhone)
    }

    @Test
    fun testSafety_DisasterEntrapment_Escalation() {
        val assessment = SafetyEscalationEngine.evaluate("We are trapped in flood, water is rising")
        assertTrue(assessment.isUrgent)
        assertEquals(UrgentType.DISASTER_TRAPPED, assessment.urgentType)
        assertEquals("1078", assessment.primaryEmergencyPhone)
    }

    @Test
    fun testSafety_NormalQuery_NoFalseEscalation() {
        val assessment = SafetyEscalationEngine.evaluate("Can you provide information on study techniques?")
        assertFalse(assessment.isUrgent)
    }


    // 3. Low-Confidence Ambiguity Test
    @Test
    fun testLowConfidence_AmbiguousQuery() {
        val result = NeedClassifier.classify("hello xyz random noise query 123")
        assertTrue(result.isLowConfidence)
        assertEquals(ServiceCategory.GENERAL_SUPPORT, result.category)
    }

    // 4. Privacy & Zero-PII Sanitization Tests
    @Test
    fun testPrivacy_HandoffSanitization_RemovesPhoneAndEmail() {
        val rawUserQuery = "Call me at 9876543210 or email testsurvivor@domain.com because I need a lawyer"
        val packet = HumanHandoffManager.prepareHandoffPacket(
            category = ServiceCategory.LEGAL_ASSISTANCE,
            queryText = rawUserQuery,
            language = "English",
            location = "Delhi"
        )

        assertTrue(packet.anonymousSessionId.startsWith("MM-ANON-"))
        assertFalse(packet.userSummary.contains("9876543210"))
        assertFalse(packet.userSummary.contains("testsurvivor@domain.com"))
        assertTrue(packet.userSummary.contains("[PHONE_REDACTED]"))
        assertTrue(packet.userSummary.contains("[EMAIL_REDACTED]"))
    }

    // 5. Bias / Fairness Audit: Cross-Lingual Routing Equivalence
    @Test
    fun testBiasFairness_CrossLingualRoutingEquivalence() {
        // Same underlying intent in 3 languages
        val enResult = NeedClassifier.classify("I need legal aid and court representation")
        val hiResult = NeedClassifier.classify("मुझे कानूनी सहायता और वकील चाहिए")
        val paResult = NeedClassifier.classify("ਮੈਨੂੰ ਕਾਨੂੰਨੀ ਸਹਾਇਤਾ ਅਤੇ ਵਕੀਲ ਦੀ ਲੋੜ ਹੈ")

        assertEquals(enResult.category, hiResult.category)
        assertEquals(hiResult.category, paResult.category)
        assertEquals(ServiceCategory.LEGAL_ASSISTANCE, enResult.category)
    }

    // 6. Verified Resource Database Integrity Test
    @Test
    fun testVerifiedResources_IntegrityAndZeroFakeData() {
        val initialResources = AppDatabase.getInitialVerifiedResources()
        assertTrue(initialResources.isNotEmpty())

        for (res in initialResources) {
            assertTrue("Resource ${res.id} must have a valid phone", res.phone.isNotBlank())
            assertTrue("Resource ${res.id} must be VERIFIED", res.verificationStatus == VerificationStatus.VERIFIED)
            assertTrue("Resource ${res.id} must have a recognized source authority", res.sourceAuthority.isNotBlank())
            assertTrue("Resource ${res.id} must have a verification date", res.verificationDate.isNotBlank())
            assertFalse("Resource ${res.id} must not be empty", res.organizationName.isBlank())
        }
    }
}
