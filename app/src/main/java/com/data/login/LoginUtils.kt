package com.data.login


import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object LoginUtils {

    private lateinit var sharedPreferences: EncryptedSharedPreferences

    fun initializeEncryptedSharedPreferences(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "LoginPrefsEncrypted",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun saveLastLoggedInUser(context: Context, username: String, password: String) {
        // Save last logged-in user's credentials to EncryptedSharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("lastUsername", username)
        editor.putString("lastPassword", password)
        editor.apply()
    }

    fun getLastLoggedInUser(context: Context): Pair<String, String>? {
        // Retrieve last logged-in user's credentials from EncryptedSharedPreferences
        val lastUsername = sharedPreferences.getString("lastUsername", null)
        val lastPassword = sharedPreferences.getString("lastPassword", null)
        return if (lastUsername != null && lastPassword != null) {
            Pair(lastUsername, lastPassword)
        } else {
            null
        }
    }
}
