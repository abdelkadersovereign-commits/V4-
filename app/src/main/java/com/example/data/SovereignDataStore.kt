package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sovereign_settings")

class SovereignDataStore(private val context: Context) {

    companion object {
        val OPERATOR_NAME = stringPreferencesKey("operator_name")
        val NEURAL_ROLE = stringPreferencesKey("neural_role")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val PROJECT_NAME = stringPreferencesKey("project_name")
        val PROJECT_ID = stringPreferencesKey("project_id")
        val PROJECT_NUMBER = stringPreferencesKey("project_number")
        val IS_ARABIC = booleanPreferencesKey("is_arabic")
        val STEALTH_MODE = booleanPreferencesKey("stealth_mode")
        val CYBER_SCORE = intPreferencesKey("cyber_score")
        val NEURAL_PROXY = booleanPreferencesKey("neural_proxy")
    }

    val operatorName: Flow<String> = context.dataStore.data.map { it[OPERATOR_NAME] ?: "Sovereign_Operator" }
    val neuralRole: Flow<String> = context.dataStore.data.map { it[NEURAL_ROLE] ?: "Sovereign Node v4" }
    val geminiApiKey: Flow<String> = context.dataStore.data.map { it[GEMINI_API_KEY] ?: "" }
    val projectName: Flow<String> = context.dataStore.data.map { it[PROJECT_NAME] ?: "" }
    val projectId: Flow<String> = context.dataStore.data.map { it[PROJECT_ID] ?: "" }
    val projectNumber: Flow<String> = context.dataStore.data.map { it[PROJECT_NUMBER] ?: "" }
    val isArabic: Flow<Boolean> = context.dataStore.data.map { it[IS_ARABIC] ?: true }
    val stealthMode: Flow<Boolean> = context.dataStore.data.map { it[STEALTH_MODE] ?: false }
    val cyberScore: Flow<Int> = context.dataStore.data.map { it[CYBER_SCORE] ?: 0 }
    val neuralProxy: Flow<Boolean> = context.dataStore.data.map { it[NEURAL_PROXY] ?: false }

    suspend fun saveOperatorName(name: String) {
        context.dataStore.edit { it[OPERATOR_NAME] = name }
    }

    suspend fun saveNeuralRole(role: String) {
        context.dataStore.edit { it[NEURAL_ROLE] = role }
    }

    suspend fun saveGeminiApiKey(key: String) {
        context.dataStore.edit { it[GEMINI_API_KEY] = key }
    }

    suspend fun saveProjectName(name: String) {
        context.dataStore.edit { it[PROJECT_NAME] = name }
    }

    suspend fun saveProjectId(id: String) {
        context.dataStore.edit { it[PROJECT_ID] = id }
    }

    suspend fun saveProjectNumber(number: String) {
        context.dataStore.edit { it[PROJECT_NUMBER] = number }
    }

    suspend fun saveIsArabic(isArabic: Boolean) {
        context.dataStore.edit { it[IS_ARABIC] = isArabic }
    }

    suspend fun saveStealthMode(isStealth: Boolean) {
        context.dataStore.edit { it[STEALTH_MODE] = isStealth }
    }

    suspend fun saveCyberScore(score: Int) {
        context.dataStore.edit { it[CYBER_SCORE] = score }
    }

    suspend fun saveNeuralProxy(isProxy: Boolean) {
        context.dataStore.edit { it[NEURAL_PROXY] = isProxy }
    }
}
