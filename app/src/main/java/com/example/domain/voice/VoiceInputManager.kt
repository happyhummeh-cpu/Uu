package com.example.domain.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    object Processing : VoiceState()
    data class Success(val recognizedText: String) : VoiceState()
    data class Error(val message: String) : VoiceState()
}

data class SampleVoicePrompt(
    val title: String,
    val languageCode: String,
    val speechText: String,
    val categoryLabel: String
)

class VoiceInputManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    companion object {
        val TEST_PROMPTS = listOf(
            SampleVoicePrompt(
                title = "English - Counselling",
                languageCode = "en-IN",
                speechText = "I need someone to talk to, feeling very overwhelmed and lonely.",
                categoryLabel = "Emotional / Counselling"
            ),
            SampleVoicePrompt(
                title = "हिन्दी (Hindi) - परामर्श",
                languageCode = "hi-IN",
                speechText = "मुझे किसी से बात करनी है, बहुत तनाव और घबराहट महसूस हो रही है।",
                categoryLabel = "भावनात्मक / परामर्श"
            ),
            SampleVoicePrompt(
                title = "ਪੰਜਾਬੀ (Punjabi) - ਕਾਨੂੰਨੀ",
                languageCode = "pa-IN",
                speechText = "ਮੈਨੂੰ ਕਾਨੂੰਨੀ ਸਹਾਇਤਾ ਚਾਹੀਦੀ ਹੈ, ਵਕੀਲ ਜਾਂ ਅਦਾਲਤ ਸੰਬੰਧੀ ਮਦਦ।",
                categoryLabel = "ਕਾਨੂੰਨੀ ਸਹਾਇਤਾ"
            ),
            SampleVoicePrompt(
                title = "Hinglish - Urgent Emergency",
                languageCode = "en-IN",
                speechText = "Emergency hai, jaan ka khatra hai please send police immediately.",
                categoryLabel = "Emergency Assistance"
            ),
            SampleVoicePrompt(
                title = "Hinglish - Shelter & Safe Stay",
                languageCode = "en-IN",
                speechText = "Mujhe safe shelter aur rehne ki jagah chahiye, domestic violence se bachna hai.",
                categoryLabel = "Social Support & Shelter"
            )
        )
    }

    fun startListening(selectedLanguageCode: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = VoiceState.Error("Voice input wasn't clear or speech service unavailable. You can type instead.")
            return
        }

        try {
            stopListening()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = VoiceState.Listening
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = VoiceState.Listening
                    }

                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {
                        // ZERO RAW AUDIO RETENTION: Buffer is strictly ignored and discarded
                    }

                    override fun onEndOfSpeech() {
                        _voiceState.value = VoiceState.Processing
                    }

                    override fun onError(error: Int) {
                        _voiceState.value = VoiceState.Error(
                            "Voice input wasn't clear. You can type instead."
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _voiceState.value = VoiceState.Success(matches[0])
                        } else {
                            _voiceState.value = VoiceState.Error("Voice input wasn't clear. You can type instead.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguageCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, selectedLanguageCode)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("Voice service unavailable. You can type instead.")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        if (_voiceState.value is VoiceState.Listening) {
            _voiceState.value = VoiceState.Idle
        }
    }

    fun simulateVoiceInput(sample: SampleVoicePrompt) {
        // Fast, deterministic simulation for judge demonstrations & testing on emulators
        _voiceState.value = VoiceState.Listening
        _voiceState.value = VoiceState.Processing
        _voiceState.value = VoiceState.Success(sample.speechText)
    }

    fun reset() {
        stopListening()
        _voiceState.value = VoiceState.Idle
    }
}
