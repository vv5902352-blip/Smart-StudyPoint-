package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.StudyLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class StudyTtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        if (utteranceId?.endsWith("_LAST") == true) {
                            _isSpeaking.value = false
                        }
                    }

                    @Deprecated("Deprecated in Android API")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun speak(text: String, language: StudyLanguage = StudyLanguage.ENGLISH): Boolean {
        val engine = tts ?: return false
        if (!isReady || text.isBlank()) return false

        val locale = when (language) {
            StudyLanguage.HINDI -> Locale("hi", "IN")
            else -> Locale("en", "IN")
        }
        val langResult = engine.setLanguage(locale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale.US)
        }

        engine.stop()
        val maxLen = (TextToSpeech.getMaxSpeechInputLength() - 100).coerceIn(500, 3500)
        val chunks = text.chunked(maxLen)
        val batchId = UUID.randomUUID().toString()
        chunks.forEachIndexed { idx, chunk ->
            val mode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val suffix = if (idx == chunks.lastIndex) "_LAST" else "_$idx"
            engine.speak(chunk, mode, null, "$batchId$suffix")
        }
        _isSpeaking.value = true
        return true
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
        _isSpeaking.value = false
    }
}
