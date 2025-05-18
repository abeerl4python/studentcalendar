package com.example.studentcalendar

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PASSWORD = stringPreferencesKey("user_password")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
    }

    suspend fun saveCredentials(email: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
            preferences[USER_PASSWORD] = password
        }
    }

    suspend fun saveLoginData(email: String, password: String, rememberMe: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
            preferences[USER_PASSWORD] = password
            preferences[IS_LOGGED_IN] = true
            preferences[REMEMBER_ME] = rememberMe
        }
    }

    suspend fun clearLoginData() {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            if (!(preferences[REMEMBER_ME] ?: false)) {
                preferences.remove(USER_EMAIL)
                preferences.remove(USER_PASSWORD)
            }
            preferences[REMEMBER_ME] = false
        }
    }

    val userEmailFlow: Flow<String?> = context.dataStore.data.map { it[USER_EMAIL] }
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }
    val rememberMeFlow: Flow<Boolean> = context.dataStore.data.map { it[REMEMBER_ME] ?: false }
}