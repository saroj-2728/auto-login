package com.data.login

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CredentialManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "wifi_credentials", Context.MODE_PRIVATE
    )
    private val gson = Gson()

    // Save a credential
    fun saveCredential(credential: SavedWifiCredential) {
        val credentials = getAllCredentials().toMutableList()

        // Check if username already exists
        val existingIndex = credentials.indexOfFirst { it.username == credential.username }

        // If this is set as primary, update other credentials to not be primary
        if (credential.isPrimary) {
            credentials.forEach { existingCred ->
                if (existingCred.username != credential.username && existingCred.isPrimary) {
                    val index = credentials.indexOf(existingCred)
                    credentials[index] = existingCred.copy(isPrimary = false)
                }
            }
        }

        // Update existing or add new
        if (existingIndex >= 0) {
            credentials[existingIndex] = credential
        } else {
            credentials.add(credential)
        }

        saveAllCredentials(credentials)
    }

    // Get the primary credential
    fun getPrimaryCredential(): SavedWifiCredential? {
        return getAllCredentials().find { it.isPrimary }
    }

    // Get all saved credentials
    fun getAllCredentials(): List<SavedWifiCredential> {
        val json = sharedPreferences.getString("credentials", null) ?: return emptyList()
        val type = object : TypeToken<List<SavedWifiCredential>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Delete a credential
    fun deleteCredential(username: String) {
        val credentials = getAllCredentials().toMutableList()
        val wasRemoved = credentials.removeAll { it.username == username }

        // If we removed the primary credential, set a new one if available
        if (wasRemoved && credentials.isNotEmpty() && !credentials.any { it.isPrimary }) {
            credentials[0] = credentials[0].copy(isPrimary = true)
        }

        saveAllCredentials(credentials)
    }

    // Set a credential as primary
    fun setPrimaryCredential(username: String) {
        val credentials = getAllCredentials().toMutableList()
        credentials.forEachIndexed { index, credential ->
            credentials[index] = credential.copy(isPrimary = credential.username == username)
        }
        saveAllCredentials(credentials)
    }

    // Save list of credentials to SharedPreferences
    private fun saveAllCredentials(credentials: List<SavedWifiCredential>) {
        val json = gson.toJson(credentials)
        sharedPreferences.edit().putString("credentials", json).apply()
    }
}