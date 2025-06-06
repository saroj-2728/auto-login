package com.data.login

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QsTile : TileService() {

    private val tag: String = QsTile::class.java.simpleName
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var credentialManager: CredentialManager

    override fun onCreate() {
        super.onCreate()
        credentialManager = CredentialManager(this)
    }

    override fun onTileAdded() {
        val tile: Tile = qsTile
        tile.state = Tile.STATE_INACTIVE
        updateTileIcon(tile)
        tile.updateTile()
    }

    override fun onClick() {
        val tile: Tile = qsTile
        tile.state = Tile.STATE_ACTIVE

        coroutineScope.launch {
            val primaryCredential = credentialManager.getPrimaryCredential()

            if (primaryCredential != null) {
                try {
                    val response = login(primaryCredential.username, primaryCredential.password)

                    if (response.contains("You are signed in as {username}")) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                parseLoginResponse(response, primaryCredential.username),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    // Try other credentials if primary fails
                    else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                "Login failed with ${primaryCredential.username}. Using other credentials.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        tryOtherCredentials()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "No saved credentials. Please login from the app first.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tile.updateTile()
    }

    private suspend fun tryOtherCredentials() {
        val allCredentials = credentialManager.getAllCredentials().filter { !it.isPrimary }

        for (credential in allCredentials) {
            try {
                val response = login(credential.username, credential.password)

                if (response.contains("You are signed in as {username}")) {
                    // If this credential works, make it primary
                    credentialManager.setPrimaryCredential(credential.username)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            applicationContext,
                            "Login successful with ${credential.username}, set as primary",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    break
                }
            } catch (e: Exception) {
                Log.e(tag, "Error trying credential ${credential.username}: ${e.message}")
            }
        }
    }

    private fun updateTileIcon(tile: Tile) {
        val iconResId: Int = R.drawable.icon
        val icon: Icon = Icon.createWithResource(applicationContext, iconResId)
        tile.icon = icon
    }
}

fun parseLoginResponse(response: String, username: String? = null): String {
    // Parse the XML response and extract the message
    val xml = response.trim()
    val start = xml.indexOf("<message><![CDATA[") + "<message><![CDATA[".length
    val end = xml.indexOf("]]></message>")
    return if (start >= 0 && end >= 0) {
        val message = xml.substring(start, end)
        // Replace placeholder with actual username if provided
        if (username != null) {
            message.replace("{username}", username)
        } else {
            message
        }
    } else {
        "Unknown error occurred"
    }
}