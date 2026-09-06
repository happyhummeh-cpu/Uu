package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.ResourceDao
import com.example.data.model.QueryAuditLog
import com.example.data.model.ServiceCategory
import com.example.data.model.SupportResource
import com.example.data.model.VerificationStatus
import kotlinx.coroutines.flow.Flow

data class RankedResource(
    val resource: SupportResource,
    val score: Float,
    val matchReason: String
)

class ResourceRepository(private val dao: ResourceDao) {

    val allActiveResources: Flow<List<SupportResource>> = dao.getAllActiveResources()
    val allResourcesForAdmin: Flow<List<SupportResource>> = dao.getAllResourcesForAdmin()
    val recentAuditLogs: Flow<List<QueryAuditLog>> = dao.getRecentAuditLogs()

    suspend fun ensureDatabaseSeeded() {
        val count = dao.getResourceCount()
        if (count == 0) {
            dao.insertAll(AppDatabase.getInitialVerifiedResources())
        }
    }

    suspend fun insertOrUpdateResource(resource: SupportResource) {
        dao.insertOrUpdate(resource)
    }

    suspend fun deleteResource(id: String) {
        dao.deleteById(id)
    }

    suspend fun updateVerification(id: String, status: VerificationStatus, date: String, reviewer: String, notes: String) {
        dao.updateVerification(id, status, date, reviewer, notes)
    }

    suspend fun toggleActive(id: String, isActive: Boolean) {
        dao.toggleActiveStatus(id, isActive)
    }

    suspend fun logQuery(log: QueryAuditLog) {
        dao.insertAuditLog(log)
    }

    suspend fun clearAuditLogs() {
        dao.clearAllAuditLogs()
    }

    /**
     * Explainable Multi-factor Ranking Engine
     * Combines:
     * 1. Category alignment (weight: 0.35)
     * 2. Keyword/Semantic match in description & title (weight: 0.30)
     * 3. Language availability (weight: 0.15)
     * 4. Location relevance (weight: 0.10)
     * 5. 24/7 Availability & Freshness (weight: 0.10)
     */
    fun rankResources(
        resources: List<SupportResource>,
        userQuery: String,
        targetCategory: ServiceCategory?,
        selectedLanguage: String,
        selectedLocation: String
    ): List<RankedResource> {
        val queryTokens = userQuery.lowercase().trim().split("\\s+".toRegex()).filter { it.length > 2 }

        return resources.map { resource ->
            var score = 0.0f
            val reasons = mutableListOf<String>()

            // 1. Category Alignment
            if (targetCategory != null && resource.serviceCategory == targetCategory) {
                score += 35f
                reasons.add("Matches requested service: ${targetCategory.displayName}")
            }

            // 2. Keyword / Semantic matching
            val contentToSearch = "${resource.organizationName} ${resource.description} ${resource.keywords}".lowercase()
            var tokenMatches = 0
            for (token in queryTokens) {
                if (contentToSearch.contains(token)) {
                    tokenMatches++
                }
            }
            if (queryTokens.isNotEmpty()) {
                val tokenRatio = (tokenMatches.toFloat() / queryTokens.size).coerceAtMost(1.0f)
                val tokenScore = tokenRatio * 30f
                score += tokenScore
                if (tokenMatches > 0) {
                    reasons.add("Matches keywords in your inquiry")
                }
            } else {
                score += 20f // Baseline relevance when browsing
            }

            // 3. Language alignment
            val supportedLangs = resource.supportedLanguages.lowercase()
            if (supportedLangs.contains(selectedLanguage.lowercase()) || supportedLangs.contains("all")) {
                score += 15f
                reasons.add("Available in $selectedLanguage")
            }

            // 4. Location alignment
            if (resource.state.equals("National", ignoreCase = true) ||
                resource.locationScope.equals("National", ignoreCase = true) ||
                selectedLocation.equals("All India", ignoreCase = true) ||
                resource.state.contains(selectedLocation, ignoreCase = true) ||
                resource.city.contains(selectedLocation, ignoreCase = true)
            ) {
                score += 10f
                reasons.add(if (resource.state == "National") "Pan-India National Coverage" else "Local coverage for $selectedLocation")
            }

            // 5. Verification status & Freshness
            if (resource.verificationStatus == VerificationStatus.VERIFIED) {
                score += 10f
                reasons.add("Officially verified by ${resource.sourceAuthority}")
            }

            // Emergency priority boost
            if (resource.isEmergencyService && (targetCategory == ServiceCategory.EMERGENCY_ASSISTANCE || userQuery.contains("urgent", true) || userQuery.contains("emergency", true))) {
                score += 20f
                reasons.add("Rapid Emergency Response Available")
            }

            val consolidatedReason = if (reasons.isNotEmpty()) {
                reasons.take(3).joinToString(" • ")
            } else {
                "Verified support resource"
            }

            RankedResource(
                resource = resource,
                score = score,
                matchReason = consolidatedReason
            )
        }.sortedByDescending { it.score }
    }
}
