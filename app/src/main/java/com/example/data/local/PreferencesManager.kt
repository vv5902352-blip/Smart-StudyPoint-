package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.studyDataStore: DataStore<Preferences> by preferencesDataStore(name = "smart_studypoint_prefs")

class PreferencesManager(private val context: Context) {
    companion object {
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_STUDENT_NAME = stringPreferencesKey("student_name")
        private val KEY_TARGET_EXAM = stringPreferencesKey("target_exam")
        private val KEY_APP_USAGE_DAYS = stringSetPreferencesKey("app_usage_days")
    }

    val isDarkMode: Flow<Boolean> = context.studyDataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: false
    }

    val studentName: Flow<String> = context.studyDataStore.data.map { prefs ->
        prefs[KEY_STUDENT_NAME] ?: "Student"
    }

    val targetExam: Flow<String> = context.studyDataStore.data.map { prefs ->
        prefs[KEY_TARGET_EXAM] ?: "CBSE / UPSC / SSC"
    }

    val appUsageDayKeys: Flow<Set<String>> = context.studyDataStore.data.map { prefs ->
        prefs[KEY_APP_USAGE_DAYS] ?: emptySet()
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.studyDataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun setStudentProfile(name: String, targetExam: String) {
        context.studyDataStore.edit { prefs ->
            prefs[KEY_STUDENT_NAME] = name.ifBlank { "Student" }
            prefs[KEY_TARGET_EXAM] = targetExam.ifBlank { "CBSE / UPSC / SSC" }
        }
    }

    suspend fun recordAppUsageToday(dayKey: String) {
        context.studyDataStore.edit { prefs ->
            val current = prefs[KEY_APP_USAGE_DAYS] ?: emptySet()
            if (dayKey !in current) {
                prefs[KEY_APP_USAGE_DAYS] = current + dayKey
            }
        }
    }
}
