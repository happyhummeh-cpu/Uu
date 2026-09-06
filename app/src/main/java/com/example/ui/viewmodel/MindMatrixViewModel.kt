package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.QueryAuditLog
import com.example.data.model.ServiceCategory
import com.example.data.model.SupportResource
import com.example.data.model.VerificationStatus
import com.example.data.repository.RankedResource
import com.example.data.repository.ResourceRepository
import com.example.domain.classifier.ClassificationResult
import com.example.domain.classifier.NeedClassifier
import com.example.domain.handoff.HandoffPacket
import com.example.domain.handoff.HandoffState
import com.example.domain.handoff.HumanHandoffManager
import com.example.domain.safety.SafetyAssessment
import com.example.domain.safety.SafetyEscalationEngine
import com.example.domain.voice.SampleVoicePrompt
import com.example.domain.voice.VoiceInputManager
import com.example.domain.voice.VoiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UiState(
    val selectedLanguage: String = "English",
    val selectedLocation: String = "All India",
    val inputText: String = "",
    val activeCategoryFilter: ServiceCategory? = null,
    val classification: ClassificationResult? = null,
    val safetyAssessment: SafetyAssessment? = null,
    val isEmergencyDismissed: Boolean = false,
    val isClarificationVisible: Boolean = false,
    val handoffState: HandoffState = HandoffState.Idle,
    val isQuickExited: Boolean = false,
    val isConsentAccepted: Boolean = true,
    val showPrivacyDialog: Boolean = false,
    val showAdminDialog: Boolean = false,
    val showJudgeDialog: Boolean = false,
    val showSampleVoiceDialog: Boolean = false,
    val showAddResourceDialog: Boolean = false,
    val editingResource: SupportResource? = null,
    val searchLatencyMs: Long = 12
)

class MindMatrixViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ResourceRepository
    val voiceInputManager: VoiceInputManager

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = ResourceRepository(db.resourceDao())
        voiceInputManager = VoiceInputManager(application)

        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureDatabaseSeeded()
        }

        // Collect voice recognition output
        viewModelScope.launch {
            voiceInputManager.voiceState.collect { state ->
                if (state is VoiceState.Success) {
                    processQuery(state.recognizedText)
                }
            }
        }
    }

    val allActiveResources: StateFlow<List<SupportResource>> = repository.allActiveResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminResources: StateFlow<List<SupportResource>> = repository.allResourcesForAdmin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<QueryAuditLog>> = repository.recentAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered and Ranked list
    val rankedResources: StateFlow<List<RankedResource>> = combine(
        allActiveResources,
        _uiState
    ) { resources, state ->
        val startTime = System.currentTimeMillis()
        val result = repository.rankResources(
            resources = resources,
            userQuery = state.inputText,
            targetCategory = state.activeCategoryFilter ?: state.classification?.category,
            selectedLanguage = state.selectedLanguage,
            selectedLocation = state.selectedLocation
        )
        val elapsed = System.currentTimeMillis() - startTime
        _uiState.value = _uiState.value.copy(searchLatencyMs = elapsed.coerceAtLeast(1))
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onLanguageSelected(language: String) {
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
    }

    fun onLocationSelected(location: String) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
    }

    fun onQueryChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun processQuery(query: String) {
        val trimmed = query.trim()
        val safety = SafetyEscalationEngine.evaluate(trimmed)
        val classification = NeedClassifier.classify(trimmed)

        _uiState.value = _uiState.value.copy(
            inputText = trimmed,
            safetyAssessment = if (safety.isUrgent) safety else null,
            classification = classification,
            isClarificationVisible = classification.isLowConfidence && trimmed.isNotBlank() && !safety.isUrgent,
            isEmergencyDismissed = false
        )

        // Log query anonymously into local Room DB
        viewModelScope.launch(Dispatchers.IO) {
            val anonymousId = HumanHandoffManager.generateAnonymousId()
            val sanitized = trimmed.take(80)
            repository.logQuery(
                QueryAuditLog(
                    anonymousSessionId = anonymousId,
                    sanitizedQuery = sanitized,
                    detectedCategory = classification.category,
                    detectedLanguage = classification.detectedLanguage,
                    confidenceScore = classification.confidence,
                    isUrgentFlagged = safety.isUrgent
                )
            )
        }
    }

    fun onCategoryFilterClicked(category: ServiceCategory?) {
        val next = if (_uiState.value.activeCategoryFilter == category) null else category
        _uiState.value = _uiState.value.copy(
            activeCategoryFilter = next,
            isClarificationVisible = false
        )
    }

    fun selectClarifiedCategory(category: ServiceCategory) {
        _uiState.value = _uiState.value.copy(
            activeCategoryFilter = category,
            isClarificationVisible = false
        )
    }

    fun dismissEmergencyBanner() {
        _uiState.value = _uiState.value.copy(isEmergencyDismissed = true)
    }

    fun triggerQuickExit() {
        // Instant safety exit: clears memory, resets UI state to safe screen
        voiceInputManager.reset()
        _uiState.value = UiState(
            isQuickExited = true,
            inputText = ""
        )
    }

    fun resumeFromQuickExit() {
        _uiState.value = _uiState.value.copy(isQuickExited = false)
    }

    fun wipeAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAuditLogs()
        }
        _uiState.value = UiState()
    }

    // Voice controls
    fun startVoiceListening() {
        val langCode = when (_uiState.value.selectedLanguage) {
            "Hindi" -> "hi-IN"
            "Punjabi" -> "pa-IN"
            else -> "en-IN"
        }
        voiceInputManager.startListening(langCode)
    }

    fun stopVoiceListening() {
        voiceInputManager.stopListening()
    }

    fun onSampleVoicePicked(sample: SampleVoicePrompt) {
        _uiState.value = _uiState.value.copy(showSampleVoiceDialog = false)
        voiceInputManager.simulateVoiceInput(sample)
    }

    // Human Handoff
    fun startHumanHandoff() {
        val category = _uiState.value.activeCategoryFilter
            ?: _uiState.value.classification?.category
            ?: ServiceCategory.GENERAL_SUPPORT

        val packet = HumanHandoffManager.prepareHandoffPacket(
            category = category,
            queryText = _uiState.value.inputText,
            language = _uiState.value.selectedLanguage,
            location = _uiState.value.selectedLocation
        )
        _uiState.value = _uiState.value.copy(handoffState = HandoffState.Drafting(packet))
    }

    fun confirmAndSendHandoff(packet: HandoffPacket) {
        _uiState.value = _uiState.value.copy(
            handoffState = HandoffState.Connecting(packet, estimatedWaitSeconds = 10)
        )
        viewModelScope.launch {
            delay(3500)
            // Section 11: "If a human operator is unavailable: Show verified alternative support resources. Never pretend that a human is available when one is not."
            _uiState.value = _uiState.value.copy(
                handoffState = HandoffState.OperatorUnavailable(
                    fallbackPhone = "14416",
                    fallbackName = "Tele-MANAS National Support Team"
                )
            )
        }
    }

    fun dismissHandoff() {
        _uiState.value = _uiState.value.copy(handoffState = HandoffState.Idle)
    }

    // Dialog toggles
    fun togglePrivacyDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPrivacyDialog = show)
    }

    fun toggleAdminDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAdminDialog = show)
    }

    fun toggleJudgeDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showJudgeDialog = show)
    }

    fun toggleSampleVoiceDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSampleVoiceDialog = show)
    }

    fun toggleAddResourceDialog(show: Boolean, editing: SupportResource? = null) {
        _uiState.value = _uiState.value.copy(
            showAddResourceDialog = show,
            editingResource = editing
        )
    }

    // Admin Actions
    fun saveResource(resource: SupportResource) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertOrUpdateResource(resource)
        }
        toggleAddResourceDialog(false)
    }

    fun toggleResourceActive(id: String, isActive: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleActive(id, isActive)
        }
    }

    fun markResourceVerified(id: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVerification(
                id = id,
                status = VerificationStatus.VERIFIED,
                date = today,
                reviewer = "SIH Lead Auditor",
                notes = "Re-verified operational via telephone line test"
            )
        }
    }

    fun markResourceNeedsVerification(id: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVerification(
                id = id,
                status = VerificationStatus.NEEDS_REVERIFICATION,
                date = today,
                reviewer = "SIH Verification Queue",
                notes = "Pending scheduled 30-day telephonic re-verification"
            )
        }
    }

    fun markResourceNeedsReverification(id: String) = markResourceNeedsVerification(id)

    fun deleteResource(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteResource(id)
        }
    }
}

