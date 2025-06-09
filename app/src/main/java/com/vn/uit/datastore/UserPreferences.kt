package com.vn.uit.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
    }

    suspend fun saveUserData(token: String, refreshToken: String, username: String, email: String) {
        context.dataStore.edit {
            it[KEY_TOKEN] = token
            it[KEY_REFRESH_TOKEN] = refreshToken
            it[KEY_USERNAME] = username
            it[KEY_EMAIL] = email
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit {
            it[KEY_USER_ID] = userId
        }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USERNAME] = username
        }
    }

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { it[KEY_TOKEN] }
    }

    fun getRefreshToken(): Flow<String?> {
        return context.dataStore.data.map { it[KEY_REFRESH_TOKEN] }
    }

    fun getUsername(): Flow<String?> {
        return context.dataStore.data.map { it[KEY_USERNAME] }
    }

    fun getEmail(): Flow<String?> {
        return context.dataStore.data.map { it[KEY_EMAIL] }
    }

    fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { it[KEY_USER_ID] }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit {
            it.remove(KEY_TOKEN)
            it.remove(KEY_REFRESH_TOKEN)
            it.remove(KEY_USERNAME)
            it.remove(KEY_EMAIL)
        }
    }
}
